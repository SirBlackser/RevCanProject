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
public class SimulationCalc {
    public ArrayList<ArrayList<Float>> calcSimulation(HashMap<Integer, ArrayList<Message>> sortedData, ArrayList<SimulationPoint> simulationPoints)
    {
        HashMap<Integer, ArrayList<Message>> data = new HashMap<>();
        data = sortedData;
        ArrayList<SimulationPoint> simulation = simulationPoints;
        //ArrayList<Float> answer = new ArrayList<>();
        ArrayList<ArrayList<Float>> answers = new ArrayList<>();
        //long beginTime = 0;
        //long endTime = 0;
        int top = 10;
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
                int simLoc = 0;
                int SimMin = 0;
                int SimMax = 0;
                //SimulationPoint simulationPoint = simulationPoints.get(simLoc);
                Long previous = Math.abs(simulationPoints.get(simLoc).getTimeStamp() - messages.get(0).time);
                for(int i = 0; i < messages.size(); i++)
                {
                    //simulationPoint = simulationPoints.get(simLoc);
                    Long timeDiff = simulationPoints.get(simLoc).getTimeStamp() - messages.get(i).time;
                    if(timeDiff <= 0)
                    {
                        if(Math.abs(timeDiff) > previous && i !=0)
                        {
                            canData.add(messages.get(i-1));
                        } else
                        {
                            canData.add(messages.get(i));
                        }
                        if(canData.size() == 1)
                        {
                            SimMin = simulationPoints.get(simLoc).getDataPoint();
                            SimMax = simulationPoints.get(simLoc).getDataPoint();
                        } else {
                            if(simulationPoints.get(simLoc).getDataPoint() < SimMin) SimMin = simulationPoints.get(simLoc).getDataPoint();
                            if(simulationPoints.get(simLoc).getDataPoint() > SimMax) SimMax = simulationPoints.get(simLoc).getDataPoint();
                        }
                        simulationToCompare.add(simulationPoints.get(simLoc));
                        simLoc++;
                        if(simLoc >=simulationPoints.size()-1)
                        {
                            i = messages.size();
                        } else {
                            i--;
                        }
                    }
                    previous = timeDiff;
                }
            //}
            //runs over different byte lengths
            for(int byteLength = 1; byteLength<5; byteLength++)
            {
                for(int currentByte = 0; currentByte < canData.get(0).data.length-(byteLength-1); currentByte++)
                {
                    float currentDifference = 0;
                    int sumOfArray = 0;
                    int CanMin = 0;
                    int CanMax = 0;
                    ArrayList<Integer> dataInInt = new ArrayList<>();
                    //runs over all messages.
                    boolean same = true;
                    for(int j = 0; j < canData.size(); j++)
                    {
                        if(byteLength == 1)
                        {
                            if(!dataInInt.isEmpty() && Byte.toUnsignedInt(canData.get(j).data[currentByte]) != dataInInt.get(dataInInt.size()-1)) {
                                same = false;
                            }
                            dataInInt.add(Byte.toUnsignedInt(canData.get(j).data[currentByte]));
                            //currentdifference += Math.pow((double)((Byte.toUnsignedInt(canData.get(j).data[currentByte]))-simulationToCompare.get(j).getDataPoint()),2);
                            sumOfArray += Byte.toUnsignedInt(canData.get(j).data[currentByte]);
                        } else {
                            byte bytesCan[] = new byte[4];
                            for(int k = 0; k < byteLength; k++)
                            {
                                if(CanReader.getEndian().equals("little")) {
                                    bytesCan[k] = canData.get(j).data[currentByte + k];
                                } else {
                                    bytesCan[3 - ((byteLength - 1) - k)] = canData.get(j).data[currentByte + k];
                                }
                            }
                            //conver bytes to int, Can messages work with little endian, OBD with big endian
                            ByteBuffer bufferCan = ByteBuffer.wrap(bytesCan);
                            if(CanReader.getEndian().equals("little")) {
                                bufferCan.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                            }
                            //bufferOBD.order(ByteOrder.LITTLE_ENDIAN);  // if you want little-endian
                            int tempCan = bufferCan.getInt();
                            if(!dataInInt.isEmpty() && tempCan != dataInInt.get(dataInInt.size()-1)) {
                                same = false;
                            }
                            dataInInt.add(tempCan);
                            //currentdifference += Math.pow((double)(tempCan- simulationToCompare.get(j).getDataPoint()),2);
                            sumOfArray += tempCan;
                        }
                        if(dataInInt.size() == 1)
                        {
                            CanMin = dataInInt.get(dataInInt.size()-1);
                            CanMax = dataInInt.get(dataInInt.size()-1);
                        } else {
                            if(dataInInt.get(dataInInt.size()-1) < CanMin) CanMin = dataInInt.get(dataInInt.size()-1);
                            else if(dataInInt.get(dataInInt.size()-1) > CanMax) CanMax = dataInInt.get(dataInInt.size()-1);
                        }
                    }

                    float scaling = (float) (CanMax - CanMin) / (float) (SimMax - SimMin);
                    if(scaling < 1) {
                        scaling = 1;
                    }

                    for(int j =0; j <dataInInt.size(); j++)
                    {
                        currentDifference += Math.pow((double)((dataInInt.get(j))-((simulationToCompare.get(j).getDataPoint()*scaling)+CanMin)),2);
                    }

                    ArrayList<Float> answer = new ArrayList<>();
                    double difference = Math.sqrt(currentDifference);
                    float rms = ((float)difference/(float)canData.size());
                    answer.add(rms);
                    answer.add((float)key);
                    answer.add((float)currentByte);
                    answer.add((float)byteLength);
                    int skip = 0;
                    for(int i = 0; i < answers.size(); i++)
                    {
                        if(answers.get(i).get(1).equals(answer.get(1)) && answers.get(i).get(2).equals(answer.get(2)))
                        {
                            if(answer.get(0) < answers.get(i).get(0))
                            {
                                answers.remove(i);
                            } else {
                                skip = 1;
                            }
                        }
                    }
                    //add answer to answers array and sorts it.
                    if(answers.size() < top && sumOfArray != 0 && same != true) {
                        answers.add(answer);
                        answers = sort(answers);
                    } else if(sumOfArray != 0 && same != true && skip == 0){
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
