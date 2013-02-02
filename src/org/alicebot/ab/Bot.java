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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.miguelff.alicebot.ab.ResourceProvider;
import org.miguelff.alicebot.ab.io.IOResource;

/**
 * Class representing the AIML bot
 */
public class Bot {
    public final Properties properties = new Properties();
    public final PreProcessor preProcessor;
    public final Graphmaster brain;
    public final Graphmaster inputGraph;
    public final Graphmaster learnfGraph;
    public final Graphmaster patternGraph;
    public final Graphmaster deletedGraph;
    public Graphmaster unfinishedGraph;
  //  public final ArrayList<Category> categories;
    public ArrayList<Category> suggestedCategories;
    public String name=MagicStrings.unknown_bot_name;
    public HashMap<String, AIMLSet> setMap = new HashMap<String, AIMLSet>();
    public HashMap<String, AIMLMap> mapMap = new HashMap<String, AIMLMap>();
   

    /**
     * Constructor (default action, default path, default bot name)
     */
    public Bot() {
        this(MagicStrings.default_bot);
    }

    /**
     * Constructor (default action, default path)
     * @param name
     */
    public Bot(String name) {
        this(name, "auto");
    }

    /**
     * Constructor
     *
     * @param name     name of bot
     * @param path     root path of Program AB
     * @param action   Program AB action
     */
    public Bot(String name, String action) {
        this.name = name;
        MagicStrings.setAllPaths(name);
        this.brain = new Graphmaster(this);
        this.inputGraph = new Graphmaster(this);
        this.learnfGraph = new Graphmaster(this);
        this.deletedGraph = new Graphmaster(this);
        this.patternGraph = new Graphmaster(this);
        this.unfinishedGraph = new Graphmaster(this);
      //  this.categories = new ArrayList<Category>();
        this.suggestedCategories = new ArrayList<Category>();
        preProcessor = new PreProcessor(this);
        addProperties();
        addAIMLSets();
        addAIMLMaps();
        AIMLSet number = new AIMLSet(MagicStrings.natural_number_set_name);
        setMap.put(MagicStrings.natural_number_set_name, number);
        AIMLMap successor = new AIMLMap(MagicStrings.map_successor);
        mapMap.put(MagicStrings.map_successor, successor);
        AIMLMap predecessor = new AIMLMap(MagicStrings.map_predecessor);
        mapMap.put(MagicStrings.map_predecessor, predecessor);
        //ResourceProvider.Log.info("setMap = "+setMap);
        Date aimlDate = new Date(ResourceProvider.IO.getResource(MagicStrings.aiml_path).getLastModified());
        Date aimlIFDate = new Date(ResourceProvider.IO.getResource(MagicStrings.aimlif_path).getLastModified());
        ResourceProvider.Log.info("AIML modified "+aimlDate+" AIMLIF modified "+aimlIFDate);
        readDeletedIFCategories();
        readUnfinishedIFCategories();
        if (action.equals("aiml2csv")) addCategoriesFromAIML();
        else if (action.equals("csv2aiml")) addCategoriesFromAIMLIF();
        else if (aimlDate.after(aimlIFDate)) {
            ResourceProvider.Log.info("AIML modified after AIMLIF");
            addCategoriesFromAIML();
            writeAIMLIFFiles();
        }
        else {
            addCategoriesFromAIMLIF();
            if (brain.getCategories().size()==0) {
                ResourceProvider.Log.info("No AIMLIF Files found.  Looking for AIML");
                addCategoriesFromAIML();
            }
        }
        ResourceProvider.Log.info("--> Bot "+name+" "+brain.getCategories().size()+" completed "+deletedGraph.getCategories().size()+" deleted "+unfinishedGraph.getCategories().size()+" unfinished");
    }

