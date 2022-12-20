package com.bjmashibing.system.io.test;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
            fileOutputStream.write();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        System.out.println("nNN");

    }

}
