package MyApp;

import obj.Message;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

/**
 * Created by dries on 24/03/2017.
 */
public class GraphPanel extends JPanel implements Observer{

    /** Time series for total memory used. */
    /** Time series for free memory. */
    //private TimeSeries free;

    private TimeSeriesCollection dataset = new TimeSeriesCollection();

    HashMap<Integer,TimeSeries> currentGraphs;

    public GraphPanel(int maxAge){
        super(new BorderLayout());
        currentGraphs = new HashMap<Integer, TimeSeries>();

        // seconds old...
        //this.total = new TimeSeries("ID X", Millisecond.class);
        //this.total.setMaximumItemAge(maxAge);
        //this.free = new TimeSeries("ID Y", Millisecond.class);
        //this.free.setMaximumItemAge(maxAge);
        //dataset.addSeries(this.total);
        //dataset.addSeries(this.free);
        DateAxis domain = new DateAxis("Time");
        NumberAxis range = new NumberAxis("Byte data");
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.green);
        renderer.setStroke(new BasicStroke(3f, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL));
        XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);
        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        JFreeChart chart = new JFreeChart("Byte visualisation", new Font("SansSerif", Font.BOLD, 24), plot, true);
        chart.setBackgroundPaint(Color.white);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createLineBorder(Color.black)));
        add(chartPanel);

        //DataGenerator d = new DataGenerator(100);
        //d.start();

    }

    /**
     * Adds an observation to the ’total memory’ time series.
     *
    private void addTotalObservation(double y) {
        this.total.add(new Millisecond(), y);
    }
    /**
     * Adds an observation to the ’free memory’ time series.
     *
    private void addFreeObservation(double y) {
        this.free.add(new Millisecond(), y);
    }*/

    public void addObservation(Message message)
    {
        HashMap<Integer, ArrayList<Integer>> toDrawGraphs = CanReader.toDrawGraphs;
        if(toDrawGraphs.containsKey(message.id) && !currentGraphs.containsKey(message.id))
        {
            TimeSeries total = new TimeSeries(Integer.toHexString(message.id).toUpperCase(), Millisecond.class);
            //30 seconden
            //total.add(new Millisecond(), Byte.toUnsignedInt(message.data[0]));
            total.setMaximumItemAge(30000);
            total.setMaximumItemCount(20000);
            dataset.addSeries(total);
            currentGraphs.put(message.id, total);
        } else if(!toDrawGraphs.containsKey(message.id) && currentGraphs.containsKey(message.id)) {
            dataset.removeSeries(currentGraphs.get(message.id));
            currentGraphs.remove(message.id);
        } else if(currentGraphs.containsKey(message.id) && toDrawGraphs.size() > 0){
            ArrayList<Integer> BytesToDraw = toDrawGraphs.get(message.id);
            byte messageData[] = message.data;
            int data = 0;
            if(BytesToDraw.size() == 1)
            {
                data = Byte.toUnsignedInt(message.data[BytesToDraw.get(0)]);
            } else {
                String dataString = bytesToHex(messageData);
                String dataDraw = dataString.substring(BytesToDraw.get(0), BytesToDraw.get(1));
                data = Integer.parseInt(dataDraw,16);
            }
            currentGraphs.get(message.id).addOrUpdate(new Millisecond(), data);
        }


//        if(toDrawGraphs.containsKey(message.id))
//        {
//            ArrayList<Integer> bytes = toDrawGraphs.get(message.id);
//            String dataString = "";
//            byte[] data = message.data;
//            for(int i = 0; i < bytes.size(); i++)
//            {
//                int temp = data[bytes.get(i)];
//                dataString += Integer.toString(temp);
//            }
//            int output = Integer.parseInt(dataString);
//        }
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void update(Observable o, Object arg) {

    }

    /**
     * The data generator.
     */
//    class DataGenerator extends Timer implements ActionListener {
//        /**
//         * Constructor.
//         *
//         * @param interval the interval (in milliseconds)
//         */
//        public DataGenerator(int interval) {
//            super(interval, null);
//            addActionListener(this);
//        }
//
//        /**
//         * Adds a new free/total memory reading to the dataset.
//         *
//         * @param event the action event.
//         */
//        public void actionPerformed(ActionEvent event) {
//            long f = Runtime.getRuntime().freeMemory();
//            long t = Runtime.getRuntime().totalMemory();
//            addTotalObservation(t);
//            addFreeObservation(f);
//        }
//    }
}
