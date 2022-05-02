package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.controller.CloudController;
import ru.peshekhonov.cloud.messages.Message;
import ru.peshekhonov.cloud.messages.StartData;
import ru.peshekhonov.cloud.messages.StatusData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

@Slf4j
public class StartHandler extends SimpleChannelInboundHandler<Message> {

    private @Getter
    SeekableByteChannel writeChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(CloudController.BUFFER_SIZE);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof StartData startdata) {
            String filename = startdata.getFilename();
            log.info("Start frame of the file \"{}\" is received", filename);
            Path path;
            try {
                path = CloudController.CLIENT_DIRECTORY.resolve(filename);
            } catch (InvalidPathException e) {
                String str = "the path string cannot be converted to a Path";
                ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR));
                log.error("File \"{}\": {}", filename, str);
                return;
            }
            try {
                writeChannel = Files.newByteChannel(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                buffer.put(startdata.getData());
                buffer.flip();
                writeChannel.write(buffer);
                buffer.clear();
                if (startdata.isEndOfFile()) {
                    writeChannel.close();
                    ctx.writeAndFlush(new StatusData(filename, StatusType.OK));
                    log.info("The file \"{}\" is saved successfully", filename);
                }
            } catch (FileAlreadyExistsException e) {
                String str = "the file already exists";
                ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                log.error("File \"{}\": {}", filename, str);
            } catch (IOException e) {
                String str = "I/O error";
                ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                log.error("File \"{}\": {}", filename, str);
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.error("", ex);
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
