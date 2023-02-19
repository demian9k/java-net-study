package io.demian.net_study.step1_IO.chat.model;

import io.demian.net_study.Util;
import io.demian.net_study.step1_IO.chat.IOChatServer;
import javafx.application.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSession {
    public Socket socket;
    public IOChatServer ioChatServer;

    public ClientSession(IOChatServer server, Socket socket) {
        this.ioChatServer = server;
        this.socket = socket;
        receive();
    }

    void receive() {
        Util.println("[Client : receive executed. ");
        Runnable runnable = () -> {
            try {
                while(true) {
                    byte[] byteArr = new byte[100];
                    InputStream inputStream = socket.getInputStream();

                    //클라이언트가 비저상 종료를 했을 경우 IOException 발생
                    int readByteCount = inputStream.read(byteArr);

                    //클라이언트가 정상적으로 Socket의 close()를 호출했을 경우
                    if(readByteCount == -1) {  throw new IOException(); }

                    String message = "[요청 처리: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";

                    Platform.runLater(()-> ioChatServer.displayText(message));

                    String data = new String(byteArr, 0, readByteCount, "UTF-8");

                    for(ClientSession client : ioChatServer.getConnections()) {
                        client.send(data);
                    }
                }
            } catch(Exception e) {
                Util.println("receive catch 1");
                e.printStackTrace();

                try {
                    ioChatServer.getConnections().remove(this);
                    String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";

                    Platform.runLater(()-> ioChatServer.displayText(message));

                    socket.close();
                } catch (IOException e2) {
                    Util.println("receive catch 2");
                    e2.printStackTrace();
                }
            }
        };

        ioChatServer.getExecutorService().submit(runnable);
    }

    void send(String data) {
        Util.println("[Client : send executed. ");

        Runnable runnable = () -> {
            try {

                byte[] byteArr = data.getBytes("UTF-8");

                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(byteArr);
                outputStream.flush();

            } catch(Exception e) {
                Util.println("send catch 1");
                e.printStackTrace();
                try {
                    String message = "[클라이언트 통신 안됨: " + socket.getRemoteSocketAddress() + ": " + Thread.currentThread().getName() + "]";

                    Platform.runLater(()-> ioChatServer.displayText(message));

                    ioChatServer.getConnections().remove(ClientSession.this);

                    socket.close();
                } catch (IOException e2) {
                    Util.println("send catch 2");
                    e2.printStackTrace();
                }
            }
        };

        ioChatServer.getExecutorService().submit(runnable);
    }
}