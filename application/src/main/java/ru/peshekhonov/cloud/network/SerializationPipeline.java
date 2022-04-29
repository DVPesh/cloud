package ru.peshekhonov.cloud.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import ru.peshekhonov.cloud.network.handlers.FileListHandler;
import ru.peshekhonov.cloud.network.handlers.StatusHandler;
import ru.peshekhonov.cloud.network.handlers.UndefinedHandler;

public class SerializationPipeline extends ChannelInitializer<SocketChannel> {

    private @Getter
    ChannelPipeline channelPipeline;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        channelPipeline = socketChannel.pipeline();
        channelPipeline.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
        channelPipeline.addLast("outBoundEncoder", new ObjectEncoder());
        channelPipeline.addLast(new StatusHandler(), new FileListHandler(), new UndefinedHandler());
    }
}
