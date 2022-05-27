package ru.peshekhonov.cloud.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.network.auth.AuthService;

import java.nio.file.Path;
import java.sql.SQLException;

@Slf4j
public class Server {

    public final static Path SERVER_DIR = Path.of("server/files");

    public static void start(int port) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        EventLoopGroup light = new NioEventLoopGroup(1);
        EventLoopGroup hard = new NioEventLoopGroup();
        try {
            AuthService.start();
            bootstrap.group(light, hard)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new SerializationPipeline());

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            log.info("Server started...");
            channelFuture.channel().closeFuture().sync();
        } catch (SQLException e) {
            log.error("Database access error", e);
            e.printStackTrace();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            AuthService.stop();
            light.shutdownGracefully();
            hard.shutdownGracefully();
        }
    }
}
