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
public class simulationCalc {
    public ArrayList<ArrayList<Float>> calcSimulation(HashMap<Integer, ArrayList<Message>> sortedData, ArrayList<SimulationPoint> simulationPoints)
    {
        HashMap<Integer, ArrayList<Message>> data = new HashMap<>();
        data = sortedData;
        ArrayList<SimulationPoint> simulation = simulationPoints;
        //ArrayList<Float> answer = new ArrayList<>();
        ArrayList<ArrayList<Float>> answers = new ArrayList<>();
        //long beginTime = 0;
        //long endTime = 0;
        int top = 5;
        int timeDifference = 10;
        Set<Integer> keys = data.keySet();
        Iterator<Integer> iterator = keys.iterator();

        //runs over all keys
        while(iterator.hasNext())
        {
            int key = iterator.next();

            ArrayList<Message> messages = data.get(key);
            ArrayList<Message> canData = new ArrayList<>();
            ArrayList<SimulationPoint> simulationToCompare = new ArrayList<>();
            if(simulation.size() > data.get(key).size()) {
                Iterator<Message> messageIterator = messages.iterator();
                Message current = messageIterator.next();
                for(int i = 0; i < simulation.size(); i++)
                {
                    if(simulation.get(i).getTimeStamp()> current.time-timeDifference && simulation.get(i).getTimeStamp()< current.time+timeDifference)
                    {
                        canData.add(current);
                        simulationToCompare.add(simulation.get(i));
                        if(messageIterator.hasNext()) {
                            current = messageIterator.next();
                        }
                    }
                }
            } else {
                Iterator<SimulationPoint> simulationIterator = simulation.iterator();
                SimulationPoint current = simulationIterator.next();
                for(int i = 0; i < messages.size(); i++)
                {
                    if(messages.get(i).time > current.getTimeStamp()-timeDifference && messages.get(i).time < current.getTimeStamp()+timeDifference)
                    {
                        canData.add(messages.get(i));
                        simulationToCompare.add(current);
                        if(simulationIterator.hasNext()) {
                            current = simulationIterator.next();
                        }
                    }
                }
            }
            //runs over different byte lengths
            for(int byteLength = 1; byteLength<5; byteLength++)
            {
                for(int currentByte = 0; currentByte < canData.get(0).data.length-(byteLength-1); currentByte++)
                {
                    int currentdifference = 0;
                    int sumOfarray = 0;
                    //runs over all messages.
                    for(int j = 0; j < canData.size(); j++)
                    {
                        if(byteLength == 1)
                        {
                            currentdifference += Math.pow((double)((Byte.toUnsignedInt(canData.get(j).data[currentByte]))-simulationToCompare.get(j).getDataPoint()),2);
                            sumOfarray += Byte.toUnsignedInt(canData.get(j).data[currentByte]);
                        } else {
                            byte bytesCan[] = new byte[4];
                            for(int k = 0; k < byteLength; k++)
                            {
                                bytesCan[k] = canData.get(j).data[currentByte+k];
                            }
                            //conver bytes to int, Can messages work with little endian, OBD with big endian
                            ByteBuffer bufferCan = ByteBuffer.wrap(bytesCan);
                            if(CanReader.getEndian().equals("little")) {
                                bufferCan.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                            }
                            //bufferOBD.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                            int tempCan = bufferCan.getInt();
                            currentdifference += Math.pow((double)(tempCan- simulationToCompare.get(j).getDataPoint()),2);
                            sumOfarray += tempCan;
                        }
                    }
                    float rms = 0;

                    //add answer to answers array and sorts it.
                    if(answers.size() < top && sumOfarray != 0 ) {
                        ArrayList<Float> answer = new ArrayList<>();
                        rms = (currentdifference/canData.size());
                        answer.add(rms);
                        answer.add((float)key);
                        answer.add((float)currentByte);
                        answer.add((float)byteLength);
                        answers.add(answer);
                        answers = sort(answers);
                    } else if(sumOfarray != 0){
                        ArrayList<Float> answer = new ArrayList<>();
                        answer.clear();
                        rms = (currentdifference/canData.size());
                        answer.add(rms);
                        answer.add((float)key);
                        answer.add((float)currentByte);
                        answer.add((float)byteLength);
                        if(rms < answers.get(top-1).get(0))
                        {
                            answers.remove(top-1);
                            answers.add(answer);
                            answers = sort(answers);
                        }
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
            if(answers.get(i).get(0) > answers.get(answers.size()-1).get(0) && help == 0) {
                sorted.add(answers.get(answers.size() - 1));
                help = 1;
            } else {
                sorted.add(answers.get(i-help));
            }
        }
        return sorted;
    }
}
