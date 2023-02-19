package io.demian.net_study.step3_NIO_NONBLOCK.basic;

import io.demian.net_study.BufferUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import static io.demian.net_study.Util.println;

public class Client {

    Selector selector;
    SocketChannel socket;

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

            println("[연결 완료: "  + isa.getHostName() + "]");

        } catch(Exception e) {
            stop();
            return;
        }

        Runnable runnable = () -> {
            while(true) {
                try {
                    int keyCount = selector.select();
                    if (keyCount == 0) continue;

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keys = selectedKeys.iterator();

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
                    stop();
                    break;
                }
            }
        };

        new Thread(runnable).start();
    }

    void stopClient() {
        try {
            if(socket!=null && socket.isOpen()) {
                socket.close();
            }

        } catch (IOException e) {}
    }

    public void stop() {
        println("[서버 통신 안됨]");
        stopClient();
    }

    void connect(SelectionKey key) {
        try {
            socket.finishConnect();
            println("[연결 완료: " + socket.getRemoteAddress() + "]");
            key.interestOps(SelectionKey.OP_READ);
        } catch(Exception e) {
            stop();
        }
    }

    void receive(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(100);
            int byteCount = socket.read(buffer);

            if( byteCount == -1 )
                throw new IOException();

            buffer.flip();
            String data = BufferUtil.decode(buffer);

            println("[받기 완료]"+ data);

        } catch (Exception e) {
            stop();
        }
    }

    void send(SelectionKey key) {
        try {
            ByteBuffer byteBuffer = (ByteBuffer) key.attachment();
            socket.write(byteBuffer);
            println("[보내기 완료]");
            key.interestOps(SelectionKey.OP_READ);
        } catch (Exception e) {
            stop();
        }
    }

    public void send(String data) {
        ByteBuffer buffer = BufferUtil.encode(data);
        SelectionKey key = socket.keyFor(selector);
        key.attach(buffer);
        key.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
        client.send("123");
        client.send("1243");
        client.send("12663");

    }

}
