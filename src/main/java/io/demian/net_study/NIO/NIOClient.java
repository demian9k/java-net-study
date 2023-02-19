package io.demian.net_study.NIO;

import io.demian.net_study.UI.ClientUI;
import io.demian.net_study.Util;
import javafx.application.Platform;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class NIOClient extends ClientUI {
    public SocketChannel socket;

    public void stopClient() {
        Util.println("[ClientUI : stopClient executed. ");

        try {

            Platform.runLater(()->{
                displayText("[연결 끊음]");
                btnConn.setText("start");
                btnSend.setDisable(true);
            });

            if(socket!=null && socket.isOpen()) {
                socket.close();
            }

        } catch (IOException e) {}
    }

    public void stop() {
        Platform.runLater(()->displayText("[서버 통신 안됨]"));
        stopClient();
    }
}


