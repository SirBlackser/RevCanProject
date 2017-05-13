package MyApp;

import obj.Message;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by dries on 11/05/2017.
 */
public class SimParser {

    public ArrayList<SimulationPoint> parseSimDoc(File file, Long offset)
    {
        ArrayList<SimulationPoint> simulationPoints = new ArrayList<>();

        try {
            FileReader input = new FileReader(file);
            BufferedReader bufRead = new BufferedReader(input);
            String myLine;

            while ( (myLine = bufRead.readLine()) != null)
            {
                if(!myLine.contains("#")) {
                    myLine = myLine.trim().replaceAll(" +", " ");
                    String substrings[] = myLine.split(" ");
                    String subTime[] = substrings[0].split("e");
                    String subData[] = substrings[1].split("e");
                    float timestamp = Float.parseFloat(subTime[0])*1000;
                    double powerTime = Double.parseDouble(subTime[1]);
                    timestamp = (float)(timestamp * Math.pow(10,powerTime));
                    timestamp = Math.round(timestamp)+offset;

                    float datatemp = Float.parseFloat(subData[0]);
                    double powerData = Double.parseDouble(subData[1]);
                    datatemp = datatemp * (float) Math.pow(10,powerData);
                    int datapoint = Math.round(datatemp);

                    //Long timestamp = new Long(0);
                    //int datapoint = 2;
                    SimulationPoint temp = new SimulationPoint((long)timestamp, datapoint);
                    simulationPoints.add(temp);
                }
            }
        } catch(FileNotFoundException e) {
            System.err.println("error parsing file: "+ e.getMessage());
        } catch (IOException e) {
            System.err.println("error buffering file: "+ e.getMessage());
        }

        return simulationPoints;
    }
}
