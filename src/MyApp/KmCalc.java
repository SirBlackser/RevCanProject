package MyApp;

import obj.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dries on 9/05/2017.
 */
public class KmCalc
{
    public ArrayList<Integer> calculateKm(HashMap<Integer, ArrayList<Message>> sortedData, Integer kilometers)
    {
        ArrayList<Integer> answer = new ArrayList<>();
        HashMap<Integer, ArrayList<Message>> data = new HashMap<>();
        data = sortedData;
        Set<Integer> keys = data.keySet();

        Iterator<Integer> iterator = keys.iterator();
        while(iterator.hasNext())
        {
            int key = iterator.next();
            Message message = data.get(key).get(0);
            //runs over differents byte lengths
            for(int byteLength = 1; byteLength<5; byteLength++)
            {
                //runs over all bytes
                for(int currentByte = 0; currentByte<message.data.length-(byteLength-1); currentByte++)
                {
                    double difference = -1;
                    if(byteLength == 1)
                    {
                        difference = Math.pow((Byte.toUnsignedInt(message.data[currentByte])-kilometers),2);
                    } else {
                        byte bytesCan[] = new byte[4];
                        for(int k = 0; k < byteLength; k++)
                        {
                            bytesCan[k] = message.data[currentByte+k];
                        }
                        ByteBuffer bufferCan = ByteBuffer.wrap(bytesCan);
                        if(CanReader.getEndian().equals("little")) {
                            bufferCan.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                        }
                        int tempCan = bufferCan.getInt();
                        difference = Math.pow((double)(tempCan-kilometers),2);
                    }

                    if(answer.isEmpty())
                    {
                        answer.add((int)difference);
                        answer.add(key);
                        answer.add(currentByte);
                        answer.add(byteLength);
                    } else if(difference<answer.get(0)) {
                        answer.clear();
                        answer.add((int)difference);
                        answer.add(key);
                        answer.add(currentByte);
                        answer.add(byteLength);
                    }
                }
            }
        }
        return answer;
    }
}
