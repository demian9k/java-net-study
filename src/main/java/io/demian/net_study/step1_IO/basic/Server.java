package io.demian.net_study.step1_IO.basic;

import io.demian.net_study.Util;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    public static void main(String[] args) {

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("localhost", 5001));


            while(true) {
                Util.println("Waiting..");
                Socket socket = serverSocket.accept();
                InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
                Util.println("accepted.." + isa.getHostName());
            }


        } catch( Exception e ) {

        }


        if( !serverSocket.isClosed()) {
            try {
                serverSocket.close();

            } catch(Exception e) {

            }
        }

    }

}
