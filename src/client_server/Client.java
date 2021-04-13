package client_server;

import java.net.*;
import java.io.*;
import java.util.Scanner;

class Client {
    private static final String EXIT_WORD = "-quit";
    public static final int PORT = 5001;

    public static void main(String[] args) throws IOException {
        try {
            Scanner inputScanner = new Scanner(System.in);
            Socket socket = new Socket("localhost", PORT);

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String userInput;

            while (true) {
                System.out.println(in.readUTF());
                userInput = inputScanner.nextLine();
                out.writeUTF(userInput);

                if (userInput.equals(EXIT_WORD)) {
                    System.out.println("Bye bye");
                    killConnection(socket, in ,out);
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