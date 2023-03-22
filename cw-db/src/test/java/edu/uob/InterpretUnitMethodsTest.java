package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterpretUnitMethodsTest {

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
    public void testUseDatabase1() {
        String response = sendCommandToServer("USE databases;");
        assertEquals("[ERROR] The [DatabaseName] \"databases\" does not exist.", response);

        response = sendCommandToServer("CREATE DATABASE database1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE DATABASE database2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("USE database1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE tableInDB1 (name);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO tableInDB1 VALUES('mark');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM tableInDB1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\n"));
        assertTrue(response.contains("1\tmark\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("USE database2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM tableInDB1;");
        assertEquals("[ERROR] The table \"tableInDB1\" does not exist.", response);

        response = sendCommandToServer("CREATE TABLE tableInDB1 (age);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO tableInDB1 VALUES(20);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM tableInDB1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tage\n"));
        assertTrue(response.contains("1\t20\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("USE database1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM tableInDB1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\n"));
        assertTrue(response.contains("1\tmark\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE database1;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("CREATE TABLE badTable;");
        assertEquals("[ERROR] You have not selected a [DatabaseName] to use yet.", response);
        response = sendCommandToServer("USE database1;");
        assertEquals("[ERROR] The [DatabaseName] \"database1\" does not exist.", response);
        response = sendCommandToServer("DROP DATABASE database2;");
        assertEquals("[OK]", response);

    }

    @Test
    public void testUseDatabase2() {
        String response = sendCommandToServer("CREATE DATABASE testDatabasePlease;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE badTable;");
        assertEquals("[ERROR] You have not selected a [DatabaseName] to use yet.", response);

        response = sendCommandToServer("USE nonExistentDatabase;");
        assertEquals("[ERROR] The [DatabaseName] \"nonExistentDatabase\" does not exist.", response);

        response = sendCommandToServer("CREATE TABLE badTable;");
        assertEquals("[ERROR] You have not selected a [DatabaseName] to use yet.", response);

        response = sendCommandToServer("USE testDatabasePlease;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE newTable (name, age, hobby);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable VALUES('Jim', 20, 'skiing');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable VALUES('Charlie', 28, 'climbing');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable VALUES('Amy', 31, 'bowling');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT hobby, age, name, id FROM newTable WHERE age > 25;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("hobby\tage\tname\tid\n"));
        assertTrue(response.contains("climbing\t28\tCharlie\t2\n"));
        assertTrue(response.contains("bowling\t31\tAmy\t3\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("USE nonExistentDatabase;");
        assertEquals("[ERROR] The [DatabaseName] \"nonExistentDatabase\" does not exist.", response);

        response = sendCommandToServer("DROP DATABASE testDatabasePlease;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("USE testDatabasePlease;");
        assertEquals("[ERROR] The [DatabaseName] \"testDatabasePlease\" does not exist.", response);

        response = sendCommandToServer("CREATE TABLE badTable;");
        assertEquals("[ERROR] You have not selected a [DatabaseName] to use yet.", response);
    }

    @Test
    public void testUseDatabase3() {
        String response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[ERROR] Cannot drop [DatabaseName] \"db1\" as it does not exist.", response);

        response = sendCommandToServer("CREATE DATABASE db1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE DATABASE DB1;");
        assertEquals("[ERROR] The [DatabaseName] \"DB1\" already exists.", response);

        response = sendCommandToServer("USE db1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE table1 (name);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO table1 VALUES('gus');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM table1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\n"));
        assertTrue(response.contains("1\tgus\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("CREATE DATABASE DB2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM table1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\n"));
        assertTrue(response.contains("1\tgus\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("USE db2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM table1;");
        assertEquals("[ERROR] The table \"table1\" does not exist.", response);

        response = sendCommandToServer("DROP DATABASE nothing;");
        assertEquals("[ERROR] Cannot drop [DatabaseName] \"nothing\" as it does not exist.", response);
        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE DB2;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE db2;");
        assertEquals("[ERROR] Cannot drop [DatabaseName] \"db2\" as it does not exist.", response);
    }

    @Test
    public void testUseDatabase4() {
        String response = sendCommandToServer("create database NEWDATABASE");
        assertEquals("[ERROR] Expected TABLE or DATABASE keyword after CREATE keyword.", response);

        response = sendCommandToServer("create database NEWDATABASE;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("drop database NEWDATABASE");
        assertEquals("[ERROR] Expected TABLE or DATABASE keyword after DROP keyword.", response);

        response = sendCommandToServer("drop database NEWDATABASE;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testDropNoColon1() {
        String response = sendCommandToServer("create database TEST;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("use test;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("create table TEST (name);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("select * from test;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("drop table TEST");
        assertEquals("[ERROR] Expected TABLE or DATABASE keyword after DROP keyword.", response);

        response = sendCommandToServer("drop table TEST;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("drop database TEST;");
        assertEquals("[OK]", response);

    }

    private void setDatabaseAlter() {
        String response = sendCommandToServer("CREATE DATABASE db1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("USE db1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE newTable1 (name, age, hobby);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('george', 20, NULL);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('gus', 21, 'skiing');");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAlterTable1() {
        setDatabaseAlter();

        String response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE newTable1 ADD food;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT food FROM newTable1 WHERE name == 'gus';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("food\n"));
        assertTrue(response.contains("NULL\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE newTable1 DROP id;");
        assertEquals("[ERROR] Cannot delete id column.", response);

        response = sendCommandToServer("ALTER TABLE newTable1 DROP nonExistent;");
        assertEquals("[ERROR] The [AttributeName] \"nonExistent\" does not exist for the [TableName] \"newtable1\".", response);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAlterTable2() {
        setDatabaseAlter();
        String response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE newTable1 DROP hobby;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\n"));
        assertTrue(response.contains("1\tgeorge\t20\n"));
        assertTrue(response.contains("2\tgus\t21\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAlterTable3() {
        setDatabaseAlter();

        String response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE newTable1 ADD food;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('jesus', 30, 'blood');");
        assertEquals("[ERROR] The table \"newtable1\" must take 4 values exactly.", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 22, 'skiing', 'apples');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\tfood\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\tNULL\n"));
        assertTrue(response.contains("3\tbala\t22\tskiing\tapples\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE newTable1 DROP hobby;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfood\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tNULL\n"));
        assertTrue(response.contains("3\tbala\t22\tapples\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testUpdateTable1() {
        setDatabaseAlter();

        String response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET hobby='climbing' WHERE hobby==NULL;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tclimbing\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET hobby='bowling', age=23 WHERE id==2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tclimbing\n"));
        assertTrue(response.contains("2\tgus\t23\tbowling\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("UPDATE newTable1 SET hobby=NULL WHERE id<=3;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t23\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET name='alice', name='amy' WHERE id==2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tamy\t23\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET id=999 WHERE id==1;");
        assertEquals("[ERROR] Cannot update the id column.", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tamy\t23\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET id=999, hobby='cricket' WHERE name=='amy';");
        assertEquals("[ERROR] Cannot update the id column.", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tamy\t23\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("UPDATE newTable1 SET hobby='cricket', id=55 WHERE name=='amy';");
        assertEquals("[ERROR] Cannot update the id column.", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tamy\t23\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testDeleteInsert1() {
        setDatabaseAlter();
        String response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE name == 'bala' AND id == 3;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('newPerson', 30, 'reading');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("4\tnewPerson\t30\treading\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE hobby == 'skiing' OR hobby == 'reading';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("4\tnewPerson\t30\treading\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE id == 4;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hector', 28, 'rugby');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("5\thector\t28\trugby\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DROP TABLE NEWTABLE1;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM NEWTABLE1;");
        assertEquals("[ERROR] The table \"NEWTABLE1\" does not exist.", response);

        response = sendCommandToServer("CREATE TABLE NEWTABLE1 (name, age);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('James', 31);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM neWTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\n"));
        assertTrue(response.contains("1\tJames\t31\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testDelete1() {
        setDatabaseAlter();

        String response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE hobby==NULL;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE name=='george';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE age<30;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testDelete2() {
        setDatabaseAlter();

        String response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('kate', 27, 'climbing');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE id > 0;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testDelete3() {
        setDatabaseAlter();

        String response = sendCommandToServer("INSERT INTO newTable1 VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('kate', 27, 'climbing');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM newTable1 WHERE age < 21 OR hobby LIKE 'c';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    private void setJoinDatabase() {
        String response = sendCommandToServer("CREATE DATABASE db1;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("USE db1;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("CREATE TABLE people (name, age, hobby);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO people VALUES('george', 20, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO people VALUES('gus', 21, 'skiing');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO people VALUES('bala', 25, 'coding');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO people VALUES('kate', 27, 'climbing');");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE employee (level, office, ename);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO employee VALUES(14, 'London', 'gus');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO employee VALUES(17, 'Paris', 'kate');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO employee VALUES(15, 'Frankfurt', 'george');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO employee VALUES(10, 'London', 'bala');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO employee VALUES(20, 'Paris', 'jimmy');");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAttributeName1() {
        setJoinDatabase();

        String response = sendCommandToServer("SELECT people.name FROM people WHERE id == 1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("george\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT employee.name FROM people WHERE id == 1;");
        assertEquals("[ERROR] The table \"people\" does not match the table \"employee\".", response);

        response = sendCommandToServer("SELECT employee.name FROM employee WHERE id == 1;");
        assertEquals("[ERROR] The table \"employee\" does not contain the column \"name\".", response);

        response = sendCommandToServer("SELECT employee.ename FROM people WHERE id == 1;");
        assertEquals("[ERROR] The table \"people\" does not match the table \"employee\".", response);

        response = sendCommandToServer("SELECT employee.ename FROM employee WHERE id == 1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("gus\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAttributeName2() {
        setJoinDatabase();

        String response = sendCommandToServer("JOIN people AND employee ON people.name AND employee.ename;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tpeople.age\tpeople.hobby\temployee.level\temployee.office\n"));
        assertTrue(response.contains("1\t20\tNULL\t15\tFrankfurt\n"));
        assertTrue(response.contains("2\t21\tskiing\t14\tLondon\n"));
        assertTrue(response.contains("3\t25\tcoding\t10\tLondon\n"));
        assertTrue(response.contains("4\t27\tclimbing\t17\tParis\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("JOIN employee AND people ON employee.ename AND people.name;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\temployee.level\temployee.office\tpeople.age\tpeople.hobby\n"));
        assertTrue(response.contains("3\t15\tFrankfurt\t20\tNULL\n"));
        assertTrue(response.contains("1\t14\tLondon\t21\tskiing\n"));
        assertTrue(response.contains("4\t10\tLondon\t25\tcoding\n"));
        assertTrue(response.contains("2\t17\tParis\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE people DROP people.hobby;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\n"));
        assertTrue(response.contains("1\tgeorge\t20\n"));
        assertTrue(response.contains("2\tgus\t21\n"));
        assertTrue(response.contains("3\tbala\t25\n"));
        assertTrue(response.contains("4\tkate\t27\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE people DROP people.id;");
        assertEquals("[ERROR] Cannot delete id column.", response);

        response = sendCommandToServer("ALTER TABLE people DROP employee.id;");
        assertEquals("[ERROR] The [TableName] \"employee\" does not match \"people\".", response);

        response = sendCommandToServer("ALTER TABLE people ADD people.id;");
        assertEquals("[ERROR] The [AttributeName] \"id\" already exists in \"people\".", response);

        response = sendCommandToServer("ALTER TABLE people ADD employee.id;");
        assertEquals("[ERROR] The [TableName] \"employee\" does not match \"people\".", response);

        response = sendCommandToServer("ALTER TABLE people ADD people.hobby;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tNULL\n"));
        assertTrue(response.contains("3\tbala\t25\tNULL\n"));
        assertTrue(response.contains("4\tkate\t27\tNULL\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAttributeName3() {
        setJoinDatabase();
        String response = sendCommandToServer("CREATE TABLE teachers (teachers.name, teachers.age, teachers.subject);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM teachers;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tsubject\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("CREATE TABLE countries (teachers.name, countries.age, countries.subject);");
        assertEquals("[ERROR] The [TableName] \"teachers\" does not match \"countries\".", response);

        response = sendCommandToServer("CREATE TABLE countries (teachers.id, countries.age, countries.subject);");
        assertEquals("[ERROR] The [TableName] \"teachers\" does not match \"countries\".", response);

        response = sendCommandToServer("CREATE TABLE countries (countries.id, countries.age, countries.subject);");
        assertEquals("[ERROR] Tables must not have duplicate column names.", response);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testAttributeName4() {
        setJoinDatabase();

        String response = sendCommandToServer("SELECT employee.id FROM people;");
        assertEquals("[ERROR] The table \"people\" does not match the table \"employee\".", response);

        response = sendCommandToServer("SELECT people.hobby, people.id FROM people WHERE age < 50;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("hobby\tid\n"));
        assertTrue(response.contains("NULL\t1\n"));
        assertTrue(response.contains("skiing\t2\n"));
        assertTrue(response.contains("coding\t3\n"));
        assertTrue(response.contains("climbing\t4\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT people.hobby, people.id FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("hobby\tid\n"));
        assertTrue(response.contains("NULL\t1\n"));
        assertTrue(response.contains("skiing\t2\n"));
        assertTrue(response.contains("coding\t3\n"));
        assertTrue(response.contains("climbing\t4\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT people.hobby, nothing.id FROM people WHERE age < 50;");
        assertEquals("[ERROR] The table \"people\" does not match the table \"nothing\".", response);

        response = sendCommandToServer("SELECT people.hobby, people.hello FROM people;");
        assertEquals("[ERROR] The table \"people\" does not contain the column \"hello\".", response);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testCasing1() {
        setJoinDatabase();

        String response = sendCommandToServer("SELECT * froM PEOPLE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("CREATE TABLE ANewTable (Attr1, Attr2, Attr3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO anewtable Values('bob', 2, NULL);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * froM anewtablE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tAttr1\tAttr2\tAttr3\n"));
        assertTrue(response.contains("1\tbob\t2\tNULL\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT attr1 from anewTABLE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("Attr1\n"));
        assertTrue(response.contains("bob\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE anewtable DROP attR3;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * froM anewtablE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tAttr1\tAttr2\n"));
        assertTrue(response.contains("1\tbob\t2\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("INSERT INTO anewtable Values('charlie', 1);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("ALTER TABLE anewtable ADD WeIrDaTtR;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * froM anewtablE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tAttr1\tAttr2\tWeIrDaTtR\n"));
        assertTrue(response.contains("1\tbob\t2\tNULL\n"));
        assertTrue(response.contains("2\tcharlie\t1\tNULL\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT weirdAttr, ID froM anewtablE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("WeIrDaTtR\tid\n"));
        assertTrue(response.contains("NULL\t1\n"));
        assertTrue(response.contains("NULL\t2\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("ALTER TABLE anewtable ADD ID;");
        assertEquals("[ERROR] The [AttributeName] \"ID\" already exists in \"anewtable\".", response);

        response = sendCommandToServer("ALTER TABLE anewtable ADD weirdATTR;");
        assertEquals("[ERROR] The [AttributeName] \"weirdATTR\" already exists in \"anewtable\".", response);

        response = sendCommandToServer("ALTER TABLE anewtable DROP ID;");
        assertEquals("[ERROR] Cannot delete id column.", response);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testCasing2() {
        setJoinDatabase();

        String response = sendCommandToServer("JOIN PEOPLE AND EMPLOYEE ON PEOPLE.NAME AND EMPLOYEE.ENAME;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tpeople.age\tpeople.hobby\temployee.level\temployee.office\n"));
        assertTrue(response.contains("1\t20\tNULL\t15\tFrankfurt\n"));
        assertTrue(response.contains("2\t21\tskiing\t14\tLondon\n"));
        assertTrue(response.contains("3\t25\tcoding\t10\tLondon\n"));
        assertTrue(response.contains("4\t27\tclimbing\t17\tParis\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("UPDATE people SET HObBY='boxing' WHERE PEOPLE.nAMe=='george';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM peoplE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tboxing\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM peOple WHERE PEOPLE.ID>2;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM peoplE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tboxing\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT pEOPlE.aGE FROM peoplE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("age\n"));
        assertTrue(response.contains("20\n"));
        assertTrue(response.contains("21\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }


    @Test
    public void testBadCommands() {
        setJoinDatabase();
        String response = sendCommandToServer("SELECT beans FROM people;");
        assertEquals("[ERROR] The table \"people\" does not contain the column \"beans\".", response);

        response = sendCommandToServer("SELECT beans FROM beans;");
        assertEquals("[ERROR] The table \"beans\" does not exist.", response);

        response = sendCommandToServer("SELECT * FROM people WHERE beans == 4;");
        assertEquals("[ERROR] The [AttributeName] \"beans\" does not exist for the [TableName] \"people\".", response);

        response = sendCommandToServer("this is complete garbage;");
        assertEquals("[ERROR] Expected \"USE\" or \"CREATE\" or \"DROP\" or \"ALTER\" or \"INSERT\" or \"SELECT\" or \"UPDATE\" or \"DELETE\" or \"JOIN\" keyword to begin the query.", response);

        response = sendCommandToServer(";");
        assertEquals("[ERROR] Expected \"USE\" or \"CREATE\" or \"DROP\" or \"ALTER\" or \"INSERT\" or \"SELECT\" or \"UPDATE\" or \"DELETE\" or \"JOIN\" keyword to begin the query.", response);

        response = sendCommandToServer("");
        assertEquals("[ERROR] Expected \"USE\" or \"CREATE\" or \"DROP\" or \"ALTER\" or \"INSERT\" or \"SELECT\" or \"UPDATE\" or \"DELETE\" or \"JOIN\" keyword to begin the query.", response);

        response = sendCommandToServer("update PEOPLE set asdf to NULL;");
        assertEquals("[ERROR] Expected a named value list after SET keyword.", response);

        response = sendCommandToServer("update PEOPLE set asdf='1234' WHERE name == 'gus';");
        assertEquals("[ERROR] The [AttributeName] \"asdf\" does not exist.", response);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testTyping() {
        setJoinDatabase();
        String response = sendCommandToServer("SELECT * from people where people.name like 'gu';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where people.age like 21;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where people.age like 21 or id == 2;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where people.hobby > true;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where people.hobby < true;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("INSERT INTO PEople valuEs  ('jim', 20, falSe);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO PEople valuEs  ('robert', 30, trUE);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * from people where peOple.hObby == false or HOBBY == true;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("5\tjim\t20\tFALSE\n"));
        assertTrue(response.contains("6\trobert\t30\tTRUE\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("UPDATE PEOPLE SET age=falsE where people.Name LIKe 'jim';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * from people where peOple.AGE == falsE;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("5\tjim\tFALSE\tFALSE\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("UPDATE PEOPLE SET age=null where people.Name LIKe 'jim';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * from people where peOple.name == 'jim';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("5\tjim\tNULL\tFALSE\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where peOple.name > 5;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("SELECT * from people where peOple.name < 5;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE dB1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testMoreTyping() {
        setJoinDatabase();

        String response = sendCommandToServer("select * from people where age like 20;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("select * from people where hobby like NULL;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where name < 'gus';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("george\n"));
        assertTrue(response.contains("bala\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where name <= 'gus';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("george\n"));
        assertTrue(response.contains("gus\n"));
        assertTrue(response.contains("bala\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where name > 'gus';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("kate\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where name >= 'gus';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertTrue(response.contains("gus\n"));
        assertTrue(response.contains("kate\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where hobby >= null;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("select people.name from people where hobby <= null;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("name\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("delete from people where id == +1;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("delete from people where id == '+2';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM people ;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE DB1;");
        assertEquals("[OK]", response);
    }


    @Test
    public void testJoin() {
        setJoinDatabase();

        String response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM employee;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tlevel\toffice\tename\n"));
        assertTrue(response.contains("1\t14\tLondon\tgus\n"));
        assertTrue(response.contains("2\t17\tParis\tkate\n"));
        assertTrue(response.contains("3\t15\tFrankfurt\tgeorge\n"));
        assertTrue(response.contains("4\t10\tLondon\tbala\n"));
        assertTrue(response.contains("5\t20\tParis\tjimmy\n"));
        assertEquals(7, response.split("\n").length);

        response = sendCommandToServer("JOIN people AND employee ON name AND enAme;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tpeople.age\tpeople.hobby\temployee.level\temployee.office\n"));
        assertTrue(response.contains("1\t20\tNULL\t15\tFrankfurt\n"));
        assertTrue(response.contains("2\t21\tskiing\t14\tLondon\n"));
        assertTrue(response.contains("3\t25\tcoding\t10\tLondon\n"));
        assertTrue(response.contains("4\t27\tclimbing\t17\tParis\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("JOIN employee AND people ON name AND ename;");
        assertEquals("[ERROR] The [AttributeName] \"name\" does not exist for the [TableName] \"employee\".", response);

        response = sendCommandToServer("JOIN employee AND people ON ename AND ename;");
        assertEquals("[ERROR] The [AttributeName] \"ename\" does not exist for the [TableName] \"people\".", response);

        response = sendCommandToServer("JOIN EMPLOYEE AND PEOPLE ON ENaME AND NAMe;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\temployee.level\temployee.office\tpeople.age\tpeople.hobby\n"));
        assertTrue(response.contains("15\tFrankfurt\t20\tNULL\n"));
        assertTrue(response.contains("14\tLondon\t21\tskiing\n"));
        assertTrue(response.contains("10\tLondon\t25\tcoding\n"));
        assertTrue(response.contains("17\tParis\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testJoin2() {
        setJoinDatabase();

        String response = sendCommandToServer("SELECT * FROM people;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\thobby\n"));
        assertTrue(response.contains("1\tgeorge\t20\tNULL\n"));
        assertTrue(response.contains("2\tgus\t21\tskiing\n"));
        assertTrue(response.contains("3\tbala\t25\tcoding\n"));
        assertTrue(response.contains("4\tkate\t27\tclimbing\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM employee;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tlevel\toffice\tename\n"));
        assertTrue(response.contains("1\t14\tLondon\tgus\n"));
        assertTrue(response.contains("2\t17\tParis\tkate\n"));
        assertTrue(response.contains("3\t15\tFrankfurt\tgeorge\n"));
        assertTrue(response.contains("4\t10\tLondon\tbala\n"));
        assertTrue(response.contains("5\t20\tParis\tjimmy\n"));
        assertEquals(7, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM employee WHERE name=='gus';");
        assertEquals("[ERROR] The [AttributeName] \"name\" does not exist for the [TableName] \"employee\".", response);

        response = sendCommandToServer("DELETE FROM employee WHERE ename=='gus';");
        assertEquals("[OK]", response);

        response = sendCommandToServer("JOIN people AND employee ON name AND ename;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tpeople.age\tpeople.hobby\temployee.level\temployee.office\n"));
        assertTrue(response.contains("1\t20\tNULL\t15\tFrankfurt\n"));
        assertTrue(response.contains("2\t25\tcoding\t10\tLondon\n"));
        assertTrue(response.contains("3\t27\tclimbing\t17\tParis\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DELETE FROM employee WHERE ename=='bala';");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DELETE FROM employee WHERE ename=='jimmy';");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT  INTO  employee values   ( 1, 'Madrid', 'alice' )  ;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("SELECT * FROM employee;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tlevel\toffice\tename\n"));
        assertTrue(response.contains("2\t17\tParis\tkate\n"));
        assertTrue(response.contains("3\t15\tFrankfurt\tgeorge\n"));
        assertTrue(response.contains("6\t1\tMadrid\talice\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("INSERT  INTO  people values('alice', 23, 'canoeing' )  ;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("JOIN people AND employee ON name AND ename;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tpeople.age\tpeople.hobby\temployee.level\temployee.office\n"));
        assertTrue(response.contains("1\t20\tNULL\t15\tFrankfurt\n"));
        assertTrue(response.contains("2\t27\tclimbing\t17\tParis\n"));
        assertTrue(response.contains("3\t23\tcanoeing\t1\tMadrid\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE db1;");
        assertEquals("[OK]", response);
    }

    public void startTestDatabase() {
        String response = sendCommandToServer("CREATE DATABASE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE newTable1 (attr1, attr2, attr3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('hi', 1, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO newTable1 VALUES('go away', 1, NULL);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id==7;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tattr1\tattr2\tattr3\n"));
        assertTrue(response.contains("7\tgo away\t1\tNULL\n"));
        assertEquals(3, response.split("\n").length);
    }

    @Test
    public void testBasicCreateAndQuery() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO newTable1 VALUES('str lit', NULL, 45);");
        assertEquals(response, "[OK]");

        response = sendCommandToServer("INSERT INTO newTable1 VALUES(1, 2, 3, 4);");
        assertEquals(response, "[ERROR] The table \"newtable1\" must take 3 values exactly.");

        response = sendCommandToServer("INSERT INTO newTable1 VALUES(NULL, 1);");
        assertEquals(response, "[ERROR] The table \"newtable1\" must take 3 values exactly.");

        response = sendCommandToServer("INSERT INTO nonExistentTable VALUES(1, 2, 3);");
        assertEquals(response, "[ERROR] The table \"nonExistentTable\" does not exist.");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testTableCreationPrevention() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE newTable1;");
        assertEquals(response, "[ERROR] The table \"newTable1\" already exists.");

        response = sendCommandToServer("CREATE TABLE testTable (id, attr1);");
        assertEquals(response, "[ERROR] Tables must not have duplicate column names.");

        response = sendCommandToServer("CREATE TABLE testTable (attr5, attr1, attr5);");
        assertEquals(response, "[ERROR] Tables must not have duplicate column names.");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testInsertIntoNonExistentTable() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO nonExistentTable VALUES(1, 2, 3);");
        assertEquals(response, "[ERROR] The table \"nonExistentTable\" does not exist.");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement1() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (attr1 == 'go away');");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement2() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE attr1 == 'go away';");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement3() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE attr1 == 'go away' AND attr3 == NULL;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement4() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE attr3 == NULL AND attr1 == 'go away';");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement5() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (attr3 == NULL) AND attr1 == 'go away';");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement6() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE attr3 == NULL AND (NEWTABLE1.ATTR1 == 'go away');");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement7() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (newTable1.attr3 == NULL) AND (attr1 == 'go away');");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement8() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (attr3 == NULL AND attr1 == 'go away');");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement9() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id==7 AND (attr3 == NULL AND attr1 == 'go away');");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement10() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (attr3 == NULL AND attr1 == 'go away') AND nEwTable1.ID==7;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement11() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 AND id==7;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement12() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR id==7;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement13() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1) OR id==7;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement14() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1) OR (id==7);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement15() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR (id==7);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement16() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1 OR id==7);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n7\tgo away\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement17() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1 OR id==7) AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement18() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR id==7 AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement19() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR (id==7 AND id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement20() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1 OR id==7 AND id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement21() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1) OR (id==7) AND (id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement22() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1) OR id==7 AND (id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement23() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR id==7 AND (id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement24() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1) OR id==7 AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement25() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE id == 1 OR (id==7) AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement26() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1 OR (id==7)) AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement27() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE ((id == 1) OR (id==7)) AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement28() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE ((id == 1) OR id==7) AND id==1;");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement29() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (((id == 1) OR id==7) AND id==1);");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testSelectStatement30() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT * FROM newTable1 WHERE (id == 1 OR (id==7 AND (id==1)));");
        assertEquals(response, "[OK]\nid\tattr1\tattr2\tattr3\n1\thi\t1\tNULL\n");
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    private void populateTable1() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE testTable (name, age, favFood);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Alice', 22, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Bob', 25, 'chocolate');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Charlie', 46, 'apples');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Dylan', 33, 'pizza');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Ekta', 22, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Freddie', 46, 'pizza');");
        assertEquals("[OK]", response);
    }

    private void populateTable2() {
        startTestDatabase();

        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE testTable (name, age, favFood);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Alice', 22, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Bob', 25, 'chocolate');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Charlie', 46, 'apples');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Dylan', 33, 'pizza');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Ekta', 22, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Freddie', 46, 'pizza');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Gus', 23, 'avocado');");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('George', 20, NULL);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO testTable VALUES('Alex', 21, 'gyros');");
        assertEquals("[OK]", response);
    }

    @Test
    public void testCreateInsertSelectDrop1() {
        populateTable1();
        String response = sendCommandToServer("SELECT * FROM testTable WHERE age == 22;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n5\tEkta\t22\tNULL\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 22 AND name == 'Ekta';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n5\tEkta\t22\tNULL\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 22 AND favFood == NULL;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n5\tEkta\t22\tNULL\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 OR favFood == 'pizza';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n4\tDylan\t33\tpizza\n6\tFreddie\t46\tpizza\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 OR name == 'Bob';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n4\tDylan\t33\tpizza\n2\tBob\t25\tchocolate\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 AND name == 'Gus';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 OR name == 'Gus';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n4\tDylan\t33\tpizza\n", response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE id == 0;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n", response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }



    @Test
    public void testCreateInsertSelectDrop2() {
        populateTable1();

        String response = sendCommandToServer("SELECT * FROM testTable WHERE age LIKE 22;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 22 AND name != 'Ekta';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 22 AND favFood != NULL;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 OR favFood LIKE 'pizza';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n4\tDylan\t33\tpizza\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 33 AND name != 'Gus';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n4\tDylan\t33\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE id <= 2;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n2\tBob\t25\tchocolate\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE id < 2;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE id > 5;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE id >= 5;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n5\tEkta\t22\tNULL\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE name LIKE 'Fred';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE name LIKE 'Freddiee';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n", response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }


    @Test
    public void testCreateInsertSelectDrop3() {
        populateTable1();

        String response = sendCommandToServer("SELECT * FROM testTable WHERE age == 46 OR (favFood == NULL AND name == 'Alice');");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46) OR (favFood == NULL AND name == 'Alice');");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 46 OR (favFood == NULL AND (name == 'Alice'));");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 46 OR ((favFood == NULL) AND name == 'Alice');");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46 OR ((favFood == NULL) AND (name == 'Alice')));");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((favFood == NULL) AND (name == 'Alice')) OR age == 46);");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((favFood == NULL) AND (name == 'Alice')) OR (age == 46));");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((favFood == NULL) AND name == 'Alice') OR age == 46);");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((favFood == NULL AND (name == 'Alice')) OR age == 46);");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((favFood == NULL) AND (name == 'Alice')) OR age == 46;");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((favFood == NULL) AND name == 'Alice') OR (age == 46));");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((favFood == NULL AND (name == 'Alice')) OR (age == 46));");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((favFood == NULL) AND (name == 'Alice')) OR (age == 46);");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n3\tCharlie\t46\tapples\n6\tFreddie\t46\tpizza\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 46 OR favFood == NULL AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46) OR favFood == NULL AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age == 46 OR (favFood == NULL) AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46 OR favFood == NULL) AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((age == 46) OR favFood == NULL) AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((age == 46) OR (favFood == NULL)) AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((age == 46) OR (favFood == NULL)) AND (name == 'Alice');");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46 OR (favFood == NULL)) AND name == 'Alice';");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age == 46 OR (favFood == NULL)) AND (name == 'Alice');");
        assertEquals("[OK]\nid\tname\tage\tfavFood\n1\tAlice\t22\tNULL\n", response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }



    private void assertionsFor4(String response) {
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfavFood\n"));
        assertTrue(response.contains("1\tAlice\t22\tNULL\n"));
        assertTrue(response.contains("5\tEkta\t22\tNULL\n"));
        assertTrue(response.contains("8\tGeorge\t20\tNULL\n"));
        assertTrue(response.contains("9\tAlex\t21\tgyros\n"));
        assertTrue(response.contains("4\tDylan\t33\tpizza\n"));
        assertTrue(response.contains("6\tFreddie\t46\tpizza\n"));
        assertTrue(response.contains("2\tBob\t25\tchocolate\n"));
        assertEquals(9, response.split("\n").length);
    }

    @Test
    public void testCreateInsertSelectDrop4() {
        populateTable2();
        String response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR favFood == NULL OR favFood == 'pizza' OR id <= 2;");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR favFood == NULL OR favFood == 'pizza' OR id <= 2);");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR favFood == NULL OR favFood == 'pizza') OR id <= 2;");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR (favFood == NULL OR favFood == 'pizza' OR id <= 2);");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR (favFood == NULL OR (favFood == 'pizza' OR (id <= 2))));");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR (favFood == NULL OR (favFood == 'pizza' OR (id <= 2)));");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((((age < 23) OR favFood == NULL) OR favFood == 'pizza') OR id <= 2);");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((age < 23) OR favFood == NULL) OR favFood == 'pizza') OR id <= 2;");
        assertionsFor4(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR (favFood == NULL)) OR ((favFood == 'pizza') OR id <= 2);");
        assertionsFor4(response);
        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void testCreateInsertSelectDrop5() {
        populateTable2();

        String response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR favFood == NULL AND favFood == 'pizza' OR id <= 2;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfavFood\n"));
        assertTrue(response.contains("1\tAlice\t22\tNULL\n"));
        assertTrue(response.contains("2\tBob\t25\tchocolate\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR favFood == NULL) AND (favFood == 'pizza' OR id <= 2);");
        assertionsJustAlice(response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR favFood == NULL OR favFood == 'pizza' AND id <= 2;");
        assertionsJustAlice(response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR favFood == NULL OR favFood == 'pizza' AND id <= 2);");
        assertionsJustAlice(response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);

    }


    private void assertionsFor6(String response) {
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfavFood\n"));
        assertTrue(response.contains("1\tAlice\t22\tNULL\n"));
        assertTrue(response.contains("5\tEkta\t22\tNULL\n"));
        assertTrue(response.contains("8\tGeorge\t20\tNULL\n"));
        assertTrue(response.contains("9\tAlex\t21\tgyros\n"));
        assertEquals(6, response.split("\n").length);
    }
    @Test
    public void testCreateInsertSelectDrop6() {
        populateTable2();
        String response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 OR favFood == NULL OR (favFood == 'pizza' AND id <= 2);");
        assertionsFor6(response);

        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 OR favFood == NULL OR (favFood == 'pizza' AND id <= 2));");
        assertionsFor6(response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    private void assertionsFor7(String response) {
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfavFood\n"));
        assertTrue(response.contains("1\tAlice\t22\tNULL\n"));
        assertTrue(response.contains("5\tEkta\t22\tNULL\n"));
        assertTrue(response.contains("8\tGeorge\t20\tNULL\n"));
        assertEquals(5, response.split("\n").length);
    }

    private void assertionsJustAlice(String response) {
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tage\tfavFood\n"));
        assertTrue(response.contains("1\tAlice\t22\tNULL\n"));
        assertEquals(3, response.split("\n").length);
    }

    @Test
    public void testCreateInsertSelectDrop7() {
        populateTable2();
        String response = sendCommandToServer("SELECT * FROM testTable WHERE age < 23 AND favFood == NULL OR (favFood == 'pizza' AND id <= 2);");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 AND favFood == NULL OR (favFood == 'pizza' AND id <= 2));");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT id FROM testTable WHERE ((age < 23) AND favFood == NULL OR (favFood == 'pizza' AND id <= 2));");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\n"));
        assertTrue(response.contains("1\n"));
        assertTrue(response.contains("5\n"));
        assertTrue(response.contains("8\n"));
        assertEquals(5, response.split("\n").length);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 AND (favFood == NULL) OR (favFood == 'pizza' AND id <= 2));");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((age < 23) AND (favFood == NULL)) OR (favFood == 'pizza' AND id <= 2));");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (((age < 23) AND favFood == NULL) OR (favFood == 'pizza' AND (id <= 2)));");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((age < 23 AND favFood == NULL) OR (favFood == 'pizza' AND id <= 2));");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 AND favFood == NULL) OR (favFood == 'pizza' AND id <= 2);");
        assertionsFor7(response);
        response = sendCommandToServer("SELECT * FROM testTAble WHERE ((agE < 23 AND fAVFood == NULL) OR favFOod == 'pizza' AND iD <= 2);");
        assertionsJustAlice(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 AND favFood == NULL) OR favFood == 'pizza' AND id <= 2;");
        assertionsJustAlice(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE (age < 23 AND favFood == NULL) OR (favFood == 'pizza') AND id <= 2;");
        assertionsJustAlice(response);
        response = sendCommandToServer("SELECT * FROM testTable WHERE ((age < 23 AND favFood == NULL) OR favFood == 'pizza') AND id <= 2;");
        assertionsJustAlice(response);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }


    @Test
    public void testSpecificSelect1() {
        populateTable2();
        String response = sendCommandToServer("SELECT AGE FROM TESTTABLE WHERE NAME LIKE 'Fred';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("age\n"));
        assertTrue(response.contains("46\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT age, favFood FROM testTable WHERE name LIKE 'Fred';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("age\tfavFood\n"));
        assertTrue(response.contains("46\tpizza\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT favFood, age FROM testTable WHERE name LIKE 'Fred';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("favFood\tage\n"));
        assertTrue(response.contains("pizza\t46\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT favFood, name FROM testTable WHERE name LIKE 'Fred' OR id <= 2;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("favFood\tname\n"));
        assertTrue(response.contains("pizza\tFreddie\n"));
        assertTrue(response.contains("NULL\tAlice\n"));
        assertTrue(response.contains("chocolate\tBob\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT favFood, name FROM testTable WHERE name LIKE 'Fred' AND id <= 2;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("favFood\tname\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DROP TABLE testTable;");
        assertEquals("[OK]", response);
        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    private void setupFloatTable() {
        String response = sendCommandToServer("USE myTestDatabase;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("CREATE TABLE FLOATTABLE (name, float);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO floatTABle VALUES('Alice', 0.1);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO floatTable VALUES('Bob', 0.9);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO floatTable VALUES('Charles', 0.5);");
        assertEquals("[OK]", response);
        response = sendCommandToServer("INSERT INTO floatTable VALUES('Derek', 0.3);");
        assertEquals("[OK]", response);
    }

    @Test
    public void selectFloats2() {
        startTestDatabase();
        setupFloatTable();
        String response = sendCommandToServer("INSERT INTO floatTable VALUES('jim', +0.3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO floatTable VALUES('james', -0.3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float < 0 and float > -1;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("6\t-0.3\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float == -0.3;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("6\t-0.3\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float == +0.9;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("2\t0.9\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float < 0.35 and float > 0.25;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("4\t0.3\n"));
        assertTrue(response.contains("5\t+0.3\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }

    @Test
    public void selectFloats1() {
        startTestDatabase();
        setupFloatTable();


        String response = sendCommandToServer("SELECT * FROM floatTable WHERE float > 0.5;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tfloat\n"));
        assertTrue(response.contains("2\tBob\t0.9\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT * FROM floatTable WHERE FLOAT <= 0.3;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tname\tfloat\n"));
        assertTrue(response.contains("1\tAlice\t0.1\n"));
        assertTrue(response.contains("4\tDerek\t0.3\n"));
        assertEquals(4, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float <= 0.3 OR nAme LIKE 'Bob';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("1\t0.1\n"));
        assertTrue(response.contains("4\t0.3\n"));
        assertTrue(response.contains("2\t0.9\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT ID, FLOAT, SOMETHING FROM floAtTable WHERE flOat <= 0.3 OR naMe LIKE 'Bob';");
        assertEquals("[ERROR] The table \"floattable\" does not contain the column \"SOMETHING\".", response);

        response = sendCommandToServer("INSERT INTO floatTable VALUES('jim', +0.3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("INSERT INTO floatTable VALUES('james', -0.3);");
        assertEquals("[OK]", response);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float <= 0.3;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("1\t0.1\n"));
        assertTrue(response.contains("4\t0.3\n"));
        assertTrue(response.contains("5\t+0.3\n"));
        assertTrue(response.contains("6\t-0.3\n"));
        assertEquals(6, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float < 0 and float > -0.5;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("6\t-0.3\n"));
        assertEquals(3, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float > 0 and float < 0.31;");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertTrue(response.contains("1\t0.1\n"));
        assertTrue(response.contains("4\t0.3\n"));
        assertTrue(response.contains("5\t+0.3\n"));
        assertEquals(5, response.split("\n").length);

        response = sendCommandToServer("SELECT id, float FROM floatTable WHERE float > '0' or float < '0.31';");
        assertTrue(response.contains("[OK]\n"));
        assertTrue(response.contains("id\tfloat\n"));
        assertEquals(2, response.split("\n").length);

        response = sendCommandToServer("DROP TABLE floatTable;");
        assertEquals("[OK]", response);

        response = sendCommandToServer("DROP DATABASE myTestDatabase;");
        assertEquals("[OK]", response);
    }
}
