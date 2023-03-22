package edu.uob;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.function.Supplier;

public class Parser {
    private boolean condition = false;
    private ArrayList<String> tokens;
    private ArrayList<Integer> conditionIndexes = new ArrayList<>();
    private int i;
    private String keyword = "";
    private String errorMessage = "";
    private int level = 1;
    private ArrayList<Boolean> levels = new ArrayList<>();
    private Interpreter interpreter;
    private ArrayList<String> currentList = new ArrayList<>();
    private boolean read = false;
    private boolean write = false;

    private final String[] keywords = {"USE", "CREATE", "DATABASE", "TABLE", "DROP", "ALTER", "INSERT", "INTO", "VALUES", "SELECT", "FROM", "WHERE", "UPDATE", "SET", "DELETE", "JOIN", "AND", "ON", "ADD", "TRUE", "FALSE", "NULL", "OR", "LIKE"};

    public Parser(ArrayList<String> tokenInput, FileReading fileReading) {
        interpreter = new Interpreter(fileReading);
        this.tokens = tokenInput;
        this.i = 0;
    }

    public void setParsing() {
        interpreter.setParsing();
    }

    public boolean getInterpretable() {
        return interpreter.getInterpretable();
    }

    public String getInterpretErrors() {
        return interpreter.getErrorMessage();
    }

    public boolean getWrite() {
        return write;
    }

    public boolean getRead() {
        return read;
    }

    public Table execute() {
        return interpreter.execute(write);
    }

    public int getI() {
        return i;
    }

    private void updateErrorMessage(String message) {
        if (errorMessage.length() == 0 && interpreter.getErrorMessage().length() == 0) {
            errorMessage = message;
        }
    }

