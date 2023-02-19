package io.demian.net_study.step3_NIO_NONBLOCK.basic;

import java.io.IOException;

public class ServerDriver {
    public static void main(String[] args) {
        new ServerDriver().initServer();
    }


    void initServer() {
        try {
            new Server(null, 5001).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initServer3() {
        new Server3().start();
    }

}