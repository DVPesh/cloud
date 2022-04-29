package ru.peshekhonov.cloud.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class Server {

    public final static Path serverDir = Path.of("server/files");

    public static void start(int port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup light = new NioEventLoopGroup(1);
        EventLoopGroup hard = new NioEventLoopGroup();
        try {
            bootstrap.group(light, hard)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SerializationPipeline());

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("Server started...");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            light.shutdownGracefully();
            hard.shutdownGracefully();
        }
    }
}
