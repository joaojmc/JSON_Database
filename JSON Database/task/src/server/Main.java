package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private final static String SERVER_STARTED = "Server started!";
    private final static Database DATABASE = new Database();
    private final static int POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(POOL_SIZE);
    private static ServerSocket SERVER;

    public static void main(String[] args) {
        var address = "127.0.0.1";
        var port = 10117;

        System.out.println(SERVER_STARTED);

        try {
            SERVER = new ServerSocket(port, 50, InetAddress.getByName(address));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Session session = new Session(SERVER.accept(), DATABASE);
                EXECUTOR_SERVICE.submit(session::start);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void stopServer() {
        EXECUTOR_SERVICE.shutdownNow();

        try {
            SERVER.close();
        } catch (IOException e) {
            System.out.println("Error in server shutdown");
            e.printStackTrace();
        }
    }
}