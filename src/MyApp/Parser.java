package MyApp;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;

import obj.Message;

/**
 * Created by dries on 10/03/2017.
 */
public class Parser extends Observable implements Runnable {

    Iterator iterator;
    int i;
    int currentLocation;
    public boolean paused=true;
    ArrayList<Message> importedMessages = new ArrayList<Message>();
    boolean simulation;

    public Parser()
    {
        i = 0;
        currentLocation = i;
        simulation = false;
    }

    //restart the itterator
    public void resetIt(){
        iterator = importedMessages.iterator();
    }

    public void setSimulation(boolean sim) {simulation = sim;}

    public void resetI() { i=0; currentLocation = i;}

    public synchronized void syncLists() {this.importedMessages = CanReader.importedMessages;}

    //pauze printing the list
    public void playPause(){
        if(paused)
            paused=false;
        else {
            paused = true;
            //will stop the for loop, prints last message tho
            i = importedMessages.size()-1;
        }
    }

    //restart the iterator and start printing the list.
    @Override
    public synchronized void run() {
        resetI();
        while (true){
            try {
                if(!paused) {
                    for (i = currentLocation; i < importedMessages.size(); i++) {
                        if(simulation) {
                            Thread.sleep(1);
                        }
                        setChanged();
                        notifyObservers(importedMessages.get(i));
                    }
                    syncLists();
                    currentLocation = i;
                } else {
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //parse the given file.
    //expected example message: (1487076865.178310) can0 220#F10300000000D40F
    //                          timestamp           can  id#message
    public ArrayList<Message> parseDoc(File file)
    {
        importedMessages = new ArrayList<>();;
        try {
            FileReader input = new FileReader(file);
            BufferedReader bufRead = new BufferedReader(input);
            String myLine = null;

            while ( (myLine = bufRead.readLine()) != null)
            {
                Message message = parseLine(myLine);
                importedMessages.add(message);;
            }
        } catch(FileNotFoundException e) {
            System.err.println("error parsing file: "+ e.getMessage());
        } catch (IOException e) {
            System.err.println("error buffering file: "+ e.getMessage());
        }
        return importedMessages;
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];
        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }
        return b;
    }

    public Message parseLine(String myLine)
    {
        //voorbeeld bericht: "(1487086751.815959) can0 153#200000FF00FF607E"
        String[] split1 = myLine.split(" ");
        //after split:  split1[0] = "(1487086751.815959)"
        //              split1[1] = "can0"
        //              split1[2] = "153#200000FF00FF607E"
        StringBuilder s = new StringBuilder(split1[0]);
        s.deleteCharAt(0);
        s.deleteCharAt(s.length()-1);
        s.deleteCharAt(s.length()-7);
        //split1[0] = split1[0].replace("\\(", "");
        //split1[0] = split1[0].replace("\\)", "");
        split1[0] = s.toString();
        //split1[0] = split1[0].replace(".", ",");

        String[] split2 = split1[2].split("\\#");
        //              split2[0] = "153"
        //              split2[1] = "20 00 00 FF 00 FF 60 7E"

        //byte[] b = split2[1].getBytes();
        byte[] b = hexStringToByteArray(split2[1]);
        BigDecimal bd = new BigDecimal(split1[0]);
        long time = bd.longValue();

        Message message = new Message(Integer.parseInt(split2[0],16), b, b.length, 0, time);
        return message;
    }

}
