package client;

import com.beust.jcommander.JCommander;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class Main {

    private static final String ADDRESS = "127.0.0.1";
    private static final int PORT = 10117;

    public static void main(String[] args) {
        try (Socket socket = new Socket(InetAddress.getByName(ADDRESS), PORT);
             var input = new DataInputStream(socket.getInputStream());
             var output = new DataOutputStream(socket.getOutputStream())) {
            log("Client started!");

            CreateDataFolder();

            Arguments arguments = parseArguments(args);
            HashMap<?, ?> request = buildRequest(arguments);

            var requestAsJson = new Gson().toJson(request);
            log("Sent: " + requestAsJson);
            output.writeUTF(requestAsJson);

            var response = input.readUTF();
            log("Received: " + response);
        } catch (UnknownHostException ignored) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<?, ?> buildRequest(Arguments arguments) throws IOException {
        HashMap parsedRequest = new HashMap<>();

        String clientDataFolderPath = System.getProperty("user.dir") + "\\src\\client\\data\\";
        Path inputFile = new File(clientDataFolderPath + arguments.fileName).toPath();

        if (arguments.fileName != null && !arguments.fileName.isBlank()) {
            parsedRequest = new Gson().fromJson(
                    Files.newBufferedReader(inputFile),
                    HashMap.class);
        } else {
            parsedRequest.put("type", arguments.command);
            parsedRequest.put("value", arguments.value);
            parsedRequest.put("key", arguments.key);
        }

        return parsedRequest;
    }

    private static Arguments parseArguments(String[] args) {
        var arguments = new Arguments();

        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        return arguments;
    }

    private static void CreateDataFolder() {
        boolean folderCreated = false;

        File dataFolder = new File("JSON Database\\task\\src\\client\\data");
        if (!dataFolder.exists()) {
            folderCreated = dataFolder.mkdirs();
        }

        if (folderCreated) {
            System.out.println("Client data folder created.");
        }
    }

    private static void log(String request) {
        System.out.println(request);
    }
}
