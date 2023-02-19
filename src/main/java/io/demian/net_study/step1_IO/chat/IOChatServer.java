package io.demian.net_study.step1_IO.chat;

import io.demian.net_study.UI.ServerUI;
import io.demian.net_study.Util;
import io.demian.net_study.step1_IO.chat.model.ClientSession;
import javafx.application.Platform;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class IOChatServer extends ServerUI {

    ExecutorService executorService;
    ServerSocket serverSocket;
    List<ClientSession> connections = new Vector<ClientSession>();

    public void startServer() {
        displayText("[ServerUI : startServer executed. ");

        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        try {
            serverSocket =  new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 5001));
        } catch(Exception e) {
            e.printStackTrace();

            if(!serverSocket.isClosed()) { stopServer(); }
            return;
        }

        Runnable runnable = () -> {

            Platform.runLater(()->{
                displayText("[서버 시작]");
                btnStartStop.setText("stop");
            });

            while(true) {
                Util.println("[ServerUI : startServer runnable. ");
                try {
                    Socket socket = serverSocket.accept();
                    Util.println("[ServerUI : startServer after accept. ");
                    String message = "[연결 수락: " + socket.getRemoteSocketAddress()  + ": " + Thread.currentThread().getName() + "]";
                    Platform.runLater(()->displayText(message));

                    ClientSession client = new ClientSession(this, socket);
                    connections.add(client);
                    Util.println("[ServerUI : startServer client added");
                    Platform.runLater(()->displayText("[연결 개수: " + connections.size() + "]"));
                } catch (Exception e) {
                    e.printStackTrace();
                    if(!serverSocket.isClosed()) { stopServer(); }
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
                ClientSession client = iterator.next();
                client.socket.close();
                iterator.remove();
            }
            if(serverSocket!=null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if(executorService!=null && !executorService.isShutdown()) {
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

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
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