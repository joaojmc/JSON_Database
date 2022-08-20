package server;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class Session extends Thread {

    private final Socket clientSocket;
    private final Database DATABASE;

    public Session(Socket clientSocket, Database database) {
        this.clientSocket = clientSocket;
        this.DATABASE = database;
    }

    private static void sendResponse(DataOutputStream outputStream, Object result) throws IOException {
        if (result != null && !result.toString().isBlank()) {
            sendSuccessResponse(result, outputStream);
        } else {
            sendErrorResponse(outputStream);
        }
    }

    private static void sendResponseNoVal(DataOutputStream outputStream, Object result) throws IOException {
        if (result != null && !result.toString().isBlank()) {
            sendSuccessResponse(outputStream);
        } else {
            sendErrorResponse(outputStream);
        }
    }

    private static void sendResponse(Response response, DataOutputStream output) throws IOException {
        System.out.println("Sent: " + response);
        output.writeUTF(new Gson().toJson(response)
                .replace("\\", "")
                .replace("\"{", "{")
                .replace("}\"", "}"));
    }

    private static void sendSuccessResponse(DataOutputStream output) throws IOException {
        var successResponse = new Response();
        successResponse.response = "OK";
        sendResponse(successResponse, output);
    }

    private static void sendSuccessResponse(Object value, DataOutputStream output) throws IOException {
        var successResponse = new Response();
        successResponse.response = "OK";
        successResponse.value = value;
        sendResponse(successResponse, output);
    }

    private static void sendErrorResponse(DataOutputStream output) throws IOException {
        var errorResponse = new Response();
        errorResponse.response = "ERROR";
        errorResponse.reason = "No such key";
        sendResponse(errorResponse, output);
    }

    @Override
    public void run() {
        try {
            // One socket per-client connection, and not per server instance
            var inputStream = new DataInputStream(clientSocket.getInputStream());
            var outputStream = new DataOutputStream(clientSocket.getOutputStream());

            var requestAsString = inputStream.readUTF();
            HashMap<String, Object> request = new Gson().fromJson(requestAsString, HashMap.class);

            System.out.println("Received: " + request.toString());

            // Request format: -t t -i i -m m
            switch (request.get("type").toString()) {
                case "exit" -> {
                    sendSuccessResponse(outputStream);
                    DATABASE.writeFile();
                    Main.stopServer();
                }
                case "get" -> {
                    var found = DATABASE.get(request.get("key").toString());
                    sendResponse(outputStream, found);
                }
                case "set" -> {
                    var set = DATABASE.set(request.get("key"), request.get("value"));
                    sendResponseNoVal(outputStream, set);
                }
                case "delete" -> {
                    var deleted = DATABASE.delete(request.get("key").toString());
                    sendResponseNoVal(outputStream, deleted);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
