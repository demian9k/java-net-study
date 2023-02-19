package io.demian.net_study.step4_NIO_ASYNC.chat;

import io.demian.net_study.UI.ServerUI;
import io.demian.net_study.step4_NIO_ASYNC.chat.model.ClientSession;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

public class NIOAsyncChatServer extends ServerUI {

    AsynchronousChannelGroup channelGroup;
    AsynchronousServerSocketChannel serverSocket;
    List<ClientSession> connections = new Vector<ClientSession>();

    public void startServer() {
        displayText("[ServerUI : startServer executed. ");

        try {
            channelGroup = AsynchronousChannelGroup.withFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                Executors.defaultThreadFactory()
            );
            serverSocket = AsynchronousServerSocketChannel.open(channelGroup);
            serverSocket.bind(new InetSocketAddress(5001));

        } catch(Exception e) {
            e.printStackTrace();
            if(serverSocket.isOpen()) { stopServer(); }
            return;
        }

        Platform.runLater(()->{
            displayText("[서버 시작]");
            btnStartStop.setText("stop");
        });

        serverSocket.accept(null, acceptCompletionHandler);
    }

    CompletionHandler acceptCompletionHandler = new CompletionHandler<AsynchronousSocketChannel, Void>() {

        @Override
        public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
            try {
                String message = "[연결 수락: " + socketChannel.getRemoteAddress() + ": " + Thread.currentThread().getName() + "]";
                Platform.runLater( () -> displayText(message));

                ClientSession session = new ClientSession(NIOAsyncChatServer.this, socketChannel);
                connections.add(session);

                Platform.runLater( () -> displayText("[연결 개수:"+ connections.size() +"]") );

                serverSocket.accept(null, this);
            } catch(Exception e) {

            }
        }

        @Override
        public void failed(Throwable exc, Void attachment) {
            if(serverSocket.isOpen()) stopServer();
        }

    };


    public void stopServer() {
        displayText("[ServerUI : stopServer executed. ");
        try {

            connections.clear();

            if( channelGroup != null && !channelGroup.isShutdown() )
                channelGroup.shutdownNow();

            Platform.runLater( () -> {
                displayText("[서버 멈춤]");
                btnStartStop.setText("start");
            });

        } catch(Exception e) {

        }
    }

    public AsynchronousServerSocketChannel getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(AsynchronousServerSocketChannel serverSocket) {
        this.serverSocket = serverSocket;
    }

    public List<ClientSession> getConnections() {
        return connections;
    }

    public void setConnections(List<ClientSession> connections) {
        this.connections = connections;
    }

    public static void main(String[] args) {
        launch(args);
    }
}