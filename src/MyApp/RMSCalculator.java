package MyApp;

import obj.Message;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dries on 21/04/2017.
 */
public class RMSCalculator {

    public RMSCalculator() { }

    public ArrayList<ArrayList<Float>> calculateRMS(HashMap<Integer, ArrayList<Message>> sortedData, ArrayList<Message> importedMessages)
    {
        HashMap<Integer, ArrayList<Message>> data = new HashMap<>();
        data = sortedData;
        ArrayList<Message> allMessages = importedMessages;
        //ArrayList<Float> answer = new ArrayList<>();
        ArrayList<ArrayList<Float>> answers = new ArrayList<>();
        //long beginTime = 0;
        //long endTime = 0;

        ArrayList<Message> knowData = new ArrayList<>();
        if(data.containsKey(2024))
        {
            knowData = data.get(2024);
            //beginTime = knowData.get(0).time;
            //endTime = knowData.get(knowData.size()-1).time;
        } else {
            return null;
        }

        Set<Integer> keys = data.keySet();
        //keys.remove(new Integer(2024));

        byte[] OBDdata =  knowData.get(0).data;
        int dataLength = Byte.toUnsignedInt(OBDdata[0])-2;
        Iterator<Integer> iterator = keys.iterator();
        float rms = 0;
        while(iterator.hasNext())
        {
            int key = iterator.next();
            if(key == 2024)
            {
                key = iterator.next();
            }
            ArrayList<Message> canData = new ArrayList<>();
            ArrayList<Message> obdData = new ArrayList<>();
            Message temp = new Message(0, new byte[]{0x0, 0x0, 0x0}, 3, 0, 0);
            Message check = new Message(0, new byte[]{0x0, 0x0, 0x0}, 3, 0, 0);
            for(Message m: allMessages)
            {
                if(m.id == 2024)
                {
                    temp = m;
                } else if(m.id == key && temp.id != 0 && check != temp) {
                    canData.add(m);
                    obdData.add(temp);
                    check = temp;
                }
            }

            //ArrayList<Message> messages = data.get(key);
            for(int i = 0; i < canData.get(0).data.length-dataLength; i++)
            {
                int currentdifference = 0;
                int sumOfarray = 0;
                for(int j = 0; j < canData.size(); j++)
                {
                    if(dataLength == 1)
                    {
                        currentdifference += Math.pow((double)((Byte.toUnsignedInt(canData.get(j).data[i]))-Byte.toUnsignedInt(obdData.get(j).data[3])),2);
                        sumOfarray += Byte.toUnsignedInt(canData.get(j).data[i]);
                    } else {
                        byte bytesCan[] = new byte[dataLength];
                        byte bytesOBD[] = new byte[dataLength];
                        for(int k = 0; k < dataLength; k++)
                        {
                            bytesCan[k] = canData.get(j).data[i+k];
                            bytesOBD[k] = obdData.get(j).data[3+k];
                        }
                        ByteBuffer bufferCan = ByteBuffer.wrap(bytesCan);
                        ByteBuffer bufferOBD = ByteBuffer.wrap(bytesOBD);
                        bufferCan.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                        //bufferOBD.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                        int tempCan = bufferCan.getShort();
                        int tempObd = bufferOBD.getShort();
                        currentdifference += Math.pow((double)(tempCan- tempObd),2);
                        sumOfarray += tempCan;
                    }
                }

                if(answers.size() < 3 && sumOfarray != 0 ) {
                    ArrayList<Float> answer = new ArrayList<>();
                    rms = (currentdifference/canData.size());
                    answer.add(rms);
                    answer.add((float)key);
                    answer.add((float)i);
                    answer.add((float)dataLength);
                    answers.add(answer);
                    answers = sort(answers);
                } else if(sumOfarray != 0){
                    ArrayList<Float> answer = new ArrayList<>();
                    answer.clear();
                    rms = (currentdifference/canData.size());
                    answer.add(rms);
                    answer.add((float)key);
                    answer.add((float)i);
                    answer.add((float)dataLength);
                    if(rms < answers.get(2).get(0))
                    {
                        answers.remove(2);
                        answers.add(answer);
                        answers = sort(answers);
                    }
                }
            }
        }
        return answers;
    }

    private ArrayList<ArrayList<Float>> sort(ArrayList<ArrayList<Float>> answers)
    {
        ArrayList<ArrayList<Float>> sorted = new ArrayList<>();
        if(answers.size() == 1)
        {
            sorted = answers;
        }
        else if(answers.size() == 2)
        {
            if(answers.get(0).get(0) > answers.get(1).get(0))
            {
                sorted.add(answers.get(1));
                sorted.add(answers.get(0));
            } else {
                sorted = answers;
            }
        } else if(answers.size() == 3) {
            if(answers.get(0).get(0) > answers.get(2).get(0))
            {
                sorted.add(answers.get(2));
                sorted.add(answers.get(0));
                sorted.add(answers.get(1));
            } else if(answers.get(1).get(0) > answers.get(2).get(0)) {
                sorted.add(answers.get(0));
                sorted.add(answers.get(2));
                sorted.add(answers.get(1));
            } else {
                sorted = answers;
            }
        }
        return sorted;
    }
}