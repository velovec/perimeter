package ru.v0rt3x.perimeter.server.storage;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PerimeterStorageFile {

    private final Map<String, byte[]> storageMap;
    private final Object storageLock = new Object();

    private final Path storageFile;

    private byte[] storageKey;

    private static final byte[] MAGIC_HEADER = "PDSv1.0".getBytes();
    private static final Logger logger = LoggerFactory.getLogger(PerimeterStorageFile.class);

    public PerimeterStorageFile(Path storagePath, byte[] storageKeyBytes) throws IOException {
        storageFile = storagePath;

        storageMap = new ConcurrentHashMap<>();
        storageKey = storageKeyBytes;

        readStorage();
    }

    private byte[] encode(byte[] inputData) {
        byte[] result = new byte[inputData.length];

        for (int elementID = 0; elementID < inputData.length; elementID++) {
            result[elementID] = (byte) (inputData[elementID] ^ storageKey[elementID % storageKey.length]);
        }

        return result;
    }

    private Integer calculateStorageSize() {
        int totalSize = 0;
        if (storageMap != null) {
            for (String key : storageMap.keySet()) {
                totalSize += 8 + key.getBytes().length + storageMap.get(key).length;
            }
        }
        return totalSize;
    }

    private void readStorage() {
        if (!Files.exists(storageFile)) {
            logger.info("Creating empty storage file: {}", storageFile);

            try {
                writeStorage();
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to create StorageFile: %s", e.getMessage()));
            }
        }

        synchronized (storageLock) {
            storageMap.clear();

            try (GZIPInputStream storageReader = new GZIPInputStream(Files.newInputStream(storageFile))) {
                byte[] RAW_MAGIC_HEADER = new byte[MAGIC_HEADER.length];
                IOUtils.readFully(storageReader, RAW_MAGIC_HEADER);

                if (!Arrays.equals(MAGIC_HEADER, RAW_MAGIC_HEADER))
                    throw new IllegalStateException("Invalid magic header in StorageFile");

                byte[] storageLengthBytes = new byte[4];
                IOUtils.readFully(storageReader, storageLengthBytes);
                Integer storageLength = ByteBuffer.wrap(storageLengthBytes).getInt();

                byte[] storageArray = new byte[storageLength];
                IOUtils.readFully(storageReader, storageArray);
                ByteBuffer storageBuffer = ByteBuffer.wrap(storageArray);

                Integer storageBytesRead = 0;
                while (storageBytesRead < storageLength) {
                    int keyLength = storageBuffer.getInt();
                    int valueLength = storageBuffer.getInt();

                    byte[] keyArray = new byte[keyLength];
                    byte[] valueArray = new byte[valueLength];

                    storageBuffer.get(keyArray);
                    storageBuffer.get(valueArray);

                    String keyName = new String(encode(keyArray));
                    byte[] valueData = encode(valueArray);

                    storageMap.put(keyName, valueData);
                    storageBytesRead += 8 + keyLength + valueLength;
                }
            } catch (IOException e) {
                throw new IllegalStateException(String.format("Unable to read StorageFile: %s", e.getMessage()));
            }
        }
    }

    public void writeStorage() throws IOException {
        synchronized (storageLock) {
            try(GZIPOutputStream storageWriter = new GZIPOutputStream(Files.newOutputStream(storageFile))) {
                storageWriter.write(MAGIC_HEADER);

                Integer storageSize = calculateStorageSize();
                ByteBuffer storageBuffer = ByteBuffer.allocate(storageSize + 4);

                storageBuffer.putInt(storageSize);

                for (String keyName : storageMap.keySet()) {
                    byte[] key = keyName.getBytes();
                    byte[] value = storageMap.get(keyName);

                    storageBuffer.putInt(key.length);
                    storageBuffer.putInt(value.length);

                    storageBuffer.put(encode(key));
                    storageBuffer.put(encode(value));
                }

                storageWriter.write(storageBuffer.array());
                storageWriter.flush();
            }
        }
    }

    public void clearStorage() throws IOException {
        storageMap.clear();
        writeStorage();
    }

    public boolean hasKey(String key) {
        return storageMap.containsKey(key);
    }

    public ByteBuffer getKey(String key) {
        return hasKey(key) ? ByteBuffer.wrap(storageMap.get(key)) : null;
    }

    public void putKey(String key, ByteBuffer value) {
        storageMap.put(key, value.array());
    }

    public void deleteKey(String key) {
        if (hasKey(key)) {
            storageMap.remove(key);
        }
    }

    public Set<String> listKeys() {
        return storageMap.keySet();
    }

    public byte[] getBytes(String key) {
        return hasKey(key) ? getKey(key).array() : null;
    }

    public void putBytes(String key, byte[] value) {
        putKey(key, ByteBuffer.wrap(value));
    }

    public Integer getInt(String key) {
        return hasKey(key) ? getKey(key).getInt() : null;
    }

    public void putInt(String key, Integer value) {
        putKey(key, ByteBuffer.allocate(4).putInt(value));
    }

    public Long getLong(String key) {
        return hasKey(key) ? getKey(key).getLong() : null;
    }

    public void putLong(String key, Long value) {
        putKey(key, ByteBuffer.allocate(8).putLong(value));
    }

    public Float getFloat(String key) {
        return hasKey(key) ? getKey(key).getFloat() : null;
    }

    public void putFloat(String key, Float value) {
        putKey(key, ByteBuffer.allocate(4).putFloat(value));
    }

    public Double getDouble(String key) {
        return hasKey(key) ? getKey(key).getDouble() : null;
    }

    public void putDouble(String key, Double value) {
        putKey(key, ByteBuffer.allocate(8).putDouble(value));
    }

    public Byte getByte(String key) {
        return hasKey(key) ? getKey(key).get() : null;
    }

    public void putByte(String key, Byte value) {
        putKey(key, ByteBuffer.allocate(1).put(value));
    }

    public String getString(String key) {
        return hasKey(key) ? new String(getKey(key).array()) : null;
    }

    public void putString(String key, String value) {
        putKey(key, ByteBuffer.wrap(value.getBytes()));
    }

    public Boolean getBool(String key) {
        return hasKey(key) && getKey(key).get() != 0;
    }

    public void putBool(String key, Boolean value) {
        putKey(key, ByteBuffer.allocate(1).put((byte) (value ? 1 : 0)));
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key, Class<T> objectClass) throws IOException, ClassNotFoundException {
        if (hasKey(key)) {
            ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(getBytes(key)));
            return (T) inputStream.readObject();
        }
        return null;
    }

    public <T> void putObject(String key, T object) throws IOException {
        ByteArrayOutputStream outputArray = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(outputArray);
        outputStream.writeObject(object);
        putBytes(key, outputArray.toByteArray());
    }
}