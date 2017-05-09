package MyApp;

import obj.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by dries on 6/03/2017.
 */
public class DataObserver implements Observer{
    private JTextArea textArea1;
    private JTextArea textArea2;
    private GraphPanel graphPanel;

    private Map<String, String> latestMessages = new TreeMap<>();

    public DataObserver(JTextArea textArea1, JTextArea textArea2, GraphPanel graphPanel){
        this.textArea1=textArea1;
        redirectSystemStreams();
        this.textArea2=textArea2;
        this.graphPanel=graphPanel;
    }


    //print the text to the texArea
    // the Object is expected to be a message
    //it will be parsed in the update function itself
    @Override
    public void update(Observable o, Object arg) {
        Message m = (Message) arg;
        if(m != null) {
            String idString = "";
            try {
                idString = String.format("%3s", Integer.toHexString(m.id)).replace(' ', '0');
            } catch (Exception e) {
                log.append(e.getMessage());
            }
            idString = idString.toUpperCase();
            String hexData = bytesToHex(m.data);
            Long currentTime= System.currentTimeMillis();
            String theTime = Long.toString(m.time);
            if (theTime.length() < 13) {
                //theTime = String.format("%13s", theTime).replace(' ', '0');
                theTime = Long.toString(currentTime+m.time);
            }
            String time = theTime.substring(0, 10) + "." + theTime.substring(10);

            /*Date date = new Date(System.currentTimeMillis());
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");
            String dateFormatted = formatter.format(date);*/
            //String time = theTime;
            //String hexData = bytesToHex(m.data);
            if (!CanReader.filterIds.contains(-1) && CanReader.filterIds.contains(m.id)) {
                System.out.printf("%s    %s\t%d  %s\n",
                        time, idString, m.length, hexData);
            } else if (CanReader.filterIds.contains(-1)) {
                System.out.printf("%s    %s\t%d  %s\n",
                        time, idString, m.length, hexData);
            }

            latestMessages.put(idString, hexData);
            StringBuilder messageString = new StringBuilder();
            for (Map.Entry<String, String> message : latestMessages.entrySet()) {
                messageString.append(message.getKey() + "\t" + message.getValue().replaceAll("..(?=..)", "$0 ") + "\n");
            }
            textArea2.setText(messageString.toString());
            graphPanel.addObservation(m);
        }
    }

    //converts the databytes to a hexString
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

    //The following codes set where the text get redirected. In this case, jTextArea1
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea1.append(text);
            }
        });
    }

    //Followings are The Methods that do the Redirect, you can simply Ignore them.
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }
}
