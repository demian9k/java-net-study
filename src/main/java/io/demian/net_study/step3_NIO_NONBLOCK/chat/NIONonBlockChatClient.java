package io.demian.net_study.step3_NIO_NONBLOCK.chat;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.NIO.NIOClient;
import javafx.application.Platform;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import static io.demian.net_study.Util.println;

public class NIONonBlockChatClient extends NIOClient {

    Selector selector;

    public void startClient() {
        println("[ClientUI : startClient executed. ");

        try {
            selector = Selector.open();
        } catch(Exception e) {
            if( socket.isOpen() )
                stopClient();
        }

        try {
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            socket.register(selector, SelectionKey.OP_CONNECT);
            socket.connect(new InetSocketAddress("localhost", 5001));

            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteAddress();

            Platform.runLater(()->{
                displayText("[연결 완료: "  + isa.getHostName() + "]");
                btnConn.setText("stop");
                btnSend.setDisable(false);
            });

        } catch(Exception e) {
            closer();
            return;
        }

        Runnable runnable = () -> {
            while(true) {
                try {

                    Iterator<SelectionKey> keys = NIONonBlockChatServer.getSelectedKeys(selector);
                    if( keys == null ) continue;

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();

                        if(key.isConnectable()) {
                            connect(key);
                        } else if( key.isReadable() ) {
                            receive(key);
                        } else if( key.isWritable() ) {
                            send(key);
                        }

                        keys.remove();
                    }

                } catch (Exception e) {
                    closer();
                    break;
                }
            }
        };

        new Thread(runnable).start();
    }

    public void connect(SelectionKey key) {
        try {
            socket.finishConnect();

            Platform.runLater( () -> {
                try {
                    displayText("[연결 완료: " + socket.getRemoteAddress() + "]");
                    btnConn.setText("stop");
                    btnSend.setDisable(false);
                } catch (Exception e) {

                }
            });
            key.interestOps(SelectionKey.OP_READ);
        } catch(Exception e) {
            closer();
        }
    }

    public void closer() {
        Platform.runLater( () -> displayText("[서버 통신 안됨]"));
        if( socket.isOpen() ) stopClient();
    }

    public void receive(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);
            int byteCount = socket.read(buffer);

            if( byteCount == -1 )
                throw new IOException();

            buffer.flip();
            String data = BufferUtil.decode(buffer);

            Platform.runLater( () -> displayText("[받기 완료]"+ data) );

        } catch (Exception e) {
           super.stop();
        }
    }

    public void send(SelectionKey key) {
        try {
            ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
            socket.write(byteBuffer);
            Platform.runLater( () -> displayText("[보내기 완료]"));
            key.interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            super.stop();
        }
    }

    @Override
    public void send(String data) {
        ByteBuffer buffer = BufferUtil.encode(data);
        SelectionKey key = socket.keyFor(selector);
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
