package com.ones.zeromqpb;

import com.ones.openplatform.protocol.Common;
import com.ones.openplatform.protocol.Control;
import com.ones.openplatform.protocol.Message;
import org.junit.Test;
import org.zeromq.SocketType;
import org.zeromq.ZSocket;

import java.util.Arrays;

/**
 * 与开放平台通信
 */
public class CommWithPlatFormTest {

  @Test
  public void javaHost() {
    Common.RouterNode.Builder distinctRouterBuilder = Common.RouterNode.newBuilder();
    distinctRouterBuilder.setID("R000001");
    Common.RouterNode.Builder sourceRouterBuilder = Common.RouterNode.newBuilder();
    sourceRouterBuilder.setID("H000001");
    Common.RouterMessage.Builder routerMessageBuilder = Common.RouterMessage.newBuilder();
    routerMessageBuilder.setDistinct(distinctRouterBuilder.build());
    routerMessageBuilder.setSource(sourceRouterBuilder.build());

    Message.PlatformMessage.Builder platformMessageBuilder = Message.PlatformMessage.newBuilder();
    platformMessageBuilder.setHeader(routerMessageBuilder.build());
    platformMessageBuilder.setControl(Control.ControlMessage.newBuilder().setHeartbeat(1000).build());
    System.out.println(platformMessageBuilder);
    System.out.println("构建消息体完成");
    try (ZSocket socket = new ZSocket(SocketType.DEALER)) {
      for (int i = 0; i < 5; i++) {
        socket.connect("tcp://*:9009");
        socket.send(platformMessageBuilder.build().toByteArray(), 1);
        byte[] receive = socket.receive(1);
        System.out.println(Arrays.toString(receive));
      }
    }
  }

  @Test
  public void platform() {
    try (ZSocket socket = new ZSocket(SocketType.ROUTER)) {
      while (!Thread.currentThread().isInterrupted()) {
        socket.connect("tcp://*:9009");
//        socket.send(platformMessageBuilder.build().toByteArray(), 1);
        Thread.sleep(2000);
        byte[] receive = socket.receive(1);
        while (receive != null && receive.length > 0) {
          socket.send(receive, 1);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
