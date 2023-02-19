package io.demian.net_study.step2_NIO_BLOCK.chat.model;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.step2_NIO_BLOCK.chat.NIOBlockChatServer;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static io.demian.net_study.Util.println;

public class ClientSession {
    public SocketChannel socket;
    public NIOBlockChatServer server;

    public ClientSession(NIOBlockChatServer server, SocketChannel socketChannel) {
        this.server = server;
        this.socket = socketChannel;
        receive();
    }

    void receive() {
        println("[Client : receive executed. ");

        Runnable runnable = () -> {
            while(true) {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(100);

                    //클라이언트가 비저상 종료를 했을 경우 IOException 발생
                    int readByteCount = socket.read(byteBuffer);

                    //클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
                    if(readByteCount == -1) {  throw new IOException(); }

                    String message = "[요청 처리: " + socket.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";

                    Platform.runLater(()-> server.displayText(message));

                    byteBuffer.flip();

                    //문자열로 변환
                    String data = BufferUtil.decode(byteBuffer);

                    for(ClientSession session : server.getConnections()) {
                        session.send(data);
                    }

                } catch(Exception e) {
                    println("receive catch 1");
                    e.printStackTrace();

                    try {
                        server.getConnections().remove(ClientSession.this);
                        String message = "[클라이언트 통신 안됨: " + socket.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";

                        Platform.runLater(()-> server.displayText(message));

                        socket.close();
                    } catch (IOException e2) {
                        println("receive catch 2");
                        e2.printStackTrace();
                    }
                }
            }

        };

        server.getExecutorService().submit(runnable);
    }

    void send(String data) {
        println("[Client : send executed. ");

        Runnable runnable = () -> {
            try {

                ByteBuffer byteBuffer = BufferUtil.encode(data);

                socket.write(byteBuffer);
            } catch(Exception e) {
                println("send catch 1");
                e.printStackTrace();
                try {
                    String message = "[클라이언트 통신 안됨: " + socket.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";

                    Platform.runLater(()-> server.displayText(message));

                    server.getConnections().remove(ClientSession.this);

                    socket.close();
                } catch (IOException e2) {
                    println("send catch 2");
                    e2.printStackTrace();
                }
            }
        };

        server.getExecutorService().submit(runnable);
    }
}