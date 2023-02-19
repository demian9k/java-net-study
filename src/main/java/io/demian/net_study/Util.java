package io.demian.net_study;

import java.lang.reflect.Method;

public class Util {

    public static void println(String ...strs) {
        String sum = "";
        for( String s : strs) {
            sum += s;
        }
        System.out.println(sum);
    }
}
