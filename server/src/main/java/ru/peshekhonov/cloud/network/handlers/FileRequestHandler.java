package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.Configuration;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.handlers.StatusHandler;
import ru.peshekhonov.cloud.messages.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Slf4j
public class FileRequestHandler extends SimpleChannelInboundHandler<Message> {

    public Path base;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof FileRequest request) {
            final String filename = request.getSource().getFileName().toString();
            final Path destination = request.getDestination();
            final Path source = request.getSource();
            final Path serverSource = base.resolve(source);
            if (Files.notExists(serverSource)) {
                ctx.writeAndFlush(new StatusData(source, StatusType.ERROR1));
                log.error("[ {} ] {}", filename, StatusType.ERROR1.getText());
                return;
            }
            if (!ctx.pipeline().get(StatusHandler.class).getTaskMap().containsKey(destination)) {
                Thread thread = new Thread(() -> {
                    try (SeekableByteChannel channel = Files.newByteChannel(serverSource, StandardOpenOption.READ)) {
                        copyFile(ctx, destination, channel);
                    } catch (IOException | InterruptedException e) {
                        ctx.writeAndFlush(new StatusData(source, StatusType.ERROR2));
                        log.error("[ {} ] {}", filename, StatusType.ERROR2.getText());
                    } finally {
                        ctx.pipeline().get(StatusHandler.class).getTaskMap().remove(destination);
                    }
                });
                ctx.pipeline().get(StatusHandler.class).getTaskMap().put(destination, thread);
                thread.start();
            }
        } else if (msg instanceof FileMoveRequest request) {
            final String filename = request.getSource().getFileName().toString();
            final Path destination = request.getDestination();
            final Path source = request.getSource();
            final Path serverSource = base.resolve(source);
            if (Files.notExists(serverSource)) {
                ctx.writeAndFlush(new StatusData(source, StatusType.ERROR1));
                log.error("[ {} ] {}", filename, StatusType.ERROR1.getText());
                return;
            }
            if (!ctx.pipeline().get(StatusHandler.class).getTaskMap().containsKey(destination)) {
                Thread thread = new Thread(() -> {
                    try (SeekableByteChannel channel = Files.newByteChannel(serverSource, StandardOpenOption.READ)) {
                        copyFile(ctx, destination, channel);
                        channel.close();
                        Files.delete(serverSource);
                    } catch (IOException | InterruptedException e) {
                        ctx.writeAndFlush(new StatusData(source, StatusType.ERROR5));
                        log.error("[ {} ] {}", filename, StatusType.ERROR5.getText());
                    } finally {
                        ctx.pipeline().get(StatusHandler.class).getTaskMap().remove(destination);
                    }
                });
                ctx.pipeline().get(StatusHandler.class).getTaskMap().put(destination, thread);
                thread.start();
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void copyFile(ChannelHandlerContext ctx, Path destination, SeekableByteChannel channel) throws IOException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(Configuration.BUFFER_SIZE);
        byte[] array;
        final long fileSize = channel.size();
        long size = channel.read(buffer);
        buffer.flip();
        array = new byte[(int) size];
        buffer.get(array);
        ctx.writeAndFlush(new StartData(destination, size == -1 || fileSize == size, array, fileSize)).sync();
        buffer.clear();
        int length;
        while ((length = channel.read(buffer)) != -1) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            size += length;
            buffer.flip();
            if (length == Configuration.BUFFER_SIZE) {
                buffer.get(array);
                ctx.writeAndFlush(new SubsequentData(destination, fileSize == size, array)).sync();
                buffer.clear();
            } else {
                array = new byte[length];
                buffer.get(array);
                ctx.writeAndFlush(new SubsequentData(destination, true, array)).sync();
                buffer.clear();
            }
        }
    }
}
