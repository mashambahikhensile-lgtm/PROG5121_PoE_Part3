package com.mycompany.chatapp.prog5121_poe_part1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Locale;

/** Stores the information and status of one QuickChat message. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    private String messageID;
    private String messageHash;
    private int messageNumber;
    private String sender;
    private String recipient;
    private String message;
    @JsonAlias("action")
    private String flag;

    public Message() { }

    public Message(String messageID, int messageNumber, String sender,
                   String recipient, String message, String flag) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.flag = flag;
        this.messageHash = createMessageHash(messageID, messageNumber, message);
    }

    /** Builds the required hash from the ID prefix, zero-based index and message words. */
    public static String createMessageHash(String id, int number, String text) {
        String prefix = id == null || id.length() < 2 ? "00" : id.substring(0, 2);
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) return (prefix + ":" + number + ":").toUpperCase(Locale.ROOT);
        String[] words = clean.split("\\s+");
        String first = "";
        String last = "";
        for (String word : words) {
            String lettersAndDigits = word.replaceAll("[^\\p{L}\\p{N}]", "");
            if (!lettersAndDigits.isEmpty()) {
                if (first.isEmpty()) {
                    first = lettersAndDigits;
                }
                last = lettersAndDigits;
            }
        }
        return (prefix + ":" + number + ":" + first + last).toUpperCase(Locale.ROOT);
    }

    public String getMessageID() { return messageID; }
    public void setMessageID(String messageID) { this.messageID = messageID; }
    public String getMessageHash() { return messageHash; }
    public void setMessageHash(String messageHash) { this.messageHash = messageHash; }
    public int getMessageNumber() { return messageNumber; }
    public void setMessageNumber(int messageNumber) { this.messageNumber = messageNumber; }
    public String getSender() { return sender == null ? "Unknown" : sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getFlag() { return flag; }
    public void setFlag(String flag) { this.flag = flag; }

    public String details() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Sender: " + sender + System.lineSeparator()
                + "Recipient: " + recipient + System.lineSeparator()
                + "Message: " + message + System.lineSeparator()
                + "Flag: " + flag;
    }
}
