package MyApp;

/**
 * Created by dries on 9/05/2017.
 */
public class SimulationPoint
{
    public SimulationPoint(long timeStamp, int dataPoint)
    {
        this.timeStamp = timeStamp;
        this.dataPoint = dataPoint;
    }

    //in miliseconds
    public Long timeStamp;

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int dataPoint;

    public int getDataPoint() {
        return dataPoint;
    }

    public void setDataPoint(int dataPoint) {
        this.dataPoint = dataPoint;
    }

}
