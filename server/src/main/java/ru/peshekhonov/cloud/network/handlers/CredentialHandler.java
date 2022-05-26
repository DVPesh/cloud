package ru.peshekhonov.cloud.network.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import ru.peshekhonov.cloud.StatusType;
import ru.peshekhonov.cloud.handlers.ContinueHandler;
import ru.peshekhonov.cloud.handlers.StartHandler;
import ru.peshekhonov.cloud.messages.*;
import ru.peshekhonov.cloud.network.Server;
import ru.peshekhonov.cloud.network.auth.AuthService;
import ru.peshekhonov.cloud.network.auth.User;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.sql.SQLException;

@Slf4j
public class CredentialHandler extends SimpleChannelInboundHandler<Message> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) {
        if (msg instanceof RegisterRequest request) {
            log.info("Request to register new user was received");
            String username = request.getUsername();
            String login = request.getLogin();
            String password = request.getPassword();
            Path directory;
            try {
                directory = Server.SERVER_DIR.resolve(login);
            } catch (InvalidPathException e) {
                ctx.writeAndFlush(new AuthNotOk(StatusType.AUTH_NOT_OK4));
                return;
            }
            try {
                if (AuthService.doesLoginExist(login)) {
                    ctx.writeAndFlush(new AuthNotOk(StatusType.AUTH_NOT_OK1));
                    return;
                }
                if (AuthService.doesPasswordExist(password)) {
                    ctx.writeAndFlush(new AuthNotOk(StatusType.AUTH_NOT_OK2));
                    return;
                }
                Files.createDirectory(directory);
                AuthService.createUser(login, password, username);
                setBasePath(ctx, directory);
                ctx.writeAndFlush(new AuthOk(login, username));
                ctx.pipeline().remove(CredentialHandler.class);
            } catch (SQLException e) {
                log.error("Database access error");
                try {
                    Files.deleteIfExists(directory);
                } catch (IOException ex) {
                    log.error("Server cannot delete directory");
                }
                ctx.writeAndFlush(new AuthNotOk(StatusType.ERROR6));
            } catch (FileAlreadyExistsException e) {
                ctx.writeAndFlush(new AuthNotOk(StatusType.AUTH_NOT_OK4));
            } catch (IOException e) {
                log.error("Server cannot create directory");
                ctx.writeAndFlush(new AuthNotOk(StatusType.ERROR7));
            }
        } else if (msg instanceof LoginRequest request) {
            log.info("Request to log in was received");
            String login = request.getLogin();
            String password = request.getPassword();
            try {
                User user = AuthService.getUserByLoginAndPassword(login, password);
                if (user == null) {
                    ctx.writeAndFlush(new AuthNotOk(StatusType.AUTH_NOT_OK3));
                    return;
                }
                setBasePath(ctx, Server.SERVER_DIR.resolve(login));
                ctx.writeAndFlush(new AuthOk(login, user.getUserName()));
                ctx.pipeline().remove(CredentialHandler.class);
            } catch (SQLException e) {
                log.error("Database access error");
                e.printStackTrace();
                ctx.writeAndFlush(new AuthNotOk(StatusType.ERROR6));
            }
        }
    }

    private void setBasePath(ChannelHandlerContext ctx, Path base) {
        ChannelPipeline pipeline = ctx.pipeline();
        pipeline.get(FileDeleteHandler.class).base = base;
        pipeline.get(FileListRequestHandler.class).base = base;
        pipeline.get(FileRenameHandler.class).base = base;
        pipeline.get(FileRequestHandler.class).base = base;
        pipeline.get(StartHandler.class).setBase(base);
        pipeline.get(ContinueHandler.class).setBase(base);
    }
}
