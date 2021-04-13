package client_server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;


public class Server {
    public static final int PORT = 5001;
    private static final String SAVE_FILE_PATH = "C:\\Users\\wykyee\\IdeaProjects\\course\\sample.json";

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server Started");

        Gson gson = new Gson();
        HashMap<String, ArrayList<String>> map = gson.fromJson(new FileReader(SAVE_FILE_PATH), HashMap.class);

        while (true) {
            Socket socket = null;
            DataInputStream in = null;
            DataOutputStream out = null;
            try {
                socket = server.accept();
                System.out.println("New client connected");

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                Thread clientManager = new ClientManager(socket, in, out, map);
                clientManager.start();
            } catch (IOException e) {
                if (socket != null) {
                    socket.close();
                }
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        }
    }
}

class ClientManager extends Thread {
    final Socket socket;
    final DataInputStream in;
    final DataOutputStream out;
    final HashMap<String, ArrayList<String>> map;
    private static final String EXIT_WORD = "-quit";

    public ClientManager(Socket socket, DataInputStream in,
                         DataOutputStream out, HashMap<String, ArrayList<String>> map) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.map = map;
    }

    @Override
    public void run() {
        String userInput;
        ArrayList<String> result;
        while (true) {
            try {
                out.writeUTF("Enter word to find or '-quit' to exit");
                userInput = in.readUTF();
                if (userInput.equals(EXIT_WORD)) {
                    killConnection();
                    out.writeUTF("Bye bye");
                    break;
                }
                System.out.println("USER IS LOOKING FOR: " + userInput);
                result = map.get(userInput);
                if (result != null) {
                    out.writeUTF(result.toString());
                } else {
                    out.writeUTF("Files don't contain word '" + userInput + "'");
                }
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
