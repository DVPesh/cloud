package ru.peshekhonov.cloud.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import ru.peshekhonov.cloud.handlers.*;
import ru.peshekhonov.cloud.network.handlers.*;

public class SerializationPipeline extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new AuthHandler(),
                new FileListHandler(),
                new StatusHandler(),
                new StartHandler(),
                new ContinueHandler(),
                new ClientStatusHandler(),
                new UndefinedHandler()
        );
    }
}
