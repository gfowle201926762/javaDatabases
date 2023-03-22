package edu.uob;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Table {
    private String name;
    private ArrayList<Field> fields;
    private ArrayList<Row> rows;

    public Table(String initName) {
        this.name = initName;
        fields = new ArrayList<Field>();
        rows = new ArrayList<Row>();
    }

    public String getName() {
        return this.name;
    }

    public int getNumberOfFields() {
        return fields.size();
    }

    public int getNumberOfRows() {
        return rows.size();
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public ArrayList<String> getRow(int index) {
        ArrayList<String> rowAsStrings = new ArrayList<>();
        for (int i = 0; i < getNumberOfFields(); i++) {
            rowAsStrings.add(rows.get(index).getValue(i));
        }
        return rowAsStrings;
    }

    public ArrayList<String> getFields() {
        ArrayList<String> fieldsAsStrings = new ArrayList<>();
        for (int i = 0; i < getNumberOfFields(); i++) {
            fieldsAsStrings.add(fields.get(i).getName());
        }
        return fieldsAsStrings;
    }

    public boolean existsField(String name) {
        for (Field field : this.fields) {
            if (field.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public String getRealField(String name) {
        for (int i = 0; i < this.fields.size(); i++) {
            if (fields.get(i).getName().equalsIgnoreCase(name)) {
                return fields.get(i).getName();
            }
        }
        return "";
    }

    public int getFieldIndex(String name) {
        for (int i = 0; i < this.fields.size(); i++) {
            if (fields.get(i).getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return 0;
    }

    public void changeRow(int rowIndex, int colIndex, String value) {
        this.rows.get(rowIndex).changeValue(colIndex, value);
    }

    private void removeField(int index) {
        this.fields.remove(index);
    }

    public void removeColumn(int index) {
        removeField(index);
        for (int i = 0; i < this.rows.size(); i++) {
            this.rows.set((i), rows.get(i).removeValue(index));
        }
    }

    public void removeColumnsAfter(int index) {
        int width = this.getNumberOfFields();
        fields.subList(index, fields.size()).clear();
        for (int i = 0; i < this.getNumberOfRows(); i++) {
            rows.get(i).getValues().subList(index, width).clear();
        }
    }

    public void copy(Table table, boolean id) {
        this.rows.clear();
        this.fields.clear();
        this.addFields(table.getFields(), id);
        for (int i = 0; i < table.getNumberOfRows(); i++) {
            this.addRows(table.getRows().get(i).getValues());
        }
    }

    public void fillRows() {
        for (int i = 0; i < rows.size(); i++) {
            rows.set(i, rows.get(i).addValueReturn());
        }
    }

    public void addField(String newField) {
        Field field = new Field(newField);
        this.fields.add(field);
    }


    public void addFields(ArrayList<String> newFields, boolean id) {
        if (id && (newFields.size() == 0 || !newFields.get(0).equals("id"))) {
            addField("id");
        }
        for (int i = 0; i < newFields.size(); i++) {
            Field field = new Field(newFields.get(i));
            this.fields.add(field);
        }
    }

    public void changeField(int index, String value) {
        fields.set(index, new Field(value));
    }

    private int getNextID() {
        int maxID = 0;
        for (int i = 0; i < this.rows.size(); i++) {
            int id = Integer.parseInt(rows.get(i).getValue(0));
            if (id > maxID) {
                maxID = id;
            }
        }
        return maxID + 1;
    }

    private int getNextConfigID(FileReading fileReading) {
        String config = fileReading.readConfig(this.name);
        return Integer.parseInt(config) + 1;
    }



    public void addRowsFile(ArrayList<String> newRowValues, FileReading fileReading) {
        Row row = new Row();
        int rowIndex = 0;
        if (this.getNumberOfFields() > newRowValues.size()) {
            row.addValue();
            row.changeValue(0, Integer.toString(getNextConfigID(fileReading)));
            rowIndex += 1;
        }
        for (int i = 0; i < newRowValues.size(); i++) {
            row.addValue();
            row.changeValue(rowIndex, newRowValues.get(i));
            rowIndex += 1;
        }
        this.rows.add(row);
    }



    public void addRows(ArrayList<String> newRowValues) {
        Row row = new Row();
        int rowIndex = 0;
        if (this.getNumberOfFields() > newRowValues.size()) {
            row.addValue();
            row.changeValue(0, Integer.toString(getNextID()));
            rowIndex += 1;
        }
        for (int i = 0; i < newRowValues.size(); i++) {
            row.addValue();
            row.changeValue(rowIndex, newRowValues.get(i));
            rowIndex += 1;
        }
        this.rows.add(row);
    }

    public void removeRows(int index) {
        rows.remove(index);
    }


    public void writeToFile(FileWriter writer) {
        try {
            for (int i = 0; i < this.fields.size(); i++) {
                writer.write(this.fields.get(i).getName());
                if (i < this.fields.size() - 1) {
                    writer.write("\t");
                }
            }
            writer.write("\n");
            for (int i = 0; i < this.rows.size(); i++) {
                for (int j = 0; j < this.fields.size(); j++) {
                    writer.write(this.rows.get(i).getValue(j));
                    if (j < this.fields.size() - 1) {
                        writer.write("\t");
                    }
                }
                writer.write("\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            System.out.println("Unable to write to a file.");
        }
    }

    public void printTable() {
        for (int i = 0; i < this.fields.size(); i++) {
            System.out.print(this.fields.get(i).getName());
            if (i < this.fields.size() - 1) {
                System.out.print("\t");
            }
        }
        System.out.print("\n");
        for (int i = 0; i < this.rows.size(); i++) {
            for (int j = 0; j < this.fields.size(); j++) {
                System.out.print(this.rows.get(i).getValue(j));
                if (j < this.fields.size() - 1) {
                    System.out.print("\t");
                }
            }
            System.out.print("\n");
        }
        System.out.print("\n");
    }

    public String writeToString(String msg) {
        msg = msg + "\n";
        for (int i = 0; i < this.fields.size(); i++) {
            msg = msg + this.fields.get(i).getName();
            if (i < this.fields.size() - 1) {
                msg = msg + "\t";
            }
        }
        msg = msg + "\n";
        for (int i = 0; i < this.rows.size(); i++) {
            for (int j = 0; j < this.fields.size(); j++) {
                msg = msg + this.rows.get(i).getValue(j);
                if (j < this.fields.size() - 1) {
                    msg = msg + "\t";
                }
            }
            msg = msg + "\n";
        }
        return msg;
    }
}
