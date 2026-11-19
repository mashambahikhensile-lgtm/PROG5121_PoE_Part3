# PROG5121 PoE — Part 3: Store Data and Display Task Report

## Project Overview

This project is the final extension of the QuickChat console application developed for Programming 1A (PROG5121). It is built using Java 17, Maven and JUnit 5.

Part 3 extends the previous application by introducing message collections, JSON persistence, stored-message management, reporting and automated unit testing.

## Features

- User registration and login validation.
- QuickChat console menu and message processing.
- Ten-digit message ID generation.
- Message hash generation using the message ID, zero-based message number and message words.
- Sent, disregarded and stored message collections.
- JSON persistence of stored messages using Jackson.
- Loading previously stored messages when the application starts.
- Displaying the sender and recipient of stored messages.
- Finding the longest stored message.
- Searching for a message using its ID.
- Searching sent and stored messages by recipient.
- Deleting stored messages using their message hash.
- Generating a full message report containing message details.

## Technologies Used

- Java 17
- Apache Maven
- JUnit 5
- Jackson Databind 2.17.2
- NetBeans IDE
- Git and GitHub
- GitHub Actions

## Running the Application

1. Open the project in NetBeans as a Maven project.
2. Ensure that Java 17 is configured.
3. Run `PROG5121_PoE_Part1.java`.
4. Follow the console prompts to register and log in.
5. Use the main menu to send messages or access Stored Messages.

Stored messages are saved in `messages.json` in the application's working directory.

## Running the Unit Tests

Run the following Maven command:

```bash
mvn clean test
```

Alternatively, use the Clean and Build or Test functionality in NetBeans.

The project includes automated tests for registration, login, message processing, message hashes, message collections, JSON persistence, searching, deletion and reporting.

## Automated Testing

GitHub Actions is configured to run the Maven unit tests using Java 17 whenever changes are pushed or pull requests are created.

## References

- The Independent Institute of Education. (2026). *Programming 1A (PROG5121): Part 3 — Store Data and Display Task Report*. Prescribed assessment material.
- FasterXML. (2024). *Jackson Databind*. https://github.com/FasterXML/jackson-databind
- JUnit Team. (2025). *JUnit 5 User Guide*. https://junit.org/junit5/docs/current/user-guide/
