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
    
    JBorg Borg = new JBorg(1,10);
    boolean loaded =Borg.loadWords(markovFile);
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        ArrayList<String> rawLogs = getLogs(getLogList());
        
        
        
        System.out.printf("Parsed %d lines\n", rawLogs.size());
    }
    public ArrayList<String> getBotList() throws FileNotFoundException{
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
    public boolean isBot(String nick) throws FileNotFoundException {
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
}
