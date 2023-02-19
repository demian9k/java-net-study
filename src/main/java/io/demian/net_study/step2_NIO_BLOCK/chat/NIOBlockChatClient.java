package io.demian.net_study.step2_NIO_BLOCK.chat;

import io.demian.net_study.BufferUtil;
import io.demian.net_study.NIO.NIOClient;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import static io.demian.net_study.Util.println;

public class NIOBlockChatClient extends NIOClient {

    public void startClient() {
        println("[ClientUI : startClient executed. ");

        Thread thread = new Thread() {

            public void run() {
                try {
                    socket = SocketChannel.open();
                    socket.configureBlocking(true);
                    socket.connect(new InetSocketAddress("localhost", 5001));

                    InetSocketAddress isa = (InetSocketAddress) socket.getRemoteAddress();

                    Platform.runLater(()->{
                        displayText("[연결 완료: "  + isa.getHostName() + "]");
                        btnConn.setText("stop");
                        btnSend.setDisable(false);
                    });

                } catch(Exception e) {
                    Platform.runLater(()->displayText("[서버 통신 안됨]"));
                    if(socket.isOpen()) { stopClient(); }
                    return;
                }

                receive();
            }

        };

        thread.start();
    }

    public void receive() {
        println("[ClientUI : receive executed. ");

        while(true) {
            println("[ClientUI : receive while. ");
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(100);

                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socket.read(byteBuffer);

                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if(readByteCount == -1) { throw new IOException(); }

                byteBuffer.flip();
                String data = BufferUtil.decode(byteBuffer);

                Platform.runLater(()->displayText("[받기 완료] "  + data));
            } catch (Exception e) {
                e.printStackTrace();
                stop();
                break;
            }
        }

    }

    public void send(String data) {
        println("[ClientUI : send executed. ");

        Runnable runnable = () -> {
            println("[ClientUI : send runnable. ");
            try {
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                ByteBuffer byteBuffer = BufferUtil.encode(data);
                socket.write(byteBuffer);
                Platform.runLater(()->displayText("[보내기 완료]"));
            } catch(Exception e) {
                stop();
            }
        };

        runnable.run();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
