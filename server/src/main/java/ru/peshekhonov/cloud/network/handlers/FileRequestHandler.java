package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.messages.*;
import ru.peshekhonov.cloud.network.Server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileRequestHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileRequest request) {
            Thread thread = new Thread(() -> {
                String filename = request.getFilename();
                log.info("File request frame for the filename \"{}\" is received", filename);
                Path path = Server.SERVER_DIR.resolve(filename);
                if (Files.notExists(path)) {
                    String str = "the file does not exist";
                    ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                    log.error("File \"{}\": {}", filename, str);
                    return;
                }
                try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
                    ByteBuffer buffer = ByteBuffer.allocate(Server.BUFFER_SIZE);
                    byte[] array;
                    final long fileSize = channel.size();
                    long size = channel.read(buffer);
                    buffer.flip();
                    array = new byte[(int) size];
                    buffer.get(array);
                    ctx.writeAndFlush(new StartData(filename, size == -1 || fileSize == size, array)).sync();
                    buffer.clear();
                    int length;
                    while ((length = channel.read(buffer)) != -1) {
                        size += length;
                        buffer.flip();
                        if (length == Server.BUFFER_SIZE) {
                            buffer.get(array);
                            ctx.writeAndFlush(new SubsequentData(filename, fileSize == size, array)).sync();
                            buffer.clear();
                        } else {
                            array = new byte[length];
                            buffer.get(array);
                            ctx.writeAndFlush(new SubsequentData(filename, true, array)).sync();
                            buffer.clear();
                        }
                    }
                } catch (IOException e) {
                    String str = "server failed to read the file";
                    ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, str));
                    log.error("Failed to read file " + "\"" + filename + "\"");
                } catch (InterruptedException e) {
                    ctx.writeAndFlush(new StatusData(filename, StatusType.ERROR, "server failed to read the file ->" + e.getMessage()));
                    log.error("Failed to read file " + "\"" + filename + "\": " + e.getMessage());
                }
            });
            thread.setDaemon(true);
            thread.start();
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
