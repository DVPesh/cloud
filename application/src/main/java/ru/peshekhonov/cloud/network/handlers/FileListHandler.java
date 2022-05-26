package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.controllers.CloudController;
import ru.peshekhonov.cloud.controllers.ServerPanelController;
import ru.peshekhonov.cloud.handlers.StartHandler;
import ru.peshekhonov.cloud.messages.FileInfoListData;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

@Slf4j
public class FileListHandler extends SimpleChannelInboundHandler<Message> {

    private ServerPanelController serverPanelController;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileInfoListData list) {
            log.info("FileInfo list from server was received");
            Platform.runLater(() -> {
                serverPanelController.updateListAndPreserveSelection(list.getFileInfoList(), list.getDirectory());
            });
        } else if (msg instanceof StatusData status && status.getStatus() == StatusType.HANDLED_ERROR4) {
            log.error("[ {} ] {}", status.getPath().toString(), StatusType.HANDLED_ERROR4.getText());
            Platform.runLater(() -> {
                serverPanelController.clearList();
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        CloudController cloudController = Client.getInstance().getCloudController();
        cloudController.setSocketChannel(ctx.channel());
        serverPanelController = cloudController.getServerPanelController();
        serverPanelController.setSocketChannel(ctx.channel());
        cloudController.getClientPanelController().setStartHandlerMap(ctx.pipeline().get(StartHandler.class).getMap());
        log.info("Channel was registered");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("Channel active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Channel inactive");
    }
}
