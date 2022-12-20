package com.bjmashibing.system.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketIO {


    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(9090, 20);

        System.out.println("step1: new ServerSocket(9090) ");

        while (true) {
            //阻塞1
            Socket client = server.accept();
            System.out.println("step2:client\t" + client.getPort());

            new Thread(() -> {
                InputStream in;
                try {
                    in = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    OutputStream output = client.getOutputStream();
                    while (true) {
                        //阻塞2
                        String dataline = reader.readLine();
                        if (null != dataline) {
                            System.out.println("dataline:"+dataline);
                            output.write("客户端回复 卧槽！".getBytes(StandardCharsets.UTF_8));
                        } else {
                            client.close();
                            break;
                        }
                    }
                    System.out.println("客户端断开");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }


}
