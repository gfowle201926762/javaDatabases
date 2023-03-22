package edu.uob;

import java.util.ArrayList;

public class Row {
    private ArrayList<String> values;
    public Row() {
        values = new ArrayList<String>();
    }

    public Row removeValue(int index) {
        if (index < values.size()) {
            values.remove(index);
        }
        return this;
    }


    public Row addValueReturn() {
        values.add("NULL");
        return this;
    }

    public void addValue() {
        values.add("NULL");
    }

    public void changeValue(int index, String value) {
        if (index < values.size()) {
            this.values.set(index, value);
        }
    }

    public ArrayList<String> getValues() {
        return values;
    }

    public String getValue(int index) {
        return values.get(index);
    }
}
