package ru.peshekhonov.cloud.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StartData;
import ru.peshekhonov.cloud.messages.StatusData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class StartHandler extends SimpleChannelInboundHandler<Message> {

    private final @Getter
    Map<Path, SeekableByteChannel> map = new HashMap<>();

    private final ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StartData startdata) {
            Path path = startdata.getPath();
            SeekableByteChannel writeChannel = null;
            String filename = path.getFileName().toString();
            log.info("Start frame of the file \"{}\" is received", filename);
            try {
                writeChannel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                map.put(path, writeChannel);
                buffer.put(startdata.getData());
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
                if (startdata.isEndOfFile()) {
                    writeChannel.close();
                    map.remove(path);
                    ctx.writeAndFlush(new StatusData(path, StatusType.OK));
                    log.info("[ {} ] {}", filename, StatusType.OK.getText());
                }
            } catch (FileAlreadyExistsException e) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR2));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR2.getText());
            } catch (IOException e) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR1));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR1.getText());
                try {
                    if (writeChannel != null) {
                        writeChannel.close();
                    }
                    map.remove(path);
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
