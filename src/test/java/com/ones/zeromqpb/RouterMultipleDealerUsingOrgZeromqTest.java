package com.ones.zeromqpb;

import org.junit.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import static org.zeromq.ZMQ.DONTWAIT;
import static org.zeromq.ZMQ.SNDMORE;

/**
 * 模仿go/java两个host与开放平台的通讯过程（使用org.zeromq包中类）
 * 两个host与开发平台通过携带ZMQ_IDENTITY，各聊各的，互不牵扯
 * ---
 * 当Router DONT_WAIT接受时，会阻塞
 */
public class RouterMultipleDealerUsingOrgZeromqTest {

  @Test
  public void platFormRouter() {
    try (ZContext zContext = new ZContext(1); org.zeromq.ZMQ.Socket socket = zContext.createSocket(SocketType.ROUTER)) {
      socket.bind("tcp://127.0.0.1:12345");
      byte[] source = null;
      int i = 0;
      while (!Thread.currentThread().isInterrupted()) {
        byte[] s = socket.recv(0);
        // 这种形式有问题
//        byte[] s = socket.recv(DONTWAIT);
        if (s != null) {
          System.out.println(new String(s));
        }
        // 当前轮次是偶数次的时候，发送消息
        if ((i++ & 1) > 0 && source != null) {
          socket.send(source, SNDMORE);
          socket.send(new String(source) + "你好，我是平台", 0);
        } else {
          source = s;
        }
      }
    }
  }

  @Test
  public void javaDealer() {
    try (ZContext zContext = new ZContext(1);
         org.zeromq.ZMQ.Socket socket = zContext.createSocket(SocketType.DEALER)) {
      socket.setIdentity("Java Host".getBytes());
      socket.connect("tcp://127.0.0.1:12345");
      for (int i = 0; i < 200; i++) {
        Thread.sleep(300);
        boolean send = socket.send("你好，我是javaHost", DONTWAIT);
        if (!send) {
          System.err.println("发送失败");
        }
        byte[] receive = socket.recv(0);
        if (null != receive) {
          System.out.println(new String(receive));
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void goDealer() {
    try (ZContext zContext = new ZContext(1);
         org.zeromq.ZMQ.Socket socket = zContext.createSocket(SocketType.DEALER)) {
      socket.setIdentity("Go Host".getBytes());
      socket.connect("tcp://127.0.0.1:12345");
      for (int i = 0; i < 200; i++) {
        Thread.sleep(500);
        boolean send = socket.send("你好，我是GoHost", DONTWAIT);
        if (!send) {
          System.err.println("发送失败");
        }
        byte[] receive = socket.recv(0);
        if (null != receive) {
          System.out.println(new String(receive));
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