    /**
     * add an array list of categories with a specific file name
     *
     * @param file      name of AIML file
     * @param moreCategories    list of categories
     */
    void addMoreCategories (String file, ArrayList<Category> moreCategories) {
        if (file.contains(MagicStrings.deleted_aiml_file)) {
            for (Category c : moreCategories) {
                //ResourceProvider.Log.info("Delete "+c.getPattern());
                deletedGraph.addCategory(c);
            }
        } else if (file.contains(MagicStrings.unfinished_aiml_file)) {
            for (Category c : moreCategories) {
                //ResourceProvider.Log.info("Delete "+c.getPattern());
                if (brain.findNode(c) == null)
                unfinishedGraph.addCategory(c);
                else ResourceProvider.Log.info("unfinished "+c.inputThatTopic()+" found in brain");
            }
        } else if (file.contains(MagicStrings.learnf_aiml_file) ) {
            ResourceProvider.Log.info("Reading Learnf file");
            for (Category c : moreCategories) {
                brain.addCategory(c);
                learnfGraph.addCategory(c);
                patternGraph.addCategory(c);
            }
            //this.categories.addAll(moreCategories);
        } else {
            for (Category c : moreCategories) {
                //ResourceProvider.Log.info("Brain size="+brain.root.size());
                //brain.printgraph();
                brain.addCategory(c);
                patternGraph.addCategory(c);
                //brain.printgraph();
            }
            //this.categories.addAll(moreCategories);
        }
    }

