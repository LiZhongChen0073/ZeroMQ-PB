package com.ones.zeromqpb.demo;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.Arrays;

public class HelloWorldClient {
  public static void main(final String[] args) {
    try (final ZContext context = new ZContext()) {
//      SocketBase dealer = ZMQ.socket(context, ZMQ.ZMQ_DEALER);
//      assertThat(dealer, notNullValue());
//      rc = ZMQ.connect(dealer, "tcp://localhost:" + port);
//      assertThat(rc, is(true));

      final ZMQ.Socket socket = context.createSocket(SocketType.DEALER);
      socket.connect("tcp://*:12345");
      
      while (!Thread.currentThread().isInterrupted()) {
        final String response = "Hello, world!";
//        socket.send(response.getBytes(ZMQ.CHARSET));
        final byte[] recv = socket.recv();
        System.out.println(Arrays.toString(recv));
      }
    }
  }
}
