package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;

public class Tokenizer {

    private final String[] supersetCharacters = {"==", "!=", "<=", ">="};
    private final String[] subsetCharacters = {"=", ">", "<"};
    private final String[] specialCharacters = {"(", ")", ",", ";", "==", "!=", "<=", ">="};
    private ArrayList<String> tokens = new ArrayList<String>();

    public Tokenizer() {

    }


    public ArrayList<String> tokenize(String query)
    {
        // Remove any whitespace at the beginning and end of the query
        query = query.trim();
        // Split the query on single quotes (to separate out query characters from string literals)
        String[] fragments = query.split("'");
        for (int i=0; i<fragments.length; i++) {
            // Every odd fragment is a string literal, so just append it without any alterations
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
                // If it's not a string literal, it must be query characters (which need further processing)
            else {
                // Tokenise the fragments into an array of strings
                ArrayList<String> tokenBatch = padSpecialChars(fragments[i]);

                tokenBatch = padSubsetChars(tokenBatch);

                // Then add these to the "result" array list (needs a bit of conversion)
                tokens.addAll(tokenBatch);

                //padSubsetChars(tokens);
            }
        }
        return tokens;
    }

    private ArrayList<String> padSubsetChars(ArrayList<String> tokens) {
        for (int t = 0; t < tokens.size(); t++) {
            boolean pad = true;
            for (String superset : supersetCharacters) {
                if (tokens.get(t).equals(superset)) {
                    pad = false;
                }
            }
            if (pad) {
                for (int i = 0; i < subsetCharacters.length; i++) {
                    tokens.set(t, tokens.get(t).replace(subsetCharacters[i], " " + subsetCharacters[i] + " "));
                }
            }
            while (tokens.get(t).contains("  ")) tokens.set(t, tokens.get(t).replaceAll("  ", " "));
            tokens.set(t, tokens.get(t).trim());
            String[] batch = tokens.get(t).split(" ");
            tokens.remove(t);
            tokens.addAll(t, Arrays.asList(batch));
        }
        return tokens;
    }

    private ArrayList<String> padSpecialChars(String input)
    {
        // Add in some extra padding spaces around the "special characters"
        // so we can be sure that they are separated by AT LEAST one space (possibly more)
        for(int i=0; i<specialCharacters.length ;i++) {
            input = input.replace(specialCharacters[i], " " + specialCharacters[i] + " ");
        }
        // Remove all double spaces (the previous replacements may had added some)
        // This is "blind" replacement - replacing if they exist, doing nothing if they don't
        while (input.contains("  ")) input = input.replaceAll("  ", " ");
        // Again, remove any whitespace from the beginning and end that might have been introduced
        input = input.trim();
        // Finally split on the space char (since there will now ALWAYS be a space between tokens)
        String[] batch = input.split(" ");

        ArrayList<String> tokenBatch = new ArrayList<>();
        tokenBatch.addAll(Arrays.asList(batch));

        return tokenBatch;
    }
}
