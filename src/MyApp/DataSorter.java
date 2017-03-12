package MyApp;

import obj.Message;

import java.util.*;

/**
 * Created by dries on 12/03/2017.
 */
public class DataSorter {

    public DataSorter() {

    }

    public Map<Integer, ArrayList<byte[]>> SortParsedData(ArrayList<Message> messageList) {
        Map<Integer, ArrayList<byte[]>> sortedData = new HashMap<Integer, ArrayList<byte[]>>();
        Iterator iterator = messageList.iterator();
        while(iterator.hasNext())
        {
            Message message = (Message)iterator.next();
            sortedData = addFromDataStream(message, sortedData);
        }
        return sortedData;
    }

    public Map<Integer, ArrayList<byte[]>> addFromDataStream (Message message, Map<Integer, ArrayList<byte[]>> currentData ) {
        Map<Integer, ArrayList<byte[]>> data = currentData;
        if(data.get(message.id) == null)
        {
            ArrayList<byte[]> idMessages = new ArrayList<byte[]>();
            idMessages.add(message.data);
            data.put(message.id, idMessages);
        } else {
            ArrayList<byte[]> idMessages = data.get(message.id);
            idMessages.add(message.data);
            data.put(message.id, idMessages);
        }
        return data;
    }
}
