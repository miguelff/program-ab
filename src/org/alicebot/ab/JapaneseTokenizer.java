package org.alicebot.ab;

import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

//import org.atilika.kuromoji.Token;
//import org.atilika.kuromoji.Tokenizer;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokenize a Japanese language input by inserting spaces between words
 *
 * see http://atilika.org/
 */
public class JapaneseTokenizer {
    //static final Tokenizer tokenizer = Tokenizer.builder().build();
    static final Pattern tagPattern = Pattern.compile("(<.*>.*</.*>)|(<.*/>)");
    static Set<Character.UnicodeBlock> japaneseUnicodeBlocks = new HashSet<Character.UnicodeBlock>() {{
        add(Character.UnicodeBlock.HIRAGANA);
        add(Character.UnicodeBlock.KATAKANA);
        add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
    }};


    /**
     * Tokenize a fragment of the input that contains only text
     *
     * @param fragment   fragment of input containing only text and no XML tags
     * @return  tokenized fragment
     */
    public static String buildFragment(String fragment) {

        String result = "";
        for(Morpheme e : Tagger.parse(fragment)) {
            result += e.surface+" ";
        }
        return result.trim();
    }

    /**
     * Morphological analysis of an input sentence that contains an AIML pattern.
     *
     * @param sentence
     * @return       morphed sentence with one space between words, preserving XML markup and AIML $ operation
     */
    public static String morphSentence (String sentence) {
        if (!MagicBooleans.jp_morphological_analysis) return sentence;
        String result = "";
        Matcher matcher = tagPattern.matcher(sentence);
        while (matcher.find()) {
            int i = matcher.start();
            int j = matcher.end();
            String prefix, tag;
            if (i > 0) prefix = sentence.substring(0, i-1); else prefix = "";
            tag = sentence.substring(i, j);
            result += " "+buildFragment(prefix)+" "+tag;
            if (j < sentence.length()) sentence = sentence.substring(j, sentence.length()); else sentence = "";
        }
        result += " "+buildFragment(sentence);
        while (result.contains("$ ")) result = result.replace("$ ", "$");
        while (result.contains("  ")) result = result.replace("  "," ");
        return result.trim();
    }

}
