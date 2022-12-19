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
import net.pistonmaster.encryptedchat.crypto.CryptoGenerator;
import net.pistonmaster.encryptedchat.crypto.CryptoStorage;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.network.ChannelDecoder;
import net.pistonmaster.encryptedchat.network.ChannelEncoder;
import net.pistonmaster.encryptedchat.packet.server.ServerboundGroupJoin;
import net.pistonmaster.encryptedchat.util.ConsoleInput;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
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

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("group").then(RequiredArgumentBuilder.argument("groupName", StringArgumentType.string()).executes(context -> {
                joinOrCreateGroup(StringArgumentType.getString(context, "groupName"));
                return 1;
            })).executes(context -> {
                System.out.println("Invalid syntax! Use group <name>...");
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

    public void joinOrCreateGroup(String groupName) {
        System.out.println("Joining group " + groupName + "...");
        channel.channel().writeAndFlush(new ServerboundGroupJoin(groupName, CryptoGenerator.generateRSAKey().getPublic().));
    }
}
