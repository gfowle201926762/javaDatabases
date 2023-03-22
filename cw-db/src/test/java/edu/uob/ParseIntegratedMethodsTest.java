package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ParseIntegratedMethodsTest {
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

    private Parser setParser(String input) {
        Tokenizer tokenizer = new Tokenizer();
        ArrayList<String> tokens = tokenizer.tokenize(input);
        FileReading fileReading = new FileReading(Paths.get("databases").toAbsolutePath().toString());
        Parser parser = new Parser(tokens, fileReading);
        return parser;
    }

    @Test
    public void testIsUse1(){
        String input = "USE tablename;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUse());
    }

    @Test
    public void testIsUse2(){
        String input = "USE 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUse());
    }

    @Test
    public void testIsUse3(){
        String input = "USE 1!;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUse());
    }

    @Test
    public void testIsUse4(){
        String input = "USE ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUse());
    }

    @Test
    public void testIsUse5(){
        String input = "validdatabasename;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUse());
    }

    @Test
    public void testIsCreate1() {
        String input = "CREATE DATABASE databaseName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCreate());
    }

    @Test
    public void testIsCreate2() {
        String input = "CREATE TABLE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCreate());
    }

    @Test
    public void testIsCreate3() {
        String input = "CREATE TABLE tableName (plaintext);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCreate());
    }

    @Test
    public void testIsCreateFail1() {
        String input = "CREATE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCreate());
    }

    @Test
    public void testIsCreateFail2() {
        String input = "TABLE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCreate());
    }

    @Test
    public void testIsCreateFail3() {
        String input = "CREATE TABLE ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCreate());
    }

    @Test
    public void testIsDrop1() {
        String input = "DROP TABLE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDrop());
    }

    @Test
    public void testIsDrop2() {
        String input = "DROP DATABASE databaseName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDrop());
    }

    @Test
    public void testIsDropFail1() {
        String input = "DROP databaseName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDrop());
    }

    @Test
    public void testIsDropFail2() {
        String input = "DROP TABLE ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDrop());
    }

    @Test
    public void testIsDropFail3() {
        String input = "DROP DATABASE;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDrop());
    }

    @Test
    public void testIsAlter1() {
        String input = "ALTER TABLE tableName ADD plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isAlter());
    }

    @Test
    public void testIsAlter2() {
        String input = "ALTER TABLE tableName DROP plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isAlter());
    }

    @Test
    public void testIsAlterFail1() {
        String input = "ALTER TABLE tableName GARBAGE plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isAlter());
    }

    @Test
    public void testIsAlterFail2() {
        String input = "ALTER TABLE tableName ADD notPla!nText;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isAlter());
    }

    @Test
    public void testIsAlterFail3() {
        String input = "GARBAGE TABLE tableName ADD plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isAlter());
    }

    @Test
    public void testIsAlterFail4() {
        String input = "ALTER DATABASE tableName ADD plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isAlter());
    }

    @Test
    public void testIsAlterFail5() {
        String input = "ALTER TABLE notPla!nText ADD plaintext;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isAlter());
    }

    @Test
    public void testIsInsert1() {
        String input = "INSERT INTO tableName VALUES(1, 2, 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isInsert());
    }

    @Test
    public void testIsInsert2() {
        String input = "INSERT INTO tableName123 VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isInsert());
    }

    @Test
    public void testIsInsertFail1() {
        String input = "INSER INTO tableName123 VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail2() {
        String input = "INSERTT INTO tableName123 VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail3() {
        String input = "INSERT INT tableName123 VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail4() {
        String input = "INSERT INTOO tableName123 VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail5() {
        String input = "INSERT INTO notPla!n VALUES(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail6() {
        String input = "INSERT INTO tableName123 VALUESS(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail7() {
        String input = "INSERT INTO tableName123 VALUE(1, 'L!tera:;y aN%$NG', 3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail8() {
        String input = "INSERT INTO tableName123 VALUES(1, 'L!tera:;y aN%$NG', notinquotes);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail9() {
        String input = "INSERT INTO tableName123 VALUES 1, 'L!tera:;y aN%$NG', 2);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsInsertFail10() {
        String input = "INSERT INTO tableName123 VALUES (1, 'L!tera:;y aN%$NG', 2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isInsert());
    }

    @Test
    public void testIsSelect1() {
        String input = "SELECT * FROM tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isSelect());
    }

    @Test
    public void testIsSelect2() {
        String input = "SELECT attr1 FROM tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isSelect());
    }

    @Test void testIsSelect3() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE (attr1 LIKE 'beans' AND attr2 == 5) OR attr8 != 16;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isSelect());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test void testIsSelect4() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isSelect());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsSelect5() {
        String input = "SELECT * FROM tableName WHERE attr1 LIKE 'hello';";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isSelect());
    }

    @Test
    public void testIsSelectFail1() {
        String input = "SELECTT attr1 FROM tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isSelect());
    }

    @Test
    public void testIsSelectFail2() {
        String input = "SELECT attr! FROM tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isSelect());
    }

    @Test
    public void testIsSelectFail3() {
        String input = "SELECT attr GARBAGE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isSelect());
    }

    @Test
    public void testIsUpdate1() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE attr1 == 5;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdate2() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE (attr1 == 5 OR attr5 != 30);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdate3() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE (attr1 == 5 OR (attr5 != 30 AND attr2 == 1));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdate4() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE ((attr1 == 5 OR attr5 != 30) AND attr2 == 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdate5() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE (attr1 == 5 OR attr5 != 30 OR attr2 == 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdate6() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE (((attr1 == 5 OR attr5 != 30) AND attr2 == 1) OR attr8 LIKE 'beans');";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isUpdate());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

    @Test
    public void testIsUpdateFail1() {
        String input = "UPDATE tableName;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUpdate());
    }

    @Test
    public void testIsUpdateFail2() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE attr1 (== 5 OR attr5 != 30) AND attr2 == 1) OR attr8 LIKE 'beans');";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUpdate());
    }

    @Test
    public void testIsUpdateFail3() {
        String input = "UPDATE tableName SET attr1 = 1 WHERE (((attr1 == 5 OR attr5 != 30) AND attr2 == 1 OR attr8 LIKE 'beans';";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isUpdate());
    }

    @Test
    public void testIsDelete1() {
        String input = "DELETE FROM tableName WHERE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDelete());
    }

    @Test
    public void testIsDelete2() {
        String input = "DELETE FROM tableName WHERE attr1 LIKE 'hello';";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDelete());
    }

    @Test
    public void testIsDelete3() {
        String input = "DELETE FROM tableName WHERE attr1 >= 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDelete());
    }

    @Test
    public void testIsDelete4() {
        String input = "DELETE FROM tableName WHERE (attr1 >= 1 AND attr1 < 5);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDelete());
    }

    @Test
    public void testIsDelete5() {
        String input = "DELETE FROM tableName WHERE (attr1 >= 1 OR attr1<5);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail1() {
        String input = "DELETE FROM tableName WHERE attr1 => 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail2() {
        String input = "GARBAGE FROM tableName WHERE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail3() {
        String input = "DELETE GARBAGE tableName WHERE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail4() {
        String input = "DELETE FROM !!! WHERE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail5() {
        String input = "DELETE FROM tableName GARBAGE attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail6() {
        String input = "DELETE FROM tableName WHERE !!! == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    public void testIsDeleteFail7() {
        String input = "DELETE FROM tableName WHERE (attr1 >= 1 GARBAGE attr1 < 5);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isDelete());
    }

    @Test
    void testIsCommand1() {
        String input = "USE Database1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand2() {
        String input = "SELECT * FROM table1 WHERE (attr1 LIKE 'beans' AND attr2==5);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand3() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE (attr1 LIKE 'beans' AND attr2 == 5);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand4() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE (attr1 LIKE 'beans' AND attr2 == 5) OR attr8!=16;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand5() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE (attr1 LIKE 'beans' AND attr2 == 5) OR (attr8!=16);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand6() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 LIKE 'beans' AND attr2 == 5 OR (attr8 != 16);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand7() {
        String input = "UPDATE tableName1 SET attr1='beans' WHERE attr2 == 6;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand8() {
        String input = "JOIN tableName1 AND tableName2 ON attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand9() {
        String input = "UPDATE tableName1 SET attr1  =  'beans' WHERE attr2 ==   6 AND attr1 == 1 OR (attr == 9) OR attr == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand10() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE attr2 == 6 AND (attr1 == 1 OR (attr == 9)) OR attr == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand11() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1 OR (attr == 9))) OR attr == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand12() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR attr == 9) OR attr == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand13() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR (attr == 9));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
       // assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand14() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR (attr == 9) AND attr == 1 OR attr == 8);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand15() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ((attr2 == 6) OR attr == 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand16() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1 OR attr == 9) OR attr == 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand17() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR (attr == 9) OR (attr == 1));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand18() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR (attr == 9) AND attr == 1 OR attr == 8 AND (attr == 15));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand19() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND (attr1 == 1) OR ((attr == 9) AND attr == 1) OR attr == 8 AND (attr == 15));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand20() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND ((attr1 == 1) OR (attr == 9)) AND attr == 1 OR attr == 8 AND (attr == 15));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand21() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr2 == 6 AND ((attr1 == 1) OR ((attr == 9) AND attr == 1)) OR attr == 8 AND (attr == 15));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand22() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE attr == 1 AND attr == 2 OR attr == 3 AND (attr == 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand23() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr == 1 AND ((attr == 2) OR attr == 3)) AND (attr == 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand231() {
        String input = "UPDATE tableName1 SET attr1 = 'beans', attr2 = 'sausage' WHERE (attr == 1 AND ((attr == 2) OR attr == 3)) AND (attr == 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand24() {
        String input = "CREATE DATABASE database1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand25() {
        String input = "CREATE TABLE table1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand26() {
        String input = "CREATE TABLE table1 (attr1, attr2, attr3);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand27() {
        String input = "CREATE TABLE table1 (attr1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand28() {
        String input = "DROP DATABASE database1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand29() {
        String input = "DROP TABLE table1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand30() {
        String input = "ALTER TABLE table1 ADD attr;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand31() {
        String input = "ALTER TABLE table1 DROP attr;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand32() {
        String input = "INSERT INTO table1 VALUES(1, 2, 3, 'beans');";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand33() {
        String input = "INSERT INTO table1 VALUES(NULL);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand34() {
        String input = "DELETE FROM table1 WHERE attr1 LIKE 'hello ';";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand35() {
        String input = "DELETE FROM table1 WHERE attr1 LIKE 'hello ' AND (attr2 != 6 OR (attr3 >= 5));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand36() {
        String input = "create database database1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand37() {
        String input = "delete from table1 where attr1 like 'hello ' and (attr2 != 6 or (attr3 >= 5));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand38() {
        String input = "insert into table1 values(null, 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand39() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }

    @Test
    void testIsCommand40() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ((attr == 1) AND (attr2 == 2));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
        assertEquals(parser.getErrorMessage().length(), 0);
    }


    //--------------------------------------//
    //----- IS COMMAND FAILURE TESTING -----//
    //--------------------------------------//

    @Test
    void testIsCommandFail1() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 LIKE 'beans' AND attr2 == 5 OR (attr8 != 16;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a closing bracket \")\".");
    }

    @Test
    void testIsCommandFail2() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE ((attr1 LIKE 'beans' AND attr2 == 5 OR (attr8 != 16);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a closing bracket \")\".");
    }

    @Test
    void testIsCommandFail3() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE ((attr1)) LIKE 'beans' AND attr2 == 5 OR (attr8 != 16);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a [Comparator] keyword after [AttributeName] \"attr1\".");
    }

    @Test
    void testIsCommandFail4() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 != 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Extraneous bracket \")\".");
    }

    @Test
    void testIsCommandFail5() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE (attr1 != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a closing bracket \")\".");
    }

    @Test
    void testIsCommandFail6() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \";\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail7() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \";\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail8() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr == ;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a [Value] after [Comparator] \"==\".");
    }

    @Test
    void testIsCommandFail9() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"==\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail10() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 == 1";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \";\" to terminate the query. Found \"1\" instead.");
    }

    @Test
    void testIsCommandFail11() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 attr1 == 1;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \"WHERE\" keyword or \";\" to terminate the query after [TableName] \"table1\".");
    }

    @Test
    void testIsCommandFail12() {
        String input = "JOIN tableName1 AND tableName2 ON attrX attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \"AND\" keyword after \"attrX\".");
    }

    @Test
    void testIsCommandFail13() {
        String input = "JOIN tableName1 AND tableName2 attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \"ON\" keyword after \"tableName2\".");
    }

    @Test
    void testIsCommandFail14() {
        String input = "JOIN tableName1  tableName2 ON attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \"AND\" keyword after \"tableName1\".");
    }

    @Test
    void testIsCommandFail15() {
        String input = " tableName1 AND tableName2 ON attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \"USE\" or \"CREATE\" or \"DROP\" or \"ALTER\" or \"INSERT\" or \"SELECT\" or \"UPDATE\" or \"DELETE\" or \"JOIN\" keyword to begin the query.");
    }

    @Test
    void testIsCommandFail16() {
        String input = "JOIN AND tableName2 ON attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"AND\" matches the SQL keyword AND.");
    }

    @Test
    void testIsCommandFail17() {
        String input = "JOIN tableName1 AND ON attrX AND attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"ON\" matches the SQL keyword ON.");
    }

    @Test
    void testIsCommandFail18() {
        String input = "JOIN tableName1 AND tableName2 ON anD attrY;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"anD\" matches the SQL keyword AND.");
    }

    @Test
    void testIsCommandFail19() {
        String input = "JOIN tableName1 AND tableName2 ON attrX AND;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \";\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail20() {
        String input = "JOIN tableName1 AND tableName2 ON attrX AND attrY";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected \";\" to terminate the query. Found \"attrY\" instead.");
    }

    @Test
    void testIsCommandFail21() {
        String input = "UPDATE t1 SET a = 'beans' WHERE (attr1 == 6 AND (attr2 == 1) OR att3 == 9) OR attr4 == 1);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Extraneous bracket \")\".");
    }

    @Test
    void testIsCommandFail22() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr == 1 AND attr == 2 OR attr == 3 AND (attr == 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a closing bracket \")\".");
    }

    @Test
    void testIsCommandFail23() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE (attr == 1 AND (attr == 2) OR attr == 3)) AND (attr == 0);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Extraneous bracket \")\".");
    }

    @Test
    void testIsCommandFail24() {
        String input = "ALTER TABLE table ADD attr1, attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"table\" matches the SQL keyword TABLE.");
    }

    @Test
    void testIsCommandFail25() {
        String input = "JOIN table1 AND table2 ON like.attr1 AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"like\" matches the SQL keyword LIKE.");
    }

    @Test
    void testIsCommandFail26() {
        String input = "JOIN table1 AND table2 ON table1.or AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"or\" matches the SQL keyword OR.");
    }

    @Test
    void testIsCommandFail27() {
        String input = "JOIN table1 AND table2 ON table1.and AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"and\" matches the SQL keyword AND.");
    }

    @Test
    void testIsCommandFail28() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND table2.from;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"from\" matches the SQL keyword FROM.");
    }

    @Test
    void testIsCommandFail29() {
        String input = "JOIN table1 AND table2 ON table1.into AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"into\" matches the SQL keyword INTO.");
    }

    @Test
    void testIsCommandFail30() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND null.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"null\" matches the SQL keyword NULL.");
    }

    @Test
    void testIsCommandFail31() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND sEt.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"sEt\" matches the SQL keyword SET.");
    }

    @Test
    void testIsCommandFail32() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND table2.true;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"true\" matches the SQL keyword TRUE.");
    }

    @Test
    void testIsCommandFail33() {
        String input = "JOIN table1 AND table2 ON table1.attr1 AND table2.false;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"false\" matches the SQL keyword FALSE.");
    }

    @Test
    void testIsCommandFail34() {
        String input = "JOIN table1 AND table2 ON table1.add AND table2.attr2;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"add\" matches the SQL keyword ADD.");
    }

    @Test
    void testIsCommandFail35() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1 !=) 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a [Value] after [Comparator] \"!=\".");
    }

    @Test
    void testIsCommandFail36() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE attr1) != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a [Comparator] keyword after [AttributeName] \"attr1\".");
    }

    @Test
    void testIsCommandFail37() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE $ != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"$\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail38() {
        String input = "SELECT attr1!, attr2, attr3 FROM table1 WHERE attr1 != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \"attr1!\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail39() {
        String input = "SELECT attr1, attr2, attr3 FROM ## WHERE attr1 != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [TableName] \"##\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail40() {
        String input = "USE %$£;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [DatabaseName] \"%$£\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail41() {
        String input = "SELECT attr1, attr2, attr3 FROM table1 WHERE ) != 0;";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "The [AttributeName] \")\" is not alphanumeric.");
    }

    @Test
    void testIsCommandFail42() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ((attr == 1));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        //assertEquals(parser.getErrorMessage(), "Expected a [BoolOperator] AND or OR instead of \")\" in a multi-conditioned statement.");
    }

    @Test
    void testIsCommandFail43() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ((attr == 1) AND (attr2) == 2));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        assertEquals(parser.getErrorMessage(), "Expected a [Comparator] keyword after [AttributeName] \"attr2\".");
    }

    @Test
    void testIsCommandFail44() {
        String input = "UPDATE tableName1 SET attr1 = 'beans' WHERE ((attr == 1) (AND) (attr2 == 2));";
        Parser parser = setParser(input);
        parser.setParsing();
        assertFalse(parser.isCommand());
        //assertEquals(parser.getErrorMessage(), "Expected a [BoolOperator] AND or OR instead of \"(\" in a multi-conditioned statement.");
    }

    @Test
    public void testIsCreate45() {
        String input = "CREATE TABLE tableName (plaintext);";
        Parser parser = setParser(input);
        parser.setParsing();
        assertTrue(parser.isCommand());
        //assertEquals(parser.getI(), tokens.size() - 1);
    }

}
