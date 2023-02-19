package io.demian.net_study.step3_NIO_NONBLOCK.basic;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import static io.demian.net_study.Util.println;

//http://rox-xmlrpc.sourceforge.net/niotut/

public class Server {

    private InetAddress addr;
    private int port;
    private Selector selector;
    ServerSocketChannel serverSocket = null;
    private Map<SocketChannel, List<byte[]>> dataMap;

    public Server(InetAddress addr, int port) throws IOException {
        this.addr = addr;
        this.port = port;
        dataMap = new HashMap<SocketChannel, List<byte[]>>();
    }

    void start() {
        println("Server starting..");

        try {
            selector = Selector.open();

            /**
             * Selector에 등록가능한 채널은 SelectableChannel 의 하위클래스이다.
             * ServerSocketChannel, SocketChannel, DatagramChannel 등
             */
            serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false); //selector는 non block에서만 사용 가능하다.

            // bind to port
            serverSocket.socket().bind(new InetSocketAddress(this.addr, this.port));

            /**
             * register 메서드는
             * 채널과 작업 유형 정보를 담은 Selection Key를 생성하고 Selector의 interest key set에 저장한 후 selection key를 반환한다.
             * serverSocket channel 은 읽기와 쓰기 작업 유형을 변경할 수 있다.
             */
            serverSocket.register(this.selector, SelectionKey.OP_ACCEPT);

            while(true) {
                // 작업 처리 준비된 키 감지
                int keyCount = this.selector.select();
                println("keyCount" + keyCount);
                //키가 없을 때 루프 처음으로 돌아감
                if( keyCount == 0) continue;

                // wakeup to work on selected keys
                //선택된 키셋 얻기
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                process(selectedKeys);
            }


        } catch( Exception e ) {

        }

    }

    void process(Set<SelectionKey> selectedKeys) throws IOException {
        println("Server process starting..");

        Iterator<SelectionKey> keys = selectedKeys.iterator();

        while(keys.hasNext()) {
            //키를 하나씩 커네서
            SelectionKey selectedKey = keys.next();

            //다음 tick에 같은 키가 다시 오는 것을 방지하기 위해 필요함
            // 선택된 키셋에서 처리 완료된 Selection Key를 제거
            keys.remove();

            if (!selectedKey.isValid())
                continue;

            if( selectedKey.isAcceptable() )
                accept(selectedKey);
            else if( selectedKey.isReadable() )
                read(selectedKey);
            else if( selectedKey.isWritable() )
                write(selectedKey);
        }
    }


    private void accept(SelectionKey key) throws IOException {
        println("Server accept()");
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);

        channel.write(ByteBuffer.wrap("echo response! \r\n".getBytes("UTF-8")));

        // register channel with selector for further IO
        dataMap.put(channel, new ArrayList<byte[]>());

        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        println("Server read()");
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);

        int numRead = -1;

        try {
            numRead = channel.read(buffer);
        } catch(IOException e) {
            e.printStackTrace();
        }

        if( numRead == -1 ) {
            this.dataMap.remove(channel);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];

        System.arraycopy(buffer.array(), 0, data, 0, numRead);

        //write back to client;
        doEcho(key, data);
    }

    private void write(SelectionKey key) throws IOException {
        println("Server write()");
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = this.dataMap.get(channel);

        Iterator<byte[]> items = pendingData.iterator();

        while(items.hasNext()) {
            byte[] item = items.next();
            items.remove();
            channel.write(ByteBuffer.wrap(item));
        }

        //socket channel 은 읽기와 쓰기 작업 유형을 변경할 수 있다.
        key.interestOps(SelectionKey.OP_WRITE);

    }

    private void doEcho(SelectionKey key, byte[] data) {
        println("Server doEcho()");
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = this.dataMap.get(channel);
        pendingData.add(data);
        key.interestOps(SelectionKey.OP_WRITE);
    }

}
