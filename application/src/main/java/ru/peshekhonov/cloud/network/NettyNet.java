package ru.peshekhonov.cloud.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyNet {

    public static final int SERVER_PORT = 8189;
    public static final String SERVER_HOST = "localhost";

    private @Getter
    SerializationPipeline pipeline;

    public NettyNet() {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup hard = new NioEventLoopGroup(1);
        pipeline = new SerializationPipeline();
        try {
            bootstrap.group(hard);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(pipeline);

            ChannelFuture channelFuture = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync();
            log.info("Client started...");
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            hard.shutdownGracefully();
        }
    }
}
