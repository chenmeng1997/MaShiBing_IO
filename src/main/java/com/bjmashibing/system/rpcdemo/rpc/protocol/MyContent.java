package com.bjmashibing.system.rpcdemo.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: 马士兵教育
 * @create: 2020-08-16 20:35
 */
@Data
public class MyContent implements Serializable {

    String name;
    String methodName;
    Class<?>[] parameterTypes;
    Object[] args;
    //返回的数据
    Object res;

}
