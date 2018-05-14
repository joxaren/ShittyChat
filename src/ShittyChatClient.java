import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

public class ShittyChatClient {

    private JFrame frame;
    private JTextField inputField;
    private JTextArea textArea;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket sock;

    public static void main(String[] args) {
        ShittyChatClient chat = new ShittyChatClient();
        chat.getToWork();
    }

    private void getToWork() {
        frame = new JFrame("Shitty Chat");
        JPanel panel = new JPanel();
        JPanel textPanel = new JPanel();
        JButton sendButton = new JButton("send");
        inputField = new JTextField(13);
        textArea = new JTextArea(10, 20);
        textArea.setLineWrap(true);
        textArea.setEditable(false);

        JScrollPane scroller = new JScrollPane(textArea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        inputField.addActionListener(new EnterActionListener());
        sendButton.addActionListener(new SendButtonListener());

        panel.add(inputField);
        panel.add(sendButton);
        textPanel.add(scroller);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveOption = new JMenuItem("Save log");
        JMenuItem loadOption = new JMenuItem("Load log");
        saveOption.addActionListener(new SaveOptionListener());
        loadOption.addActionListener(new LoadOptionListener());
        fileMenu.add(saveOption);
        fileMenu.add(loadOption);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.getContentPane().add(textPanel, BorderLayout.NORTH);
        frame.setSize(300, 250);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        inputField.requestFocus();

        setUpNetworking();

        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    private void setUpNetworking() {
        try {
            sock = new Socket("127.0.0.1", 5500);
            InputStreamReader ISreader = new InputStreamReader(sock.getInputStream());
            reader = new BufferedReader(ISreader);
            writer = new PrintWriter(sock.getOutputStream());
            System.out.println("connection to server established");
        }
        catch (ConnectException ex) {
            System.out.println("server not responding");
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException exception) {
                ex.printStackTrace();
            }
            System.out.println("establishing connection...");
            setUpNetworking();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveFile(File file) {
        try {
            BufferedWriter buff = new BufferedWriter(new FileWriter(file, true));
            textArea.write(buff); // saves content of textArea as plain text with line breaks and shit
            buff.close();
        } catch (IOException ex) {
            System.out.println("couldn't save the log");
            ex.printStackTrace();
        }
    }

    private void loadFileToTextArea() throws IOException {
        try {
            BufferedReader buff = new BufferedReader(new FileReader(chooseFile()));
            String readLine;
            while ((readLine = buff.readLine()) != null) {
                textArea.append(readLine + "\n");
            }
            buff.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) { }
    }

    private File chooseFile() {
        JFileChooser fileOpener = new JFileChooser();
        File fileToLoad = null;
        int ret = fileOpener.showOpenDialog(textArea); // i copypasted this from somewhere and i have no idea what int ret is or does
        if (ret == JFileChooser.APPROVE_OPTION) {
            fileToLoad = fileOpener.getSelectedFile();
        }
        return fileToLoad;
    }

    class SaveOptionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            JFileChooser saveFileDialogBox = new JFileChooser();
            saveFileDialogBox.showSaveDialog(frame);
            saveFile(saveFileDialogBox.getSelectedFile());
        }
    }

    class LoadOptionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            textArea.setText("");
            try {
                loadFileToTextArea();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class SendButtonListener implements ActionListener { // listens to enter button presses
        public void actionPerformed(ActionEvent event) {
            try {
                writer.println(inputField.getText()); // sends inputField's contents to server
                writer.flush();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            inputField.setText(""); // clear the inputField
            inputField.requestFocus(); // put the cursor back in the inputField
        }
    }

    class EnterActionListener implements ActionListener { // listens to enter/return key presses
        public void actionPerformed(ActionEvent event) {
            try {
                writer.println(inputField.getText()); // sends inputField's contents to server
                writer.flush();

            }
            catch (NullPointerException nullPointer) {
                textArea.append("connection lost" + "\n");
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
            inputField.setText(""); // clear the inputField
            inputField.requestFocus(); // put the cursor back in the inputField
        }
    }

    class IncomingReader implements Runnable {
        public void run () {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("message read: " + message);
                    textArea.append(message + "\n");
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}