// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: addressbook.proto

package com.ones.zeromqpb.example;

public interface AddressBookOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tutorial.AddressBook)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .tutorial.Person people = 1;</code>
   */
  java.util.List<com.ones.zeromqpb.example.Person>
  getPeopleList();

  /**
   * <code>repeated .tutorial.Person people = 1;</code>
   */
  com.ones.zeromqpb.example.Person getPeople(int index);

  /**
   * <code>repeated .tutorial.Person people = 1;</code>
   */
  int getPeopleCount();

  /**
   * <code>repeated .tutorial.Person people = 1;</code>
   */
  java.util.List<? extends com.ones.zeromqpb.example.PersonOrBuilder>
  getPeopleOrBuilderList();

  /**
   * <code>repeated .tutorial.Person people = 1;</code>
   */
  com.ones.zeromqpb.example.PersonOrBuilder getPeopleOrBuilder(
      int index);
}
