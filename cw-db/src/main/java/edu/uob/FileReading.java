package edu.uob;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class FileReading {
    private String  storageFolderPath;
    public FileReading(String currentPath) {
        storageFolderPath = currentPath;
    }

    public String getRootPath() {
        return Paths.get("databases").toAbsolutePath().toString();
    }

    public String getCurrentPath() {
        return storageFolderPath;
    }

    public boolean deleteDatabase(String name) {
        String databaseName = getRootPath() + File.separator + name;
        File dir = new File(databaseName);
        File[] contents = dir.listFiles();
        if (contents != null) {
            for (File file : contents) {
                if (!file.delete()) return false;
            }
        }
        return dir.delete();
    }

    public void setPathRoot() {
        storageFolderPath = getRootPath();
    }


    public void changePath(String name) {
        storageFolderPath = getRootPath() + File.separator + name;
    }

    public boolean createFolder(String name) {
        name = name.toLowerCase();
        File directory = new File(getRootPath() + File.separator + name);
        return directory.mkdir();
    }

    public boolean doesDatabaseExist(String name) {
        name = name.toLowerCase();
        String databaseName = this.getRootPath() + File.separator + name;
        File dir = new File(databaseName);
        return dir.exists();
    }

    public boolean doesTableExist(String name) {
        name = name.toLowerCase();
        String filename = this.storageFolderPath + File.separator + name + ".tab";
        File f = new File(filename);
        return f.exists();
    }

    public void deleteFile(String name) {
        name = name.toLowerCase();
        String fileName = this.storageFolderPath + File.separator + name;
        File fileOpened = new File(fileName);
        fileOpened.delete();
    }

    public Table readFileToTable(String name) {
        name = name.toLowerCase();
        String fileName = this.storageFolderPath + File.separator + name + ".tab";
        File fileOpened = new File(fileName);
        Table table = new Table(name);
        try {
            FileReader reader = new FileReader(fileOpened);
            BufferedReader buffReader = new BufferedReader(reader);
            String line;
            int lineIndex = 0;
            while ((line = buffReader.readLine()) != null && line.length() > 0) {
                this.processLine(line.trim(), table, lineIndex);
                lineIndex += 1;
            }
        } catch (IOException e){
            System.out.println("Could not open file.");
        }

        return table;
    }

    private void processLine(String line, Table table, int index) {
        if (line.length() == 0) {
            return;
        }
        String[] elements = line.split("\t");
        ArrayList<String> elementArray = new ArrayList<String>();
        elementArray.addAll(Arrays.asList(elements));
        if (index == 0) {
            table.addFields(elementArray, true);
        } else {
            table.addRows(elementArray);
        }

    }


    public String readConfig(String name) {
        name = name.toLowerCase();
        String fileName = this.storageFolderPath + File.separator + name + ".info";
        File fileOpened = new File(fileName);
        try {
            FileReader reader = new FileReader(fileOpened);
            BufferedReader buffReader = new BufferedReader(reader);
            String line;
            if ((line = buffReader.readLine()) != null && line.length() > 0) {
                return line.trim();
            }
        } catch (IOException e){
            System.out.println("Could not open file.");
        }
        return "0";
    }

    public void writeConfigToFile(String name, String config) {
        name = name.toLowerCase();
        String fileName = this.storageFolderPath + File.separator + name + ".info";
        File fileOpened = new File(fileName);
        try {
            FileWriter writer = new FileWriter(fileOpened);
            writer.write(config);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not open file.");
        }
    }


    public void writeTableToFile(Table table) {
        String fileName = this.storageFolderPath + File.separator + table.getName() + ".tab";
        File fileOpened = new File(fileName);
        try {
            FileWriter writer = new FileWriter(fileOpened);
            table.writeToFile(writer);
        } catch (IOException e) {
            System.out.println("Could not open file.");
        }
    }
}
