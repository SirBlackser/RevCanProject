package MyApp;

import obj.Message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by dries on 1/05/2017.
 */
public class GrangerPrep {

    GrangerTest grangerTest;
    public GrangerPrep() {
        grangerTest = new GrangerTest();
    }

    public ArrayList<ArrayList<Float>> grangerTester(HashMap<Integer, ArrayList<Message>> sortedData, ArrayList<Message> importedMessages)
    {
        HashMap<Integer, ArrayList<Message>> data = new HashMap<>();
        data = sortedData;
        ArrayList<Message> allMessages = importedMessages;
        ArrayList<ArrayList<Float>> answers = new ArrayList<>();
        int top = 5;

        // Save all the messages from OBD (hex value (id) 7E8, int value 2024)
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
        // Iterate over all keys and all bytes in the messages.
        while(iterator.hasNext()) {
            int key = iterator.next();
            if (key == 2024) {
                key = iterator.next();
            }
            ArrayList<Message> canData = new ArrayList<>();
            ArrayList<Message> obdData = new ArrayList<>();
            Message temp = new Message(0, new byte[]{0x0, 0x0, 0x0}, 3, 0, 0);
            Message check = new Message(0, new byte[]{0x0, 0x0, 0x0}, 3, 0, 0);
            // Makes 2 arrays of the same length, with the timestamps as close as possible
            for (Message m : allMessages) {
                if (m.id == 2024) {
                    temp = m;
                } else if (m.id == key && temp.id != 0 && check != temp) {
                    canData.add(m);
                    obdData.add(temp);
                    check = temp;
                }
            }

            //ArrayList<Message> messages = data.get(key);
            //runs over all bytes.
            for(int i = 0; i < canData.get(0).data.length-dataLength; i++)
            {
                int currentdifference = 0;
                int sumOfarray = 0;
                double[] y = new double[canData.size()];
                double[] x = new double[canData.size()];
                //runs over all messages.
                for(int j = 0; j < canData.size(); j++)
                {
                    if(dataLength == 1)
                    {
                        //currentdifference += Math.pow((double)((Byte.toUnsignedInt(canData.get(j).data[i]))-Byte.toUnsignedInt(obdData.get(j).data[3])),2);
                        y[j] = (double)Byte.toUnsignedInt(canData.get(j).data[i]);
                        x[j] = (double)Byte.toUnsignedInt(obdData.get(j).data[3]);
                        sumOfarray += Byte.toUnsignedInt(canData.get(j).data[i]);
                    } else {
                        byte bytesCan[] = new byte[dataLength];
                        byte bytesOBD[] = new byte[dataLength];
                        for(int k = 0; k < dataLength; k++)
                        {
                            bytesCan[k] = canData.get(j).data[i+k];
                            bytesOBD[k] = obdData.get(j).data[3+k];
                        }
                        //conver bytes to int, Can messages work with little endian, OBD with big endian
                        ByteBuffer bufferCan = ByteBuffer.wrap(bytesCan);
                        ByteBuffer bufferOBD = ByteBuffer.wrap(bytesOBD);
                        bufferCan.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                        //bufferOBD.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                        int tempCan = bufferCan.getShort();
                        int tempObd = bufferOBD.getShort();
                        y[j] = (double)tempCan;
                        x[j] = (double) tempObd;
                        //currentdifference += Math.pow((double)(tempCan- tempObd),2);
                        sumOfarray += tempCan;
                    }
                }

                GrangerTestResult testResult = grangerTest.granger(y,x,1);

                //add answer to answers array and sorts it.
                if(answers.size() < top && sumOfarray != 0 ) {
                    ArrayList<Float> answer = new ArrayList<>();
                    //rms = (currentdifference/canData.size());
                    answer.add((float)testResult.getPValue());
                    answer.add((float)key);
                    answer.add((float)i);
                    answer.add((float)dataLength);
                    answers.add(answer);
                    answers = sort(answers);
                } else if(sumOfarray != 0){
                    ArrayList<Float> answer = new ArrayList<>();
                    answer.clear();
                    //rms = (currentdifference/canData.size());
                    answer.add((float)testResult.getPValue());
                    answer.add((float)key);
                    answer.add((float)i);
                    answer.add((float)dataLength);
                    if((float)testResult.getPValue() > answers.get(top-1).get(0))
                    {
                        answers.remove(top-1);
                        answers.add(answer);
                        answers = sort(answers);
                    }
                }
            }
        }
        return answers;
    }

    //sorts the answer array from best match to least best match.
    private ArrayList<ArrayList<Float>> sort(ArrayList<ArrayList<Float>> answers)
    {
        ArrayList<ArrayList<Float>> sorted = new ArrayList<>();
        int help = 0;
        for(int i=0; i < answers.size(); i++)
        {
            if(answers.get(i).get(0) < answers.get(answers.size()-1).get(0) && help == 0) {
                sorted.add(answers.get(answers.size() - 1));
                help = 1;
            } else {
                sorted.add(answers.get(i-help));
            }
        }
        return sorted;
    }
}