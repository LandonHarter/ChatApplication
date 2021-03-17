import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Server {

    public static ServerSocket serverSocket;
    public static int port;

    private static boolean shouldClose = false;

    private static Thread serverThread;
    private static Thread messageThread;

    public static List<Socket> clients = new ArrayList<Socket>();
    public static List<String> nicknames = new ArrayList<String>();
    public static List<DataInputStream> socketstreams = new ArrayList<DataInputStream>();
    public static List<DataOutputStream> outputstreams = new ArrayList<DataOutputStream>();

    public static int clientsconnected = 0;

    static Scanner exit;

    @SuppressWarnings("resource")
    public static void main(String args[]) {
        System.out.print("Server port: ");
        Scanner d = new Scanner(System.in);
        String portn = d.nextLine();

        port = Integer.parseInt(portn);

        serverThread = new Thread(() -> {
            CreateServer();
        });
        serverThread.start();

        exit = new Scanner(System.in);
        exit.nextLine();
        CloseServer();
    }

    public static void CreateServer() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server up and running on port " + port);

            while (!shouldClose) {
                Socket socket = serverSocket.accept();
                exit.reset();
                DataInputStream stream = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                System.out.println("Client connected from " + socket.getInetAddress().toString().split("/")[1]);
                clients.add(socket);
                socketstreams.add(stream);
                outputstreams.add(output);
                nicknames.add("Client" + new Random().nextInt(100000));

                int c = clientsconnected;
                new Thread(() -> {
                    CheckMessages(c);
                }).start();

                clientsconnected++;
            }

            serverSocket.close();
        } catch (Exception e) {

        }
    }

    public static void CheckMessages(int id) {
        while (!shouldClose) {
                try {
                    String message = socketstreams.get(id).readUTF();
                    boolean command = false;

                    //Commands
                    if (message.equals("clientdisconnected")) {
                        exit.reset();
                        System.out.println(clients.get(id).getInetAddress().toString().split("/")[1] + " just disconnected.");
                        socketstreams.remove(id);
                        outputstreams.remove(id);
                        clients.remove(id);
                        nicknames.remove(id);
                        clientsconnected--;
                        Thread.currentThread().stop();
                        command = true;
                    }
                    if (message.split("-143258327")[0].equals("nickname")) {
                        nicknames.set(id, message.split("-143258327")[1]);
                        command = true;
                    }

                    if (!command) {
                        exit.reset();
                        System.out.println(clients.get(id).getInetAddress().toString().split("/")[1] + ": " + message);
                        SendMessage("chat-143258327" + message + "-143258327" + nicknames.get(id));
                    }
                }
                catch (IOException e) {

                }
        }
    }

    public static void SendMessage(String message) {
        for (int i = 0; i < outputstreams.size(); i++) {
            try {
                outputstreams.get(i).writeUTF(message);
                outputstreams.get(i).flush();
            } catch (IOException e) {

            }
        }
    }

    public static void SendMessageToUser(int id, String message) {
        try {
            outputstreams.get(id).writeUTF(message);
            outputstreams.get(id).flush();
        }
        catch(Exception e) {

        }
    }

    public static void CloseServer() {
        SendMessage("serverclosed");
        shouldClose = true;
        System.out.println("Server closed.");

        System.out.println("Press enter to exit application...");
        @SuppressWarnings("resource")
        Scanner s = new Scanner(System.in);
        s.nextLine();
        System.exit(0);
    }
}