package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.FileInfoListData;
import ru.peshekhonov.cloud.messages.FileInfoListRequest;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;
import ru.peshekhonov.cloud.network.Server;

import java.io.IOException;

@Slf4j
public class FileListRequestHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Client disconnected...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileInfoListRequest request) {
            log.info("Request on file info list was received");
            try {
                ctx.writeAndFlush(new FileInfoListData(Server.SERVER_DIR, request.getDirectory()));
            } catch (IOException e) {
                log.error("Server failed to create file info list");
                ctx.writeAndFlush(new StatusData(request.getDirectory(), StatusType.HANDLED_ERROR4));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
