package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.CreateDirectoryRequest;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class CreateDirectoryHandler extends SimpleChannelInboundHandler<Message> {

    public Path base;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof CreateDirectoryRequest request) {
            log.info("Request to create directory was received");
            try {
                Files.createDirectory(base.resolve(request.getDirectory()));
            } catch (Exception e) {
                log.error("Server failed to create directory");
                ctx.writeAndFlush(new StatusData(request.getDirectory(), StatusType.ERROR7));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
