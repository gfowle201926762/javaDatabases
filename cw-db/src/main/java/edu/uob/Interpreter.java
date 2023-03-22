package edu.uob;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class Interpreter {
    private int localLevel;
    private int attrIndex;
    private String config;
    private String value;
    private Table intermediary;
    private Table joinTable;
    private Table original = new Table("original");
    private Table shiftTable = new Table("shiftTable");
    private Table saved = new Table("saved");
    private ArrayList<ArrayList<Table>> conditionTables = new ArrayList<>();
    private ArrayList<ArrayList<BooleanOperator>> operatorTables = new ArrayList<>();
    private ArrayList<Integer> conditionLevel = new ArrayList<>();
    private boolean parsing = false;
    private boolean isStrLit = false;
    private ArrayList<String> query = new ArrayList<>();
    private FileReading fileReading;
    private Boolean interpretable = false;
    private String errorMessage = "";
    public Interpreter(FileReading initFileReading) {
        fileReading = initFileReading;
    }

    public boolean useDatabase(String name) {
        if (parsing) return true;
        if (fileReading.doesDatabaseExist(name)) {
            fileReading.changePath(name);
            return true;
        }
        updateErrorMessage("The [DatabaseName] \"" + name + "\" does not exist.");
        return false;
    }

    public boolean createDatabase(String name) {
        if (parsing) return true;
        if (!fileReading.doesDatabaseExist(name)) {
            return fileReading.createFolder(name);
        }
        updateErrorMessage("The [DatabaseName] \"" + name + "\" already exists.");
        return false;
    }

    public void setParsing() {
        parsing = true;
    }
    public void clearMessage() {
        errorMessage = "";
    }

    private void updateErrorMessage(String msg) {
        if (errorMessage.length() == 0) {
            errorMessage = msg;
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean getInterpretable() {
        return interpretable;
    }

    private void shiftColumn(int from, int to) {
        String newField = intermediary.getFields().get(from);
        shiftTable.changeField(to, newField);
        for (int i = 0; i < intermediary.getNumberOfRows(); i++) {
            String value = intermediary.getRows().get(i).getValue(from);
            shiftTable.getRows().get(i).changeValue(to, value);
        }
    }

    public void specifyQuery() {
        if (parsing) return;
        shiftTable.copy(intermediary, true);
        for (int i = 0; i < query.size(); i++) {
            String attr = query.get(i);
            String[] attrCompound = query.get(i).split("\\.");
            if (attrCompound.length == 2) attr = attrCompound[1];
            if (!intermediary.getFields().get(i).equalsIgnoreCase(attr)) {
                shiftColumn(intermediary.getFieldIndex(attr), i);
            }
        }
        shiftTable.removeColumnsAfter(query.size());
        intermediary.copy(shiftTable, false);
    }

    public Table execute(boolean write) {
        if (write) fileReading.writeConfigToFile(intermediary.getName() ,config);
        return intermediary;
    }

    private boolean alterDrop(String attributeName) {
        if (!checkAttrExists(attributeName, intermediary)) return false;
        String attr = getAttribute(attributeName);
        if (attr.equalsIgnoreCase("id")) {
            updateErrorMessage("Cannot delete id column.");
            return false;
        }
        int index = intermediary.getFieldIndex(attr);
        intermediary.removeColumn(index);
        return true;
    }

    private boolean alterAdd(String attributeName) {
        String attr = getAttribute(attributeName);
        if (!checkTableMatch(attributeName, intermediary)) return false;
        if (checkAttrExists(attributeName, intermediary)) {
            updateErrorMessage("The [AttributeName] \"" + attr + "\" already exists in \"" + intermediary.getName() + "\".");
            return false;
        }
        intermediary.addField(attr);
        intermediary.fillRows();
        return true;
    }

    public boolean alterTable(String alterationType, String attributeName) {
        if (parsing) return true;
        if (alterationType.equalsIgnoreCase("DROP")) {
            return alterDrop(attributeName);
        }
        return alterAdd(attributeName);
    }

    public boolean createTable(String name) {
        if (parsing) return true;
        if (!fileReading.doesTableExist(name)) {
            if (!fileReading.getCurrentPath().equals(fileReading.getRootPath())) {
                intermediary = new Table(name.toLowerCase());
                config = "0";
                return true;
            }
            updateErrorMessage("You have not selected a [DatabaseName] to use yet.");
        }
        updateErrorMessage("The table \"" + name + "\" already exists.");
        return false;
    }

    public boolean dropDatabase(String name) {
        if (parsing) return true;
        if (fileReading.doesDatabaseExist(name)) {
            if (fileReading.deleteDatabase(name)) {
                if (fileReading.getCurrentPath().equals(fileReading.getRootPath() + File.separator + name)) {
                    fileReading.setPathRoot();
                }
                return true;
            }
            updateErrorMessage("Failed to drop [DatabaseName] \"" + name + "\".");
        }
        updateErrorMessage("Cannot drop [DatabaseName] \"" + name + "\" as it does not exist.");
        return false;
    }

    public boolean dropTable(String name) {
        if (parsing) return true;
        if (fileReading.doesTableExist(name)) {
            fileReading.deleteFile(name + ".info");
            fileReading.deleteFile(name + ".tab");
            return true;
        }
        updateErrorMessage("Cannot drop table \"" + name + "\" as it does not exist.");
        return false;
    }


    private boolean checkNameValueList(ArrayList<String> nameValueList) {
        if (parsing) return true;
        if (nameValueList.size() % 2 != 0) return false;
        for (int i = 0; i < nameValueList.size(); i++) {
            if (i % 2 == 0) {
                if (!intermediary.existsField(nameValueList.get(i))) {
                    updateErrorMessage("The [AttributeName] \"" + nameValueList.get(i) + "\" does not exist.");
                    return false;
                }
                if (nameValueList.get(i).equalsIgnoreCase("id")) {
                    updateErrorMessage("Cannot update the id column.");
                    return false;
                }
            }
        }
        return true;
    }


    public boolean specifyUpdate(ArrayList<String> nameValueList) {
        if (parsing) return true;
        if (!checkNameValueList(nameValueList)) return false;
        for (int row = 0; row < intermediary.getNumberOfRows(); row++) {
            for (int j = 0; j < original.getNumberOfRows(); j++) {
                boolean skip = false;
                for (int i = 0; i < nameValueList.size() / 2; i++) {
                    int fieldIndex = intermediary.getFieldIndex(nameValueList.get(i * 2));
                    String value = nameValueList.get((i * 2) + 1);
                    value = capitaliseKeyword(value);
                    value = value.replaceAll("^'|'$", "");
                    String originalID = original.getRows().get(j).getValue(0);
                    String intermediaryID = intermediary.getRows().get(row).getValue(0);
                    if (originalID.equals(intermediaryID)) {
                        original.changeRow(j, fieldIndex, value);
                        skip = true;
                    }
                }
                if (skip) j = original.getNumberOfRows();
            }
        }
        intermediary.copy(original, false);
        return true;
    }


    public void specifyDelete() {
        if (parsing) return;
        for (int i = 0; i < original.getNumberOfRows(); i++) {
            for (int j = 0; j < intermediary.getNumberOfRows(); j++) {
                String originalID = original.getRows().get(i).getValue(0);
                String intermediaryID = intermediary.getRows().get(j).getValue(0);
                if (originalID.equals(intermediaryID)) {
                    original.removeRows(i);
                }
            }
        }
        intermediary.copy(original, false);
    }

    public void useQuery(ArrayList<String> newQuery) {
        if (parsing) return;
        this.query = newQuery;
    }

    private Table union(Table table1, Table table2) {

        Table newTable = new Table("resultOfOR");
        newTable.copy(table1, true);
        for (int i = 0; i < table2.getNumberOfRows(); i++) {
            boolean add = true;
            for (int j = 0; j < table1.getNumberOfRows(); j++) {
                if (table1.getRows().get(j).getValue(0).equals(table2.getRows().get(i).getValue(0))) {
                    add = false;
                    break;
                }
            }
            if (add) newTable.addRows(table2.getRow(i));
        }
        return newTable;
    }

    private Table discriminate(Table table1, Table table2) {
        Table newTable = new Table("resultOfAND");
        newTable.addFields(table1.getFields(), true);
        for (int i = 0; i < table1.getNumberOfRows(); i++) {
            for (int j = 0; j < table2.getNumberOfRows(); j++) {
                if (table1.getRows().get(i).getValue(0).equals(table2.getRows().get(j).getValue(0))) {
                    newTable.addRows(table1.getRow(i));
                }
            }
        }
        return newTable;
    }

    public void updateIntermediary() {
        if (parsing) return;
        intermediary.copy(saved, true);
    }


    // This is called when we have reached the end (right-hand side) of a set of definable conditions.
    // e.g. (a==1 AND (b==2 AND (c==3 AND d==4))) AND (e==5)
    // _________________________________________^___________
    // at this point this function will collapse the readable material (what is to the left of that point).
    // This readable material will collapse into a conditionLevelTable at level 0 and index 0.
    // Therefore, it will be impossible to mix condition tables which are separated (such as a==1 and e==5).

    // always condense upon seeing a closed bracket.

    public void condenseQuery() {
        if (parsing) return;
        int levelIndex = conditionTables.size() - 1;

        // Initialise the table.
        Table table = conditionTables.get(levelIndex).get(0);

        // only perform the AND / OR operation if an operation has been saved at that level.
        if (operatorTables.size() > levelIndex) {
            table = calculateComparison(levelIndex, table);
        }

        // remove tables from the ArrayList which have used.
        cleanLevelTables(levelIndex, table);
    }

    private Table calculateComparison(int levelIndex, Table table) {
        ArrayList<BooleanOperator> booleanOperators = operatorTables.get(levelIndex);
        ArrayList<Table> tableLevel = conditionTables.get(levelIndex);
        for (int i = 0; i < booleanOperators.size(); i++) {
            if (i + 1 < tableLevel.size()) {
                if (booleanOperators.get(i) == BooleanOperator.OR) {
                    table = union(table, tableLevel.get(i + 1));
                }
                else if (booleanOperators.get(i) == BooleanOperator.AND) {
                    table = discriminate(table, tableLevel.get(i + 1));
                }
                conditionTables.get(levelIndex).set(0, table);
            }
        }
        // this is a safe operation because we know operatorTables.size() > levelIndex.
        operatorTables.remove(levelIndex);
        return table;
    }

    private void cleanLevelTables(int levelIndex, Table table) {
        if (levelIndex > 0) {
            cleanHighIndex(levelIndex, table);
        }
        else if (levelIndex == 0) {
            cleanZeroIndex(table);
        }
    }

    private boolean detectOperators (int index) {
        return (operatorTables.size() <= index || operatorTables.get(index).size() == 0);
    }

    private void cleanHighIndex(int levelIndex, Table table) {
        conditionTables.remove(levelIndex);
        conditionLevel.remove(levelIndex);
        conditionTables.get(levelIndex - 1).add(table);
        if (conditionTables.get(levelIndex - 1).size() == 2 && detectOperators(levelIndex - 1)) {
            // if there are no boolean operators left at the level below,
            // and the conditionTable level has 2 tables, they should be added.
            Table replacement = discriminate(table, conditionTables.get(levelIndex - 1).get(0));
            conditionTables.get(levelIndex - 1).set(0, replacement);
            conditionTables.get(levelIndex - 1).remove(1);
        }
    }

    private void cleanZeroIndex(Table table) {
        saved.copy(table, true);
        conditionTables.get(0).set(0, table);
        while (conditionTables.get(0).size() > 1) {
            conditionTables.get(0).remove(1);
            conditionLevel.set(0, (conditionLevel.get(0) - 1));
        }
    }

    public void addOperator(String operator) {
        if (parsing) return;
        for (int i = operatorTables.size(); i < conditionTables.size(); i++) {
            ArrayList<BooleanOperator> operatorList = new ArrayList<>();
            operatorTables.add(operatorList);
        }
        if (operator.equalsIgnoreCase("OR")) {
            operatorTables.get(operatorTables.size() - 1).add(BooleanOperator.OR);
        }
        else if (operator.equalsIgnoreCase("AND")) {
            operatorTables.get(operatorTables.size() - 1).add(BooleanOperator.AND);
        }
        else {
            System.out.println("WARNING: Unrecognised operator found.");
        }
    }


    private void addLevels(int level) {
        for (int i = conditionTables.size(); i < level; i++) {
            ArrayList<Table> tableLevel = new ArrayList<>();
            conditionTables.add(tableLevel);
            conditionLevel.add(0);
            Table table = new Table("conditionLevel" + i + ".0");
            table.copy(intermediary, true);
            conditionTables.get(i).add(table);
        }
    }

    private void extendLevel(int level) {
        conditionLevel.set((level - 1), (conditionLevel.get(level - 1) + 1));
        Table table = new Table("conditionLevel" + (level - 1));
        table.copy(intermediary, true);
        conditionTables.get(level - 1).add(table);
    }

    private int emptyTable(int i) {
        conditionTables.get(localLevel).get(conditionLevel.get(localLevel)).removeRows(i);
        return i - 1;
    }

    private String capitaliseKeyword(String newValue) {
        if (newValue.equalsIgnoreCase("TRUE")) return "TRUE";
        if (newValue.equalsIgnoreCase("FALSE")) return "FALSE";
        if (newValue.equalsIgnoreCase("NULL")) return "NULL";
        return newValue;
    }

    private boolean isSensibleCompare(String saved, String comparator, String value) {
        if (isKeyword(saved) || isKeyword(value)) {
            if (comparator.contains(">") || comparator.contains("<")) {
                return false;
            }
        }
        if (comparator.equalsIgnoreCase("LIKE") && !value.contains("'")) {
            return false;
        }
        if (!saved.matches("^[+-]?[0-9]*\\.?[0-9]+$") && !value.matches("^'.*'$")) {
            //System.out.println("first prevention, saved: " + saved + ", value: " + value);
            return !(comparator.contains(">") || comparator.contains("<"));
        }
        if (saved.matches("^[+-]?[0-9]*\\.?[0-9]+$") && value.matches("^'.*'$")) {
            //System.out.println("second prevention, saved: " + saved + ", value: " + value);
            return !(comparator.contains(">") || comparator.contains("<"));
        }
        return true;
    }

    public boolean addCondition(String attr, String comparator, String newValue, int level) {
        if (parsing) return true;
        if (!checkTableMatch(attr, intermediary)) return false;
        if (!checkAttrExists(attr, intermediary)) return false;
        newValue = capitaliseKeyword(newValue);
        if (intermediary.existsField(getAttribute(attr))) {
            attr = intermediary.getRealField(getAttribute(attr));
            if (conditionTables.size() == level) {
                // extend this level because it has already been seen.
                extendLevel(level);
            } else if (conditionTables.size() < level) {
                // add a new level because this level is new.
                addLevels(level);
            }
            // now perform the comparison operation.
            setValues(level, attr, newValue);
            Function<Integer, Integer> method = decideComparator(comparator);
            Table table = conditionTables.get(level - 1).get(conditionLevel.get(level - 1));
            //boolean empty = isEmptyTable(comparator, newValue);
            for (int i = 0; i < table.getNumberOfRows(); i++) {
                boolean isSensible = isSensibleCompare(table.getRows().get(i).getValue(attrIndex), comparator, newValue);
                if (isSensible) i = method.apply(i);
                else i = emptyTable(i);
            }
            return true;
        }
        updateErrorMessage("The [AttributeName] \"" + attr + "\" does not exist in [TableName] \"" + intermediary.getName() + "\".");
        return false;
    }


    private void setValues(int level, String attr, String newValue) {
        this.localLevel = level - 1;
        this.isStrLit = false;
        if (newValue.contains("'")) this.isStrLit = true;
        this.value = newValue.replaceAll("^'|'$", "");
        this.attrIndex = intermediary.getFields().indexOf(attr);;
    }

    private int removeRow(int i) {
        conditionTables.get(localLevel).get(conditionLevel.get(localLevel)).removeRows(i);
        return i - 1;
    }


    private int equalsComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);
        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) != Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (!table.getRows().get(i).getValue(attrIndex).equals(value)) {
            i = removeRow(i);
        }
        return i;
    }
    private int notEqualsComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);
        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) == Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (table.getRows().get(i).getValue(attrIndex).equals(value)) {
            i = removeRow(i);
        }
        return i;
    }

    private int likeComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        if (!table.getRows().get(i).getValue(attrIndex).contains(value)) {
            i = removeRow(i);
        }
        return i;
    }

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private int greaterComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);

        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) >= Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (saved.compareTo(value) >= 0) {
            i = removeRow(i);
        }
        return i;
    }

    private int greaterEqualComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);
        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) > Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (table.getRows().get(i).getValue(attrIndex).compareTo(value) > 0) {
            i = removeRow(i);
        }
        return i;
    }

    private int lessComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);
        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) <= Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (saved.compareTo(value) <= 0) {
            i = removeRow(i);
        }
        return i;
    }

    private int lessEqualComparator(int i) {
        Table table = conditionTables.get(localLevel).get(conditionLevel.get(localLevel));
        String saved = table.getRows().get(i).getValue(attrIndex);
        if (isDouble(saved) && isDouble(value) && !this.isStrLit) {
            if (Double.parseDouble(saved) < Double.parseDouble(value)) {
                i = removeRow(i);
            }
            return i;
        }
        if (table.getRows().get(i).getValue(attrIndex).compareTo(value) < 0) {
            i = removeRow(i);
        }
        return i;
    }

    private boolean isKeyword(String value) {
        return (value.equals("TRUE") || value.equals("FALSE") || value.equals("NULL"));
    }

    private Function<Integer, Integer> decideComparator(String operator) {
        switch (operator) {
            case "==":
                return this::equalsComparator;
            case "!=":
                return this::notEqualsComparator;
            case "<=":
                return this::greaterEqualComparator;
            case ">=":
                return this::lessEqualComparator;
            case "<":
                return this::greaterComparator;
            case ">":
                return this::lessComparator;
            default:
                return this::likeComparator;
        }
    }

    private boolean checkTableMatch(String attr, Table table) {
        String[] attrCompund = attr.split("\\.");
        if (attrCompund.length == 2) {
            if (!attrCompund[0].equalsIgnoreCase(table.getName())){
                updateErrorMessage("The [TableName] \"" + attrCompund[0] + "\" does not match \"" + table.getName() + "\".");
                return false;
            }
        }
        return true;
    }

    private boolean checkAttrExists(String attr, Table table) {
        if (!checkTableMatch(attr, table)) return false;
        attr = getAttribute(attr);
        for (int i = 0; i < table.getNumberOfFields(); i++) {
            if (table.getFields().get(i).equalsIgnoreCase(attr)) {
                return true;
            }
        }
        updateErrorMessage("The [AttributeName] \"" + attr + "\" does not exist for the [TableName] \"" + table.getName() + "\".");
        return false;
    }

    private void setJoinAttributes(Table table, int index1, int index2) {
        table.addField("id");
        for (int i = 1; i < intermediary.getNumberOfFields(); i++) {
            if (i != index1) {
                table.addField(intermediary.getName() + "." + intermediary.getFields().get(i));
            }
        }
        for (int i = 1; i < joinTable.getNumberOfFields(); i++) {
            if (i != index2) {
                table.addField(joinTable.getName() + "." + joinTable.getFields().get(i));
            }
        }
    }

    private void addJoinRow(Table joined, ArrayList<String> row1, ArrayList<String> row2, int index1, int index2) {
        if (index1 != 0) row1.remove(index1);
        if (index2 != 0) row2.remove(index2);
        row1.remove(0);
        row2.remove(0);
        row1.addAll(row2);
        joined.addRows(row1);
    }

    private String getAttribute(String attr) {
        String[] atttrCompund = attr.split("\\.");
        if (atttrCompund.length == 2) return atttrCompund[1];
        return attr;
    }

    public boolean executeJoin(String match1, String match2) {
        if (parsing) return true;
        if (!checkAttrExists(match1, intermediary) || !checkAttrExists(match2, joinTable)) {
            return false;
        }
        Table table = new Table("joinedTable");
        int index1 = intermediary.getFieldIndex(getAttribute(match1));
        int index2 = joinTable.getFieldIndex(getAttribute(match2));
        setJoinAttributes(table, index1, index2);
        for (int i = 0; i < intermediary.getNumberOfRows(); i++) {
            for (int j = 0; j < joinTable.getNumberOfRows(); j++) {
                String value1 = intermediary.getRow(i).get(index1);
                String value2 = joinTable.getRow(j).get(index2);
                if (value1.equals(value2)) {
                    addJoinRow(table, intermediary.getRow(i), joinTable.getRow(j), index1, index2);
                }
            }
        }
        intermediary.copy(table, false);
        return true;
    }

    public boolean useJoin(String name) {
        if (parsing) return true;
        if (fileReading.doesTableExist(name)) {
            joinTable = fileReading.readFileToTable(name);
            return true;
        }
        updateErrorMessage("The table \"" + name + "\" does not exist.");
        return false;
    }

    private boolean setQuery(String name) {
        if (this.query.size() == 0) {
            this.query = this.intermediary.getFields();
        }
        for (int i = 0; i < query.size(); i++) {
            String attr = query.get(i);
            String[] attrCompound = query.get(i).split("\\.");
            if (attrCompound.length == 2) {
                attr = attrCompound[1];
                if (!name.equalsIgnoreCase(attrCompound[0])) {
                    updateErrorMessage("The table \"" + name + "\" does not match the table \"" + attrCompound[0] + "\".");
                    return false;
                }
            }
            if (!intermediary.existsField(attr)) {
                updateErrorMessage("The table \"" + intermediary.getName() + "\" does not contain the column \"" + attr + "\".");
                return false;
            }
        }
        config = fileReading.readConfig(name);
        return true;
    }

    public boolean useTable(String name) {
        if (parsing) return true;
        if (fileReading.doesTableExist(name)) {
            intermediary = fileReading.readFileToTable(name);
            original.copy(intermediary, false);
            return setQuery(name.toLowerCase());
        }
        updateErrorMessage("The table \"" + name + "\" does not exist.");
        return false;
    }

    public boolean writeFields(ArrayList<String> currentList) {
        if (parsing) return true;
        for (int i = 0; i < currentList.size(); i++) {
            if (!checkTableMatch(currentList.get(i), intermediary)) return false;
            currentList.set(i, getAttribute(currentList.get(i)));
        }
        Set<String> set = new HashSet<String>(currentList);
        if (!currentList.contains("id") && set.size() == currentList.size()) {
            intermediary.addFields(currentList, true);
            return true;
        }
        updateErrorMessage("Tables must not have duplicate column names.");
        return false;
    }

    public boolean writeRows(ArrayList<String> currentList) {
        if (parsing) return true;
        if (currentList.size() == intermediary.getNumberOfFields() - 1) {
            for (int i = 0; i < currentList.size(); i++) {
                String newValue = capitaliseKeyword(currentList.get(i));
                currentList.set(i, newValue.replaceAll("^'|'$", ""));
            }
            intermediary.addRowsFile(currentList, fileReading);
            incrementConfig();
            return true;
        }
        updateErrorMessage("The table \"" + intermediary.getName() + "\" must take " + (intermediary.getNumberOfFields() - 1) + " values exactly.");
        return false;
    }


    private void incrementConfig() {
        int newMax = Integer.parseInt(config) + 1;
        config = Integer.toString(newMax);
    }
}
