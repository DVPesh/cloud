package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Metadata;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.handlers.StartHandler;
import ru.peshekhonov.cloud.messages.FileInfoListData;
import ru.peshekhonov.cloud.messages.FileInfoListRequest;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public class FileListRequestHandler extends SimpleChannelInboundHandler<Message> {

    private Map<Path, Metadata> startHandlerMap;
    public Path base;

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
                ctx.writeAndFlush(new FileInfoListData(base, request.getDirectory(), startHandlerMap));
            } catch (IOException e) {
                log.error("Server failed to create file info list");
                ctx.writeAndFlush(new StatusData(request.getDirectory(), StatusType.HANDLED_ERROR4));
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        startHandlerMap = ctx.pipeline().get(StartHandler.class).getMap();
        log.info("Channel was registered");
    }
}
