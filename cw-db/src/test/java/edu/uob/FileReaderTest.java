package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class FileReaderTest {
    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void testTokenizer(){
        String failedMessage = "Error in the tokenizer";
        String command = "INSERT INTO tablename VALUES('whatever', 1)";
        Tokenizer tokenizer = new Tokenizer();
        ArrayList<String> tokens = tokenizer.tokenize(command);
        assertEquals(tokens.get(0), "INSERT", failedMessage);
        assertEquals(tokens.get(1), "INTO", failedMessage);
        assertEquals(tokens.get(2), "tablename", failedMessage);
        assertEquals(tokens.get(3), "VALUES", failedMessage);
        assertEquals(tokens.get(4), "(", failedMessage);
        assertEquals(tokens.get(5), "'whatever'", failedMessage);
        assertEquals(tokens.get(6), ",", failedMessage);
        assertEquals(tokens.get(7), "1", failedMessage);
        assertEquals(tokens.get(8), ")", failedMessage);
    }
}
