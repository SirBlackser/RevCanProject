package MyApp;

import obj.Message;

import java.util.*;

/**
 * Created by dries on 12/03/2017.
 */
public class DataSorter {

    public DataSorter() {

    }

    //sort the messages from a file which has been loaded.
    public HashMap<Integer, ArrayList<Message>> SortParsedData(ArrayList<Message> messageList) {
        HashMap<Integer, ArrayList<Message>> sortedData = new HashMap<Integer, ArrayList<Message>>();
        Iterator iterator = messageList.iterator();
        while(iterator.hasNext())
        {
            Message message = (Message)iterator.next();
            sortedData = addFromDataStream(message, sortedData);
        }
        return sortedData;
    }

    //sort the messages from a incoming data stream. Also used for the sorting of a file.
    public HashMap<Integer, ArrayList<Message>> addFromDataStream (Message message, HashMap<Integer, ArrayList<Message>> currentData ) {
        HashMap<Integer, ArrayList<Message>> sortedData = currentData;
        ArrayList<Message> idMessages = new ArrayList<>();
        if(sortedData.get(message.id) != null) {
            idMessages = sortedData.get(message.id);
        }
        idMessages.add(message);
        sortedData.put(message.id, idMessages);
        return sortedData;
    }
}
