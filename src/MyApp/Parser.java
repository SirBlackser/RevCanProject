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
    public boolean paused=true;
    ArrayList<Message> importedMessages = new ArrayList<Message>();

    public Parser()
    {

    }

    //restart the itterator
    public void resetIt(){
        iterator = importedMessages.iterator();
    }

    //pauze printing the list
    public void playPause(){
        if(paused)
            paused=false;
        else
            paused=true;
    }

    //restart the iterator and start printing the list.
    @Override
    public synchronized void run() {
        resetIt();
        while (true){
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!paused  && iterator.hasNext()) {
                setChanged();
                notifyObservers(iterator.next());
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
        //split1[0] = split1[0].replace("\\(", "");
        //split1[0] = split1[0].replace("\\)", "");
        split1[0] = s.toString();
        //split1[0] = split1[0].replace(".", ",");

        String[] split2 = split1[2].split("\\#");
        //              split2[0] = "153"
        //              split2[1] = "200000FF00FF607E"

        byte[] b = split2[1].getBytes();
        BigDecimal bd = new BigDecimal(split1[0]);
        long time = bd.longValue();

        Message message = new Message(Integer.parseInt(split2[0],16), b, myLine.length(), 0, time);
        return message;
    }

}
