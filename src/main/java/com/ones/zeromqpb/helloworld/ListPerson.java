package com.ones.zeromqpb.helloworld;

import com.ones.protocol.example.AddressBook;
import com.ones.protocol.example.Person;

import java.io.FileInputStream;

/**
 * 官网demo；从二进制文件读取并输出
 */
public class ListPerson {
  // Iterates though all people in the AddressBook and prints info about them.
  static void print(final AddressBook addressBook) {
    for (final Person person : addressBook.getPeopleList()) {
      System.out.println("Person ID: " + person.getId());
      System.out.println("  Name: " + person.getName());
      person.getEmail();
      System.out.println("  E-mail address: " + person.getEmail());

      for (final Person.PhoneNumber phoneNumber : person.getPhonesList()) {
        switch (phoneNumber.getType()) {
          case MOBILE:
            System.out.print("  Mobile phone #: ");
            break;
          case HOME:
            System.out.print("  Home phone #: ");
            break;
          case WORK:
            System.out.print("  Work phone #: ");
            break;
          default:
            System.out.print("  default phone #: ");
        }
        System.out.println(phoneNumber.getNumber());
      }
    }
  }

  // Main function:  Reads the entire address book from a file and prints all
  //   the information inside.
  public static void main(final String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage:  ListPeople ADDRESS_BOOK_FILE");
      System.exit(-1);
    }

    // Read the existing address book.
    final AddressBook addressBook =
        AddressBook.parseFrom(new FileInputStream(args[0]));

    print(addressBook);
  }
}

