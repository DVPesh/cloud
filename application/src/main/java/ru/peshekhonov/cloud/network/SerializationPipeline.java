package ru.peshekhonov.cloud.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import ru.peshekhonov.cloud.handlers.ContinueHandler;
import ru.peshekhonov.cloud.handlers.StartHandler;
import ru.peshekhonov.cloud.handlers.StatusHandler;
import ru.peshekhonov.cloud.handlers.UndefinedHandler;
import ru.peshekhonov.cloud.network.handlers.*;

public class SerializationPipeline extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new FileListHandler(),
                new StatusHandler(),
                new StartHandler(),
                new ContinueHandler(),
                new UndefinedHandler()
        );
    }
}
