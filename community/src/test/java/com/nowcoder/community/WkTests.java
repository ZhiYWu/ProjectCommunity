package com.nowcoder.community;

import java.io.IOException;

public class WkTests {
    public static void main(String[] args) {
        String cmd = "d:/DevelopTolls/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://www.qq.com d:/code/JavaWorks/data/wk-images/1.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
