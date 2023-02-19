package io.demian.net_study.step3_NIO_NONBLOCK.chat;

import io.demian.net_study.UI.ServerUI;
import io.demian.net_study.step3_NIO_NONBLOCK.chat.model.ClientSession;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;

public class NIONonBlockChatServer extends ServerUI {

    Selector selector;
    ServerSocketChannel serverSocket;
    List<ClientSession> connections = new Vector<ClientSession>();

    public void startServer() {
        displayText("[ServerUI : startServer executed. ");

        try {
            selector = Selector.open();
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress( 5001));
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        } catch(Exception e) {
            e.printStackTrace();
            if(serverSocket.isOpen()) { stopServer(); }
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Iterator<SelectionKey> keys = NIONonBlockChatServer.getSelectedKeys(selector);

                        if( keys == null ) continue;

                        while (keys.hasNext()) {
                            SelectionKey key = keys.next();

                            if (key.isAcceptable()) {
                                accept(key);
                            } else if (key.isReadable()) {
                                ClientSession session = (ClientSession) key.attachment();
                                session.receive(key);
                            } else if (key.isWritable()) {
                                ClientSession session = (ClientSession) key.attachment();
                                session.send(key);
                            }
                            keys.remove();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (serverSocket.isOpen()) {
                            stopServer();
                        }
                        break;
                    }
                }
            }
        };

        thread.start();

        Platform.runLater(()->{
            displayText("[서버 시작]");
            btnStartStop.setText("stop");
        });
    }

    public void accept(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            SocketChannel socketChannel = serverSocketChannel.accept();

            String message = "[연결 수락:" + socketChannel.getRemoteAddress() + ": " + Thread.currentThread() +"]";
            Platform.runLater( () -> displayText(message) );

            ClientSession client = new ClientSession(this, socketChannel);
            connections.add(client);

            Platform.runLater( () -> displayText("[연결 개수 :"+ connections.size() +"]") );


        } catch(Exception e) {
            if( serverSocket.isOpen() ) { stopServer(); }
        }
    }

    public void stopServer() {
        displayText("[ServerUI : stopServer executed. ");
        try {
            Iterator<ClientSession> iter = connections.iterator();

            while(iter.hasNext()) {
                ClientSession client = iter.next();
                client.socket.close();
                iter.remove();
            }

            if( serverSocket != null && serverSocket.isOpen() )
                serverSocket.close();

            if( selector != null && selector.isOpen() )
                selector.close();

            Platform.runLater( () -> {
                displayText("[서버 멈춤]");
                btnStartStop.setText("start");
            });

        } catch(Exception e) {

        }
    }

    public ServerSocketChannel getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocketChannel serverSocket) {
        this.serverSocket = serverSocket;
    }

    public List<ClientSession> getConnections() {
        return connections;
    }

    public void setConnections(List<ClientSession> connections) {
        this.connections = connections;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public static Iterator<SelectionKey> getSelectedKeys(Selector selector) throws IOException {
        int keyCount = selector.select();
        if (keyCount == 0) return null;

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keys = selectedKeys.iterator();
        return keys;
    }

    public static void main(String[] args) {
        launch(args);
    }
}