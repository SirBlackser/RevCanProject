package MyApp;

import obj.Message;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by dries on 6/03/2017.
 */
public class DataObserver implements Observer{
    private JTextArea textArea1;

    public DataObserver(JTextArea textArea1){
        this.textArea1=textArea1;
        redirectSystemStreams();
    }

    //print the text to the texArea
    @Override
    public void update(Observable o, Object arg) {
        Message m = (Message)arg;
        String idString = String.format("%s", Integer.toHexString(m.id)).replace(' ', '0');
        idString = idString.toUpperCase();

        //String hexData = bytesToHex(m.data);
        if(!CanReader.filterIds.contains(-1) &&CanReader.filterIds.contains(m.id)){
            String hexData = bytesToHex(m.data);
            if(hexData.length()<7)
            {
                System.out.printf("%s\t%d  %s\t\t%d\n",
                        idString, m.length, hexData, m.time);
            }
            else
            {
                System.out.printf("%s\t%d  %s\t%d\n",
                        idString, m.length, hexData, m.time);
            }
        }else if(CanReader.filterIds.contains(-1)){
            String hexData = bytesToHex(m.data);
            if(hexData.length()<7)
            {
                System.out.printf("%s\t%d  %s\t\t%d\n",
                        idString, m.length, hexData, m.time);
            }
            else
            {
                System.out.printf("%s\t%d  %s\t%d\n",
                        idString, m.length, hexData, m.time);
            }
        }
    }

    //converts the databytes to a hexString
    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(Character.toString((char)b));
        }
        return builder.toString();
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
