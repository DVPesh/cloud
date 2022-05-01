package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.messages.FileListData;
import ru.peshekhonov.cloud.messages.FileListRequest;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.network.Server;

import java.io.IOException;

@Slf4j
public class FileListRequestHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client disconnected...");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileListRequest) {
            log.info("Request on file list is received");
            try {
                ctx.writeAndFlush(new FileListData(Server.serverDir));
            } catch (IOException e) {
                log.error("Server fails to read list of files");
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
