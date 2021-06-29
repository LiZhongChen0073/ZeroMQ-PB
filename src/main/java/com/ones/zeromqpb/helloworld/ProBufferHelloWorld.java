package com.ones.zeromqpb.helloworld;

import com.google.protobuf.InvalidProtocolBufferException;
import com.ones.zeromqpb.example.Person;

public class ProBufferHelloWorld {
  public static void main(final String[] args) throws InvalidProtocolBufferException {
    final Person.Builder personBuilder = Person.newBuilder();
    personBuilder.setEmail("548@qq.com");
    personBuilder.setId(1);
    personBuilder.setName("tom");

    final Person person = personBuilder.build();
    System.out.println(person);
    System.out.println("序列化后的toString");

    for (final byte b : person.toByteArray()) {
      System.out.print(b);
    }
    System.out.println();
    System.out.println("序列话后的byteString：\r\n" + person.toByteString());

    System.out.println();
    System.out.println("==二进制输出完毕==");

    System.out.println("反序列化对象开始");
    Person deserialization = null;
    try {
      deserialization = Person.parseFrom(person.toByteArray());
    } catch (final InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    assert deserialization != null;
    System.out.println(deserialization);
    deserialization.isInitialized();

    System.out.println("尝试从byteString反序列化");
    System.out.println(Person.parseFrom(person.toByteString()).toString());
  }
}
