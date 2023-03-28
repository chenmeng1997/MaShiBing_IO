package com.bjmashibing.system.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
/**
 * @author 陈萌
 * @describe 多路复用器-单线程
 * @date 2023/3/21 21:55
 */
public class SocketMultiplexingSingleThreadV1 {

    // Linux下多路复用器 select poll epoll

    /**
     * 选择器-多路复用器
     */
    private Selector selector;

    /**
     * 端口号
     */
    private static final int PORT = 9090;


    /**
     * 初始化Service
     */
    public void initServer() {
        try {
            ServerSocketChannel server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(PORT));
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动多路选择器
     */
    public void start() {
        // 初始化Service
        this.initServer();
        System.out.println("服务器启动了。。。。。");

        while (true) {
            try {
                // 选择器的键集
                Set<SelectionKey> keys = selector.keys();
                System.out.println("keys.size:" + keys.size());

                // 选择一组键，其对应的通道已准备好用于I/O操作
                while (selector.select(500) > 0) {
                    // 选择器的选定键集
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> item = selectionKeys.iterator();
                    while (item.hasNext()) {
                        SelectionKey key = item.next();
                        item.remove();
                        if (key.isAcceptable()) {
                            // 接受数据
                            this.acceptHandler(key);
                        } else if (key.isReadable()) {
                            // 读取数据
                            this.readHandler(key);
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 接受数据
     *
     * @param key 表示SelectableChannel注册到选择器的标记
     */
    private void acceptHandler(SelectionKey key) throws IOException {

        // 服务端channel
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        // 客户端channel
        SocketChannel client = serverSocketChannel.accept();
        // 非阻塞
        client.configureBlocking(false);
        // 开辟堆外空间
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        client.register(selector, SelectionKey.OP_READ, buffer);
        System.out.println("-------------------------------------------");
        System.out.println("新客户端：" + client.getRemoteAddress());
        System.out.println("-------------------------------------------");
    }

    /**
     * 读取数据
     *
     * @param key 表示SelectableChannel注册到选择器的标记
     */
    private void readHandler(SelectionKey key) throws IOException {

        // 获取客户端链接
        SocketChannel client = (SocketChannel) key.channel();
        // 堆外缓存空间
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear();

        while (true) {
            int read = client.read(buffer);
            if (read > 0) {
                // 指针翻转
                buffer.flip();
                // 诉在当前位置和极限之间是否有任何元素
                while (buffer.hasRemaining()) {
                    // 写入数据
                    client.write(buffer);
                }
                buffer.clear();
            } else if (read == 0) {
                break;
            } else {
                client.close();
                break;
            }
        }
    }

    public static void main(String[] args) {
        new SocketMultiplexingSingleThreadV1().start();
    }

}
