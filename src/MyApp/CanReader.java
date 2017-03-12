package MyApp;

import obj.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by dries on 6/03/2017.
 */
public class CanReader implements Runnable{
    private JPanel panel1;
    private JTabbedPane ReadResults;
    private JComboBox comboBox1;
    private JLabel BitRate;
    private JFormattedTextField a0FormattedTextField;
    private JButton applyButton;
    private JTextField textField1;
    private JButton PlayButton;
    private JTextArea textArea1;
    private JButton setButton;
    private JFormattedTextField formattedTextField1;
    private JButton importButton;
    private JButton simulateButton;

    DataGenerator dataGenerator;
    DataObserver dataObserver;
    Parser parser;
    Thread t;
    File file;
    static DataSorter dataSorter;
    //static int filterId = -1;
    static ArrayList<Integer> filterIds;
    Map<Integer, ArrayList<byte[]>> sortedData;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("CanApp");
        CanReader canReader = new CanReader();
        dataSorter = new DataSorter();
        filterIds = new ArrayList<Integer>();
        filterIds.add(-1);
        frame.setContentPane(canReader.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(500,250,500,500);
        frame.setVisible(true);
        canReader.run();
        //frame.setLocation(500,250);

    }

    public CanReader() {
        //print the simulation (print the lines of a imported file).
        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //log.append("done Parsing\n");
                parser.resetIt();
                parser.playPause();
                //JOptionPane.showMessageDialog(null,"hello");
            }
        });
        //set the channel properties
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textFieldValue = a0FormattedTextField.getText();
                int channel = Integer.parseInt(textFieldValue);
                int bitRate = Integer.parseInt(comboBox1.getSelectedItem().toString());
            }
        });

        String cwd = System.getProperty("user.dir");
        final JFileChooser fc = new JFileChooser(cwd);

        //import a saved file of logs from the obd plug.
        //expected example message: (1487076865.178310) can0 220#F10300000000D40F
        //                          timestamp           can  id#message
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(importButton.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    //This is where a real application would open the file.
                    log.append("Opening: " + file.getName() + "\n");
                    ArrayList<Message> importedMessages = parser.parseDoc(file);
                    sortedData = dataSorter.SortParsedData(importedMessages);
                    //log.append("done Parsing\n");
                    String path= file.getAbsolutePath();
                    formattedTextField1.setText(path);
                } else {
                    log.append("Open command cancelled by user.\n");
                }
            }
        });
        //set the filter for incoming messages on the message board.
        setButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textFieldValue = textField1.getText();
                if(textFieldValue.equals("")) {
                    //filterId = -1;
                    filterIds.clear();
                    filterIds.add(-1);
                }
                else {
                    //filterId = Integer.parseInt(textFieldValue, 16);
                    filterIds.clear();
                    String[] toFilter = textFieldValue.split(";");
                    for(int i = 0; i < toFilter.length; i++)
                    {
                        toFilter[i] = toFilter[i].replaceAll("\\s", "");
                        filterIds.add(Integer.parseInt(toFilter[i],16));
                    }
                }
            }
        });
    }

    //run thread for printing the messages.
    @Override
    public void run() {
        //dataGenerator = new DataGenerator();
        parser = new Parser();
        dataObserver = new DataObserver(textArea1);
        parser.addObserver(dataObserver);
        t = new Thread(parser);
        t.run();
    }
}
