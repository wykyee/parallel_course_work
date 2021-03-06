package client_server;

import com.google.gson.Gson;
import configs.Config;
import inverted_index.InvertedIndexCreator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Server {
    private static final Config config = new Config();

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(config.PORT);
        System.out.println("Server Started");

        Gson gson = new Gson();
        File jsonFile = new File(config.JSON_FILE_PATH);
        if (!jsonFile.exists()) {
            jsonFile.createNewFile();
            FileWriter fileWriter = new FileWriter(config.JSON_FILE_PATH);
            fileWriter.write("{}");
            fileWriter.close();
        }

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
        ArrayList<String> result, finalResult;
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
                    InvertedIndexCreator invertedIndexCreator = new InvertedIndexCreator(null, config.JSON_FILE_PATH);
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
                String[] splitedUserInput = userInput.split("\\s+");
                finalResult = null;

                for (String word : splitedUserInput) {
                    word = prepareWord(word);
                    result = map.get(word);

                    if (result == null) {
                        out.writeUTF("Files don't contain word '" + word + "'");
                        break;
                    }

                    if (finalResult == null) {
                        finalResult = new ArrayList<>(result);
                    } else {
                        finalResult.retainAll(result);
                    }
                }

                if (finalResult == null) {
                    out.writeUTF("Enter another sequence");
                } else {
                    out.writeUTF(finalResult.toString());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String prepareWord(String word) {
        /* Normalizes word to add it in index map.
           All words will be lowercase, without non letters symbols */
        return word.toLowerCase().replaceAll("[^a-zA-Z]", "");
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
