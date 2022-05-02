package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Client;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

@Slf4j
public class StatusHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StatusData status) {
            if (status.getStatus() == StatusType.OK) {
                log.info("File \"{}\" is successfully copied to server", status.getFilename());
            } else if (status.getStatus() == StatusType.ERROR) {
                log.error("File \"{}\": {}", status.getFilename(), status.getMessage());
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        Client.getInstance().getCloudController().setSocketChannel(ctx.channel());
        log.info("Channel registered");
    }
}
