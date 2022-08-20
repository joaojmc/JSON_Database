package server;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    private HashMap<Object, Object> DATABASE;

    public Database() {
        File databaseFile = createFolderAndFile();
        instantiateFromFile(databaseFile);
    }

    private static File createFolderAndFile() {
        boolean folderCreated = false;
        File databaseFolder = new File(System.getProperty("user.dir") + "/src/client/data/");
        if (!databaseFolder.exists()) {
            folderCreated = databaseFolder.mkdirs();
        }

        boolean fileCreated = false;
        File databaseFile = new File(System.getProperty("user.dir") + "/src/client/data/" + "db.json");
        if (!databaseFile.exists()) {
            try {
                fileCreated = databaseFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (folderCreated && fileCreated) {
            System.out.println("Database folder and file created.");
        }
        return databaseFile;
    }

    private void instantiateFromFile(File databaseFile) {
        try {
            this.DATABASE = (HashMap<Object, Object>) new Gson().fromJson(Files.newBufferedReader(databaseFile.toPath()), HashMap.class);
            if (this.DATABASE == null) this.DATABASE = new HashMap<>();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeFile() {
        File databaseFile = new File(System.getProperty("user.dir") + "/src/client/data/" + "db.json");
        try {
            writeLock.lock();

            FileWriter fileWriter = new FileWriter(databaseFile);
            BufferedWriter writer = new BufferedWriter(fileWriter);

            new Gson().toJson(this.DATABASE, writer);
            writer.close();

            writeLock.unlock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return The passed-in key or null if the key is not present.
     */
    public Object get(Object key) {
        readLock.lock();

        Object result = null;

        if (key.toString().startsWith("[")) {
            var keyAsString = key.toString();
            var parsedKey = keyAsString.substring(1, keyAsString.length() - 1).split(",");
            LinkedTreeMap objectToCrawl = new LinkedTreeMap();

            for (var k : parsedKey) {
                if (parsedKey.length == 1) {
                    result = new Gson().toJson(this.DATABASE.get(k));
                    break;
                }
                if (k.trim().equals(parsedKey[parsedKey.length - 1].trim())) {
                    result = objectToCrawl.get(k.trim());
                } else {
                    objectToCrawl = (LinkedTreeMap) this.DATABASE.get(k.trim());
                }
            }
        } else {
            result = this.DATABASE.get(key);
        }

        readLock.unlock();

        return result;
    }

    public Object set(Object key, Object value) {
        writeLock.lock();

        LinkedTreeMap objectToCrawl = new LinkedTreeMap();

        if (key.toString().startsWith("[")) {
            var keyAsString = key.toString();
            var parsedKey = keyAsString.substring(1, keyAsString.length() - 1).split(",");
            for (var k : parsedKey) {
                if (parsedKey.length == 1 || k.trim().equals(parsedKey[parsedKey.length - 1].trim())) {
                    objectToCrawl.put(k.trim(), value);
                    break;
                } else if (objectToCrawl.size() != 0) {
                    objectToCrawl = (LinkedTreeMap) objectToCrawl.get(k.trim());
                } else {
                    objectToCrawl = (LinkedTreeMap) this.DATABASE.get(k.trim());
                }
            }
        } else {
            this.DATABASE.put(key, value);
        }

        writeLock.unlock();

        return value;
    }

    /**
     * @return The passed-in key or null if the key is not present.
     */
    public Object delete(Object key) {
        readLock.lock();

        LinkedTreeMap objectToCrawl = new LinkedTreeMap();

        if (key.toString().startsWith("[")) {
            var keyAsString = key.toString();
            var parsedKey = keyAsString.substring(1, keyAsString.length() - 1).split(",");
            for (var k : parsedKey) {
                if (parsedKey.length == 1 || k.trim().equals(parsedKey[parsedKey.length - 1].trim())) {
                    objectToCrawl.remove(k.trim());
                    break;
                } else if (objectToCrawl.size() != 0) {
                    objectToCrawl = (LinkedTreeMap) objectToCrawl.get(k.trim());
                } else {
                    objectToCrawl = (LinkedTreeMap) this.DATABASE.get(k.trim());
                }
            }
        } else {
            this.DATABASE.remove(key);
        }

//        this.DATABASE.remove(key);

        readLock.unlock();

        return key;
    }
}
