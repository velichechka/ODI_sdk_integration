package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.logging.log4j.*;

import utilities.PropertiesFile;


public class SvnPathUI extends JDialog {

    private final JPanel contentPanel = new JPanel();
    private TextField textField;
    private JLabel lblNewLabel;
    private static Logger logger = LogManager.getLogger(SvnPathUI.class.getName());

    /**

     * Create the dialog.

     */

    public SvnPathUI() {
        setBounds(100, 100, 336, 121);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setModal(true);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        {
            JPanel buttonPane = new JPanel();
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            {
                JButton okButton = new JButton("OK");
                okButton.setFont(new Font("Tahoma", Font.BOLD, 12));
                okButton.setForeground(new Color(0, 100, 0));
                okButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent arg0) {
                        try {
                            PropertiesFile propFile = new PropertiesFile();
                            propFile.setPropertyValue("SvnRepositoryPath", textField.getText());
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
                        setVisible(false);
                    }
                });

                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }

            lblNewLabel = new JLabel("Please specify the path to SVN repository");
            lblNewLabel.setForeground(new Color(51, 0, 255));
            lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
        }
        
                    textField = new TextField();
                    contentPanel.setLayout(new GridLayout(0, 1, 0, 0));
                    contentPanel.add(lblNewLabel);
                    contentPanel.add(textField);

        setVisible(true);
    }

    public String getUserInputString() {
        return textField.getText();
    }
}
