package com.bjmashibing.system.io.testreactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: 马士兵教育
 * @describe 选择器线程组
 * @create: 2020-06-21 20:37
 */
public class SelectorThreadGroup {

    SelectorThread[] selectorThreadArray;
    ServerSocketChannel server = null;
    AtomicInteger xid = new AtomicInteger(0);
    SelectorThreadGroup stg = this;

    /**
     * 选择器线程组-工作线程组
     *
     * @param worker 选择器线程组
     */
    public void setWorker(SelectorThreadGroup worker) {
        this.stg = worker;
    }

    /**
     * 选择器线程组
     *
     * @param num 选择器线程数
     */
    SelectorThreadGroup(int num) {
        // num 线程数
        selectorThreadArray = new SelectorThread[num];
        for (int i = 0; i < num; i++) {
            selectorThreadArray[i] = new SelectorThread(this);
            new Thread(selectorThreadArray[i]).start();
        }
    }

    /**
     * 选择器绑定端口
     *
     * @param port 绑定端口
     */
    public void bind(int port) {
        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));

            // 注册到那个selector上
            this.nextSelectorV3(server);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取选择器
     *
     * @param c 链接/通道
     */
    public void nextSelectorV3(Channel c) {
        try {
            if (c instanceof ServerSocketChannel) {
                // listen 选择了 boss组中的一个线程后，要更新这个线程的work组
                SelectorThread st = this.next();
                st.blockingQueues.put(c);
                st.setWorker(stg);
                st.selector.wakeup();
            } else {
                // 在 main线程种，取到堆里的selectorThread对象
                SelectorThread st = this.nextV3();
                // 1,通过队列传递数据 消息
                st.blockingQueues.add(c);
                // 2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
                st.selector.wakeup();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void nextSelectorV2(Channel c) {
        try {
            if (c instanceof ServerSocketChannel) {
                selectorThreadArray[0].blockingQueues.put(c);
                selectorThreadArray[0].selector.wakeup();
            } else {
                // 在 main线程种，取到堆里的selectorThread对象
                SelectorThread st = this.nextV2();
                // 1,通过队列传递数据 消息
                st.blockingQueues.add(c);
                // 2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
                st.selector.wakeup();

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void nextSelector(Channel c) {
        // 在 main线程种，取到堆里的selectorThread对象
        SelectorThread st = this.next();
        // 1,通过队列传递数据 消息
        st.blockingQueues.add(c);
        // 2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
        st.selector.wakeup();

    }

    public void nextSelector2(Channel c) {
        // 在 main线程种，取到堆里的selectorThread对象
        SelectorThread st = this.next();
        // 1,通过队列传递数据 消息
        st.blockingQueues.add(c);
        // 2,通过打断阻塞，让对应的线程去自己在打断后完成注册selector
        st.selector.wakeup();
        // 重点：c有可能是 server 有可能是client
        ServerSocketChannel s = (ServerSocketChannel) c;
        // 呼应上，int nums = selector.select();  //阻塞  wakeup()
        try {
            // 会被阻塞的!!!!!
            s.register(st.selector, SelectionKey.OP_ACCEPT);
            // 功能是让 selector的select（）方法，立刻返回，不阻塞！
            st.selector.wakeup();
            System.out.println("aaaaa");
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    // 无论 serversocket  socket  都复用这个方法
    private SelectorThread next() {
        // 轮询就会很尴尬，倾斜
        int index = xid.incrementAndGet() % selectorThreadArray.length;
        return selectorThreadArray[index];
    }

    private SelectorThread nextV2() {
        // 轮询就会很尴尬，倾斜
        int index = xid.incrementAndGet() % (selectorThreadArray.length - 1);
        return selectorThreadArray[index + 1];
    }

    private SelectorThread nextV3() {
        // 动用worker的线程分配
        int index = xid.incrementAndGet() % stg.selectorThreadArray.length;
        return stg.selectorThreadArray[index];
    }

}
