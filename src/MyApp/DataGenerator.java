package MyApp;

import java.util.Observable;

/**
 * Created by dries on 6/03/2017.
 */
public class DataGenerator extends Observable implements Runnable{
    public StringBuilder data;
    public boolean paused=true;

    public synchronized void playPause(){
        if(paused)
            paused=false;
        else
            paused=true;
    }

    public void generateData(){
        data = new StringBuilder();

        data.append(Math.random());

        setChanged();
        notifyObservers(data.toString());
    }

    @Override
    public void run() {
        while (true){
            try {
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(!paused)
                generateData();
        }
    }
}
