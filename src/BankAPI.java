import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class BankAPI {
    private static String IP = "127.0.0.1";
    private static int port = 2222;
    private static DataOutputStream output;
    private static DataInputStream input;
    private static Socket socket;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        System.out.println("--------------------------------------");
        System.out.println("Trying to connect to bank server...");
        try {
            socket = new Socket(IP, port);
            output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Successfully connected to bank server!");
        System.out.println("--------------------------------------\n");
        System.out.println("Enter your command:");
        getCommand();
    }

    private static void getCommand() throws IOException {
        String message = "";
        while (!message.equals("exit")) {
            message = scanner.nextLine();
            try {
                output.writeUTF(message);
                output.flush();
                System.out.println(input.readUTF());
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        socket.close();
    }
}
