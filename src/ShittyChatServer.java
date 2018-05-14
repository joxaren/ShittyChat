import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

public class ShittyChatServer {

    private ArrayList clientOutputStreams;

    public static void main(String[] args) {
        ShittyChatServer server = new ShittyChatServer();
        server.go();
    }

    private void go() {
        clientOutputStreams = new ArrayList();
        try {
            ServerSocket serverSock = new ServerSocket(5500);
            System.out.println("server online");
            while (true) {
                Socket clientSocket = serverSock.accept();
                System.out.println("got connection request");
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                clientOutputStreams.add(writer);
                Thread t = new Thread(new ShittyChatServer.ClientHandler(clientSocket));
                t.start();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void tellEveryone(String message) {
        Iterator <PrintWriter> it = clientOutputStreams.iterator();
        while (it.hasNext()) {
            try {
                PrintWriter writer = it.next();
                writer.println(message);
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public class ClientHandler implements Runnable {
        BufferedReader reader;
        Socket sock;

        ClientHandler(Socket clientSocket) {
            try {
                sock = clientSocket;
                InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
                reader = new BufferedReader(isReader);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("message: " + message);
                    tellEveryone(message);
                }
            } catch (SocketException ex) {
                System.out.println("client disconnected");
            } catch (SocketTimeoutException ex) {
                System.out.println("connection timeout");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}