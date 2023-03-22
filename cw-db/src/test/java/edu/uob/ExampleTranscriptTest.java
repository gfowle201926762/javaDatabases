package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class ExampleTranscriptTest {

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
    public void testExampleTranscript() {
        String response = sendCommandToServer("CREATE DATABASE markbook;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("USE markbook;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("2\tDave\t55\tTRUE\n"));
        assertTrue(response.contains("3\tBob\t35\tFALSE\n"));
        assertTrue(response.contains("4\tClive\t20\tFALSE\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM marks WHERE name != 'Dave';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("3\tBob\t35\tFALSE\n"));
        assertTrue(response.contains("4\tClive\t20\tFALSE\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("2\tDave\t55\tTRUE\n"));
        assertEquals(4, response.split("\n").length);



        response = sendCommandToServer("CREATE TABLE coursework (task, submission);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM coursework;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\ttask\tsubmission\n"));
        assertTrue(response.contains("1\tOXO\t3\n"));
        assertTrue(response.contains("2\tDB\t1\n"));
        assertTrue(response.contains("3\tOXO\t4\n"));
        assertTrue(response.contains("4\tSTAG\t2\n"));
        assertEquals(6, response.split("\n").length);


        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tcoursework.task\tmarks.name\tmarks.mark\tmarks.pass\n"));
        assertTrue(response.contains("1\tOXO\tBob\t35\tFALSE\n"));
        assertTrue(response.contains("2\tDB\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("3\tOXO\tClive\t20\tFALSE\n"));
        assertTrue(response.contains("4\tSTAG\tDave\t55\tTRUE\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Clive';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("4\tClive\t38\tFALSE\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM marks WHERE name == 'Dave';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("3\tBob\t35\tFALSE\n"));
        assertTrue(response.contains("4\tClive\t38\tFALSE\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("4\tClive\t38\tFALSE\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 've';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertTrue(response.contains("4\tClive\t38\tFALSE\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT id FROM marks WHERE pass == FALSE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\n"));
        assertTrue(response.contains("3\n"));
        assertTrue(response.contains("4\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT name FROM marks WHERE mark>60;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("Steve\n"));
        assertEquals(3, response.split("\n").length);


        response = sendCommandToServer("DELETE FROM marks WHERE mark<40;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tmark\tpass\n"));
        assertTrue(response.contains("1\tSteve\t65\tTRUE\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM marks");
        assertEquals("[ERROR] Expected \";\" to terminate the query. Found \"marks\" instead.", response);

        response = sendCommandToServer("SELECT * FROM crew;");
        assertEquals("[ERROR] The table \"crew\" does not exist.", response);

        response = sendCommandToServer("SELECT * FROM marks pass == TRUE;");
        assertEquals("[ERROR] Expected \"WHERE\" keyword or \";\" to terminate the query after [TableName] \"marks\".", response);

        response = sendCommandToServer("DROP DATABASE markbook;");
        assertEquals("[OK]", response);
    }
}
