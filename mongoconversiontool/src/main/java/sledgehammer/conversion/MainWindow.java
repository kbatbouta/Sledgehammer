package sledgehammer.conversion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow {
    JPanel panelMain;
    private JPanel mongoPanel;
    private JPasswordField passwordField;
    private JTextField textFieldDatabase;
    private JTextField textFieldURL;
    private JTextField textFieldPORT;
    private JTextField textFieldUsername;
    private JTextArea textAreaLog;
    private JTextField textFieldDBFile;
    private JButton selectFileButton;
    private JProgressBar progressBar1;
    private JButton convertButton;
    private JScrollPane scrollPane1;
    private String databaseUsername;
    private String databasePassword;
    private String databaseURL;

    private void createUIComponents() {

        panelMain = new JPanel();

        FileFilter dbFilter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".db");
            }

            @Override
            public String getDescription() {
                return ".db (SQLite Database)";
            }
        };
        String home = System.getProperty("user.home");
        String zomboid = home + File.separator + "Zomboid";
        File dbDirectory = new File(zomboid + File.separator + "db");
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(dbDirectory);
        fileChooser.addChoosableFileFilter(dbFilter);
        fileChooser.setFileFilter(dbFilter);
        selectFileButton = new JButton("Select File");
        selectFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    textFieldDBFile.setText(filePath);
                }
            }
        });
        convertButton = new JButton("Convert");
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SQLiteToMongo.instance.run();
            }
        });
        textAreaLog = new ConsoleTextArea();
        scrollPane1 = new JScrollPane(textAreaLog,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textFieldDBFile = new JTextField();
        textFieldDBFile.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SQLiteToMongo.instance.setFilePath(textFieldDBFile.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SQLiteToMongo.instance.setFilePath(textFieldDBFile.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                SQLiteToMongo.instance.setFilePath(textFieldDBFile.getText());
            }
        });
    }

    public String getDatabaseDatabase() {
        return textFieldDatabase.getText();
    }

    public String getDatabaseUsername() {
        return textFieldUsername.getText();
    }

    public String getDatabasePassword() {
        StringBuilder password = new StringBuilder();
        password.append(passwordField.getPassword());
        return password.toString();
    }

    public String getDatabaseURL() {
        return textFieldURL.getText();
    }

    public String getDatabasePort() {
        return textFieldPORT.getText();
    }

    public ConsoleTextArea getConsole() {
        return (ConsoleTextArea) textAreaLog;
    }
}
