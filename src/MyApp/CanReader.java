package MyApp;

import core.Canlib;
import obj.CanlibException;
import obj.Handle;
import obj.Message;
import sun.rmi.runtime.Log;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
    private JTextField textField2;
    private JButton saveButton;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JButton startStopButton;
    private JTextField textField9;
    private JComboBox comboBox2;
    private JTabbedPane tabbedPane1;
    private JTextArea textArea2;
    private JButton openChannelButton;

    private DataObserver dataObserver;
    private Parser parser;
    private MessageHandler messageHandler;
    private Thread t;
    private Thread thandler;
    private File file;
    private static DataSorter dataSorter;
    //static int filterId = -1;
    public static ArrayList<Integer> filterIds;
    private static Map<Integer, ArrayList<byte[]>> sortedData;
    private Handle handle;
    private static int channel;
    private static String bitRate;
    private static boolean readBus;
    public static ArrayList<Message> importedMessages;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("CanApp");
        CanReader canReader = new CanReader();
        channel = 0;
        bitRate = "500K";
        readBus = false;
        importedMessages = new ArrayList<Message>();
        dataSorter = new DataSorter();
        filterIds = new ArrayList<Integer>();
        filterIds.add(-1);
        frame.setContentPane(canReader.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(500,150,500,600);
        frame.setVisible(true);
        Thread canReaderThread = new Thread(canReader);
        canReaderThread.start();
        //frame.setLocation(500,250);

    }

    public CanReader() {
        //print the simulation (print the lines of a imported file).
        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //log.append("done Parsing\n");
                parser.setSimulation(true);
                parser.resetIt();
                parser.resetI();
                parser.playPause();
                //JOptionPane.showMessageDialog(null,"hello");
            }
        });
        //set the channel properties
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String textFieldValue = a0FormattedTextField.getText();
                channel = Integer.parseInt(textFieldValue);
                bitRate = comboBox1.getSelectedItem().toString();
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
                    importedMessages = parser.parseDoc(file);
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
                    if(textFieldValue.contains(";") || textFieldValue.contains(",")) {
                        String[] toFilter = null;
                        if(textFieldValue.contains(";")) {
                            toFilter = textFieldValue.split(";");
                        }
                        else if(textFieldValue.contains(",")) {
                            toFilter = textFieldValue.split(",");
                        }
                        for(int i = 0; i < toFilter.length; i++)
                        {
                            toFilter[i] = toFilter[i].replaceAll("\\s", "");
                            filterIds.add(Integer.parseInt(toFilter[i],16));
                        }
                    } else {
                        String toFilter = textField1.getText();
                        filterIds.add(Integer.parseInt(toFilter,16));
                    }

                }
            }
        });
        //open channel to car
        PlayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parser.setSimulation(false);
                if(readBus) {
                    parser.playPause();
                }
                else {
                    parser.playPause();
                }
                if(readBus) {
                    messageHandler.setActive(true);
                    messageHandler.setHandle(handle);
                    thandler = new Thread(messageHandler);
                    thandler.start();
                } else {
                    messageHandler.setActive(true);
                }
            }
        });
        openChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(readBus) {
                    readBus = false;
                }
                else {
                    readBus = true;
                }
                try{
                    handle = new Handle(channel);
                    if(readBus) {
                        handle.setBusParams(getBitrate(), 0, 0, 0, 0, 0);
                        handle.busOn();
                        log.append("channel opened\n");
                    } else {
                        handle.busOff();
                        handle.close();
                        log.append("channel closed\n");
                    }
                } catch(CanlibException o) {
                    System.err.println("failed to open channel: " + o);
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new java.io.File("."));
                    //fc.setDialogTitle(fc);
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    String location = "C:/Users/dries/CanlibWrapperTest/TestFiles/";
                    //
                    // disable the "All files" option.
                    //
                    chooser.setAcceptAllFileFilterUsed(false);
                    //
                    if (chooser.showOpenDialog(saveButton.getParent()) == JFileChooser.APPROVE_OPTION) {
                        location = fc.getCurrentDirectory().getAbsolutePath() + "/";
                    /*System.out.println("getCurrentDirectory(): "
                            +  chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "
                            +  chooser.getSelectedFile());*/
                    }
                    else {
                        System.out.println("No Selection ");
                    }
                    //String location = "C:/Users/dries/CanlibWrapperTest/TestFiles/";
                    String fileName =  location + textField2.getText() + ".log";
                    File file = new File(fileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter writer = new FileWriter(file);
                    for(Message m: importedMessages) {
                        String idString = String.format("%3s", Integer.toHexString(m.id)).replace(' ', '0');
                        String hexData = bytesToHex(m.data);
                        String theTime = Long.toString(m.time);
                        String time = theTime.substring(0,theTime.length()-6) + "." + theTime.substring(theTime.length()-6);
                        String str = "(" + time + ") can0 " + idString.toUpperCase() + "#" + hexData + "\n";
                        writer.write(str);
                    }
                } catch (IOException o) {
                    System.out.println("Failed saving file: " + o);
                }

            }
        });
        startStopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(messageHandler.getsend() == false) {
                    messageHandler.setSend(true);
                    if (textField3.isValid() && textField4.isValid() && textField5.isValid()) {
                        // textField3 -> ID
                        // textField4 -> Inital Message
                        messageHandler.setMessage(Integer.parseInt(textField3.getText()), parser.hexStringToByteArray(textField4.getText()));
                        // textField5 -> important bytes
                        messageHandler.setImportantBytes(textField5.getText());
                    }

                    // textField6 -> increment
                    // textField7 -> upper limit
                    // textField8 -> lower limit
                    // textField9 -> increment speed in ms
                    if (!comboBox1.getSelectedItem().toString().equals("None")) {
                        messageHandler.setIncrement(Integer.parseInt(textField6.getText(), 16),
                                Integer.parseInt(textField7.getText(), 16),
                                Integer.parseInt(textField8.getText(), 16),
                                Integer.parseInt(textField9.getText()));
                    }
                } else {
                    messageHandler.setSend(false);
                }
            }
        });
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //run thread for printing the messages.
    @Override
    public void run() {
        //dataGenerator = new DataGenerator();
        parser = new Parser();
        dataObserver = new DataObserver(textArea1, textArea2);
        parser.addObserver(dataObserver);
        t = new Thread(parser);
        t.start();

        messageHandler = new MessageHandler(false);
    }

    private int getBitrate()
    {
        int busRate = 0;
        switch (bitRate) {
            case "1M" : busRate = Canlib.canBITRATE_1M; break;
            case "500K" : busRate = Canlib.canBITRATE_500K; break;
            case "250K" : busRate = Canlib.canBITRATE_250K; break;
            case "125K" : busRate = Canlib.canBITRATE_125K; break;
            case "100K" : busRate = Canlib.canBITRATE_100K; break;
            case "83K" : busRate = Canlib.canBITRATE_83K; break;
            case "62K" : busRate = Canlib.canBITRATE_62K; break;
            case "50K" : busRate = Canlib.canBITRATE_50K; break;
            case "10K" : busRate = Canlib.canBITRATE_10K; break;
        }
        return busRate;
    }

    public static void saveIncomingStream(Message message)
    {
        importedMessages.add(message);
        sortedData = dataSorter.addFromDataStream(message, sortedData);
    }
}
