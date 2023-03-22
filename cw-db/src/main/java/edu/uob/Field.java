package edu.uob;

public class Field {
    private String name;
    private boolean isPK;

    public Field(String initName) {
        this.name = initName;
    }

    public boolean getIsPK() {
        return this.isPK;
    }

    public void setIsPK(boolean newPK) {
        this.isPK = newPK;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }
}
