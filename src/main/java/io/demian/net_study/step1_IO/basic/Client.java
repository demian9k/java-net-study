package io.demian.net_study.step1_IO.basic;

import io.demian.net_study.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        Socket socket = null;

        try {
            socket = new Socket();
            Util.println("socket request!");
            socket.connect(new InetSocketAddress("localhost", 5001));
            Util.println("request success!!");
        } catch(Exception e) {

        }



        if( !socket.isClosed() ) {
            try {
                socket.close();

            } catch(IOException e) {

            }
        }



    }

}
