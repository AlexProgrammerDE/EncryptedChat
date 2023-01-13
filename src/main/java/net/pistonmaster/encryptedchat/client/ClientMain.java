package net.pistonmaster.encryptedchat.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.pistonmaster.encryptedchat.EncryptedChat;
import net.pistonmaster.encryptedchat.crypto.CryptoAESUtils;
import net.pistonmaster.encryptedchat.crypto.CryptoGenerator;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.crypto.CryptoRSAUtils;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.network.ChannelDecoder;
import net.pistonmaster.encryptedchat.network.ChannelEncoder;
import net.pistonmaster.encryptedchat.packet.server.*;
import net.pistonmaster.encryptedchat.util.ConsoleInput;

import javax.crypto.SecretKey;
import javax.net.ssl.X509TrustManager;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@RequiredArgsConstructor
public class ClientMain implements Runnable {
    private final Scanner consoleScanner;
    private final ConsoleInput consoleInput;
    private final String targetHost;
    private final int targetPort;
    private final String username;
    private final KeyPair pair;
    private ChannelFuture channel;
    private final ClientPacketBus bus = new ClientPacketBus(this);
    private final ExecutorService shutdownExecutor = Executors.newSingleThreadExecutor();
    private final Set<StorageUser> knownUsers = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            SslContext sslContext = SslContextBuilder.forClient().trustManager(new X509TrustManager() { // TODO Add a real trust manager
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            }).build();
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addFirst(sslContext.newHandler(ch.alloc(), targetHost, targetPort));
                    ch.pipeline().addLast(new ChannelDecoder(), new ChannelEncoder(), new ChatClientHandler(ClientMain.this));
                }
            });

            channel = b.connect(targetHost, targetPort).sync();
            System.out.println("Started client!");

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("stop").executes(context -> {
                System.out.println("Stopping client...");
                shutdown();
                return 1;
            }));

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("group")
                    .then(LiteralArgumentBuilder.literal("create")
                            .then(RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        System.out.println("Creating group " + name + "...");

                                        SecretKey secretKey = CryptoGenerator.generateAESKey();
                                        UUID keyId = UUID.randomUUID();

                                        CryptoStorage.saveKey(secretKey, EncryptedChat.CLIENT_PATH.resolve(keyId + ".aeskey"));

                                        channel.channel().writeAndFlush(new ServerboundGroupCreate(name, keyId));

                                        return 1;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.literal("join")
                            .then(RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                                    .executes(context -> {
                                        String name = StringArgumentType.getString(context, "name");
                                        System.out.println("Joining group " + name + "...");

                                        channel.channel().writeAndFlush(new ServerboundGroupJoin(name));

                                        return 1;
                                    })
                            )
                    )
                    .then(LiteralArgumentBuilder.literal("add")
                            .then(RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                                    .executes(context -> {
                                        if (bus.getGroupInfo() == null || bus.getGroupSecretKey() == null) {
                                            System.out.println("You are not in a group!");
                                            return 1;
                                        }

                                        String name = StringArgumentType.getString(context, "name");
                                        System.out.println("Adding " + name + " to the group...");
                                        Optional<StorageUser> optional = knownUsers.stream().filter(storageUser -> storageUser.username().equals(name)).findFirst();

                                        if (optional.isPresent()) {
                                            StorageUser user = optional.get();
                                            channel.channel().writeAndFlush(new ServerboundGroupMemberAdd(user.userId(), bus.getGroupInfo().groupId(),
                                                    CryptoRSAUtils.encrypt(CryptoStorage.saveKeyToString(bus.getGroupSecretKey()), user.userKey())));
                                        } else {
                                            System.out.println("User not found! Requesting data from server. Run command again!");
                                            channel.channel().writeAndFlush(new ServerboundUserDataRequest(UUID.randomUUID(), name));
                                        }

                                        return 1;
                                    })
                            )
                    )
                    .executes(context -> {
                        System.out.println("Invalid syntax! Use group <join|create> <name>...");
                        return 1;
                    }));

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("say")
                    .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())
                            .executes(context -> {
                                String message = StringArgumentType.getString(context, "message");
                                if (bus.getGroupInfo() == null) {
                                    System.out.println("You are not in a group!");
                                    return 1;
                                }

                                System.out.println("Sending message " + message + " to " + bus.getGroupInfo().groupName() + "...");

                                channel.channel().writeAndFlush(new ServerboundGroupMessage(
                                        CryptoAESUtils.encrypt(message, bus.getGroupSecretKey()),
                                        CryptoRSAUtils.sign(message, pair.getPrivate())));

                                return 1;
                            })
                    )
                    .executes(context -> {
                        System.out.println("Invalid syntax! Use say <message>...");
                        return 1;
                    }));

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("sayunsafe")
                    .then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString())
                            .executes(context -> {
                                String message = StringArgumentType.getString(context, "message");

                                System.out.println("Sending unsafe message " + message + "...");

                                channel.channel().writeAndFlush(new ServerboundUnsecureMessage(message));

                                return 1;
                            })
                    )
                    .executes(context -> {
                        System.out.println("Invalid syntax! Use sayunsafe <message>...");
                        return 1;
                    }));

            consoleInput.runReadCommandInput(a -> !channel.channel().closeFuture().isDone(), consoleScanner);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        if (channel == null) {
            return;
        }

        try {
            channel.channel().eventLoop().shutdownGracefully().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
