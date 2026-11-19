package com.mycompany.chatapp.prog5121_poe_part1;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class MessageTest {
    @Test
    void messageDetailsDisplayInRequiredOrder() {
        Message message = new Message("1234567890", 0, "Mash", "+27834557896",
                "Did you get the cake?", "Sent");
        String details = message.details();
        assertTrue(details.indexOf("Message ID:") < details.indexOf("Message Hash:"));
        assertTrue(details.indexOf("Message Hash:") < details.indexOf("Recipient:"));
        assertTrue(details.indexOf("Recipient:") < details.indexOf("Message:"));
    }

    @Test
    void hashUsesFirstTwoIDCharactersAndMessageWords() {
        Message message = new Message("1234567890", 0, "Mash", "+27834557896",
                "Did you get the cake?", "Sent");
        assertEquals("12:0:DIDCAKE", message.getMessageHash());
    }

    @Test
    void hashHandlesPunctuationAndWhitespace() {
        Message message = new Message("9876543210", 2, "Mash", "+27834557896",
                "  Hello, this is QuickChat!  ", "Sent");
        assertEquals("98:2:HELLOQUICKCHAT", message.getMessageHash());
    }


    @Test
    void hashIgnoresTrailingPunctuationOnlyTokens() {
        Message message = new Message("8864309518", 3, "Mash", "+27838884567",
                "It is dinner time !", "Sent");
        assertEquals("88:3:ITTIME", message.getMessageHash());
    }

    @Test
    void messageIDHasTenNumericCharacters() {
        assertTrue(MessageManager.generateMessageID().matches("\\d{10}"));
    }

    @Test
    void messageLengthBoundaryIsCorrect() {
        assertTrue(MessageManager.isValidMessage("a".repeat(250)));
        assertFalse(MessageManager.isValidMessage("a".repeat(251)));
        assertFalse(MessageManager.isValidMessage(""));
    }

    @Test
    void messageConstructorRetainsAllFields() {
        Message message = new Message("1234567890", 4, "Mash", "0838884567",
                "It is dinner time !", "Sent");
        assertEquals("1234567890", message.getMessageID());
        assertEquals(4, message.getMessageNumber());
        assertEquals("Mash", message.getSender());
        assertEquals("0838884567", message.getRecipient());
        assertEquals("It is dinner time !", message.getMessage());
        assertEquals("Sent", message.getFlag());
    }
}
