package com.bjmashibing.system.rpc;

/**
 * @author: 马士兵教育
 * @create: 2020-07-19 20:47
 */
public class Packmsg {

    MyHeader header;
    MyContent content;

    public MyHeader getHeader() {
        return header;
    }

    public void setHeader(MyHeader header) {
        this.header = header;
    }

    public MyContent getContent() {
        return content;
    }

    public void setContent(MyContent content) {
        this.content = content;
    }

    public Packmsg(MyHeader header, MyContent content) {
        this.header = header;
        this.content = content;
    }
}
