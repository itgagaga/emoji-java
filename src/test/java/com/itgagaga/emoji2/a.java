package com.itgagaga.emoji2;

import java.util.Arrays;

public class a {
    public static void main(String[] args) {
        String s ="\n" +
                "                    \uD83D\uDCA9\n" +
                "                \n" +
                "                    \uD83D\uDC0D\n" +
                "                \n" +
                "                    \uD83C\uDF44\u200D\uD83D\uDFEB\n" +
                "                ";
        System.out.println((s.split(" +")[0] + s.split(" +")[1] + s.split(" +")[2]));
    }
}
