# 简介

Stomp-J是一个轻量级stomp协议的java client api，除了java标准库外、api没有其他依赖。

Stomp协议是一个类似http格式的MQ领域的消息传输协议：<https://stomp.github.io/stomp-specification-1.2.html>

大部分主流消息队列产品都支持stomp协议。包括ActiveMQ，RabbitMQ等。

示例代码基于rabbitmq。

# API用法

## 协议参数

- server ip：服务IP
- port：服务端口、默认61613
- host：虚拟主机、等同于名字空间。MQ用虚拟主机对资源（topic、queue等）分组。

    rabbitmq使用不同的host来控制访问权限。

- login(username)：用户名
- passcode：密码


## 创建连接

```java

Connection con = new Connection("localhost", 61613, "username", "passcode");

```


## 添加消息处理器

```java

// message listener
con.setMessageHandler(new MessageHandler() {
    public void onMessage(Message msg) {
        System.out.println("message---------");
        String[] propNameS = msg.getPropertyNames();    // 消息头字段
        for (String propName : propNameS) {
            System.out.println(propName + ":" + msg.getProperty(propName));
        }
        System.out.println(msg.getContentAsString());   // 消息内容
    }
});

// error listener
con.setErrorHandler(new ErrorHandler() {
    @Override
    public void onError(ErrorMessage errormessage) {
        System.out.println(errormessage.getMessage());
    }
});

```


## 发起连接

```java

HashMap<String, String> connectOptionalHeaders = new HashMap<>();
connectOptionalHeaders.put("host", "vhost-test");

con.connect(connectOptionalHeaders);

```

注意：

1. 发起连接必须在设置消息监听器之后
1. 连接时、必须设置host头字段、否则权限校验会失败。


## 订阅消息

订阅发送目标名能匹配“*.sub2”的消息。

```java

con.subscribe("/topic/*.sub2");

```

rabbitmq的destnation部分可以使用通配符。


## 发送消息

```java

con.send("you can recive it", "/topic/sender1.sub2");   // 因为发送目标名能匹配“*.sub2”，所以前面的订阅者（当前会话）可以收到。
con.send("you cannot recive it", "/topic/sender1.sub3");    // 发送目标名不匹配“*.sub2”，所以前面的订阅者（当前会话）无法收到。

```

## 关闭链接

```java

con.disconnect();

```

# 示例代码

## 发布者

```java

package example;

import pk.aamir.stompj.Connection;
import pk.aamir.stompj.StompJException;

import java.util.HashMap;

public class Publisher {

    public static void main(String[] args) throws StompJException {
        // connection
        Connection con = new Connection("rabbitmq-host-ip", 61613, "user-test", "123");

        // connect
        HashMap<String, String> connectOptionalHeaders = new HashMap<>();
        connectOptionalHeaders.put("host", "vhost-test");
        con.connect(connectOptionalHeaders);

        // send
        con.send("Java test message, from sender1!", "/topic/sender1.sub2");

        // disconnect
        con.disconnect();
    }
}

```

## 订阅者

```java

package example;

import pk.aamir.stompj.*;

import java.io.IOException;
import java.util.HashMap;

public class Listener {

    public static void main(String[] args) throws StompJException, IOException {
        // connect
        Connection con = new Connection("rabbitmq-host-ip", 61613, "user-test", "123");

        // add msg listener
        con.setMessageHandler(new MessageHandler() {
            public void onMessage(Message msg) {
                System.out.println("message---------");
                String[] propNameS = msg.getPropertyNames();
                for (String propName : propNameS) {
                    System.out.println(propName + ":" + msg.getProperty(propName));
                }
                System.out.println(msg.getContentAsString());
            }
        });

        // add error listener
        con.setErrorHandler(new ErrorHandler() {
            @Override
            public void onError(ErrorMessage errormessage) {
                System.out.println(errormessage.getMessage());
            }
        });

        // connect
        HashMap<String, String> connectOptionalHeaders = new HashMap<>();
        connectOptionalHeaders.put("host", "vhost-test");
        con.connect(connectOptionalHeaders);

        // subscribe
        con.subscribe("/topic/*.sub2");

        System.in.read();

        // disconnect
        con.disconnect();
    }
}


```


# API存在的问题

这个库代码是从别人开源的代码中clone过来的、但是原始库的地址找不到了。这个库对stomp协议的实现很不完整、只能满足一些基本功能。

1. 一个connection只能发起一个订阅

    发送SUBSCRIBE时没有设置id、收到的消息没有办法正确路由到不同的subscribtion。

2. 不支持主动UNSUBSCRIBE、请直接采用disconnect方式。

    这里的理由同上、因为前面没有设置id。

3. 不支持事务
