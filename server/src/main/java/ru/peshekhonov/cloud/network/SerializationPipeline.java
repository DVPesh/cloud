package ru.peshekhonov.cloud.network;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.Getter;
import ru.peshekhonov.cloud.network.handlers.*;

@Getter
public class SerializationPipeline extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(
                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                new ObjectEncoder(),
                new FileListRequestHandler(),
                new StartHandler(),
                new ContinueHandler(),
                new FileRequestHandler(),
                new UndefinedHandler()
        );
    }
}
