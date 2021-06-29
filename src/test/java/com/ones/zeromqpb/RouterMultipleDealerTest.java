package com.ones.zeromqpb;

import org.junit.Assert;
import org.junit.Test;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;

import java.nio.charset.StandardCharsets;

/**
 * 模仿go/java两个host与开放平台的通讯过程
 * 两个host与开发平台通过携带ZMQ_IDENTITY，各聊各的，互不牵扯
 * 使用ZQM的api
 */
public class RouterMultipleDealerTest {

  @Test
  public void platFormRouter() {
    Ctx ctx = null;
    SocketBase router = null;
    try {
      ctx = ZMQ.init(1);
      router = ZMQ.socket(ctx, ZMQ.ZMQ_ROUTER);
      ZMQ.bind(router, "tcp://127.0.0.1:12345");
      ZMQ.setSocketOption(router, ZMQ.ZMQ_ROUTER_HANDOVER, 1);
      while (!Thread.currentThread().isInterrupted()) {
        // 收到的第一个消息应该是Dealer的optionValue0
        Msg msg = ZMQ.recv(router, 0);
        String dealer = convertFromMsg(msg);
        System.out.println("Dealer.option value is: " + dealer);
        // 收到的第二个消息是Dealer实体消息
        msg = ZMQ.recv(router, 0);
        System.out.println("Dealer.really message is: " + convertFromMsg(msg));
        int sendFlag = ZMQ.send(router, dealer, ZMQ.ZMQ_SNDMORE);
        sendValidate(sendFlag);
        switch (dealer) {
          case "Go":
            sendFlag = ZMQ.send(router, "Success: 请调用Go插件", 0);
            sendValidate(sendFlag);
            break;
          case "Java":
            sendFlag = ZMQ.send(router, "Success: 请调用Java插件", 0);
            sendValidate(sendFlag);
            break;
          default:
        }
      }
    } finally {
      destroySocket(ctx, router);
    }
  }

  @Test
  public void javaDealer() throws InterruptedException {
    Ctx ctx = null;
    SocketBase dealer = null;
    try {
      ctx = ZMQ.init(1);
      dealer = ZMQ.socket(ctx, ZMQ.ZMQ_DEALER);
      ZMQ.setSocketOption(dealer, ZMQ.ZMQ_IDENTITY, "Java");
      ZMQ.connect(dealer, "tcp://127.0.0.1:12345");
      String helloStr = "Hello, I'm Java Dealer";
      for (int i = 0; i < 20; i++) {
        Thread.sleep(1000);
        int byteLength = ZMQ.send(dealer, helloStr, 0);
        Assert.assertEquals(byteLength, helloStr.length());
        Msg result = ZMQ.recv(dealer, 0);
        System.out.println("receive msg from platform: " + convertFromMsg(result));
      }
    } finally {
      destroySocket(ctx, dealer);
    }
  }

  @Test
  public void goDealer() throws InterruptedException {
    Ctx ctx = null;
    SocketBase dealer = null;
    try {
      ctx = ZMQ.init(1);
      dealer = ZMQ.socket(ctx, ZMQ.ZMQ_DEALER);
      ZMQ.setSocketOption(dealer, ZMQ.ZMQ_IDENTITY, "Go");
      ZMQ.connect(dealer, "tcp://127.0.0.1:12345");
      String helloStr = "Hello, I'm go Dealer";
      for (int i = 0; i < 20; i++) {
        Thread.sleep(1000);
        int byteLength = ZMQ.send(dealer, helloStr, 0);
        Assert.assertEquals(byteLength, helloStr.length());
        Msg result = ZMQ.recv(dealer, 0);
        System.out.println("receive msg from platform: " + convertFromMsg(result));
      }
    } finally {
      destroySocket(ctx, dealer);
    }
  }

  private String convertFromMsg(Msg msg) {
    return new String(msg.data(), StandardCharsets.UTF_8);
  }

  private void destroySocket(Ctx ctx, SocketBase router) {
    if (router != null) {
      ZMQ.close(router);
    }
    if (ctx != null) {
      ZMQ.term(ctx);
    }
  }

  private void sendValidate(int sendFlag) {
    if (sendFlag <= 0) {
      System.err.println("msg send fail");
    }
  }

}
