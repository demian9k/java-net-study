package io.demian.net_study;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class BufferUtil {

    private static Charset charset = Charset.forName("UTF-8");

    public static String decode(ByteBuffer buffer) {
        return charset.decode(buffer).toString();
    }

    public static ByteBuffer encode(String data) {
        return charset.encode(data);
    }

}
