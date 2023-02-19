package io.demian.net_study.step2_NIO_BLOCK.chat;

import io.demian.net_study.UI.ServerUI;
import io.demian.net_study.step2_NIO_BLOCK.chat.model.ClientSession;
import javafx.application.Platform;

import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.demian.net_study.Util.println;


public class NIOBlockChatServer extends ServerUI {

    ExecutorService executorService;
    ServerSocketChannel serverSocket;
    List<ClientSession> connections = new Vector<ClientSession>();

    public void startServer() {
        displayText("[ServerUI : startServer executed. ");

        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        try {
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(true);
            serverSocket.bind(new InetSocketAddress( 5001));
        } catch(Exception e) {
            e.printStackTrace();

            if(serverSocket.isOpen()) { stopServer(); }
            return;
        }


        Runnable runnable = () -> {

            Platform.runLater(()->{
                displayText("[서버 시작]");
                btnStartStop.setText("stop");
            });

            while(true) {
                try {
                    SocketChannel socket = serverSocket.accept();
                    println("[ServerUI : startServer after accept. ");
                    InetSocketAddress isa = (InetSocketAddress) socket.getRemoteAddress();

                    String message = "[연결 수락: " + isa.getHostName() + "]";
                    Platform.runLater(()->displayText(message));

                    ClientSession session = new ClientSession(this, socket);
                    connections.add(session);

                    Platform.runLater(()->displayText("[연결 개수: " + connections.size() + "]"));
                } catch (Exception e) {
                    e.printStackTrace();
                    if(serverSocket.isOpen()) { stopServer(); }
                    break;
                }
            }
        };

        executorService.submit(runnable);
    }

    public void stopServer() {
        displayText("[ServerUI : stopServer executed. ");
        try {
            Iterator<ClientSession> iterator = connections.iterator();
            while(iterator.hasNext()) {
                ClientSession session = iterator.next();
                session.socket.close();
                iterator.remove();
            }
            if(serverSocket !=null && serverSocket.isOpen()) {
                serverSocket.close();
            }
            if(executorService !=null && !executorService.isShutdown()) {
                executorService.shutdown();
            }

            Platform.runLater(()->{
                displayText("[서버 멈춤]");
                btnStartStop.setText("start");
            });

        } catch (Exception e) { }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
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

    public static void main(String[] args) {
        launch(args);
    }
}