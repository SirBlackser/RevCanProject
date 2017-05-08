package MyApp;

import core.Canlib;
import obj.CanlibException;
import obj.Handle;
import obj.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
    private JButton applyChannelButton;
    private JTextField textField1;
    private JButton PlayStreamButton;
    private JTextArea textArea1;
    private JButton setStreamIDButton;
    private JFormattedTextField formattedTextField1;
    private JButton importFileButton;
    private JButton simulateStreamButton;
    private JTextField textField2;
    private JButton saveFileButton;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JButton startStopSpoofButton;
    private JComboBox comboBox2;
    private JTabbedPane tabbedPane1;
    private JTextArea textArea2;
    private JButton openChannelButton;
    private JTextField byteToPrint;
    private JButton SetGraphVar;
    private JTextField idToPrint;
    private GraphPanel graphPanel;
    private JButton RemoveGraph;
    private JComboBox comboBox3;
    private JButton throttleTestButton;
    private JButton RPMTestButton;
    private JButton speedTestButton;
    private JLabel ThrottleID1;
    private JLabel RPMID1;
    private JLabel SpeedID1;
    private JLabel ThrottleID2;
    private JLabel ThrottleID3;
    private JLabel RPMID2;
    private JLabel RPMID3;
    private JLabel SpeedID2;
    private JLabel SpeedID3;
    private JComboBox IDCheck;
    private JLabel IdText;
    private JLabel ThrottleID4;
    private JLabel ThrottleID5;

    private DataObserver dataObserver;
    private Parser parser;
    private MessageHandler messageHandler;
    private Thread t;
    private Thread thandler;
    private File file;
    private static DataSorter dataSorter;
    //static int filterId = -1;
    public static ArrayList<Integer> filterIds;
    private Handle handle;
    private static int channel;
    private static String bitRate;
    private static boolean readBus;
    public static ArrayList<Message> importedMessages;
    private static int returnVal;
    public static HashMap<Integer, String> toDrawGraphs;
    private static HashMap<Integer, ArrayList<Message>> sortedData;
    private static ArrayList<Integer> foundIDS;
    private static RMSCalculator rmsCalculator;
    private boolean rpmFound = false;
    private boolean speedFound = false;
    //private static GrangerPrep grangerPrep;
    private static long time;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("CanApp");
        CanReader canReader = new CanReader();
        rmsCalculator = new RMSCalculator();
        channel = 0;
        bitRate = "500K";
        readBus = false;
        importedMessages = new ArrayList<>();
        dataSorter = new DataSorter();
        filterIds = new ArrayList<>();
        filterIds.add(-1);
        toDrawGraphs = new HashMap<>();
        sortedData = new HashMap<>();
        foundIDS = new ArrayList<>();
        //grangerPrep = new GrangerPrep();
        frame.setContentPane(canReader.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(500,150,700,600);
        frame.setVisible(true);
        Thread canReaderThread = new Thread(canReader);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        canReaderThread.start();
        //frame.setLocation(500,250);

    }

    private CanReader() {
        //print the simulation (print the lines of a imported file).
        simulateStreamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(file != null) {
                    //log.append("done Parsing\n");
                    parser.setSimulation(true);
                    if (parser.getPaused()) {
                        parser.setPause(false);
                        parser.resetI();
                        log.append("start streaming\n");
                    } else {
                        parser.setPause(true);
                        toDrawGraphs.clear();
                        log.append("stop streaming\n");
                    }
                    //JOptionPane.showMessageDialog(null,"hello");
                } else {
                    log.append("no file selected \n");
                }
            }
        });
        //set the channel properties
        applyChannelButton.addActionListener(new ActionListener() {
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
        importFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnVal = fc.showOpenDialog(importFileButton.getParent());

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
        //set the filter for incoming messages on the message board. Can filter on multiple ID's
        setStreamIDButton.addActionListener(new ActionListener() {
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
        //open and close channel to car
        openChannelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                readBus = !readBus;
                try{
                    handle = new Handle(channel);
                    if(readBus) {
                        time = System.currentTimeMillis();
                        handle.setBusParams(getBitrate(), 0, 0, 0, 0, 0);
                        handle.busOn();
                        log.append("channel opened\n");
                        openChannelButton.setText("Close Channel");
                    } else {
                        messageHandler.setActive(false);
                        parser.setPause(true);
                        log.append("stop reading stream\n");
                        parser.setSimulation(true);
                        handle.busOff();
                        handle.close();
                        log.append("channel closed\n");
                        openChannelButton.setText("Open Channel");
                    }
                } catch(CanlibException o) {
                    System.err.println("failed to open channel: " + o);
                }
            }
        });
        //start and stop printing and saving of the stream
        //restart the stream will overwrite the current saved data.
        PlayStreamButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parser.setSimulation(false);
                if(readBus && parser.getPaused()) {
                    parser.setPause(false);
                    PlayStreamButton.setText("Pause");
                    //long time = System.currentTimeMillis();
                    if(!importedMessages.isEmpty())
                        importedMessages.clear();
                    //log.append(new Long(System.currentTimeMillis()-time).toString());
                    if(!sortedData.isEmpty())
                        sortedData.clear();
                    if(!foundIDS.isEmpty())
                        foundIDS.clear();
                    log.append("all clear!\n");
                    messageHandler.setActive(true);
                    messageHandler.setHandle(handle);
                    thandler = new Thread(messageHandler);
                    thandler.start();
                    log.append("start reading stream\n");
                } else {
                    toDrawGraphs.clear();
                    messageHandler.setActive(false);
                    PlayStreamButton.setText("Play");
                    /*try {
                        thandler.join();
                    } catch (InterruptedException o) {
                        log.append(o.getMessage());
                    }*/
                    parser.setPause(true);
                    log.append("stop reading stream\n");
                }
            }
        });
        //export all recorded messages to a file
        //from the sesion between the last start and stop.
        saveFileButton.addActionListener(new ActionListener() {
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
                    if (chooser.showOpenDialog(saveFileButton.getParent()) == JFileChooser.APPROVE_OPTION) {
                        location = fc.getCurrentDirectory().getAbsolutePath() + "/";
                    /*System.out.println("getCurrentDirectory(): "
                            +  chooser.getCurrentDirectory());
                    System.out.println("getSelectedFile() : "
                            +  chooser.getSelectedFile());*/
                    }
                    else {
                        log.append("No Selection \n");
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
                        Long actualTime = time + m.time;
                        String theTime = Long.toString(actualTime);
                        if(theTime.length()<9)
                        {
                            theTime = String.format("%9s", theTime).replace(' ', '0');
                        }
                        String time = theTime.substring(0,10) + "." + theTime.substring(10);
                        String str;
                        try {
                            str = "(" + time + ") can" + channel + " " + idString.toUpperCase() + "#" + hexData + "\n";
                        } catch (Exception o) {
                            str = o.getMessage();
                        }
                        writer.write(str);
                    }
                    writer.close();
                } catch (IOException o) {
                    log.append("Failed saving file: " + o);
                }

            }
        });
        //start sending messages on the Can bus, currently only used to request obd data.
        //possible addition could be message spoofing.
        startStopSpoofButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!messageHandler.getSend()) {
                    messageHandler.setSend(true);
                    if (textField3.isValid() && textField4.isValid() && textField5.isValid()) {
                        // textField3 -> ID
                        // textField4 -> Inital Message
                        messageHandler.setMessage(Integer.parseInt(textField3.getText(),16), parser.hexStringToByteArray(textField4.getText()));
                    }
                    log.append("start sending messages\n");

                    // textField6 -> increment
                    // textField7 -> upper limit
                    // textField8 -> lower limit
                    // textField9 -> increment speed in ms
                    if (!comboBox2.getSelectedItem().toString().equals("None")) {
                        // textField5 -> important bytes
                        messageHandler.setImportantBytes(textField5.getText());
                        messageHandler.setIncrement(Integer.parseInt(textField6.getText(), 16),
                                Integer.parseInt(textField7.getText(), 16),
                                Integer.parseInt(textField8.getText(), 16),
                                Integer.parseInt(textField9.getText()));
                    }
                } else {
                    messageHandler.setSend(false);
                    log.append("stop sending messages\n");
                }
            }
        });
        //add which id and which byte should be drawn on the graph panel.
        //currently only one set of byte(s) can be shown per id.
        //possible improvement: multiple sets of bytes of a single id can be drawn.
        //not possible now because of use of hashmap.
        SetGraphVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int printId = Integer.parseInt(idToPrint.getText(), 16);
                if (toDrawGraphs.get(printId) == null) {
                    ArrayList<Integer> BytesToDraw = new ArrayList<>();
                    String bytes = byteToPrint.getText();
                    /*if (bytes.contains("-")) {
                        String[] borders = bytes.split("-");
                        BytesToDraw.add(Integer.parseInt(borders[0]));
                        BytesToDraw.add(Integer.parseInt(borders[1]));
                    } else {
                        BytesToDraw.add(Integer.parseInt(bytes));
                    }*/
                    toDrawGraphs.put(printId,bytes);
                }
            }
        });
        //remove the id from the hashmap. Stops drawing the graph.
        RemoveGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int printId = Integer.parseInt(idToPrint.getText(), 16);
                if(toDrawGraphs.get(printId)!=null)
                {
                    toDrawGraphs.remove(printId);
                }
            }
        });
        //start the test for throttle
        //improvement: if button is pressed, data will start to be recorded and a test needs to be done.
        //currently this is done manual be either first doing a measurement yourself or by loading in a test file.
        throttleTestButton.addActionListener(new ActionListener() {
            @Override
            //known obd codes: 11, 45, 47
            public void actionPerformed(ActionEvent e) {
                if(sortedData.containsKey(2024))
                {
                    String temp = Integer.toHexString(Byte.toUnsignedInt(sortedData.get(2024).get(0).data[2]));
                    String checker = String.format("%2s", temp).replace(' ', '0');
                    //checker.toUpperCase();
                    String ID = IDCheck.getSelectedItem().toString();
                    String[] sub = ID.split("-");
                    ArrayList<ArrayList<Float>> answers = new ArrayList<>();
                    String DevOrP;
                    if(checker.equals(sub[0].toLowerCase())) {
                        if(comboBox3.getSelectedItem().toString().equals("Root Mean Square")) {
                            answers = rmsCalculator.calculateRMS(sortedData, importedMessages);
                            DevOrP = "Deviation: ";
                        } else {
                            //answers = grangerPrep.grangerTester(sortedData,importedMessages);
                            DevOrP = "P value: ";
                        }
                        IdText.setText(sub[1]);
                        if (answers.get(0).get(3) == 1) {
                            ThrottleID1.setText(DevOrP + answers.get(0).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(0).get(1))) + " byte: " + answers.get(0).get(2));
                            ThrottleID2.setText(DevOrP + answers.get(1).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(1).get(1))) + " byte: " + answers.get(1).get(2));
                            ThrottleID3.setText(DevOrP + answers.get(2).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(2).get(1))) + " byte: " + answers.get(2).get(2));
                            ThrottleID4.setText(DevOrP + answers.get(3).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(3).get(1))) + " byte: " + answers.get(3).get(2));
                            ThrottleID5.setText(DevOrP + answers.get(4).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(4).get(1))) + " byte: " + answers.get(4).get(2));
                        } else {
                            float temp1 = answers.get(0).get(2) + (answers.get(0).get(3) - 1);
                            float temp2 = answers.get(1).get(2) + (answers.get(1).get(3) - 1);
                            float temp3 = answers.get(2).get(2) + (answers.get(2).get(3) - 1);
                            float temp4 = answers.get(3).get(2) + (answers.get(3).get(3) - 1);
                            float temp5 = answers.get(4).get(2) + (answers.get(4).get(3) - 1);
                            //ThrottleID1.setText("ID: " + Integer.toHexString(answer.get(0)) + " byte(s): " + answer.get(1) + "-" + temp);
                            ThrottleID1.setText(DevOrP + answers.get(0).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(0).get(1))) + " bytes: " + answers.get(0).get(2) + "-" + temp1);
                            ThrottleID2.setText(DevOrP + answers.get(1).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(1).get(1))) + " bytes: " + answers.get(1).get(2) + "-" + temp2);
                            ThrottleID3.setText(DevOrP + answers.get(2).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(2).get(1))) + " bytes: " + answers.get(2).get(2) + "-" + temp3);
                            ThrottleID4.setText(DevOrP + answers.get(3).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(3).get(1))) + " bytes: " + answers.get(3).get(2) + "-" + temp4);
                            ThrottleID5.setText(DevOrP + answers.get(4).get(0) + " at ID: " + Integer.toHexString(Math.round(answers.get(4).get(1))) + " bytes: " + answers.get(4).get(2) + "-" + temp5);
                        }
                    } else{
                        ThrottleID1.setText("right obd code not present: " + sub[0]);
                    }
                } else{
                    ThrottleID1.setText("no obd messages pressent");
                }
            }
        });
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

    //run thread for printing the messages.
    @Override
    public void run() {
        //dataGenerator = new DataGenerator();
        parser = new Parser(this);
        dataObserver = new DataObserver(textArea1, textArea2, graphPanel);
        parser.addObserver(dataObserver);
        t = new Thread(parser);
        t.start();

        messageHandler = new MessageHandler(false, this);
    }

    //set the bitrate of the bus
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

    //save the messages acquired from the can-bus network
    public static void saveIncomingStream(Message message)
    {
        if(readBus) {
            importedMessages.add(message);
            sortedData = dataSorter.addFromDataStream(message, sortedData);
            //parser.addObserver(graphPanel);
            /*if(foundIDS.isEmpty())
            {
                foundIDS.add(message.id);
            }
            //alternatief ArrayUtils.contains(foundIDS, message.id);
            else if (!Arrays.asList(foundIDS).contains(message.id)){
                foundIDS.add(message.id);
            }*/
        }
    }

    //get the last received message.
    public static Message getLatestMessage()
    {
        return importedMessages.get(importedMessages.size()-1);
    }

    //custom definition of the graph panel
    private void createUIComponents() {
        graphPanel = new GraphPanel(30000);
    }
}
