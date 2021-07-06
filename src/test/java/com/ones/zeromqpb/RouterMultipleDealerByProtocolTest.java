package com.ones.zeromqpb;

import com.ones.openplatform.protocol.Common;
import com.ones.openplatform.protocol.Control;
import com.ones.openplatform.protocol.Message;
import com.ones.openplatform.protocol.Plugin;
import org.junit.Assert;
import org.junit.Test;
import zmq.Ctx;
import zmq.Msg;
import zmq.SocketBase;
import zmq.ZMQ;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 模仿go/java两个host与开放平台的通讯过程
 * 两个host与开发平台通过携带ZMQ_IDENTITY，各聊各的，互不牵扯
 * 使用ZQM的api
 */
public class RouterMultipleDealerByProtocolTest {

  @Test
  public void platFormRouter() {
    Message.PlatformMessage platformMessage = getPlatformMessage();

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
            sendFlag = ZMQ.send(router, platformMessage.toByteArray(), 0);
            sendValidate(sendFlag);
            break;
          default:
        }
      }
    } finally {
      destroySocket(ctx, router);
    }
  }

  private Message.PlatformMessage getPlatformMessage() {
    Common.RouterNode.Builder distinctRouterBuilder = Common.RouterNode.newBuilder();
    distinctRouterBuilder.setID("R000001");
    distinctRouterBuilder.putTags("1","1");
    Common.RouterNode.Builder sourceRouterBuilder = Common.RouterNode.newBuilder();
    sourceRouterBuilder.setID("H000001");
    Common.RouterMessage.Builder routerMessageBuilder = Common.RouterMessage.newBuilder();
    routerMessageBuilder.setDistinct(distinctRouterBuilder.build());
    routerMessageBuilder.setSource(sourceRouterBuilder.build());
    Common.HttpRequestMessage.Builder httpRequestMessageBuilder = Common.HttpRequestMessage.newBuilder();
    httpRequestMessageBuilder.setMethod("GET");
    httpRequestMessageBuilder.setUrl("https://www.baidu.com");
    httpRequestMessageBuilder.putHeaders("wd", Common.HeaderVal.newBuilder().addVal("你好").build());
    Common.HttpContextMessage.Builder httpContextMessageBuilder = Common.HttpContextMessage.newBuilder();
    httpContextMessageBuilder.setRequest(httpRequestMessageBuilder.build());
    Plugin.PluginMessage.Builder pluginMessageBuilder = Plugin.PluginMessage.newBuilder();
    pluginMessageBuilder.setHttp(httpContextMessageBuilder);
    Message.PlatformMessage.Builder platformMessageBuilder = Message.PlatformMessage.newBuilder();
    platformMessageBuilder.setHeader(routerMessageBuilder.build());
    platformMessageBuilder.setControl(Control.ControlMessage.newBuilder().setHeartbeat(1000).build());
    platformMessageBuilder.setPlugin(pluginMessageBuilder.build());
    return platformMessageBuilder.build();
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
        Message.PlatformMessage platformMessage = Message.PlatformMessage.parseFrom(result.data());
        Common.HttpRequestMessage request = platformMessage.getPlugin().getHttp().getRequest();
        String url = request.getUrl();
        String method = request.getMethod();
        System.out.println(url);
        System.out.println(method);
        HttpResponse<String> httpResponse = httpRequest(url);
        System.out.println(httpResponse.body());
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      destroySocket(ctx, dealer);
    }
  }

  private HttpResponse<String> httpRequest(String url) throws IOException, InterruptedException {
    HttpClient client = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(6))
        .build();
    HttpRequest request = HttpRequest.newBuilder()
        .header("Content-Type", "application/json")
        .version(HttpClient.Version.HTTP_2)
        .uri(URI.create(url))
        .timeout(Duration.ofMillis(5009))
        .GET()
        .build();
    return client.send(request, HttpResponse.BodyHandlers.ofString());
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
      for (int i = 0; i < 200; i++) {
        Thread.sleep(1000);
        int byteLength = ZMQ.send(dealer, i + helloStr, 0);
        Assert.assertEquals(byteLength, (i + helloStr).length());
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
