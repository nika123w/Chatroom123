import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatUser2 {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String address = SERVER_ADDRESS;
        int port = SERVER_PORT;

        while (true) {
            try {
                Socket socket = new Socket(address, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Connected to server. Type your messages.");

                new Thread(new ServerListener(socket)).start();

                String message;
                while (true) {
                    message = scanner.nextLine();
                    if (message.equals("/exit")) {
                        out.println("/exit");
                        break;
                    } else {
                        System.out.println("Sending message: " + message);
                        out.println(message);
                    }
                }
                socket.close();
                break;
            } catch (IOException e) {
                System.out.println("Unable to connect to server, retrying...");
                continue;
            }
        }
    }

    private static class ServerListener implements Runnable {
        private BufferedReader reader;

        public ServerListener(Socket socket) throws IOException {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}