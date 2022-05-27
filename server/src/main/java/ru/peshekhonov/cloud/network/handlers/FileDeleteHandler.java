package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.FileDeleteRequest;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileDeleteHandler extends SimpleChannelInboundHandler<Message> {

    public Path base;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileDeleteRequest request) {
            log.info("Request to delete file was received");
            try {
                Files.deleteIfExists(base.resolve(request.getPath()));
            } catch (DirectoryNotEmptyException e) {
                log.warn("Server cannot delete non-empty directory");
                ctx.writeAndFlush(new StatusData(request.getPath(), StatusType.WARNING1));
            } catch (IOException e) {
                log.error("Server failed to delete file");
                ctx.writeAndFlush(new StatusData(request.getPath(), StatusType.ERROR4));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
