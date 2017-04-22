package MyApp;

import obj.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dries on 21/04/2017.
 */
public class RMSCalculator {

    public RMSCalculator() { }

    public int calculateRMS(HashMap<Integer, ArrayList<Message>> sortedData)
    {
        int error = -1;
        HashMap<Integer, ArrayList<Message>> data = sortedData;
        Set<Integer> keys = data.keySet();
        keys.remove(new Integer(2024));

        ArrayList<Message> knowData = new ArrayList<>();
        if(data.containsKey(2024))
        {
            knowData = data.get(2024);
        } else {
            return error;
        }

        Iterator<Integer> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            int key = iterator.next();
            ArrayList<Message> messages = data.get(key);
            for(int i = 0; i < messages.get(0).data.length; i++)
            {

            }
        }
        return 0;
    }
}
