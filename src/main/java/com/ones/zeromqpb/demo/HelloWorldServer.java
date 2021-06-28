package com.ones.zeromqpb.demo;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class HelloWorldServer {
  public static void main(final String[] args) throws Exception {
    try (final ZContext context = new ZContext()) {
      // Socket to talk to clients
      final ZMQ.Socket socket = context.createSocket(SocketType.ROUTER);
      socket.bind("tcp://127.0.0.1:5555");

      while (!Thread.currentThread().isInterrupted()) {
        // Block until a message is received
        final byte[] reply = socket.recv(0);

        // Print the message
        System.out.println(
            "Received: [" + new String(reply, ZMQ.CHARSET) + "]"
        );

        // Send a response
        final String response = "Hello, world!";
        socket.send(response.getBytes(ZMQ.CHARSET), 0);
      }
    }
  }
}
