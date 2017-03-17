package MyApp;

import obj.CanlibException;
import obj.Handle;
import obj.Message;

/**
 * Created by dries on 13/03/2017.
 */
public class MessageHandler implements Runnable{
    private boolean finished;
    private Handle handle;

    public MessageHandler(boolean finished)
    {
        this.finished = finished;
    }

    public void setHandle(Handle handle){this.handle = handle;}
    public void setFinished(boolean finished) {this.finished = finished;}

    @Override
    public synchronized void run() {
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
