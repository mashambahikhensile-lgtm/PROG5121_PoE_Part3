
package com.mycompany.chatapp.prog5121_poe_part1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages QuickChat messages, message arrays and JSON storage.
 */
public class MessageManager {

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();

    private final ObjectMapper mapper = new ObjectMapper();
    private final Path storageFile;

    /**
     * Uses messages.json in the application's working directory.
     */
    public MessageManager() {
        this(Path.of(System.getProperty("user.dir"))
                .toAbsolutePath()
                .normalize()
                .resolve("messages.json"));
    }

    /**
     * Allows a specific JSON file to be supplied, especially for unit tests.
     */
    public MessageManager(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException(
                    "The message storage file cannot be null.");
        }

        this.storageFile = storageFile.toAbsolutePath().normalize();

        System.out.println("Message storage file: " + this.storageFile);

        loadStoredMessages();
    }

    /**
     * Generates a random ten-digit message ID.
     */
    public static String generateMessageID() {
        return Long.toString(
                ThreadLocalRandom.current().nextLong(
                        1_000_000_000L, 10_000_000_000L));
    }

    /**
     * Accepts South African international and local cellphone formats.
     */
    public static boolean isValidRecipient(String recipient) {
        if (recipient == null) {
            return false;
        }

        return recipient.matches("^\\+27[6-8]\\d{8}$")
                || recipient.matches("^0[6-8]\\d{8}$");
    }

    /**
     * Checks that a message is not empty and does not exceed 250 characters.
     */
    public static boolean isValidMessage(String message) {
        return message != null
                && !message.trim().isEmpty()
                && message.length() <= 250;
    }

    /**
     * Adds a message to the appropriate array according to its action.
     */
    public String addMessage(Message message, String action) {

        if (message == null) {
            return "No message was supplied.";
        }

        if (!isValidRecipient(message.getRecipient())) {
            return "Invalid recipient number. Use +27 followed by nine digits, "
                    + "or a valid 10-digit local number.";
        }

        if (!isValidMessage(message.getMessage())) {
            if (message.getMessage() != null
                    && message.getMessage().length() > 250) {
                return "Message exceeds 250 characters. Please shorten it.";
            }

            return "Message cannot be empty.";
        }

        String status = action == null
                ? ""
                : action.trim().toLowerCase();

        switch (status) {

            case "sent":
                message.setFlag("Sent");
                sentMessages.add(message);
                return "Message successfully sent.";

            case "stored":
                message.setFlag("Stored");
                storedMessages.add(message);

                try {
                    saveStoredMessages();
                    return "Message successfully stored.";

                } catch (IOException exception) {
                    storedMessages.remove(message);

                    return "Message could not be stored: "
                            + exception.getMessage();
                }

            case "disregard":
            case "disregarded":
                message.setFlag("Disregard");
                disregardedMessages.add(message);
                return "Message disregarded.";

            default:
                return "Unknown message action. "
                        + "Choose Sent, Stored, or Disregard.";
        }
    }

    /**
     * Loads stored messages from the configured JSON file.
     */
    private void loadStoredMessages() {

        try {
            if (!Files.exists(storageFile)) {
                System.out.println(
                        "No existing messages.json file was found. "
                        + "A new file will be created when a message is stored.");

                return;
            }

            if (Files.size(storageFile) == 0) {
                System.out.println("The message storage file is empty.");
                return;
            }

            List<Message> loadedMessages = mapper.readValue(
                    storageFile.toFile(),
                    new TypeReference<List<Message>>() {
                    });

            if (loadedMessages != null) {
                storedMessages.addAll(loadedMessages);
            }

            System.out.println(
                    "Stored messages loaded successfully: "
                    + storedMessages.size());

        } catch (IOException exception) {
            System.err.println(
                    "ERROR: Could not load stored messages from "
                    + storageFile);

            System.err.println(exception.getMessage());
        }
    }

    /**
     * Saves all stored messages to the JSON file.
     */
    private void saveStoredMessages() throws IOException {

        Path parentDirectory = storageFile.getParent();

        if (parentDirectory != null) {
            Files.createDirectories(parentDirectory);
        }

        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(storageFile.toFile(), storedMessages);

        System.out.println(
                "Stored messages saved successfully: "
                + storedMessages.size());
    }

    /**
     * Returns the absolute location of the JSON storage file.
     */
    public Path getStorageFile() {
        return storageFile;
    }

    public Message[] getSentMessages() {
        return sentMessages.toArray(new Message[0]);
    }

    public Message[] getDisregardedMessages() {
        return disregardedMessages.toArray(new Message[0]);
    }

    public Message[] getStoredMessages() {
        return storedMessages.toArray(new Message[0]);
    }

    /**
     * Returns all message hashes from the three message arrays.
     */
    public String[] getMessageHashes() {

        List<String> values = new ArrayList<>();

        addHashes(values, sentMessages);
        addHashes(values, disregardedMessages);
        addHashes(values, storedMessages);

        return values.toArray(new String[0]);
    }

    /**
     * Returns all message IDs from the three message arrays.
     */
    public String[] getMessageIDs() {

        List<String> values = new ArrayList<>();

        addIDs(values, sentMessages);
        addIDs(values, disregardedMessages);
        addIDs(values, storedMessages);

        return values.toArray(new String[0]);
    }

    private void addHashes(
            List<String> result, List<Message> messages) {

        for (Message message : messages) {
            result.add(message.getMessageHash());
        }
    }

    private void addIDs(
            List<String> result, List<Message> messages) {

        for (Message message : messages) {
            result.add(message.getMessageID());
        }
    }

    /**
     * Returns the total number of sent messages.
     */
    public int returnTotalMessages() {
        return sentMessages.size();
    }

    public String displaySentMessages() {
        return display(sentMessages);
    }

    public String displayStoredMessages() {
        return display(storedMessages);
    }

    /**
     * Displays all messages, including sent, disregarded and stored messages.
     */
    public String displayReport() {

        List<Message> allMessages = new ArrayList<>();

        allMessages.addAll(sentMessages);
        allMessages.addAll(disregardedMessages);
        allMessages.addAll(storedMessages);

        return display(allMessages);
    }

    private String display(List<Message> messages) {

        if (messages.isEmpty()) {
            return "No messages found.";
        }

        StringBuilder result = new StringBuilder();

        for (Message message : messages) {

            if (result.length() > 0) {
                result.append(System.lineSeparator())
                        .append("------------------------------")
                        .append(System.lineSeparator());
            }

            result.append(message.details());
        }

        return result.toString();
    }

    /**
     * Finds and displays the longest stored message.
     */
    public String displayLongestStoredMessage() {

        Message longest = null;

        for (Message message : storedMessages) {

            if (longest == null
                    || message.getMessage().length()
                    > longest.getMessage().length()) {

                longest = message;
            }
        }

        if (longest == null) {
            return "No stored messages found.";
        }

        return "Longest stored message ("
                + longest.getMessage().length()
                + " characters): "
                + longest.getMessage();
    }

    /**
     * Searches all message arrays using a message ID.
     */
    public String searchByMessageID(String id) {

        Message found = findByID(id);

        if (found == null) {
            return "No message found with that ID.";
        }

        return "Recipient: " + found.getRecipient()
                + System.lineSeparator()
                + "Message: " + found.getMessage();
    }

    private Message findByID(String id) {

        for (Message message : allMessages()) {

            if (message.getMessageID().equals(id)) {
                return message;
            }
        }

        return null;
    }

    /**
     * Searches sent and stored messages for a particular recipient.
     */
    public String searchStoredByRecipient(String recipient) {

        StringBuilder result = new StringBuilder();

        List<Message> searchableMessages = new ArrayList<>();

        searchableMessages.addAll(sentMessages);
        searchableMessages.addAll(storedMessages);

        for (Message message : searchableMessages) {

            if (message.getRecipient().equals(recipient)) {

                if (result.length() > 0) {
                    result.append(System.lineSeparator());
                }

                result.append(message.getMessage());
            }
        }

        if (result.length() == 0) {
            return "No sent or stored messages found for that recipient.";
        }

        return result.toString();
    }

    /**
     * Deletes a stored message using its message hash.
     */
    public String deleteStoredMessageByHash(String hash) {

        for (int i = 0; i < storedMessages.size(); i++) {

            Message message = storedMessages.get(i);

            if (message.getMessageHash().equalsIgnoreCase(hash)) {

                Message removed = storedMessages.remove(i);

                try {
                    saveStoredMessages();

                    return "Message: \""
                            + removed.getMessage()
                            + "\" successfully deleted.";

                } catch (IOException exception) {

                    storedMessages.add(i, removed);

                    return "Message could not be deleted: "
                            + exception.getMessage();
                }
            }
        }

        return "No stored message found with that hash.";
    }

    /**
     * Combines all three arrays for searching and reporting.
     */
    private List<Message> allMessages() {

        List<Message> all = new ArrayList<>();

        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);

        return all;
    }
}
