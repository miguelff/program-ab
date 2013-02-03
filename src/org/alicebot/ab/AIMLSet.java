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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miguelff.alicebot.ab.ResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;

/**
 * implements AIML Sets
 */
public class AIMLSet extends HashSet<String> {
    public String setName;
    int maxLength = 1; // there are no empty sets
    String host; // for external sets
    String botid; // for external sets
  
    /**
     * constructor
     * @param name    name of set
     */
    public AIMLSet (String name) {
        super();
        this.setName = name.toLowerCase();
        if (setName.equals(MagicStrings.natural_number_set_name))  maxLength = 1;
    }
    public boolean contains(String s) {
        if (setName.equals(MagicStrings.natural_number_set_name)) {
            Pattern numberPattern = Pattern.compile("[0-9]+");
            Matcher numberMatcher = numberPattern.matcher(s);
            Boolean isanumber = numberMatcher.matches();
            ResourceProvider.Log.info("AIMLSet isanumber '"+s+"' "+isanumber);
            return isanumber;
        }
        else return super.contains(s);
    }
    
    public  void writeAIMLSet () {
        ResourceProvider.Log.info("Writing AIML Set "+setName);
        try{
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(ResourceProvider.IO.outputFor(MagicStrings.sets_path+"/"+setName+".txt")));
            for (String p : this) {

                out.write(p.trim());
                out.newLine();
            }
            //Close the output stream
            out.close();
        }catch (Exception e){//Catch exception if any
           ResourceProvider.Log.error("Error: " + e.getMessage());
        }
    }
    public int readAIMLSetFromInputStream(InputStream in, Bot bot)  {
        String strLine;
        int cnt = 0;
        //Read File Line By Line
        try {
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((strLine = br.readLine()) != null  && strLine.length() > 0)   {
                cnt++;
                //strLine = bot.preProcessor.normalize(strLine).toUpperCase();
                // assume the set is pre-normalized for faster loading                
                if (!strLine.startsWith("external")) {
                    strLine = strLine.toUpperCase().trim();
                    String [] splitLine = strLine.split(" ");
                    int length = splitLine.length;
                    if (length > maxLength) maxLength = length;
                    //ResourceProvider.Log.info("readAIMLSetFromInputStream "+strLine);
                    add(strLine.trim());
                }
                /*Category c = new Category(0, "ISA"+setName.toUpperCase()+" "+strLine.toUpperCase(), "*", "*", "true", MagicStrings.null_aiml_file);
                bot.brain.addCategory(c);*/
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return cnt;
    }

    public void readAIMLSet (Bot bot) {
        ResourceProvider.Log.info("Reading AIML Set "+MagicStrings.sets_path+"/"+setName+".txt");
        try{
            // Open the file that is the first
            // command line parameter
        	IOResource file = ResourceProvider.IO.getResource(MagicStrings.sets_path+"/"+setName+".txt");
            if (file.exists()) {
                // Get the object
                readAIMLSetFromInputStream(file.input(), bot);                
            }
            else ResourceProvider.Log.info(MagicStrings.sets_path+"/"+setName+".txt not found");
        }catch (Exception e){//Catch exception if any
           ResourceProvider.Log.error("Error: " + e.getMessage());
        }

    }

}
