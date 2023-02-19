package io.demian.net_study.step4_NIO_ASYNC.chat;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.NIO.NIOClient;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.Executors;

import static io.demian.net_study.Util.println;

public class NIOAsyncChatClient extends NIOClient {
    AsynchronousChannelGroup channelGroup;
    AsynchronousSocketChannel socket;

    public void startClient() {
        println("[ClientUI : startClient executed. ");

        try {

            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                    Runtime.getRuntime().availableProcessors(),
                    Executors.defaultThreadFactory()
            );

            socket = AsynchronousSocketChannel.open(channelGroup);
            socket.connect(new InetSocketAddress("localhost", 5001), null, new CompletionHandler<Void, Void>() {

                @Override
                public void completed(Void result, Void attachment) {
                    try {
                        InetSocketAddress isa = (InetSocketAddress) socket.getRemoteAddress();

                        Platform.runLater(()->{
                            displayText("[연결 완료: "  + isa.getHostName() + "]");
                            btnConn.setText("stop");
                            btnSend.setDisable(false);
                        });
                    } catch(Exception e) {

                    }

                    receive();

                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    closer();
                }
            });



        } catch(Exception e) {
            closer();
            return;
        }
    }

    public void closer() {
        Platform.runLater( () -> displayText("[서버 통신 안됨]"));
        if( socket.isOpen() ) stopClient();
    }

    public void receive() {
        ByteBuffer buffer = ByteBuffer.allocate(100);

        socket.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                try {
                    attachment.flip();
                    String data = BufferUtil.decode(attachment);
                    Platform.runLater( () -> displayText("[받기 완료]"+ data) );

                    ByteBuffer buffer = ByteBuffer.allocate(100);
                    socket.read(buffer, buffer, this);
                } catch(Exception e) {

                }

            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                NIOAsyncChatClient.super.stop();
            }
        });
    }

    @Override
    public void send(String data) {
        ByteBuffer buffer = BufferUtil.encode(data);

        socket.write(buffer, null, new CompletionHandler<Integer, Void>() {

            @Override
            public void completed(Integer result, Void attachment) {
                Platform.runLater(()->displayText("[보내기 완료]"));
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                NIOAsyncChatClient.super.stop();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
