package com.mycompany.chatapp.prog5121_poe_part1;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the Part 3 message arrays, JSON storage and stored-message features. */
class MessageManagerTest {
    @TempDir Path tempDir;

    private Message message(String id, int index, String recipient, String text, String flag) {
        return new Message(id, index, "Hikhensile Mashamba", recipient, text, flag);
    }

    private MessageManager manager() {
        return new MessageManager(tempDir.resolve("messages.json"));
    }

    private void addTaskData(MessageManager manager) {
        assertEquals("Message successfully sent.",
                manager.addMessage(message("1000000001", 0, "+27834557896",
                        "Did you get the cake?", "Pending"), "Sent"));
        assertEquals("Message successfully stored.",
                manager.addMessage(message("1000000002", 1, "+27838884567",
                        "Where are you? You are late! I have asked you to be on time.",
                        "Pending"), "Stored"));
        assertEquals("Message disregarded.",
                manager.addMessage(message("1000000003", 2, "+27834484567",
                        "Yohhoooo, I am at your gate.", "Pending"), "Disregard"));
        assertEquals("Message successfully sent.",
                manager.addMessage(message("0838884567", 3, "0838884567",
                        "It is dinner time !", "Pending"), "Sent"));
        assertEquals("Message successfully stored.",
                manager.addMessage(message("1000000005", 4, "+27838884567",
                        "Ok, I am leaving without you.", "Pending"), "Stored"));
    }

    @Test
    void allStatusArraysArePopulatedFromMessageActions() {
        MessageManager manager = manager();
        addTaskData(manager);
        assertEquals(2, manager.getSentMessages().length);
        assertEquals(1, manager.getDisregardedMessages().length);
        assertEquals(2, manager.getStoredMessages().length);
        assertEquals(5, manager.getMessageIDs().length);
        assertEquals(5, manager.getMessageHashes().length);
        assertEquals(2, manager.returnTotalMessages());
    }

    @Test
    void longestStoredMessageIsReturned() {
        MessageManager manager = manager();
        addTaskData(manager);
        assertTrue(manager.displayLongestStoredMessage().contains(
                "Where are you? You are late! I have asked you to be on time."));
    }

    @Test
    void messageIDSearchReturnsRecipientAndMessage() {
        MessageManager manager = manager();
        addTaskData(manager);
        String result = manager.searchByMessageID("0838884567");
        assertTrue(result.contains("0838884567"));
        assertTrue(result.contains("It is dinner time !"));
    }

    @Test
    void recipientSearchReturnsEveryStoredMessageForRecipient() {
        MessageManager manager = manager();
        addTaskData(manager);
        String result = manager.searchStoredByRecipient("+27838884567");
        assertTrue(result.contains("Where are you?"));
        assertTrue(result.contains("Ok, I am leaving without you."));
    }

    @Test
    void messageCanBeDeletedUsingItsHashAndJsonIsUpdated() throws Exception {
        MessageManager manager = manager();
        addTaskData(manager);
        String hash = manager.getStoredMessages()[0].getMessageHash();
        assertTrue(manager.deleteStoredMessageByHash(hash).contains("successfully deleted"));
        assertEquals(1, manager.getStoredMessages().length);
        assertEquals(1, new ObjectMapper().readTree(
                tempDir.resolve("messages.json").toFile()).size());
    }

    @Test
    void fullReportContainsEveryStatusAndRequiredFields() {
        MessageManager manager = manager();
        addTaskData(manager);
        String report = manager.displayReport();
        assertTrue(report.contains("Message ID:"));
        assertTrue(report.contains("Message Hash:"));
        assertTrue(report.contains("Sender:"));
        assertTrue(report.contains("Recipient:"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("Where are you?"));
        assertTrue(report.contains("Yohhoooo"));
    }

    @Test
    void storedMessagesAreLoadedFromJsonAfterRestart() {
        MessageManager firstRun = manager();
        addTaskData(firstRun);
        MessageManager secondRun = manager();
        assertEquals(2, secondRun.getStoredMessages().length);
        assertTrue(secondRun.displayStoredMessages().contains("Ok, I am leaving without you."));
    }

    @Test
    void jsonFileContainsStoredMessagesAsAJsonArray() throws Exception {
        MessageManager manager = manager();
        addTaskData(manager);
        Path file = tempDir.resolve("messages.json");
        assertTrue(Files.exists(file));
        assertTrue(new ObjectMapper().readTree(file.toFile()).isArray());
    }

    @Test
    void exact250CharacterMessageIsAcceptedAnd251IsRejected() {
        assertTrue(MessageManager.isValidMessage("x".repeat(250)));
        assertFalse(MessageManager.isValidMessage("x".repeat(251)));
    }

    @Test
    void invalidRecipientDoesNotEnterAnyArray() {
        MessageManager manager = manager();
        String result = manager.addMessage(
                message("1000000009", 0, "12345", "Hello there", "Pending"), "Sent");
        assertTrue(result.startsWith("Invalid recipient"));
        assertEquals(0, manager.getSentMessages().length);
    }

    @Test
    void southAfricanLocalAndInternationalNumbersAreAccepted() {
        assertTrue(MessageManager.isValidRecipient("+27834557896"));
        assertTrue(MessageManager.isValidRecipient("0838884567"));
        assertFalse(MessageManager.isValidRecipient("12345"));
    }

    @Test
    void generatedIDsAreTenDigits() {
        for (int i = 0; i < 100; i++) {
            String id = MessageManager.generateMessageID();
            assertTrue(id.matches("\\d{10}"));
        }
    }

    @Test
    void messageHashUsesIDPrefixZeroBasedIndexAndFirstAndLastWords() {
        Message message = message("1234567890", 0, "+27834557896",
                "Did you get the cake?", "Pending");
        assertEquals("12:0:DIDCAKE", message.getMessageHash());
    }

    @Test
    void searchForMissingMessageReturnsClearResult() {
        assertEquals("No message found with that ID.", manager().searchByMessageID("not-found"));
    }

    @Test
    void recipientSearchIncludesBothSentAndStoredMessages() {
        MessageManager manager = manager();
        manager.addMessage(message("1000000011", 0, "+27834557896", "Sent to this person", "Pending"), "Sent");
        manager.addMessage(message("1000000012", 1, "+27834557896", "Stored for this person", "Pending"), "Stored");
        String result = manager.searchStoredByRecipient("+27834557896");
        assertTrue(result.contains("Sent to this person"));
        assertTrue(result.contains("Stored for this person"));
    }

    @Test
    void storedSenderAndRecipientCanBeDisplayed() {
        MessageManager manager = manager();
        addTaskData(manager);
        Message stored = manager.getStoredMessages()[0];
        assertEquals("Hikhensile Mashamba", stored.getSender());
        assertEquals("+27838884567", stored.getRecipient());
    }

    @Test
    void deleteForUnknownHashDoesNotChangeStoredMessages() {
        MessageManager manager = manager();
        addTaskData(manager);
        assertEquals("No stored message found with that hash.",
                manager.deleteStoredMessageByHash("missing"));
        assertEquals(2, manager.getStoredMessages().length);
    }
}
