import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatUser2 {
    private static final String SERVER_ADDRESS = "localhost";  // Server address
    private static final int SERVER_PORT = 12345;  // Server port
    private static PrintWriter out;
    private static BufferedReader in;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String address = SERVER_ADDRESS;
        int port = SERVER_PORT;

        while (true) {
            try {
                // Attempt to connect to the server
                Socket socket = new Socket(address, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                System.out.println("Connected to server. Type your messages.");

                // Start a thread to listen for server messages
                new Thread(new ServerListener(socket)).start();

                // Read user input and send to server
                String message;
                while (true) {
                    message = scanner.nextLine();
                    if (message.equals("/exit")) {
                        out.println("/exit");  // Send exit message to server
                        break;
                    } else {
                        System.out.println("Sending message: " + message);  // Debugging line
                        out.println(message);  // Send message to server
                    }
                }
                socket.close();
                break;
            } catch (IOException e) {
                System.out.println("Unable to connect to server, retrying...");
                continue;  // Retry if connection fails
            }
        }
    }

    // Listener thread to receive messages from the server
    private static class ServerListener implements Runnable {
        private BufferedReader reader;

        public ServerListener(Socket socket) throws IOException {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        public void run() {
            try {
                String message;
                while ((message = reader.readLine()) != null) {  // Read messages from server
                    System.out.println(message);  // Display message on client side
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}