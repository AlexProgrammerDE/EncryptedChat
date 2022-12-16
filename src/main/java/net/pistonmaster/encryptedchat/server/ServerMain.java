package net.pistonmaster.encryptedchat.server;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.pistonmaster.encryptedchat.data.StorageUser;
import net.pistonmaster.encryptedchat.network.ChannelDecoder;
import net.pistonmaster.encryptedchat.network.ChannelEncoder;
import net.pistonmaster.encryptedchat.util.ConsoleInput;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@RequiredArgsConstructor
public class ServerMain implements Runnable {
    private final Scanner consoleScanner;
    private final ConsoleInput consoleInput;
    private final int port;
    private ChannelFuture channel;
    private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private final Map<ChannelId, ConnectionState> channelStates = new ConcurrentHashMap<>();
    private final ServerStorage storage = new ServerStorage(this);

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslContext = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addFirst(sslContext.newHandler(ch.alloc()));
                            ch.pipeline().addLast(new ChannelDecoder(), new ChannelEncoder(), new ChatServerHandler(ServerMain.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // Bind and start to accept incoming connections.
            channel = b.bind(port).sync();
            System.out.println("Started server!");

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("stop").executes(context -> {
                System.out.println("Stopping server...");
                shutdown();
                return 1;
            }));

            consoleInput.getDispatcher().register(LiteralArgumentBuilder.literal("list").executes(context -> {
                System.out.println("Listing connected users...");
                if (channels.isEmpty()) {
                    System.out.println("No users connected!");
                } else {
                    for (Channel channel : channels) {
                        StringBuilder builder = new StringBuilder();
                        builder.append(channel.remoteAddress());
                        Object bus = channelStates.get(channel.id()).getBus();
                        if (bus instanceof ServerPacketBus serverPacketBus) {
                            builder.append(" - ").append(serverPacketBus.getUser().username());
                        }
                        System.out.println(builder.toString());
                    }
                }
                return 1;
            }));

            consoleInput.runReadCommandInput((a) -> !channel.channel().closeFuture().isDone(), consoleScanner);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
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

    public Set<StorageUser> getConnectedUsers() {
        Set<StorageUser> users = ConcurrentHashMap.newKeySet();

        for (ConnectionState state : channelStates.values()) {
            if (state.getState() == ConnectionState.State.CONNECTED) {
                users.add(((ServerPacketBus) state.getBus()).getUser());
            }
        }

        return users;
    }
}
