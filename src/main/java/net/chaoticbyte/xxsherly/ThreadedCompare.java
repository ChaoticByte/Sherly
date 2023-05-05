package net.chaoticbyte.xxsherly;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;
import org.apache.commons.codec.digest.XXHash32;

public class ThreadedCompare extends Thread {

    private final List<Path> pathsToCompareTo;

    public ThreadedCompare (List<Path> pathsToCompareTo) {
        this.pathsToCompareTo = pathsToCompareTo;
    }

    @Override
    public void run() {
        for (Path file : pathsToCompareTo) {
            List<Path> fileArray = new ArrayList<>();
            assert fileArray != null;
            fileArray.add(file);
            String checksum;
            try {
                checksum = getChecksum(file.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (App.fileMap.containsKey(checksum)) {
                fileArray.addAll(App.fileMap.get(checksum));
                App.fileMap.put(checksum, fileArray);
            } else {
                App.fileMap.put(checksum, fileArray);
            }
            App.progress++;
        }
        App.completedThreads++;
    }

    //this is used to get the MD5 String of one of the files (one of them is just fine since they both have the same value)
    private String getChecksum (File file) throws IOException {

        String digest = "";

        // Calculate xxHash32 and add it's hexadecimal presentation to the digest

        Checksum xxHash = new XXHash32();
        FileInputStream inputStream = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];
        int unread = 0;
        while ((unread = inputStream.read(dataBytes)) != -1) {
            xxHash.update(dataBytes, 0, unread);
        }
        inputStream.close();
        digest += Long.toHexString(xxHash.getValue());

        // Add File length to the digest

        digest += Long.toHexString(file.length());

        // return result
        return digest;
    }
}
