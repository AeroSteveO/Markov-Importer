/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package markovimporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jborg.JBorg;
import org.w3c.dom.Element;

/**
 *
 * @author Stephen
 * Imports Xchat IRC logs into jborg, a java port of seeborg
 * 
 */
public class MarkovImporter {
    static ArrayList<String> botlist = null;
    static JBorg Borg = new JBorg(1,10);
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
//        ArrayList<String> rawLogs = getLogs(getLogList());
//        ArrayList<String> removedStamps = removeTimeStamp(rawLogs);
//        ArrayList<String> removedNicks = parseBadLines(removedStamps);
        
        
        ArrayList<String> parsedLogs = parseBadLines(removeTimeStamp(getLogs(getLogList())));//The heart of the parsing
        
        
        
        
        for (int i=0;i<parsedLogs.size();i++)
            Borg.learn(parsedLogs.get(i));
        
        
//        System.out.printf("Parsed %d lines\n", rawLogs.size());
//        for (int i=0;i<3;i++)
//            System.out.printf(rawLogs.get(i)+"\n");
//
//        System.out.printf("Parsed %d lines\n", removedStamps.size());
//        for (int i=0;i<3;i++)
//            System.out.printf(removedStamps.get(i)+"\n");
        
        System.out.printf("Parsed %d lines\n", parsedLogs.size()); //Printing out some stats on the imported lines
        for (int i=0;i<3;i++)
            System.out.printf(parsedLogs.get(i)+"\n");//Printing out some example lines
        
        File oddFile = new File("ImportedMarkov");
        Borg.saveWords(oddFile);
        
    }
    public static ArrayList<String> getBotList() throws FileNotFoundException{
        try{
            ArrayList<String> botlist = new ArrayList<String>();
            File fXmlFile = new File("SettingMarkov.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Element eElement = (Element) dBuilder.parse(fXmlFile).getElementsByTagName("ignorebots").item(0);
            for (int i=0;i<eElement.getElementsByTagName("bot").getLength();i++)
            {
                botlist.add(eElement.getElementsByTagName("bot").item(i).getTextContent());
            }
            return (botlist);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
    }
    public static boolean isBot(String nick) throws FileNotFoundException {
        boolean bot = false;
        if (botlist==null){
            botlist = getBotList();
        }
        int i=0;
        while(bot==false&&i<botlist.size()){
            if (nick.trim().equalsIgnoreCase(botlist.get(i))){
                bot=true;
            }
            i++;
        }
        return(bot);
    }
    public static ArrayList<String> getLogList() throws FileNotFoundException{
        try{
            ArrayList<String> IrcLogList = new ArrayList<String>();
            File fXmlFile = new File("SettingMarkov.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Element eElement = (Element) dBuilder.parse(fXmlFile).getElementsByTagName("importsettings").item(0);
            for (int i=0;i<eElement.getElementsByTagName("file").getLength();i++)
            {
                IrcLogList.add(eElement.getElementsByTagName("file").item(i).getTextContent()+".log");
            }
            return (IrcLogList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
    }
    public static ArrayList<String> getLogs(ArrayList<String> fileNameList) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
        String fileName = null;
        
        for(int i=0;i<fileNameList.size();i++){
            try{
                fileName = fileNameList.get(i);
                
                Scanner wordfile = new Scanner(new File(fileName));
                while (wordfile.hasNextLine()){
                    log.add(wordfile.nextLine());
                }
                wordfile.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                System.out.printf(fileName+"\n");
                return null;
            }
        }
        return (log);
    }
    public static ArrayList<String> removeTimeStamp(ArrayList<String> rawlog) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
        for (int i = 0;i<rawlog.size();i++)
            if (!rawlog.get(i).startsWith("****")){
                String[] line = rawlog.get(i).split(" ");
                if (line.length>4){
                    if (!line[3].startsWith("*")){
                        String formedLine = "";
                        for(int c = 3;c<line.length;c++){
                            formedLine = formedLine +" "+ line[c];
                        }
                        log.add(formedLine.trim());
                    }
                }
            }
        return(log);
    }
    public static ArrayList<String> parseBadLines(ArrayList<String> rawlog) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
        for (int i = 0;i<rawlog.size();i++){
            String[] line = rawlog.get(i).split(" ");
            String formedLine;
            if (line.length>2){
                if(Pattern.matches("\\[[0-9][0-9]:[0-9][0-9]:[0-9][0-9]\\]",line[1])){
                    formedLine = line[2];
                }
                else{
                    formedLine = line[1];
                }
                for(int c = 2;c<line.length;c++){
                    formedLine = formedLine +" "+ line[c];
                }
                if (line[0].length()>2){
                    String nick = line[0].substring(1,line[0].length()-2);
                    if (!isBot(nick)&&
                            !formedLine.toLowerCase().startsWith("tehfire")&&
                            !formedLine.toLowerCase().startsWith("tehreq")&&
                            !formedLine.startsWith("!")&&
                            !formedLine.startsWith(".")&&
                            !formedLine.toLowerCase().startsWith("zelda")&&
                            !formedLine.toLowerCase().startsWith("Wheatley, ")&&
                            !Pattern.matches("[a-zA-Z_0-9]+?", formedLine.toLowerCase())&&
                            !Pattern.matches("[a-zA-Z]{1}", formedLine)&&
                            !formedLine.toLowerCase().startsWith("The TV listings for ")&&
                            !Pattern.matches("[a-zA-Z_0-9]+\\++", formedLine.toLowerCase())&&
                            !formedLine.toLowerCase().startsWith("bit.ly url")){
                        log.add(formedLine.trim());
                    }
                }
            }
        }
        return(log);
    }
}