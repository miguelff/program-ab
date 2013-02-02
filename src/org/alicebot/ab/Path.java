package org.alicebot.ab;
import java.util.ArrayList;

import org.miguelff.alicebot.ab.ResourceProvider;

/**
 * Linked list representation of Pattern Path and Input Path
 */
public class Path extends ArrayList<String>{
    public String word;
    public Path next;
    public int length;

    /**
     * Constructor - class has public members
     */
    private Path() {
        next = null;
        word = null;
        length = 0;
    }

    /**
     * convert a sentence (a string consisting of words separated by single spaces) into a Path
     *
     * @param sentence        sentence to convert
     * @return                sentence in Path form
     */
    public static Path sentenceToPath(String sentence) {
        sentence = sentence.trim();
        return arrayToPath(sentence.split(" "));
    }

    /**
     * The inverse of sentenceToPath
     *
     * @param path           input path
     * @return               sentence
     */
    public static String pathToSentence (Path path) {
        if (path == null) return "";
        else return path.word+" "+pathToSentence(path.next);
    }

    /**
     * convert an array of strings to a Path
     *
     * @param array     array of strings
     * @return          sequence of strings as Path
     */
    private static Path arrayToPath(String[] array) {
        return arrayToPath(array, 0);
    }

    /**
     * recursively convert an array to a Path
     *
     * @param array  array of strings
     * @param index  array index
     * @return       Path form
     */
    private static Path arrayToPath(String[] array, int index)  {
        if (index >= array.length) return null;
        else {
            Path newPath = new Path();
            newPath.word = array[index];
            newPath.next = arrayToPath(array, index+1);
            if (newPath.next == null) newPath.length = 1;
            else newPath.length = newPath.next.length + 1;
            return newPath;
        }
    }

    /**
     * print a Path
     */
    public void print() {
        String result = "";
        for (Path p = this; p != null; p = p.next) {
            result += p.word+",";
        }
        if (result.endsWith(",")) result = result.substring(0, result.length()-1);
        ResourceProvider.Log.info(result);
    }

}
