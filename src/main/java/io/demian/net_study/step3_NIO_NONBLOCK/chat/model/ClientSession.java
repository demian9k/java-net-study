package io.demian.net_study.step3_NIO_NONBLOCK.chat.model;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.step3_NIO_NONBLOCK.chat.NIONonBlockChatServer;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class ClientSession {
    public SocketChannel socket;
    public NIONonBlockChatServer server;
    private String data;

    public ClientSession(NIONonBlockChatServer server, SocketChannel socketChannel) throws IOException {
        this.server = server;
        this.socket = socketChannel;
        socketChannel.configureBlocking(false);
        SelectionKey key = socketChannel.register(server.getSelector(), SelectionKey.OP_READ);
        key.attach(this);
    }

    public void receive(SelectionKey key) {

        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);

            int byteCount = socket.read(buffer);

            if( byteCount == -1 ) throw new IOException();

            String message = "[요청 처리: " + socket.getRemoteAddress() +"" + Thread.currentThread().getName() + "]";
            Platform.runLater( () -> server.displayText(message));

            buffer.flip();
            String data = BufferUtil.decode(buffer);

            for(ClientSession client: server.getConnections()) {
                client.data = data;
                SelectionKey clientKey = client.socket.keyFor(server.getSelector());
                clientKey.interestOps(SelectionKey.OP_WRITE);
            }

            server.getSelector().wakeup();

        } catch (Exception e) {

            closer();
        }
    }

    public void send(SelectionKey key) {
        try {
            ByteBuffer buffer = BufferUtil.encode(data);
            socket.write(buffer);
            key.interestOps(SelectionKey.OP_READ);
            server.getSelector().wakeup();
        } catch(Exception e) {
            closer();
        }
    }

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