    /**
     * Load all brain categories from AIML directory
     */
    void addCategoriesFromAIML() {
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            IOResource folder = ResourceProvider.IO.getResource(MagicStrings.aiml_path);
            if (folder.hasNested()) {
                List<IOResource> listOfFiles = folder.getNested();
                ResourceProvider.Log.info("Loading AIML files from "+MagicStrings.aiml_path);
                for (IOResource listOfFile : listOfFiles) {
                    if (! listOfFile.hasNested()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".aiml") || file.endsWith(".AIML")) {
                            ResourceProvider.Log.info(file);
                            try {
                                ArrayList<Category> moreCategories = AIMLProcessor.AIMLToCategories(MagicStrings.aiml_path, file);
                                addMoreCategories(file, moreCategories);
                            } catch (Exception iex) {
                                ResourceProvider.Log.info("Problem loading " + file);
                                iex.printStackTrace();
                            }
                        }
                    }
                }
            }
            else ResourceProvider.Log.info("addCategories: "+MagicStrings.aiml_path+" does not exist.");
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
        ResourceProvider.Log.info("Loaded " + brain.getCategories().size() + " categories in " + timer.elapsedTimeSecs() + " sec");
    }

    /**
     * load all brain categories from AIMLIF directory
     */
    void addCategoriesFromAIMLIF() {
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            IOResource folder = ResourceProvider.IO.getResource(MagicStrings.aimlif_path);
            if (folder.hasNested()) {
            	List<IOResource> listOfFiles = folder.getNested();
                ResourceProvider.Log.info("Loading AIML files from "+MagicStrings.aimlif_path);
                for (IOResource listOfFile : listOfFiles) {
                    if (! listOfFile.hasNested()) {
                        file = listOfFile.getName();
                        if (file.endsWith(MagicStrings.aimlif_file_suffix) || file.endsWith(MagicStrings.aimlif_file_suffix.toUpperCase())) {
                            //ResourceProvider.Log.info(file);
                            try {
                                ArrayList<Category> moreCategories = readIFCategories(MagicStrings.aimlif_path + "/" + file);
                                addMoreCategories(file, moreCategories);
                             //   MemStats.memStats();
                            } catch (Exception iex) {
                                ResourceProvider.Log.info("Problem loading " + file);
                                iex.printStackTrace();
                            }
                        }
                    }
                }
            }
            else ResourceProvider.Log.info("addCategories: "+MagicStrings.aimlif_path+" does not exist.");
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
        ResourceProvider.Log.info("Loaded " + brain.getCategories().size() + " categories in " + timer.elapsedTimeSecs() + " sec");
    }

    /**
     * read deleted categories from AIMLIF file
     */
    public void readDeletedIFCategories() {
        readCertainIFCategories(deletedGraph, MagicStrings.deleted_aiml_file);
    }

    /**
     * read unfinished categories from AIMLIF file
     */
    public void readUnfinishedIFCategories() {
        readCertainIFCategories(unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    /**
     * update unfinished categories removing any categories that have been finished
     */
    public void updateUnfinishedCategories () {
        ArrayList<Category> unfinished = unfinishedGraph.getCategories();
        unfinishedGraph = new Graphmaster(this);
        for (Category c : unfinished) {
            if (!brain.existsCategory(c)) unfinishedGraph.addCategory(c);
        }
    }

    /**
     * write all AIML and AIMLIF categories
     */
    public void writeQuit() {
        writeAIMLIFFiles();
        ResourceProvider.Log.info("Wrote AIMLIF Files");
        writeAIMLFiles();
        ResourceProvider.Log.info("Wrote AIML Files");
        writeDeletedIFCategories();
        updateUnfinishedCategories();
        writeUnfinishedIFCategories();

    }

    /**
     * read categories from specified AIMLIF file into specified graph
     *
     * @param graph   Graphmaster to store categories
     * @param fileName   file name of AIMLIF file
     */
    public void readCertainIFCategories(Graphmaster graph, String fileName) {
    	IOResource file = ResourceProvider.IO.getResource(MagicStrings.aimlif_path+"/"+fileName+MagicStrings.aimlif_file_suffix);        
        if (file.exists()) {
            try {
                ArrayList<Category> deletedCategories = readIFCategories(MagicStrings.aimlif_path+"/"+fileName+MagicStrings.aimlif_file_suffix);
                for (Category d : deletedCategories) graph.addCategory(d);
                ResourceProvider.Log.info("readCertainIFCategories "+graph.getCategories().size()+" categories from "+fileName+MagicStrings.aimlif_file_suffix);
            } catch (Exception iex) {
                ResourceProvider.Log.info("Problem loading " + fileName);
                iex.printStackTrace();
            }
        }
        else ResourceProvider.Log.info("No "+MagicStrings.deleted_aiml_file+MagicStrings.aimlif_file_suffix+" file found");
    }

    /**
     * write certain specified categories as AIMLIF files
     *
     * @param graph       the Graphmaster containing the categories to write
     * @param file        the destination AIMLIF file
     */
    public void writeCertainIFCategories(Graphmaster graph, String file) {
        if (MagicBooleans.trace_mode) ResourceProvider.Log.info("writeCertainIFCaegories "+file+" size= "+graph.getCategories().size());
        writeIFCategories(graph.getCategories(), file+MagicStrings.aimlif_file_suffix);
        IOResource dir = ResourceProvider.IO.getResource(MagicStrings.aimlif_path);
        dir.touch();
    }

    /**
     * write deleted categories to AIMLIF file
     */
    public void writeDeletedIFCategories() {
        writeCertainIFCategories(deletedGraph, MagicStrings.deleted_aiml_file);
    }

    /**
     * write learned categories to AIMLIF file
     */
    public void writeLearnfIFCategories() {
        writeCertainIFCategories(learnfGraph, MagicStrings.learnf_aiml_file);
    }

    /**
     * write unfinished categories to AIMLIF file
     */
    public void writeUnfinishedIFCategories() {
        writeCertainIFCategories(unfinishedGraph, MagicStrings.unfinished_aiml_file);
    }

    /**
     * write categories to AIMLIF file
     *
     * @param cats           array list of categories
     * @param filename       AIMLIF filename
     */
    public void writeIFCategories (ArrayList<Category> cats, String filename)  {
        //ResourceProvider.Log.info("writeIFCategories "+filename);
        BufferedWriter bw = null;
        IOResource existsPath = ResourceProvider.IO.getResource(MagicStrings.aimlif_path);
        if (existsPath.exists())
        try {
            //Construct the bw object
            bw = new BufferedWriter(new OutputStreamWriter(ResourceProvider.IO.outputFor(MagicStrings.aimlif_path+"/"+filename))) ;
            for (Category category : cats) {
                bw.write(Category.categoryToIF(category));
                bw.newLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the bw
            try {
                if (bw != null) {
                    bw.flush();
                    bw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Write all AIMLIF files from bot brain
     */
    public void writeAIMLIFFiles () {
        ResourceProvider.Log.info("writeAIMLIFFiles");
        HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
        if (deletedGraph.getCategories().size() > 0) writeDeletedIFCategories();
        ArrayList<Category> brainCategories = brain.getCategories();
        Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {
            try {
                BufferedWriter bw;
                String fileName = c.getFilename();
                if (fileMap.containsKey(fileName)) bw = fileMap.get(fileName);
                else {
                    bw = new BufferedWriter(new OutputStreamWriter(ResourceProvider.IO.outputFor(MagicStrings.aimlif_path+"/"+fileName+MagicStrings.aimlif_file_suffix)));
                    fileMap.put(fileName, bw);

                }
                bw.write(Category.categoryToIF(c));
                bw.newLine();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Set set = fileMap.keySet();
        for (Object aSet : set) {
            BufferedWriter bw = fileMap.get(aSet);
            //Close the bw
            try {
                if (bw != null) {
                    bw.flush();
                    bw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }

        }
        IOResource dir = ResourceProvider.IO.getResource(MagicStrings.aimlif_path);
        dir.touch();
    }

    /**
     * Write all AIML files.  Adds categories for BUILD and DEVELOPMENT ENVIRONMENT
     */
    public void writeAIMLFiles () {
        HashMap<String, BufferedWriter> fileMap = new HashMap<String, BufferedWriter>();
        Category b = new Category(0, "BUILD", "*", "*", new Date().toString(), "update.aiml");
        brain.addCategory(b);
        b = new Category(0, "DELEVLOPMENT ENVIRONMENT", "*", "*", MagicStrings.programNameVersion, "update.aiml");
        brain.addCategory(b);
        ArrayList<Category> brainCategories = brain.getCategories();
        Collections.sort(brainCategories, Category.CATEGORY_NUMBER_COMPARATOR);
        for (Category c : brainCategories) {

            if (!c.getFilename().equals(MagicStrings.null_aiml_file))
            try {
                //ResourceProvider.Log.info("Writing "+c.getCategoryNumber()+" "+c.inputThatTopic());
                BufferedWriter bw;
                String fileName = c.getFilename();
                if (fileMap.containsKey(fileName)) bw = fileMap.get(fileName);
                else {
                    String copyright = Utilities.getCopyright(this, fileName);
                    bw = new BufferedWriter(new OutputStreamWriter(ResourceProvider.IO.outputFor(MagicStrings.aiml_path+"/"+fileName)));
                    fileMap.put(fileName, bw);
                    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
                            "<aiml>\n");
                    bw.write(copyright);
                     //bw.newLine();
                }
                bw.write(Category.categoryToAIML(c)+"\n");
                //bw.newLine();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Set set = fileMap.keySet();
        for (Object aSet : set) {
            BufferedWriter bw = fileMap.get(aSet);
            //Close the bw
            try {
                if (bw != null) {
                    bw.write("</aiml>\n");
                    bw.flush();
                    bw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();

            }

        }
        IOResource dir =  ResourceProvider.IO.getResource(MagicStrings.aiml_path);
        dir.touch();
    }

    /**
     * load bot properties
     */
    void addProperties() {
        try {
            properties.getProperties(MagicStrings.config_path+"/properties.txt");
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
    }

    static int leafPatternCnt = 0;
    static int starPatternCnt = 0;

    /** find suggested patterns in a graph of inputs
     *
     */
    public void findPatterns() {
        findPatterns(inputGraph.root, "");
        ResourceProvider.Log.info(leafPatternCnt+ " Leaf Patterns "+starPatternCnt+" Star Patterns");
    }

    /** find patterns recursively
     *
     * @param node                      current graph node
     * @param partialPatternThatTopic   partial pattern path
     */
    void findPatterns(Nodemapper node, String partialPatternThatTopic) {
        if (NodemapperOperator.isLeaf(node)) {
            //ResourceProvider.Log.info("LEAF: "+node.category.getActivationCnt()+". "+partialPatternThatTopic);
            if (node.category.getActivationCnt() > MagicNumbers.node_activation_cnt) {
                //ResourceProvider.Log.info("LEAF: "+node.category.getActivationCnt()+". "+partialPatternThatTopic+" "+node.shortCut);    //Start writing to the output stream
                leafPatternCnt ++;
                try {
                    String categoryPatternThatTopic = "";
                    if (node.shortCut) {
                        //ResourceProvider.Log.info("Partial patternThatTopic = "+partialPatternThatTopic);
                        categoryPatternThatTopic = partialPatternThatTopic + " <THAT> * <TOPIC> *";
                    }
                    else categoryPatternThatTopic = partialPatternThatTopic;
                    Category c = new Category(0, categoryPatternThatTopic,  MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                    //if (brain.existsCategory(c)) ResourceProvider.Log.info(c.inputThatTopic()+" Exists");
                    //if (deleted.existsCategory(c)) ResourceProvider.Log.info(c.inputThatTopic()+ " Deleted");
                    if (!brain.existsCategory(c) && !deletedGraph.existsCategory(c) && !unfinishedGraph.existsCategory(c)) {
                        patternGraph.addCategory(c);
                        suggestedCategories.add(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if(NodemapperOperator.size(node) > MagicNumbers.node_size) {
            //ResourceProvider.Log.info("STAR: "+NodemapperOperator.size(node)+". "+partialPatternThatTopic+" * <that> * <topic> *");
            starPatternCnt ++;
            try {
                Category c = new Category(0, partialPatternThatTopic+" * <THAT> * <TOPIC> *",  MagicStrings.blank_template, MagicStrings.unknown_aiml_file);
                //if (brain.existsCategory(c)) ResourceProvider.Log.info(c.inputThatTopic()+" Exists");
                //if (deleted.existsCategory(c)) ResourceProvider.Log.info(c.inputThatTopic()+ " Deleted");
                if (!brain.existsCategory(c) && !deletedGraph.existsCategory(c) && !unfinishedGraph.existsCategory(c)) {
                    patternGraph.addCategory(c);
                    suggestedCategories.add(c);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (String key : NodemapperOperator.keySet(node)) {
            Nodemapper value = NodemapperOperator.get(node, key);
            findPatterns(value, partialPatternThatTopic + " " + key);
        }

    }

    /** classify inputs into matching categories
     *
     * @param filename    file containing sample normalized inputs
     */
    public void classifyInputs (String filename) {
        try{            
            BufferedReader br = new BufferedReader(new InputStreamReader(ResourceProvider.IO.inputFor(filename)));
            String strLine;
            //Read File Line By Line
            int count = 0;
            while ((strLine = br.readLine())!= null)   {
                // Print the content on the console
                //ResourceProvider.Log.info("Classifying "+strLine);
                if (strLine.startsWith("Human: ")) strLine = strLine.substring("Human: ".length(), strLine.length());
                Nodemapper match = patternGraph.match(strLine, "unknown", "unknown");
                match.category.incrementActivationCnt();
                count += 1;
            }
            //Close the input stream
            br.close();
        } catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /** read sample inputs from filename, turn them into Paths, and
     * add them to the graph.
     *
     * @param filename file containing sample inputs
     */
    public void graphInputs (String filename) {
        try{
            // Get the object
            BufferedReader br = new BufferedReader(new InputStreamReader(ResourceProvider.IO.inputFor(filename)));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                //strLine = preProcessor.normalize(strLine);
                Category c = new Category(0, strLine, "*", "*", "nothing", MagicStrings.unknown_aiml_file);
                Nodemapper node = inputGraph.findNode(c);
                if (node == null) {
                  inputGraph.addCategory(c);
                  c.incrementActivationCnt();
                }
                else node.category.incrementActivationCnt();
                //ResourceProvider.Log.info("Root branches="+g.root.size());
            }
            //Close the input stream
            br.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Excel sometimes adds mysterious formatting to CSV files.
     * This function tries to clean it up.
     *
     * @param line     line from AIMLIF file
     * @return   reformatted line
     */
    public String fixCSV (String line) {
        while (line.endsWith(";")) line = line.substring(0, line.length()-1);
        if (line.startsWith("\"")) line = line.substring(1, line.length());
        if (line.endsWith("\"")) line = line.substring(0, line.length()-1);
        line = line.replaceAll("\"\"", "\"");
        return line;
    }

    /**
     * read AIMLIF categories from a file into bot brain
     *
     * @param filename    name of AIMLIF file
     * @return   array list of categories read
     */
    public ArrayList<Category> readIFCategories (String filename) {
        ArrayList<Category> categories = new ArrayList<Category>();
        try{
            // Get the object
            BufferedReader br = new BufferedReader(new InputStreamReader(ResourceProvider.IO.inputFor(filename)));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null)   {
                try {
                    strLine = fixCSV(strLine);
                    Category c = Category.IFToCategory(strLine);
                    categories.add(c);
                } catch (Exception ex) {
                    ResourceProvider.Log.info("Invalid AIMLIF in "+filename+" line "+strLine);
                }
            }
            //Close the input stream
            br.close();
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        return categories;
    }

    /**
     * check Graphmaster for shadowed categories
     */
    public void shadowChecker () {
        shadowChecker(brain.root) ;
    }

    /** traverse graph and test all categories found in leaf nodes for shadows
     *
     * @param node
     */
    void shadowChecker(Nodemapper node) {
        if (NodemapperOperator.isLeaf(node)) {
            String input = node.category.getPattern().replace("*", "XXX").replace("_", "XXX");
            String that = node.category.getThat().replace("*", "XXX").replace("_", "XXX");
            String topic = node.category.getTopic().replace("*", "XXX").replace("_", "XXX");
            Nodemapper match = brain.match(input, that, topic);
            if (match != node) {
                ResourceProvider.Log.info("" + Graphmaster.inputThatTopic(input, that, topic));
                ResourceProvider.Log.info("MATCHED:     "+match.category.inputThatTopic());
                ResourceProvider.Log.info("SHOULD MATCH:"+node.category.inputThatTopic());
            }
        }
        else {
            for (String key : NodemapperOperator.keySet(node)) {
                shadowChecker(NodemapperOperator.get(node, key));
            }
        }
    }

    /**
     * Load all AIML Sets
     */
    void addAIMLSets() {
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            IOResource folder = ResourceProvider.IO.getResource(MagicStrings.sets_path);
            if (folder.hasNested()) {
            	List<IOResource> listOfFiles = folder.getNested();
                ResourceProvider.Log.info("Loading AIML Sets files from "+MagicStrings.sets_path);
                for (IOResource listOfFile : listOfFiles) {
                    if (! listOfFile.hasNested()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".txt") || file.endsWith(".TXT")) {
                            ResourceProvider.Log.info(file);
                            String setName = file.substring(0, file.length()-".txt".length());
                            ResourceProvider.Log.info("Read AIML Set "+setName);
                            AIMLSet aimlSet = new AIMLSet(setName);
                            aimlSet.readAIMLSet(this);
                            setMap.put(setName, aimlSet);
                        }
                    }
                }
            }
            else ResourceProvider.Log.info("addAIMLSets: "+MagicStrings.sets_path+" does not exist.");
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
    }

    /**
     * Load all AIML Maps
     */
    void addAIMLMaps() {
        Timer timer = new Timer();
        timer.start();
        try {
            // Directory path here
            String file;
            IOResource folder = ResourceProvider.IO.getResource(MagicStrings.maps_path);
            if (folder.exists()) {
            	List<IOResource> listOfFiles = folder.getNested();
                ResourceProvider.Log.info("Loading AIML Map files from "+MagicStrings.maps_path);
                for (IOResource listOfFile : listOfFiles) {
                    if (! listOfFile.hasNested()) {
                        file = listOfFile.getName();
                        if (file.endsWith(".txt") || file.endsWith(".TXT")) {
                            ResourceProvider.Log.info(file);
                            String mapName = file.substring(0, file.length()-".txt".length());
                            ResourceProvider.Log.info("Read AIML Map "+mapName);
                            AIMLMap aimlMap = new AIMLMap(mapName);
                            aimlMap.readAIMLMap(this);
                            mapMap.put(mapName, aimlMap);
                        }
                    }
                }
            }
            else ResourceProvider.Log.info("addCategories: "+MagicStrings.aiml_path+" does not exist.");
        } catch (Exception ex)  {
            ex.printStackTrace();
        }
    }

}
