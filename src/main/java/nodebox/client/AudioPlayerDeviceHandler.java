package nodebox.client;

import ddf.minim.AudioBuffer;
import ddf.minim.AudioPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AudioPlayerDeviceHandler implements DeviceHandler {
    private String name;
    private String fileName;
    private boolean autostart;
    private JFrame frame = null;

    private MinimApplet applet = null;

    public AudioPlayerDeviceHandler(String name) {
        this(name, "", false);
    }

    public AudioPlayerDeviceHandler(String name, String fileName, boolean autostart) {
        this.name = name;
        this.fileName = fileName;
        this.autostart = autostart;
    }

    public String getName() {
        return name;
    }

    public boolean isAutoStart() {
        return autostart;
    }

    public AudioBuffer getMix() {
        if (applet == null) return null;
        return applet.getMix();
    }

    public AudioPlayer getPlayer() {
        if (applet == null) return null;
        return applet.getPlayer();
    }

    public void start() {
        frame = new JFrame();
        applet = new MinimApplet(fileName, true);
        applet.init();
        frame.add(applet);
    }

    public void stop() {
        applet.stop();
        applet.dispose();
        frame.dispose();
    }

    public AbstractDeviceControl createControl() {
        return new AudioPlayerDeviceControl(this);
    }

    private class AudioPlayerDeviceControl extends AbstractDeviceControl {

        private JLabel deviceNameLabel;
        private JTextField fileNameField;
        private JCheckBox autoStartCheck;
        private JButton startButton;
        private JButton stopButton;

        public AudioPlayerDeviceControl(AudioPlayerDeviceHandler deviceHandler) {
            super(deviceHandler);
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            Dimension d = new Dimension(450, 30);
            setPreferredSize(d);
            setMaximumSize(d);
            setSize(d);

            deviceNameLabel = new JLabel(deviceHandler.getName());
            fileNameField = new JTextField(100);
            fileNameField.setText(fileName);
            autoStartCheck = new JCheckBox("autostart");
            autoStartCheck.setSelected(isAutoStart());
            autoStartCheck.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent itemEvent) {
                    autostart = autoStartCheck.isSelected();
                    setPropertyValue("autostart", String.valueOf(autostart));
                }
            });
            startButton = new JButton("Start");
            startButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    fileName = fileNameField.getText();
                    start();
                }
            });
            stopButton = new JButton("Stop");
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    stop();
                }
            });
            add(Box.createHorizontalStrut(10));
            add(deviceNameLabel);
            add(Box.createHorizontalStrut(5));
            add(fileNameField);
            add(Box.createHorizontalStrut(5));
            add(autoStartCheck);
            add(Box.createHorizontalStrut(5));
            add(startButton);
            add(Box.createHorizontalStrut(5));
            add(stopButton);
            add(Box.createHorizontalGlue());
        }
    }
}
