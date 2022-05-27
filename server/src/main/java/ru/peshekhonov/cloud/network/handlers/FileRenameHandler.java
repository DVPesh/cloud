package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.*;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileRenameHandler extends SimpleChannelInboundHandler<Message> {

    public Path base;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileRenameRequest request) {
            log.info("Request to rename file was received");
            try {
                String newFilename = request.getNewFilename();
                Path path = base.resolve(request.getFilename());
                Files.move(path, path.resolveSibling(newFilename));
            } catch (Exception e) {
                log.error("Server failed to rename file");
                ctx.writeAndFlush(new StatusData(request.getFilename(), StatusType.ERROR3));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
