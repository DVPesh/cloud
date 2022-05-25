package ru.peshekhonov.cloud.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import ru.peshekhonov.cloud.handlers.ContinueHandler;
import ru.peshekhonov.cloud.handlers.StartHandler;
import ru.peshekhonov.cloud.handlers.StatusHandler;
import ru.peshekhonov.cloud.handlers.UndefinedHandler;
import ru.peshekhonov.cloud.network.handlers.*;

@Getter
public class SerializationPipeline extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new FileListRequestHandler(),
                new StatusHandler(),
                new StartHandler(),
                new ContinueHandler(),
                new FileRequestHandler(),
                new FileRenameHandler(),
                new FileDeleteHandler(),
                new UndefinedHandler()
        );
    }
}
