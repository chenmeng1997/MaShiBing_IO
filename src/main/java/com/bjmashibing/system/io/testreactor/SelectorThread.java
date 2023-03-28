package com.bjmashibing.system.io.testreactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: 马士兵教育
 * @create: 2020-06-21 20:14
 */
public class SelectorThread extends ThreadLocal<LinkedBlockingQueue<Channel>> implements Runnable {
    // 每线程对应一个selector，
    // 多线程情况下，该主机，该程序的并发客户端被分配到多个selector上
    // 注意，每个客户端，只绑定到其中一个selector
    // 其实不会有交互问题
    /**
     * 选择器
     */
    Selector selector = null;

    /**
     * 在接口或者类中是固定使用方式逻辑写死了。你需要是lbq每个线程持有自己的独立对象
     */
    LinkedBlockingQueue<Channel> blockingQueues = this.get();

    /**
     * 工作选择器组
     */
    SelectorThreadGroup threadGroup;

    @Override
    protected LinkedBlockingQueue<Channel> initialValue() {
        //你要丰富的是这里！  pool。。。
        return new LinkedBlockingQueue<>();
    }

    SelectorThread(SelectorThreadGroup threadGroup) {
        try {
            this.threadGroup = threadGroup;
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        while (true) {
            try {
                // 阻塞  wakeup()
                int nums = selector.select();
                // 2,处理selectkeys
                if (nums > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        // 线程处理的过程
                        SelectionKey key = iter.next();
                        iter.remove();
                        // 复杂,接受客户端的过程（接收之后，要注册，多线程下，新的客户端，注册到那里呢？）
                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        } else if (key.isWritable()) {

                        }
                    }
                }
                // 3,处理一些task :  listen  client
                if (!blockingQueues.isEmpty()) {   //队列是个啥东西啊？ 堆里的对象，线程的栈是独立，堆是共享的
                    //只有方法的逻辑，本地变量是线程隔离的
                    Channel c = blockingQueues.take();
                    if (c instanceof ServerSocketChannel) {
                        ServerSocketChannel server = (ServerSocketChannel) c;
                        server.register(selector, SelectionKey.OP_ACCEPT);
                        System.out.println(Thread.currentThread().getName() + " register listen");
                    } else if (c instanceof SocketChannel) {
                        SocketChannel client = (SocketChannel) c;
                        ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                        client.register(selector, SelectionKey.OP_READ, buffer);
                        System.out.println(Thread.currentThread().getName() + " register client: " + client.getRemoteAddress());
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void readHandler(SelectionKey key) throws IOException {
        System.out.println(Thread.currentThread().getName() + " read......");
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel client = (SocketChannel) key.channel();
        buffer.clear();
        while (true) {
            int num = client.read(buffer);
            if (num > 0) {
                // 将读到的内容翻转，然后直接写出
                buffer.flip();
                while (buffer.hasRemaining()) {
                    client.write(buffer);
                }
                buffer.clear();
            } else if (num == 0) {
                break;
            } else {
                // 客户端断开了
                System.out.println("client: " + client.getRemoteAddress() + "closed......");
                key.cancel();
                break;
            }
        }
    }

    private void acceptHandler(SelectionKey key) throws IOException {
        System.out.println(Thread.currentThread().getName() + "   acceptHandler......");
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        // choose a selector  and  register!!
        threadGroup.nextSelectorV3(client);
        // stg.nextSelectorV2(client);
    }

    /**
     * 设置工作选择器组
     *
     * @param stgWorker 工作选择器组
     */
    public void setWorker(SelectorThreadGroup stgWorker) {
        this.threadGroup = stgWorker;
    }

}
