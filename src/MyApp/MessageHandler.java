package MyApp;

import obj.CanlibException;
import obj.Handle;
import obj.Message;

/**
 * Created by dries on 13/03/2017.
 */
public class MessageHandler implements Runnable{
    public MessageHandler()
    {

    }

    /*
    public void runHandler(Handle handle)
    {
        boolean finished = false;

        System.out.println("Channel 0 opened.");
        System.out.println("   ID    DLC DATA                      Timestamp");

        while (!finished)
        {
            try{
                while(handle.hasMessage()){
                    Message m = handle.read();
                    CanReader.saveIncomingStream(m);
                }
            }
            catch (CanlibException e){
                e.printStackTrace();
                System.err.println("An error occurred while reading messages");
                finished = true;
            }
        }
    }
    */
    private Handle handle;

    public void setHandle(Handle handle){this.handle = handle;}

    @Override
    public synchronized void run() {
        boolean finished = false;

        while (!finished)
        {
            try{
                while(handle.hasMessage()){
                    Message m = handle.read();
                    CanReader.saveIncomingStream(m);
                }
            }
            catch (CanlibException e){
                e.printStackTrace();
                System.err.println("An error occurred while reading messages");
                finished = true;
            }
        }
    }

}
