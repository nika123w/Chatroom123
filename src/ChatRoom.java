import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatRoom {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, PrintWriter> userWriters = new ConcurrentHashMap<>();
    private static int userCount = 0;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter port number: ");
        int port = scanner.nextInt();
        scanner.close();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started. Listening on port " + port);


        while (true) {
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private static void broadcastMessage(String message) {
        System.out.println("Broadcasting message: " + message);
        for (PrintWriter writer : clientWriters) {
            writer.println(message);
        }
    }

    private static void privateMessage(String message, String targetUser) {
        PrintWriter writer = userWriters.get(targetUser);
        if (writer != null) {
            writer.println(message);
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String userName;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                synchronized (clientWriters) {
                    clientWriters.add(out);
                    userCount++;
                    broadcastMessage("New user joined! Total users: " + userCount);
                }

                out.println("WELCOME TO CHAT! Type '/name <your-name>' to change your name.");

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received from client: " + message);

                    if (message.startsWith("/name ")) {
                        userName = message.substring(6);
                        userWriters.put(userName, out);
                        out.println("Your name is now: " + userName);
                    } else if (message.startsWith("/private ")) {
                        String[] parts = message.split(" ", 3);
                        if (parts.length == 3) {
                            privateMessage(parts[2], parts[1]);
                        }
                    } else if (message.equals("/exit")) {
                        break;
                    } else {
                        broadcastMessage(userName + ": " + message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                    userCount--;
                    broadcastMessage(userName + " has left. Total users: " + userCount);
                }
            }
        }
    }
}