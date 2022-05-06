package ru.peshekhonov.cloud.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StatusData;
import ru.peshekhonov.cloud.messages.SubsequentData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Slf4j
public class ContinueHandler extends SimpleChannelInboundHandler<Message> {

    private final ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof SubsequentData data) {
            Path path = data.getPath();
            Map<Path, SeekableByteChannel> map = ctx.pipeline().get(StartHandler.class).getMap();
            Map<Path, Long> timeMap = ctx.pipeline().get(StartHandler.class).getTimeMap();
            SeekableByteChannel writeChannel = map.get(path);
            String filename = path.getFileName().toString();
            log.debug("Continue frame of the file \"{}\" is received", filename);
            if (writeChannel == null || !writeChannel.isOpen()) {
                return;
            }
            if (System.currentTimeMillis() - timeMap.get(path) > Configuration.FRAME_TIMEOUT_MS) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR3));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR3.getText());
                try {
                    writeChannel.close();
                    map.remove(path);
                    timeMap.remove(path);
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.error("File \"" + filename + "\"", ex);
                }
                return;
            }
            try {
                buffer.clear();
                buffer.put(data.getData());
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
                if (data.isEndOfFile()) {
                    writeChannel.close();
                    map.remove(path);
                    timeMap.remove(path);
                    ctx.writeAndFlush(new StatusData(path, StatusType.OK));
                    log.info("[ {} ] {}", filename, StatusType.OK.getText());
                    return;
                }
                timeMap.put(path, System.currentTimeMillis());
            } catch (IOException e) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR1));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR1.getText());
                try {
                    writeChannel.close();
                    map.remove(path);
                    timeMap.remove(path);
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.error("File \"" + filename + "\"", ex);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
