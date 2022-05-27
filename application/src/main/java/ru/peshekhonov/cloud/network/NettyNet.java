package ru.peshekhonov.cloud.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.Configuration;

@Slf4j
public class NettyNet {

    public static final String SERVER_HOST = "localhost";

    @Getter
    private final EventLoopGroup hard;
    @Getter
    private ChannelFuture channelFuture;
    private final Bootstrap bootstrap;

    public NettyNet() {
        bootstrap = new Bootstrap();
        hard = new NioEventLoopGroup();
        bootstrap.group(hard);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new SerializationPipeline());
    }

    public void startNetty() {
        try {
            channelFuture = bootstrap.connect(SERVER_HOST, Configuration.SERVER_PORT).sync();
            log.info("Client started...");
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        log.error("", cause);
                    }
                    Platform.runLater(() -> {
                        Client.username = null;
                        Client.login = null;
                        Client.getInstance().getPrimaryStage().setTitle("Сетевое хранилище");
                        Client.getInstance().getCloudController().getServerPanelController().clearList();
                    });
                }
            });
        } catch (Exception e) {
            log.error("", e);
        }
    }

    public void stopNetty() {
        if (channelFuture != null && channelFuture.channel() != null && channelFuture.channel().isOpen()) {
            channelFuture.channel().close();
        }
    }
}
