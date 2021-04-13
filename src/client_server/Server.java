package client_server;

import com.google.gson.Gson;
import configs.Config;
import inverted_index.InvertedIndexCreator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private static final Config config = new Config();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(config.PORT);
        System.out.println("Server Started");

        Gson gson = new Gson();
        HashMap<String, ArrayList<String>> map = gson.fromJson(
            new FileReader(config.JSON_FILE_PATH),
            HashMap.class
        );

        while (true) {
            Socket socket = null;
            DataInputStream in;
            DataOutputStream out;
            try {
                socket = server.accept();
                System.out.println("New client connected");

                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());

                Thread clientManager = new ClientManager(socket, in, out, map);
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
    private HashMap<String, ArrayList<String>> map;
    private final Config config = new Config();

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
        try {
            while (true) {
                out.writeUTF("Enter word to find, '-quit' to exit or '-update' to update index.");
                userInput = in.readUTF();
                if (userInput.equals(config.CLIENT_EXIT_WORD)) {
                    System.out.println("Client is disconnected");
                    killConnection();
                    break;
                }
                if (userInput.equals(config.INDEX_UPDATE_WORD)) {
                    System.out.println("Updating file with index...");
                    InvertedIndexCreator invertedIndexCreator = new InvertedIndexCreator(null);
                    invertedIndexCreator.start();
                    try {
                        invertedIndexCreator.join();
                    } catch (InterruptedException ignored) {}

                    map = new Gson().fromJson(
                        new FileReader(config.JSON_FILE_PATH),
                        HashMap.class
                    );
                    out.writeUTF("File is updated");
                    continue;
                }
                System.out.println("USER IS LOOKING FOR: " + userInput);
                result = map.get(userInput);
                if (result != null) {
                    out.writeUTF(result.toString());
                } else {
                    out.writeUTF("Files don't contain word '" + userInput + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
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
