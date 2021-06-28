package com.ones.zeromqpb.helloworld;

import com.ones.protocol.example.AddressBook;
import com.ones.protocol.example.Person;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 官网demo；写入文件
 * this function fills in a person messages based on user input.
 */
public class AddPerson {

  public static void main(final String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("Usage:  AddPerson ADDRESS_BOOK_FILE");
      System.exit(-1);
    }

    final AddressBook.Builder addressBook = AddressBook.newBuilder();

    // Read the existing address book.
    try {
      addressBook.mergeFrom(new FileInputStream(args[0]));
    } catch (final FileNotFoundException e) {
      System.out.println(args[0] + ": File not found.  Creating a new file.");
    }

    // Add an address.
    addressBook.addPeople(promptForAddress(new BufferedReader(new InputStreamReader(System.in))));

    // Write the new address book back to disk.
    final FileOutputStream output = new FileOutputStream(args[0]);
    addressBook.build().writeTo(output);
    output.close();
  }

  static Person promptForAddress(final BufferedReader stdin) throws IOException {
    final Person.Builder person = Person.newBuilder();
    System.out.print("Enter person ID: ");
    person.setId(Integer.parseInt(stdin.readLine()));

    System.out.println("Enter name: ");
    person.setName(stdin.readLine());

    System.out.print("Enter email address (blank for none): ");
    final String email = stdin.readLine();
    if (email.length() > 0) {
      person.setEmail(email);
    }

    while (true) {
      System.out.print("Enter a phone number (or leave blank to finish): ");
      final String number = stdin.readLine();
      if (number.length() == 0) {
        break;
      }

      final Person.PhoneNumber.Builder phoneNumber =
          Person.PhoneNumber.newBuilder().setNumber(number);

      System.out.print("Is this a mobile, home, or work phone? ");
      final String type = stdin.readLine();
      switch (type) {
        case "mobile":
          phoneNumber.setType(Person.PhoneType.MOBILE);
          break;
        case "home":
          phoneNumber.setType(Person.PhoneType.HOME);
          break;
        case "work":
          phoneNumber.setType(Person.PhoneType.WORK);
          break;
        default:
          System.out.println("Unknown phone type.  Using default.");
          break;
      }

      person.addPhones(phoneNumber);
    }

    return person.build();
  }
}