package client_server;

import configs.Config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

class Client {
    private static final Config config = new Config();

    public static void main(String[] args) throws IOException {
        try {
            Scanner inputScanner = new Scanner(System.in);
            Socket socket = new Socket("localhost", config.PORT);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String userInput;

            while (true) {
                System.out.println(in.readUTF());
                userInput = inputScanner.nextLine();
                out.writeUTF(userInput);

                if (userInput.equals(config.CLIENT_EXIT_WORD)) {
                    System.out.println("Bye bye");
                    killConnection(socket, in ,out);
                    break;
                }
                String serverResponse = in.readUTF();
                System.out.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void killConnection(Socket socket, DataInputStream in, DataOutputStream out) {
        try {
            socket.close();
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}