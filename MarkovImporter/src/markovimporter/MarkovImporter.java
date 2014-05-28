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
 */
public class MarkovImporter {
    static ArrayList<String> botlist = null;
    int newLines = 0;
    String previousMessage = new String();
    int newLinesBeforeUpdate = 10;
    File markovFile = new File("MarkovWords.txt");
    
    static JBorg Borg = new JBorg(1,10);
    boolean loaded =Borg.loadWords(markovFile);
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
//        ArrayList<String> rawLogs = getLogs(getLogList());
//        ArrayList<String> removedStamps = removeTimeStamp(rawLogs);
//        ArrayList<String> removedNicks = removeNick(removedStamps);
        
        
        ArrayList<String> parsedLogs = removeNick(removeTimeStamp(getLogs(getLogList())));
        
        
        
        
        for (int i=0;i<parsedLogs.size();i++)
            Borg.learn(parsedLogs.get(i));
        
        
//        System.out.printf("Parsed %d lines\n", rawLogs.size());
//        for (int i=0;i<3;i++)
//            System.out.printf(rawLogs.get(i)+"\n");
//
//        System.out.printf("Parsed %d lines\n", removedStamps.size());
//        for (int i=0;i<3;i++)
//            System.out.printf(removedStamps.get(i)+"\n");
        
        System.out.printf("Parsed %d lines\n", parsedLogs.size());
        for (int i=0;i<3;i++)
            System.out.printf(parsedLogs.get(i)+"\n");
        
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
            //for (int i=0;i<botlist.size();i++){
            if (nick.equalsIgnoreCase(botlist.get(i))){
                bot=true;
            }
            i++;
        }
        return(bot);
    }
    public static ArrayList<String> getLogList() throws FileNotFoundException{
        try{
            ArrayList<String> bots = new ArrayList<String>();
            File fXmlFile = new File("SettingMarkov.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Element eElement = (Element) dBuilder.parse(fXmlFile).getElementsByTagName("importsettings").item(0);
            for (int i=0;i<eElement.getElementsByTagName("file").getLength();i++)
            {
                bots.add(eElement.getElementsByTagName("file").item(i).getTextContent()+".log");
            }
            return (bots);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return(null);
        }
    }
    public static ArrayList<String> getLogs(ArrayList<String> fileNameList) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
        String fileName = null;
        try{
            for(int i=0;i<fileNameList.size();i++){
                fileName = fileNameList.get(i);
                
                Scanner wordfile = new Scanner(new File(fileName));
                while (wordfile.hasNextLine()){
                    log.add(wordfile.nextLine());
                }
                wordfile.close();
            }
            return (log);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            System.out.printf(fileName+"\n");
            return null;
        }
    }
    public static ArrayList<String> removeTimeStamp(ArrayList<String> rawlog) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
//        String[] line = new String();
//        String formedLine;
        for (int i = 0;i<rawlog.size();i++)
            if (!rawlog.get(i).startsWith("****")){
                String[] line = rawlog.get(i).split(" ");
                if (line.length>4){
                    if (!line[3].startsWith("*")){
                        String formedLine = "";
                        for(int c = 3;c<line.length;c++){
                            formedLine = formedLine +" "+ line[c];
                        }
                        log.add(filterString(formedLine));
                        
                    }
                }
            }
        return(log);
    }
    private static String filterString(String inputLine) {
        inputLine = inputLine.replaceAll("\\! ", "!. ");
        inputLine = inputLine.replaceAll("\\? ", "?. ");
        inputLine = inputLine.replaceAll("[\r\n\"]","");
        inputLine = inputLine.replaceAll("\t", " ");
        inputLine = inputLine.trim();   //Remove leading/trailing whitespace
        return inputLine;
    }
    public static ArrayList<String> removeNick(ArrayList<String> rawlog) throws FileNotFoundException{
        ArrayList<String> log = new ArrayList<String>();
        for (int i = 0;i<rawlog.size();i++){
            String[] line = rawlog.get(i).split(" ");
            if (line.length>2){
                String formedLine = line[1];
                for(int c = 2;c<line.length;c++){
                    formedLine = formedLine +" "+ line[c];
                }
                if (line[0].length()>2){
                    String nick = line[0].substring(1,line[0].length()-2);
                    if (!isBot(nick)&&!formedLine.toLowerCase().startsWith("tehfire")&&!formedLine.toLowerCase().startsWith("tehreq")&&!formedLine.startsWith("!")&&!formedLine.startsWith(".")&&!formedLine.toLowerCase().startsWith("zelda")&&!Pattern.matches("[a-zA-Z_0-9]+?", formedLine.toLowerCase())&&!Pattern.matches("[a-zA-Z]{1}", formedLine))
                        log.add(filterString(formedLine));
                }
            }
        }
        return(log);
    }
}
