package com.bjmashibing.system.io;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * @author: 马士兵教育 客户端
 * @create: 2020-05-17 16:18
 */
public class SocketClient {

    public static void main(String[] args) {

        try {
            // 183.159.127.146
            Socket client = new Socket("127.0.0.1", 9090);
            client.setSendBufferSize(20);
            // 不延迟
            client.setTcpNoDelay(true);
            OutputStream out = client.getOutputStream();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                // 发消息给服务端
                System.out.println("请输入：");
                String line = scanner.next();

                if ("out".equalsIgnoreCase(line)) {
                    break;
                }

                if (line != null) {
                    out.write(line.getBytes(StandardCharsets.UTF_8));
                }

                // 读服务端发来消息
                InputStream inputStream = client.getInputStream();
                byte[] bytes = new byte[1024];
                int read = inputStream.read(bytes);
                System.out.println("读服务端发来消息:" + new String(bytes, 0, read, Charset.defaultCharset()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
