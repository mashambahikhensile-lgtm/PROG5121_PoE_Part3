package com.mycompany.chatapp.prog5121_poe_part1;

import java.util.Scanner;

/** Console entry point for QuickChat. */
public class PROG5121_PoE_Part1 {
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to QuickChat registration.");
        System.out.print("Enter your first name: ");
        String firstName = INPUT.nextLine().trim();
        System.out.print("Enter your last name: ");
        String lastName = INPUT.nextLine().trim();
        System.out.print("Enter your username: ");
        String username = INPUT.nextLine().trim();
        System.out.print("Enter your password: ");
        String password = INPUT.nextLine();
        System.out.print("Enter your cell phone number: ");
        String phone = INPUT.nextLine().trim();

        Login login = new Login(firstName, lastName, username, password, phone);
        String registration = login.registerUser();
        System.out.println(registration);
        if (!"User registered successfully.".equals(registration)) {
            System.out.println("Registration was not completed. Please restart and correct the details.");
            return;
        }

        System.out.print("Enter your username to login: ");
        String loginName = INPUT.nextLine().trim();
        System.out.print("Enter your password to login: ");
        String loginPassword = INPUT.nextLine();
        boolean loggedIn = login.loginUser(loginName, loginPassword);
        System.out.println(login.returnLoginStatus(loggedIn));
        if (!loggedIn) return;

        System.out.println("Welcome to QuickChat.");
        int messageLimit = readMessageLimit();
        if (messageLimit < 0) return;

        MessageManager manager = new MessageManager();
        runMainMenu(manager, firstName + " " + lastName, messageLimit);
    }

    private static int readMessageLimit() {
        while (true) {
            System.out.print("How many messages would you like to process? ");
            String input = INPUT.nextLine().trim();
            try {
                int count = Integer.parseInt(input);
                if (count >= 0) return count;
            } catch (NumberFormatException ignored) {
                // Ask again rather than ending the application on a typing mistake.
            }
            System.out.println("Please enter a whole number that is zero or greater.");
        }
    }

    private static void runMainMenu(MessageManager manager, String sender, int messageLimit) {
        int processedMessages = 0;
        boolean quit = false;
        while (!quit) {
            System.out.println("\n1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");
            String option = INPUT.nextLine().trim();

            switch (option) {
                case "1":
                    if (processedMessages >= messageLimit) {
                        System.out.println("You have reached the number of messages selected.");
                        break;
                    }
                    for (int index = processedMessages; processedMessages < messageLimit;
                            index = processedMessages) {
                        Message message = captureMessage(sender, index);
                        if (message == null) continue;
                        System.out.println("Message ID generated: " + message.getMessageID());
                        System.out.println("Message Hash: " + message.getMessageHash());
                        System.out.println("Choose an action: 1) Send  0) Disregard  2) Store");
                        String choice = INPUT.nextLine().trim();
                        String result;
                        if ("1".equals(choice)) result = manager.addMessage(message, "Sent");
                        else if ("0".equals(choice)) result = manager.addMessage(message, "Disregard");
                        else if ("2".equals(choice)) result = manager.addMessage(message, "Stored");
                        else {
                            System.out.println("Invalid action. The message was not counted; try again.");
                            continue;
                        }
                        System.out.println(result);
                        if (result.startsWith("Invalid recipient") || result.startsWith("Message exceeds")
                                || result.startsWith("Message cannot")) {
                            System.out.println("This message was not counted. Please enter it again.");
                            continue;
                        }
                        if ("Sent".equals(message.getFlag())) System.out.println(message.details());
                        processedMessages++;
                    }
                    System.out.println("Total messages sent: " + manager.returnTotalMessages());
                    break;
                case "2":
                    System.out.println(manager.displaySentMessages());
                    break;
                case "3":
                    quit = true;
                    System.out.println("Goodbye.");
                    break;
                case "4":
                    runStoredMessagesMenu(manager);
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2, 3, or 4.");
            }
        }
    }

    private static Message captureMessage(String sender, int zeroBasedIndex) {
        System.out.println("\nMessage " + (zeroBasedIndex + 1));
        System.out.print("Enter recipient cell number: ");
        String recipient = INPUT.nextLine().trim();
        System.out.print("Enter message (maximum 250 characters): ");
        String text = INPUT.nextLine();
        if (text.length() > 250) {
            System.out.println("Message exceeds 250 characters by " + (text.length() - 250)
                    + "; please reduce the size.");
            return null;
        }
        if (text.trim().isEmpty()) {
            System.out.println("Message cannot be empty.");
            return null;
        }
        if (!MessageManager.isValidRecipient(recipient)) {
            System.out.println("Cell phone number is incorrectly formatted. Please correct it and try again.");
            return null;
        }
        String id = MessageManager.generateMessageID();
        Message message = new Message(id, zeroBasedIndex, sender, recipient, text, "Pending");
        return message;
    }

    private static void runStoredMessagesMenu(MessageManager manager) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display sender and recipient of stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search stored messages by recipient");
            System.out.println("5) Delete a stored message using its hash");
            System.out.println("6) Display full message report");
            System.out.println("7) Back to main menu");
            System.out.print("Choose an option: ");
            String choice = INPUT.nextLine().trim();
            switch (choice) {
                case "1":
                    Message[] stored = manager.getStoredMessages();
                    if (stored.length == 0) System.out.println("No stored messages found.");
                    for (Message message : stored) {
                        System.out.println("Sender: " + message.getSender()
                                + " | Recipient: " + message.getRecipient());
                    }
                    break;
                case "2":
                    System.out.println(manager.displayLongestStoredMessage());
                    break;
                case "3":
                    System.out.print("Enter message ID: ");
                    System.out.println(manager.searchByMessageID(INPUT.nextLine().trim()));
                    break;
                case "4":
                    System.out.print("Enter recipient number: ");
                    System.out.println(manager.searchStoredByRecipient(INPUT.nextLine().trim()));
                    break;
                case "5":
                    System.out.print("Enter message hash: ");
                    System.out.println(manager.deleteStoredMessageByHash(INPUT.nextLine().trim()));
                    break;
                case "6":
                    System.out.println(manager.displayReport());
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option. Choose 1 to 7.");
            }
        }
    }
}
