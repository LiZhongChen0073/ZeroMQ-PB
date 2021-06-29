package com.ones.zeromqpb;

import org.junit.Test;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class RouterTest {

  @Test
  public void testRouterHandover() throws Exception {
    int rc;
    boolean brc;

    final Ctx ctx = ZMQ.init(1);

    final SocketBase router = ZMQ.socket(ctx, ZMQ.ZMQ_ROUTER);
    brc = ZMQ.bind(router, "tcp://127.0.0.1:*");

    // Enable the handover flag
    ZMQ.setSocketOption(router, ZMQ.ZMQ_ROUTER_HANDOVER, 1);

    // Create dealer called "X" and connect it to our router
    final SocketBase dealerOne = ZMQ.socket(ctx, ZMQ.ZMQ_DEALER);

    ZMQ.setSocketOption(dealerOne, ZMQ.ZMQ_IDENTITY, "X");

    final String host = (String) ZMQ.getSocketOptionExt(router, ZMQ.ZMQ_LAST_ENDPOINT);

    brc = ZMQ.connect(dealerOne, host);

    // Get message from dealer to know when connection is ready
    rc = ZMQ.send(dealerOne, "Hello", 0);

    Msg msg = ZMQ.recv(router, 0);

    msg = ZMQ.recv(router, 0);

    // Now create a second dealer that uses the same identity

    msg = ZMQ.recv(router, 0);
    msg = ZMQ.recv(router, 0);
    // Send a message to 'X' identity. This should be delivered
    // to the second dealer, instead of the first because of the handover.
    rc = ZMQ.send(router, "X", ZMQ.ZMQ_SNDMORE);
    rc = ZMQ.send(router, "Hello", 0);

    // Ensure that the first dealer doesn't receive the message
    // but the second one does
    msg = ZMQ.recv(dealerOne, ZMQ.ZMQ_DONTWAIT);
    //  Clean up.
    ZMQ.close(router);
    ZMQ.close(dealerOne);
    ZMQ.term(ctx);
  }
}