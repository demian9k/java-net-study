package io.demian.net_study.step4_NIO_ASYNC.chat.model;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.step4_NIO_ASYNC.chat.NIOAsyncChatServer;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ClientSession {
    public AsynchronousSocketChannel socket;
    public NIOAsyncChatServer server;
    private String data;

    public ClientSession(NIOAsyncChatServer server, AsynchronousSocketChannel socketChannel) throws IOException {
        this.server = server;
        this.socket = socketChannel;
        receive();
    }

    public void receive() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        socket.read(buffer, buffer, readCompletionHandler);
    }

    public void send(String data) {
        ByteBuffer buffer = BufferUtil.encode(data);
        socket.write(buffer, null, writeCompletionHandler);
    }

    CompletionHandler<Integer, ByteBuffer> readCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {

        @Override
        public void completed(Integer result, ByteBuffer attachment) {
            try {
                String message = "[요청 처리: " + socket.getRemoteAddress() +"" + Thread.currentThread().getName() + "]";
                Platform.runLater( () -> server.displayText(message));

                attachment.flip();
                String data = BufferUtil.decode(attachment);

                for(ClientSession session: server.getConnections()) {
                    session.send(data);
                }

                ByteBuffer buffer = ByteBuffer.allocate(100);
                socket.read(buffer, buffer, this);

            } catch(Exception e) {

            }
        }

        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {
            closer();
        }
    };

    CompletionHandler<Integer, Void> writeCompletionHandler = new CompletionHandler<Integer, Void>() {

        @Override
        public void completed(Integer result, Void attachment) {

        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            closer();
        }
    };

    private void closer() {
        try {
            server.getConnections().remove(this);
            String message = "[클라이언트 통신 안됨: " + socket.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";
            Platform.runLater(() -> server.displayText(message));
            socket.close();
        } catch (IOException e2) {

        }
    }
}