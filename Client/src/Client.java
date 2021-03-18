import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class Client extends JFrame {

    //GUI
    public JFrame window;
    public JTextArea messages;

    //Networking
    public boolean connected = false;
    public Socket clientSocket;
    public DataInputStream inputStream;
    public DataOutputStream outputStream;

    public static void main(String[] args) {
        new Client().Start();
    }

    public void Start() {
        window = new JFrame();
        window.setTitle("Message App");
        window.setSize(700, 500);
        window.setLayout(null);
        window.setDefaultCloseOperation(3);
        ImageIcon icon = new ImageIcon("icon.png");
        window.setIconImage(icon.getImage());

        //GUI Items
        JTextField input = new JTextField();
        input.setSize(600, 30);
        input.setLocation(0, 433);
        input.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessage(input.getText());
                input.setText("");
            }
        });

        JButton send = new JButton("Send");
        send.setSize(100, 30);
        send.setLocation(590, 433);
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SendMessage(input.getText());
                input.setText("");
            }
        });

        messages = new JTextArea();
        messages.setSize(700, 437);
        messages.setEditable(false);
        messages.setBackground(Color.gray);

        window.add(input);
        window.add(send);
        window.add(messages);
        window.setVisible(true);

        JOptionPane nickname = new JOptionPane();
        String result = nickname.showInputDialog("Enter nickname");

        ConnectClient(result);
    }

    public void ConnectClient(String nickname) {
        try {
            clientSocket = new Socket(/*Your IP here*/ "127.0.0.1" /* Localhost Adress */, 444);
            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());
            connected = true;

            SendMessage("nickname-143258327" + nickname);

            new Thread(() -> {
                ReceiveMessages();
            }).start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendMessage(String message) {
        try {
            if (!connected) { return; }
            outputStream.writeUTF(message);
            outputStream.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ReceiveMessages() {
        while (connected) {
            try {
                String message = inputStream.readUTF();
                if (message != null) {
                    OnMessage(message);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            SendMessage("clientdisconnected");
            clientSocket.close();
            System.exit(0);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OnMessage(String message) {
        boolean command = false;

        if (message.equals("serverclosed")) {
            connected = false;
            command = true;
        }
        if (message.split("-143258327")[0].equals("chat")) {
            messages.append("\n" + message.split("-143258327")[2] + ": " + message.split("-143258327")[1]);
            command = true;
        }
        if (!command) { System.out.println(message); }
    }
}
