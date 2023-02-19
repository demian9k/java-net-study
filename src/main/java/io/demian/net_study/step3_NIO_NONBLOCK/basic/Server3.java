package io.demian.net_study.step3_NIO_NONBLOCK.basic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Server3 {

    private Map<SocketChannel, List<byte[]>> dataTrack = new HashMap<>();
    private ByteBuffer buff = ByteBuffer.allocate(2048);

    public void start(){

        try (
                Selector selector = Selector.open();
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        ) {

            if(selector.isOpen() && serverSocketChannel.isOpen()) {

                // non-blocking
                serverSocketChannel.configureBlocking(false);

                // bind port
                serverSocketChannel.bind(new InetSocketAddress(10020));


                // op_accept register selector
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


                while(true) {

                    selector.select();

                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();


                    while(iter.hasNext()) {

                        SelectionKey selectedKey = iter.next();
                        iter.remove();

                        System.out.println("selectedKey : " + selectedKey);
                        System.out.println("isValid : " + selectedKey.isValid());
                        System.out.println("isWritable : " + selectedKey.isWritable());
                        System.out.println("isAcceptable : " + selectedKey.isAcceptable());
                        System.out.println("isConnectable : " + selectedKey.isConnectable());
                        System.out.println("isReadable : " + selectedKey.isReadable());


                        if(selectedKey.isValid() && selectedKey.isAcceptable()) {
                            accept(selector, selectedKey);
                        }

                        if(selectedKey.isValid() && selectedKey.isReadable()) {
                            read(selectedKey);
                        }

                        if(selectedKey.isValid() && selectedKey.isWritable()) {
                            write(selectedKey);
                        }

                    }


                }

            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    public void accept(Selector seleector, SelectionKey key){
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(seleector, SelectionKey.OP_READ);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void read(SelectionKey key){

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

            buff.clear();
            int numLen  = -1;
            try {
                numLen = socketChannel.read(buff);

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            if(numLen == -1){
                dataTrack.remove(socketChannel);
                socketChannel.close();
                key.cancel();
                return;
            }

            byte[] data = new byte[numLen];
            System.arraycopy(buff.array(), 0, data, 0, numLen);
            System.out.println(new String(data));


            List<byte[]> channelData = dataTrack.computeIfAbsent(socketChannel, (k) -> {
                return new ArrayList<byte[]>();
            });

            channelData.add(data);

            key.interestOps(SelectionKey.OP_WRITE);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public void write(SelectionKey key){

        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();

            List<byte[]> channelData = dataTrack.computeIfAbsent(socketChannel, (k) -> {
                return new ArrayList<byte[]>();
            });


            try {
                System.out.println("500ms wait()");
                TimeUnit.MILLISECONDS.sleep(500);
                System.out.println("500ms wait() end");
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            socketChannel.write(ByteBuffer.wrap("HTTP/1.1 200 OK!!".getBytes()));

            Iterator<byte[]> iter = channelData.iterator();
            while(iter.hasNext()) {

                byte[] data = iter.next();

                iter.remove();

                socketChannel.write(ByteBuffer.wrap(data));
            }

            key.interestOps(SelectionKey.OP_READ);


        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }
}