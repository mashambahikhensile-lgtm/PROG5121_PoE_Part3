
package com.mycompany.chatapp.prog5121_poe_part1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages QuickChat messages using arrays and JSON storage.
 */
public class MessageManager {

    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];

    // Parallel lookup arrays: matching indexes represent the same message.
    private String[] messageIDs = new String[0];
    private String[] messageHashes = new String[0];
    private String[] messageRecipients = new String[0];
    private String[] messageTexts = new String[0];
    private String[] messageStatuses = new String[0];

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
     * Allows a specific JSON file to be supplied for unit tests.
     */
    public MessageManager(Path storageFile) {
        if (storageFile == null) {
            throw new IllegalArgumentException(
                    "The message storage file cannot be null.");
        }

        this.storageFile = storageFile.toAbsolutePath().normalize();

        System.out.println("Message storage file: " + this.storageFile);

        loadStoredMessages();
        rebuildParallelArrays();
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
                sentMessages = appendMessage(sentMessages, message);
                rebuildParallelArrays();
                return "Message successfully sent.";

            case "stored":
                message.setFlag("Stored");
                storedMessages = appendMessage(storedMessages, message);

                try {
                    saveStoredMessages();
                    rebuildParallelArrays();
                    return "Message successfully stored.";

                } catch (IOException exception) {
                    storedMessages = removeMessage(
                            storedMessages, storedMessages.length - 1);
                    rebuildParallelArrays();

                    return "Message could not be stored: "
                            + exception.getMessage();
                }

            case "disregard":
            case "disregarded":
                message.setFlag("Disregard");
                disregardedMessages = appendMessage(
                        disregardedMessages, message);
                rebuildParallelArrays();
                return "Message disregarded.";

            default:
                return "Unknown message action. "
                        + "Choose Sent, Stored, or Disregard.";
        }
    }

    /**
     * Adds a message to the end of an array.
     */
    private Message[] appendMessage(Message[] messages, Message message) {
        Message[] updated = Arrays.copyOf(
                messages, messages.length + 1);

        updated[updated.length - 1] = message;

        return updated;
    }

    /**
     * Removes a message at a specified array index.
     */
    private Message[] removeMessage(Message[] messages, int index) {
        if (index < 0 || index >= messages.length) {
            return messages;
        }

        Message[] updated = new Message[messages.length - 1];

        System.arraycopy(messages, 0, updated, 0, index);

        System.arraycopy(
                messages, index + 1,
                updated, index,
                messages.length - index - 1);

        return updated;
    }

    /**
     * Inserts a message back into an array at a specified index.
     */
    private Message[] insertMessage(
            Message[] messages, Message message, int index) {

        Message[] updated = new Message[messages.length + 1];

        System.arraycopy(messages, 0, updated, 0, index);

        updated[index] = message;

        System.arraycopy(
                messages, index,
                updated, index + 1,
                messages.length - index);

        return updated;
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

            Message[] loadedMessages = mapper.readValue(
                    storageFile.toFile(), Message[].class);

            if (loadedMessages != null) {
                storedMessages = loadedMessages;
            }
            rebuildParallelArrays();

            System.out.println(
                    "Stored messages loaded successfully: "
                    + storedMessages.length);

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
                + storedMessages.length);
    }

    /**
     * Returns the absolute location of the JSON storage file.
     */
    public Path getStorageFile() {
        return storageFile;
    }

    public Message[] getSentMessages() {
        return sentMessages.clone();
    }

    public Message[] getDisregardedMessages() {
        return disregardedMessages.clone();
    }

    public Message[] getStoredMessages() {
        return storedMessages.clone();
    }

    /**
     * Combines all messages into one array.
     */
    private Message[] allMessages() {
        Message[] all = new Message[
                sentMessages.length
                + disregardedMessages.length
                + storedMessages.length];

        int position = 0;

        System.arraycopy(
                sentMessages, 0, all, position, sentMessages.length);
        position += sentMessages.length;

        System.arraycopy(
                disregardedMessages, 0, all, position,
                disregardedMessages.length);
        position += disregardedMessages.length;

        System.arraycopy(
                storedMessages, 0, all, position, storedMessages.length);

        return all;
    }

    /**
     * Rebuilds parallel lookup arrays in the same order as allMessages().
     * The same index always refers to the same Message object.
     */
    private void rebuildParallelArrays() {
        Message[] messages = allMessages();

        messageIDs = new String[messages.length];
        messageHashes = new String[messages.length];
        messageRecipients = new String[messages.length];
        messageTexts = new String[messages.length];
        messageStatuses = new String[messages.length];

        for (int i = 0; i < messages.length; i++) {
            Message message = messages[i];
            messageIDs[i] = message.getMessageID();
            messageHashes[i] = message.getMessageHash();
            messageRecipients[i] = message.getRecipient();
            messageTexts[i] = message.getMessage();
            messageStatuses[i] = message.getFlag();
        }
    }

    /**
     * Returns a defensive copy of all message hashes.
     */
    public String[] getMessageHashes() {
        return messageHashes.clone();
    }

    /**
     * Returns a defensive copy of all message IDs.
     */
    public String[] getMessageIDs() {
        return messageIDs.clone();
    }

    /**
     * Returns the total number of sent messages.
     */
    public int returnTotalMessages() {
        return sentMessages.length;
    }

    public String displaySentMessages() {
        return display(sentMessages);
    }

    public String displayStoredMessages() {
        return display(storedMessages);
    }

    /**
     * Displays sent, disregarded and stored messages.
     */
    public String displayReport() {
        return display(allMessages());
    }

    /**
     * Formats an array of messages for display.
     */
    private String display(Message[] messages) {

        if (messages.length == 0) {
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
        if (id == null) {
            return "No message found with that ID.";
        }

        for (int i = 0; i < messageIDs.length; i++) {
            if (id.equals(messageIDs[i])) {
                return "Recipient: " + messageRecipients[i]
                        + System.lineSeparator()
                        + "Message: " + messageTexts[i];
            }
        }

        return "No message found with that ID.";
    }

    /**
     * Searches sent and stored messages for a particular recipient.
     */
    public String searchStoredByRecipient(String recipient) {
        StringBuilder result = new StringBuilder();

        if (recipient == null) {
            return "No sent or stored messages found for that recipient.";
        }

        // Search the parallel arrays by matching index.
        for (int i = 0; i < messageRecipients.length; i++) {
            if (recipient.equals(messageRecipients[i])
                    && ("Sent".equalsIgnoreCase(messageStatuses[i])
                    || "Stored".equalsIgnoreCase(messageStatuses[i]))) {
                if (result.length() > 0) {
                    result.append(System.lineSeparator());
                }
                result.append(messageTexts[i]);
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

        if (hash == null) {
            return "No stored message found with that hash.";
        }

        for (int i = 0; i < storedMessages.length; i++) {

            Message message = storedMessages[i];

            if (message.getMessageHash().equalsIgnoreCase(hash)) {

                storedMessages = removeMessage(storedMessages, i);
                rebuildParallelArrays();

                try {
                    saveStoredMessages();

                    return "Message: \""
                            + message.getMessage()
                            + "\" successfully deleted.";

                } catch (IOException exception) {

                    storedMessages = insertMessage(
                            storedMessages, message, i);
                    rebuildParallelArrays();

                    return "Message could not be deleted: "
                            + exception.getMessage();
                }
            }
        }

        return "No stored message found with that hash.";
    }
}
