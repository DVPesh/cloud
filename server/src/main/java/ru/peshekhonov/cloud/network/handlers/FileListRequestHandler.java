package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.messages.FileInfoListData;
import ru.peshekhonov.cloud.messages.FileInfoListRequest;
import ru.peshekhonov.cloud.messages.Message;
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
        if (msg instanceof FileInfoListRequest) {
            log.info("Request on file list is received");
            try {
                ctx.writeAndFlush(new FileInfoListData(Server.SERVER_DIR));
            } catch (IOException e) {
                log.error("Server failed to read list of files");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
