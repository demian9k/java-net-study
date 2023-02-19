package io.demian.net_study.step1_IO.chat;

import java.io.*;
import java.net.*;

import io.demian.net_study.UI.ClientUI;
import io.demian.net_study.Util;
import javafx.application.*;

public class IOChatClient extends ClientUI {
    Socket socket;

    public void startClient() {
        Util.println("[ClientUI : startClient executed. ");

        Thread thread = new Thread() {

            public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress("localhost", 5001));

                    Platform.runLater(()->{
                        displayText("[연결 완료: "  + socket.getRemoteSocketAddress() + "]");
                        btnConn.setText("stop");
                        btnSend.setDisable(false);
                    });

                } catch(Exception e) {
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                    if(!socket.isClosed()) { stopClient(); }
                    return;
                }

                receive();
            }

        };

        thread.start();
    }

    public void stopClient() {
        Util.println("[ClientUI : stopClient executed. ");

        try {

            Platform.runLater(()->{
                displayText("[연결 끊음]");
                btnConn.setText("start");
                btnSend.setDisable(true);
            });

            if(socket!=null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException e) {}
    }

    public void receive() {
        Util.println("[ClientUI : receive executed. ");

        while(true) {
            Util.println("[ClientUI : receive while. ");
            try {
                byte[] byteArr = new byte[100];
                InputStream inputStream = socket.getInputStream();

                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = inputStream.read(byteArr);

                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if(readByteCount == -1) { throw new IOException(); }

                String data = new String(byteArr, 0, readByteCount, "UTF-8");

                Platform.runLater(()->displayText("[받기 완료] "  + data));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(()->displayText("[서버 통신 안됨]"));
                stopClient();
                break;
            }
        }

    }

    public void send(String data) {
        Util.println("[ClientUI : send executed. ");

        Runnable runnable = () -> {
            Util.println("[ClientUI : send runnable. ");
            try {
                byte[] byteArr = data.getBytes("UTF-8");
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(byteArr);
                outputStream.flush();
                Platform.runLater(()->displayText("[보내기 완료]"));
            } catch(Exception e) {
                Platform.runLater(()->displayText("[서버 통신 안됨]"));
                stopClient();
            }
        };

        runnable.run();
    }
}
