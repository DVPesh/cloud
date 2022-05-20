package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.controllers.CloudController;
import ru.peshekhonov.cloud.messages.FileInfoListData;
import ru.peshekhonov.cloud.messages.Message;

@Slf4j
public class FileListHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
//        if (msg instanceof FileInfoListData list) {
//            log.info("File list from server is received");
//            Platform.runLater(() -> {
//                CloudController controller = Client.getInstance().getCloudController();
//                controller.setServerDir((list.getDirectory()));
//                controller.updateServerListView(list.getFileInfoList());
//            });
//        } else {
//            ctx.fireChannelRead(msg);
//        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
//        Client.getInstance().getCloudController().setSocketChannel(ctx.channel());
        log.info("Channel registered");
    }
}
