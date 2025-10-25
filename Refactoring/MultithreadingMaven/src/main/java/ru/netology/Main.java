package ru.netology;

public class Main {
    public static void main(String[] args) {
        final Server server = new Server();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("Server fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
