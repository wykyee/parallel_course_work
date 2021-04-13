package client_server;

import java.net.*;
import java.io.*;


public class Server {
    public static final int PORT = 5001;

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");

        while (true) {
            Socket socket = null;
            try {
                socket = server.accept();
                System.out.println("New client connected");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                Thread clientManager = new ClientManager(socket, in, out);
                clientManager.start();

            } catch (IOException e) {
                socket.close();
            }
        }
    }
}

class ClientManager extends Thread {
    final Socket socket;
    final DataInputStream in;
    final DataOutputStream out;
    private static final String EXIT_WORD = "-quit";

    public ClientManager(Socket socket, DataInputStream in, DataOutputStream out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        String userInput;
        while (true) {
            try {
                out.writeUTF("Enter word to find or '-quit' to exit");
                userInput = in.readUTF();
                if (userInput.equals(EXIT_WORD)) {
                    killConnection();
                    out.writeUTF("Bye bye");
                    break;
                }
                System.out.println("USER WROTE: " + userInput);
                out.writeUTF("Server response test");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void killConnection() {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
