package ru.peshekhonov.cloud.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.Metadata;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StartData;
import ru.peshekhonov.cloud.messages.StatusData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StartHandler extends SimpleChannelInboundHandler<Message> {

    private final @Getter
    Map<Path, Metadata> map = new ConcurrentHashMap<>();

    private final ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
    @Setter
    private Path base;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StartData startdata) {
            Path path = startdata.getPath();
            Path fullPath = (base != null) ? base.resolve(path) : path;
            SeekableByteChannel writeChannel = null;
            String filename = path.getFileName().toString();
            Metadata metadata = new Metadata();
            log.info("Start frame of the file \"{}\" is received", filename);
            try {
                writeChannel = Files.newByteChannel(fullPath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                metadata.channel = writeChannel;
                metadata.size = startdata.getSize();
                map.put(path, metadata);
                buffer.clear();
                buffer.put(startdata.getData());
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
                if (startdata.isEndOfFile()) {
                    writeChannel.close();
                    map.remove(path);
                    ctx.writeAndFlush(new StatusData(path, StatusType.OK));
                    log.info("[ {} ] {}", filename, StatusType.OK.getText());
                    return;
                }
                metadata.timestamp = System.currentTimeMillis();
            } catch (FileAlreadyExistsException e) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR2));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR2.getText());
            } catch (IOException e) {
                ctx.writeAndFlush(new StatusData(path, StatusType.HANDLED_ERROR1));
                log.error("[ {} ] {}", filename, StatusType.HANDLED_ERROR1.getText());
                try {
                    if (writeChannel != null) {
                        writeChannel.close();
                        map.remove(path);
                        Files.deleteIfExists(fullPath);
                    }
                } catch (IOException ex) {
                    log.error("File \"" + filename + "\"", ex);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
