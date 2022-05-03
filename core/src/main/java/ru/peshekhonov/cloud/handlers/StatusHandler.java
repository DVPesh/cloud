package ru.peshekhonov.cloud.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StatusHandler extends SimpleChannelInboundHandler<Message> {

    private final @Getter
    Map<Path, Thread> taskMap = new HashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StatusData status) {
            String filename = status.getPath().getFileName().toString();
            switch (status.getStatus()) {
                case OK:
                    log.info("File \"{}\" is successfully copied", filename);
                    break;
                case ERROR1:
                    log.error("File \"{}\": {}", filename, status.getMessage());
                    taskMap.get(status.getPath()).interrupt();
                    break;
                case ERROR2:
                    log.error("File \"{}\": {}", filename, status.getMessage());
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
