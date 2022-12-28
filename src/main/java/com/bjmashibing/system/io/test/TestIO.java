package com.bjmashibing.system.io.test;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author 陈萌
 * @describe TestIO
 * @date 2022/11/17 23:17
 */
public class TestIO {

    @Test
    public void test() {

        try {
            FileOutputStream fileOutputStream = new FileOutputStream("");
            try {
                fileOutputStream.write(new byte[1024]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("nNN");

    }

}