    private void clearMessages() {
        errorMessage = "";
        interpreter.clearMessage();
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    private boolean incrementIndex() {
        if (i + 1 < tokens.size()) {
            i += 1;
            return true;
        }
        return false;
    }


    public boolean isCommand() {
        this.clearMessages();
        if (isCommandType()) {
            if (tokens.get(i).equals(";")) {
                return true;
            }
        }
        updateErrorMessage("Expected \";\" to terminate the query. Found \"" + tokens.get(i) + "\" instead.");
        return false;
    }


    public boolean isCommandType() {
        switch (tokens.get(i).toUpperCase()) {
            case "USE":
                return isUse();
            case "CREATE":
                write = true;
                return isCreate();
            case "DROP":
                //write = true;
                return isDrop();
            case "ALTER":
                write = true;
                return isAlter();
            case "INSERT":
                write = true;
                return isInsert();
            case "SELECT":
                condition = true;
                read = true;
                return isSelect();
            case "UPDATE":
                condition = true;
                write = true;
                return isUpdate();
            case "DELETE":
                condition = true;
                write = true;
                return isDelete();
            case "JOIN":
                read = true;
                return isJoin();
            default:
                updateErrorMessage("Expected \"USE\" or \"CREATE\" or \"DROP\" or \"ALTER\" or \"INSERT\" or \"SELECT\" or \"UPDATE\" or \"DELETE\" or \"JOIN\" keyword to begin the query.");
                return false;
        }
    }



    public boolean isWordThenMethod(String word, Supplier<Boolean> method, String msg) {
        if (tokens.get(i).equalsIgnoreCase(word)) {
            if (!incrementIndex()) return false;
            if (method.get()) {
                return incrementIndex();
            }
            updateErrorMessage(msg);
        }
        msg = "Expected \"" + word + "\" keyword";
        if (i>0) msg = msg + " after \"" + tokens.get(i-1) + "\"";
        msg = msg + ".";
        updateErrorMessage(msg);
        return false;
    }

    public boolean isUse() {
        String msg = "Expected <databaseName> after USE keyword.";
        if (isWordThenMethod("USE", this::isDatabaseName, msg)) {
            return interpreter.useDatabase(tokens.get(i-1));
        }
        return false;
    }

    public boolean isCreate() {
        if (tokens.get(i).equalsIgnoreCase("CREATE")) {
            if (!incrementIndex()) return false;
            String msg = "Expected <tableName> after CREATE TABLE keywords.";
            if (isWordThenMethod("TABLE", this::isTableName, msg)) {
                if (interpreter.createTable(tokens.get(i-1))) {
                    int savedI = i;
                    this.keyword = ")";
                    if (isBracketed(this::isAttributeList)) {
                        if (interpreter.writeFields(currentList)) {
                            return incrementIndex();
                        }
                        return false;
                    }
                    i = savedI;
                    return interpreter.writeFields(currentList);
                }
                return false;
            }
            errorMessage = "";
            msg = "Expected <databaseName> after CREATE DATABASE keywords.";
            if (isWordThenMethod("DATABASE", this::isDatabaseName, msg)) {
                write = false;
                return interpreter.createDatabase(tokens.get(i-1));
            }
            errorMessage = "Expected TABLE or DATABASE keyword after CREATE keyword.";
        }
        return false;
    }

    public boolean isDrop() {
        if (tokens.get(i).equalsIgnoreCase("DROP")) {
            if (!incrementIndex()) return false;
            String msg = "Expected <tableName> after DROP TABLE keywords.";
            if (isWordThenMethod("TABLE", this::isTableName, msg)) {
                return interpreter.dropTable(tokens.get(i-1));
                //return true;
            }
            errorMessage = "";
            msg = "Expected <databaseName> after DROP DATABASE keywords.";
            if (isWordThenMethod("DATABASE", this::isDatabaseName, msg)) {
                return interpreter.dropDatabase(tokens.get(i-1));
            }
            errorMessage = "Expected TABLE or DATABASE keyword after DROP keyword.";
        }
        return false;
    }

    public boolean isAlter() {
        if (tokens.get(i).equalsIgnoreCase("ALTER")) {
            if (!incrementIndex()) return false;
            String msg = "Expected <tableName> after ALTER TABLE keywords.";
            if (isWordThenMethod("TABLE", this::isTableName, msg)) {
                if (interpreter.useTable(tokens.get(i-1))) {
                    if (isAlterationType()) {
                        if (!incrementIndex()) return false;
                        if (isAttributeName()) {
                            if (interpreter.alterTable(tokens.get(i-1), tokens.get(i))) {
                                return incrementIndex();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isInsert() {
        if (tokens.get(i).equalsIgnoreCase("INSERT")) {
            if (!incrementIndex()) return false;
            String msg = "Expected <tableName> after INSERT INTO keywords.";
            if (isWordThenMethod("INTO", this::isTableName, msg)) {
                if (interpreter.useTable(tokens.get(i-1))) {
                    if (tokens.get(i).equalsIgnoreCase("VALUES")) {
                        if (!incrementIndex()) return false;
                        msg = "Expected a list of values after ( character.";
                        if (isWordThenMethod("(", this::isValueList, msg)) {
                            if (interpreter.writeRows(currentList)) {
                                return incrementIndex();
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isSelect() {
        String msg = "Expected \"*\" or a list of attributes after SELECT keyword.";
        if (isWordThenMethod("SELECT", this::isWildAttribList, msg)) {
            interpreter.useQuery(currentList);
            msg = "Expected <tableName> after FROM keyword.";
            if (isWordThenMethod("FROM", this::isTableName, msg)) {
                if (interpreter.useTable(tokens.get(i-1))) {
                    if (tokens.get(i).equalsIgnoreCase("WHERE")) {
                        if (!incrementIndex()) return false;
                        if (isAllConditions()) {
                            incrementIndex();
                            //return interpreter.applyQuery();
                            interpreter.specifyQuery();
                            return true;
                        }
                        updateErrorMessage("Expected a <Condition> after WHERE keyword.");
                        return false;
                    }
                    if (!tokens.get(i).equals(";"))
                        updateErrorMessage("Expected \"WHERE\" keyword or \";\" to terminate the query after [TableName] \"" + tokens.get(i - 1) + "\".");
                    interpreter.specifyQuery();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUpdate() {
        String msg = "Expected <tableName> after UPDATE keyword.";
        if (isWordThenMethod("UPDATE", this::isTableName, msg)) {
            if (interpreter.useTable(tokens.get(i-1))) {
                msg = "Expected a named value list after SET keyword.";
                if (isWordThenMethod("SET", this::isNameValueList, msg)) {
                    msg = "Expected a <Condition> after WHERE keyword.";
                    if (isWordThenMethod("WHERE", this::isAllConditions, msg)) {
                        return interpreter.specifyUpdate(currentList);
                    }
                }
            }
        }
        return false;
    }

    public boolean isDelete() {
        if (tokens.get(i).equalsIgnoreCase("DELETE")) {
            if (!incrementIndex()) return false;
            String msg = "Expected <tableName> after FROM keyword.";
            if (isWordThenMethod("FROM", this::isTableName, msg)) {
                if (interpreter.useTable(tokens.get(i-1))) {
                    msg = "Expected <condition> after WHERE keyword.";
                    if (isWordThenMethod("WHERE", this::isAllConditions, msg)) {
                        interpreter.specifyDelete();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isJoin() {
        String msg = "Expected <tableName> after JOIN keyword.";
        if (isWordThenMethod("JOIN", this::isTableName, msg)) {
            if (interpreter.useTable(tokens.get(i-1))) {
                msg = "Expected <tableName> after AND keyword.";
                if (isWordThenMethod("AND", this::isTableName, msg)) {
                    if (interpreter.useJoin(tokens.get(i-1))) {
                        msg = "Expected <attributeName> after ON keyword.";
                        if (isWordThenMethod("ON", this::isAttributeName, msg)) {
                            msg = "Expected <attributeName> after AND keyword.";
                            if (isWordThenMethod("AND", this::isAttributeName, msg)) {
                                return interpreter.executeJoin(tokens.get(i-3), tokens.get(i-1));
                            }
                        }
                    }
                }
            }
        }
        return false;
    }


    public boolean isBracketed(Supplier<Boolean> method) {
        if (tokens.get(i).equals("(")) {
            if (!conditionIndexes.contains(i)) {
                conditionIndexes.add(i);
                level += 1;
            }
            if (!incrementIndex()) return false;
            if (method.get()) {
                if (!incrementIndex()) return false;
                if (tokens.get(i).equals(")")) {
                    if (!conditionIndexes.contains(i)) {
                        conditionIndexes.add(i);
                        level -= 1;
                        if (condition) interpreter.condenseQuery();
                    }
                    //System.out.println("literally how are there two closing brackets??");
                    return true;
                }
                updateErrorMessage("Expected a closing bracket \")\".");
            }
        }
        return false;
    }


    // CONDITION //

    public boolean isComparator() {
        if (tokens.get(i).matches("^[><]$") || tokens.get(i).equalsIgnoreCase("LIKE")) {
            return true;
        }
        return tokens.get(i).matches("^[=!<>]=$");
    }

    public boolean isBoolOperator() {
        if (tokens.get(i).equalsIgnoreCase("AND") || tokens.get(i).equalsIgnoreCase("OR")) {
            interpreter.addOperator(tokens.get(i));
            return true;
        }
        return false;
    }







    public boolean isAllConditions() {
        boolean loop = true;
        int count = 0;
        while (loop) {
            loop = false;
            if (isCondition()) {
                interpreter.condenseQuery();
                int savedI = i;
                incrementIndex();
                count += 1;
                if (isBoolOperator()) {
                    incrementIndex();
                    count += 1;
                    loop = true;
                }
                else {
                    if (tokens.get(i).equals(")")) {
                        updateErrorMessage("Extraneous bracket \")\".");
                    }
                    i = savedI;
                }
            }
            if (i+1 < tokens.size() && tokens.get(i+1).equals(")")) {
                updateErrorMessage("Extraneous bracket \")\".");
            }
        }
        interpreter.updateIntermediary();
        return count % 2 == 1;
    }





    public boolean isCondition() {
        int savedI = i;
        if (isBracketed(this::isInnerNestedCondition)) {
            //System.out.println("found bracketed inner nested condition");
            return true;
        }
        i = savedI;
        if (isBracketed(this::isSimpleCondition)) {
            //System.out.println("found bracketed simple condition");
            return true;
        }
        i = savedI;
        if (isSimpleCondition()) {
            //System.out.println("found simple condition");
            return true;
        }
        return false;
    }


    public boolean isBoolThenCondition() {
        if (isBoolOperator()) {
            if (!incrementIndex()) return false;
            return isCondition();
        }
        return false;
    }


    private boolean needBoolOperator() {
        int bracketCount = 0;
        for (int index = i; index >= 0; index--) {
            if (tokens.get(index).equals(")")) bracketCount -= 1;
            if (tokens.get(index).equals("(")) bracketCount += 1;
        }
        if (bracketCount > 0) return true;
        return false;
    }

    public boolean isInnerNestedCondition() {
        if (isCondition()) {
            if (!incrementIndex()) return false;
            if (isBoolThenCondition()) {
                return isExtraConditions();
            }
            if (needBoolOperator()) {
                //updateErrorMessage("Expected a [BoolOperator] AND or OR instead of \"" + tokens.get(i) + "\" in a multi-conditioned statement.");
            }

        }
        return false;
    }

    public boolean isSimpleCondition() {
        if (isAttributeName()) {
            if (!incrementIndex()) return false;
            if (isComparator()) {
                if (!incrementIndex()) return false;
                if (isValue()) {
                    if (!conditionIndexes.contains(i)) {
                        conditionIndexes.add(i);
                        if (!interpreter.addCondition(tokens.get(i-2), tokens.get(i-1), tokens.get(i), level)){
                            return false;
                        }
                    }
                    return true;
                }
                updateErrorMessage("Expected a [Value] after [Comparator] \"" + tokens.get(i-1) + "\".");
            }
            updateErrorMessage("Expected a [Comparator] keyword after [AttributeName] \"" + tokens.get(i-1) + "\".");
        }
        return false;
    }

    public boolean isExtraConditions() {
        int savedI = i;
        boolean loop = true;
        int count = 0;
        while (loop) {
            loop = false;
            if (!incrementIndex()) return false;
            if (isBoolOperator()) {
                count += 1;
                if (!incrementIndex()) return false;
                if (isCondition()) {
                    count += 1;
                    loop = true;
                }
                else{
                    i = savedI;
                }
            }
        }
        if (count % 2 == 0) {
            i -= 1;
            return true;
        }
        i = savedI;

        return false;
    }





    // NAMES //

    public boolean isTableName() {
        if (isPlainText("[TableName]")) {
            return isDistinct(tokens.get(i), "[TableName]");
        }
        return false;
    }

    public boolean isAttributeName() {
        if (tokens.get(i).matches("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
            int index = tokens.get(i).indexOf(".");
            String tableName = tokens.get(i).substring(0, index);
            String attrName = tokens.get(i).substring(index + 1);
            if (isDistinct(tableName, "[TableName]") && isDistinct(attrName, "[AttributeName]")) {
                return true;
            }
        }
        if (isPlainText("[AttributeName]")) {
            return isDistinct(tokens.get(i), "[AttributeName]");
        }
        return false;
    }

    public boolean isDatabaseName() {
        if (isPlainText("[DatabaseName]")) {
            return isDistinct(tokens.get(i), "[DatabaseName]");
        }
        return false;
    }

    public boolean isDistinct(String name, String category) {
        for (String keyword: this.keywords) {
            if (name.toUpperCase().equals(keyword)) {
                updateErrorMessage("The " + category + " \"" + name + "\" matches the SQL keyword " + keyword + ".");
                return false;
            }
        }
        return true;
    }



    // LISTS //

    public boolean isList(String word, Supplier<Boolean> method) {
        int count = 0;
        while (!tokens.get(i).equalsIgnoreCase(word)) {
            if (count % 2 == 0 && method.get()) {
                currentList.add(tokens.get(i));
                count += 1;
            }
            else if (count % 2 != 0 && tokens.get(i).equals(",")) {
                count += 1;
            }
            else {
                return false;
            }
            if (!incrementIndex()) return false;
        }
        i -= 1;
        return count % 2 == 1;
    }

    public boolean isNameValueList() {
        return isList("WHERE", this::isNameValuePair);
    }

    public boolean isNameValuePair() {
        if (isAttributeName()) {
            currentList.add(tokens.get(i));
            if (!incrementIndex()) return false;
            if (tokens.get(i).equals("=")) {
                if (!incrementIndex()) return false;
                return isValue();
            }
        }
        return false;
    }

    public boolean isAttributeList() {
        return isList(this.keyword, this::isAttributeName);
    }

    public boolean isWildAttribList() {
        this.keyword = "FROM";
        return tokens.get(i).equals("*") || isAttributeList();
    }

    public boolean isValueList() {
        return isList(")", this::isValue);
    }




    // BASE CONDITIONS //

    public boolean isAlterationType() {
        if (tokens.get(i).equalsIgnoreCase("ADD") || tokens.get(i).equalsIgnoreCase("DROP")) {
            return true;
        }
        return false;
    }

    public boolean isValue() {
        if (isQuotedStringLiteral() || isBooleanLiteral() || isFloatLiteral() || isIntegerLiteral() || tokens.get(i).equalsIgnoreCase("NULL")) {
            return true;
        }
        return false;
    }

    public boolean isBooleanLiteral() {
        if (tokens.get(i).equalsIgnoreCase("TRUE") || tokens.get(i).equalsIgnoreCase("FALSE")) {
            return true;
        }
        return false;
    }

    public boolean isIntegerLiteral() {
        return tokens.get(i).matches("^[+-]?[0-9]+$");
    }

    public boolean isFloatLiteral() {
        return tokens.get(i).matches("^[+-]?[0-9]+\\.[0-9]+$");
    }

    public boolean isQuotedStringLiteral() {
        // space, letter, symbol, digit...
        String regex = "^'[a-zA-Z0-9\\s\\Q!#$%&()*+,-./:;>=<?@[\\]^_`{}~\\E]*'$";
        return tokens.get(i).matches(regex);
    }

    public boolean isPlainText(String name) {
        if (tokens.get(i).matches("^[a-zA-Z0-9]+$")) {
            return true;
        }
        updateErrorMessage("The " + name + " \"" + tokens.get(i) + "\" is not alphanumeric.");
        return false;
    }
}
