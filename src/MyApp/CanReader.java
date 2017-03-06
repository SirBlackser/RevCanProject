package MyApp;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dries on 6/03/2017.
 */
public class CanReader implements Runnable{
    private JPanel panel1;
    private JTabbedPane ReadResults;
    private JTabbedPane tabbedPane2;
    private JComboBox comboBox1;
    private JLabel BitRate;
    private JFormattedTextField a0FormattedTextField;
    private JButton applyButton;
    private JTextField textField1;
    private JButton applyButton2;
    private JTextArea textArea1;
    private JButton setButton;

    DataGenerator dataGenerator;

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

        applyButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pause();
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
    }

    public void pause(){
       dataGenerator.playPause();
    }

    @Override
    public void run() {
        dataGenerator = new DataGenerator();
        DataObserver dataObserver = new DataObserver(textArea1);
        dataGenerator.addObserver(dataObserver);
        Thread t = new Thread(dataGenerator);
        t.run();
    }
}
