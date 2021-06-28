package com.ones.zeromqpb;

import com.ones.openplatform.protocol.Common;
import com.ones.openplatform.protocol.Message;
import org.junit.Test;
import org.zeromq.SocketType;
import org.zeromq.ZSocket;

/**
 * 与开放平台通信
 */
public class CommWithPlatFormTest {

  @Test
  public void sayHello() {
    Common.RouterNode.Builder routerBuilder = Common.RouterNode.newBuilder();
    routerBuilder.setID("H0000001");
    Common.RouterMessage.Builder routerMessageBuilder = Common.RouterMessage.newBuilder();
    routerMessageBuilder.setDistinct(routerBuilder.build());
    Message.PlatformMessage.Builder platformMessageBuilder = Message.PlatformMessage.newBuilder();
    platformMessageBuilder.setHeader(routerMessageBuilder.build());
    System.out.println(platformMessageBuilder.toString());

    try(ZSocket socket = new ZSocket(SocketType.DEALER);){
      socket.connect("tcp://127.0.0.1:12345");
      socket.send(platformMessageBuilder.build().toByteArray(),1);
    }

  }
}
