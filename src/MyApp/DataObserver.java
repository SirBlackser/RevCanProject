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
public class DataObserver implements Observer {
    private JTextArea textArea1;

    public DataObserver(JTextArea textArea1){
        this.textArea1=textArea1;
        redirectSystemStreams();
    }

    @Override
    public void update(Observable o, Object arg) {
        Message m = (Message)arg;
        String idString = String.format("%4s", Integer.toHexString(m.id)).replace(' ', '0');
        String hexData = bytesToHex(m.data);
        System.out.printf("%s   %d      %2d %2d %2d %2d %2d %2d %2d %2d     %d\n",
                idString, m.length, m.data[0], m.data[1], m.data[2], m.data[3], m.data[4],
                m.data[5], m.data[6], m.data[7], m.time);
    }

    //The following codes set where the text get redirected. In this case, jTextArea1
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textArea1.append(text);
            }
        });
    }

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for(byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
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
