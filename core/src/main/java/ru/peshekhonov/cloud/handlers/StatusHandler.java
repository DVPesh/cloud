package ru.peshekhonov.cloud.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StatusHandler extends SimpleChannelInboundHandler<Message> {

    private final @Getter
    Map<Path, Thread> taskMap = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StatusData status) {
            String filename = status.getPath().getFileName().toString();
            switch (status.getStatus()) {
                case OK:
                    log.info("[ {} ] {}", filename, StatusType.OK.getText());
                    break;
                case HANDLED_ERROR1:
                    interruptTaskThread(status);
                    log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR1.getText());
                    break;
                case HANDLED_ERROR2:
                    interruptTaskThread(status);
                    log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR2.getText());
                    break;
                case HANDLED_ERROR3:
                    interruptTaskThread(status);
                    log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR3.getText());
                    break;
                case ERROR1:
                    log.error("[ {} ] {}", filename, StatusType.ERROR1.getText());
                    break;
                case ERROR2:
                    log.error("[ {} ] {}", filename, StatusType.ERROR2.getText());
                    break;
                default:
                    ctx.fireChannelRead(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void interruptTaskThread(StatusData status) {
        Path path = status.getPath();
        Thread thread = taskMap.get(path);
        if (thread != null) {
            thread.interrupt();
        }
    }
}
