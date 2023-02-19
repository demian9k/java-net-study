package io.demian.net_study.step1_IO.basic_tr;

import io.demian.net_study.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

    public static void main(String[] args) {

        Socket socket = null;

        try {
            socket = new Socket();
            Util.println("request connect");
            socket.connect(new InetSocketAddress("localhost", 5001));
            Util.println("connected.");


            byte[] bytes = null;
            String message = null;

            OutputStream os = socket.getOutputStream();
            message = "Hello Server";
            bytes = message.getBytes();
            os.write(bytes);
            os.flush();

            Util.println("sending data success!");

            InputStream is = socket.getInputStream();
            bytes = new byte[100];
            int readByteCount = is.read(bytes);
            message = new String(bytes, 0, readByteCount, "UTF-8");
            Util.println("receving data success!");


            os.close();
            is.close();

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
