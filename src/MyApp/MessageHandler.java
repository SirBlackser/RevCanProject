package MyApp;

import obj.CanlibException;
import obj.Handle;
import obj.Message;

import java.util.ArrayList;

/**
 * Created by dries on 13/03/2017.
 */
public class MessageHandler implements Runnable {
    private boolean active;
    private Handle handle;
    private boolean send;
    private int msgId;
    private byte[] msgData;
    private int msgDlc;
    private ArrayList<ArrayList<Integer>> importantBytes;
    private int increment;
    private int upperLimit;
    private int lowerLimit;
    private int incrementSpeed;
    private int mode;
    private int counter;

    private CanReader canReader;

    public MessageHandler(boolean active, CanReader canReader) {
        this.active = active;
        send = false;
        importantBytes = new ArrayList<>();
        counter = 0;
        this.canReader = canReader;
    }

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public Handle getHandle() {
        return this.handle;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean getSend() {
        return this.send;
    }

    public void setMessage(int id, byte[] data) {
        msgId = id;
        msgData = data;
        msgDlc = msgData.length;
    }

    //split on , for seperate bytes
    //split with "-" for range
    public void setImportantBytes(String theBytes) {
        String[] split1 = theBytes.split(",");
        for(int i = 0; i < split1.length; i++)
        {
            if(split1[i].contains("-"))
            {
                String[] split2 = split1[i].split("-");
                ArrayList<Integer> bytes = new ArrayList<>();
                bytes.add(Integer.parseInt(split2[0]),16);
                bytes.add(Integer.parseInt(split2[1]));
                importantBytes.add(bytes);
            } else {
                ArrayList<Integer> bytes = new ArrayList<>();
                bytes.add(Integer.parseInt(split1[i]));
            }
        }
    }

    public void setIncrement(int increment, int upper, int lower, int speed)
    {
        this.increment = increment;
        upperLimit = upper;
        lowerLimit = lower;
        incrementSpeed = speed;
    }

    @Override
    public synchronized void run() {
        while (active) {
            try {
                if (handle.hasMessage()) {
                    Message m = handle.read();
                    canReader.saveIncomingStream(m);
                }
            } catch (CanlibException e) {
                e.printStackTrace();
                System.err.println("An error occurred while reading messages: " + e);
                active = false;
            }

            if(send && counter == 10) {
                try {
                    handle.write(new Message(msgId, msgData, msgDlc, 0));
                    handle.writeSync(50);
                    //log.append("sending\n");
                    counter = 0;
                } catch (CanlibException o) {
                    o.printStackTrace();
                    System.err.println("Could not write message to bus: " + o);
                }
            } else if(send) {
                if(counter > 11 ) {
                    counter = 0;
                    //log.append("counter: " + counter + "\n");
                } else {
                    counter++;
                    //log.append("counter: " + counter + "\n");
                }
            }
        }
    }
}
