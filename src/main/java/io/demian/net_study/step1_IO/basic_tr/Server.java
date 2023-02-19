package io.demian.net_study.step1_IO.basic_tr;

import io.demian.net_study.Util;

import java.io.InputStream;
import java.io.OutputStream;
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

                byte[] bytes = null;
                String message = null;


                InputStream is = socket.getInputStream();
                bytes = new byte[100];
                int readByteCount = is.read(bytes);

                message = new String(bytes, 0, readByteCount, "UTF-8");
                Util.println("message received!. " + message);


                OutputStream os = socket.getOutputStream();
                message = "hello client!";

                bytes = message.getBytes("UTF-8");
                os.write(bytes);

                is.close();
                os.close();
                socket.close();
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
