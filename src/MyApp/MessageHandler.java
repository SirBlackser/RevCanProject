package MyApp;

import obj.CanlibException;
import obj.Handle;
import obj.Message;

import java.util.ArrayList;

/**
 * Created by dries on 13/03/2017.
 */
public class MessageHandler implements Runnable {
    private boolean Active;
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

    public MessageHandler(boolean Active) {
        this.Active = Active;
        send = false;
        importantBytes = new ArrayList<>();
    }

    public void setHandle(Handle handle) {
        this.handle = handle;
    }

    public Handle getHandle() {
        return this.handle;
    }

    public void setActive(boolean Active) {
        this.Active = Active;
    }

    public boolean getActive() {
        return this.Active;
    }

    public void setSend(boolean send) {
        this.send = send;
    }

    public boolean getsend() {
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
        while (!Active) {
            try {
                while (handle.hasMessage()) {
                    Message m = handle.read();
                    CanReader.saveIncomingStream(m);
                }
            } catch (CanlibException e) {
                e.printStackTrace();
                System.err.println("An error occurred while reading messages");
                Active = true;
            }

            if (send == true) {
                try {
                    handle.write(new Message(msgId, msgData, msgDlc, 0));
                    handle.writeSync(50);
                } catch (CanlibException o) {
                    o.printStackTrace();
                    System.err.println("Could not write message to bus");
                }
            }

        }
    }
}
