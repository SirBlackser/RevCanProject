package MyApp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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
    Parser parser;
    File file;

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("CanApp");
        CanReader canReader = new CanReader();
        frame.setContentPane(canReader.panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setBounds(500,250,500,500);
        frame.setVisible(true);
        canReader.run();
        //frame.setLocation(500,250);

    }

    public CanReader() {
        //redirectSystemStreams();

        simulateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parser.parseDoc(file);
                log.append("done Parsing\n");
                parser.playPause();
                parser.run();
                //JOptionPane.showMessageDialog(null,"hello");
            }
        });
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

        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fc.showOpenDialog(importButton.getParent());

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    //This is where a real application would open the file.
                    log.append("Opening: " + file.getName() + "\n");
                    String path= file.getAbsolutePath();
                    formattedTextField1.setText(path);
                } else {
                    log.append("Open command cancelled by user.\n");
                }
            }
        });
    }

    @Override
    public void run() {
        //dataGenerator = new DataGenerator();
        parser = new Parser();
        DataObserver dataObserver = new DataObserver(textArea1);
        parser.addObserver(dataObserver);
        Thread t = new Thread(parser);
        t.run();
    }
}
