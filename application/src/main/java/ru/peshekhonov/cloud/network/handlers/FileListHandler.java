package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.messages.FileListData;
import ru.peshekhonov.cloud.messages.Message;

@Slf4j
public class FileListHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg instanceof FileListData list) {
            log.info("File list from server is received");
            Platform.runLater(() -> {
                Client.INSTANCE.getCloudController().updateServerListView(list.getFileList());
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
