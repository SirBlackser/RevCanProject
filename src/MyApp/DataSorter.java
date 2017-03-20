package MyApp;

import obj.Message;

import java.util.*;

/**
 * Created by dries on 12/03/2017.
 */
public class DataSorter {

    public DataSorter() {

    }

    //sort the messages from a file.
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

    //sort the messages from a incoming data stream.
    public Map<Integer, ArrayList<byte[]>> addFromDataStream (Message message, Map<Integer, ArrayList<byte[]>> currentData ) {
        Map<Integer, ArrayList<byte[]>> sortedData = currentData;
        ArrayList<byte[]> idMessages = new ArrayList<byte[]>();;
        if(sortedData.get(message.id) != null) {
            idMessages = sortedData.get(message.id);
        }
        idMessages.add(message.data);
        sortedData.put(message.id, idMessages);
        return sortedData;
    }
}
