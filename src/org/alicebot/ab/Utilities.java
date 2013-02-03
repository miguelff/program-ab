package org.alicebot.ab;
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org

        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.

        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.

        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/
import org.alicebot.ab.utils.CalendarUtils;
import org.miguelff.alicebot.ab.ResourceProvider;

import java.io.*;
import java.util.HashSet;

public class Utilities {
    public static String tagTrim(String xmlExpression, String tagName) {
        String stag = "<"+tagName+">";
        String etag = "</"+tagName+">";
        if (xmlExpression.length() >= (stag+etag).length()) {
            xmlExpression = xmlExpression.substring(stag.length());
            xmlExpression = xmlExpression.substring(0, xmlExpression.length()-etag.length());
        }
        return xmlExpression;
    }
    public static HashSet<String> stringSet(String... strings)  {
        HashSet<String> set = new HashSet<String>();
        for (String s : strings) set.add(s);
        return set;
    }
   
    public static String getCopyright (Bot bot, String AIMLFilename) {
        String copyright = "";
        String year = CalendarUtils.year();
        String date = CalendarUtils.date();
        try {                
            copyright = MagicStrings.copyRight;                		                
            copyright = copyright.replace("[url]", bot.properties.get("url"));
            copyright = copyright.replace("[date]", date);
            copyright = copyright.replace("[YYYY]", year);
            copyright = copyright.replace("[version]", bot.properties.get("version"));
            copyright = copyright.replace("[botname]", bot.name.toUpperCase());
            copyright = copyright.replace("[filename]", AIMLFilename);
            copyright = copyright.replace("[botmaster]", bot.properties.get("botmaster"));
            copyright = copyright.replace("[organization]", bot.properties.get("organization"));
        } catch (Exception e){//Catch exception if any
           ResourceProvider.Log.error(e.getMessage());
        }
        //ResourceProvider.Log.info("Copyright: "+copyright);
        return copyright;
    }

    /**
     * Returns if a character is one of Chinese-Japanese-Korean characters.
     *
     * @param c
     *            the character to be tested
     * @return true if CJK, false otherwise
     */
    public static boolean isCharCJK(final char c) {
        if ((Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION)
                || (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS)) {
            return true;
        }
        return false;
    }



}
