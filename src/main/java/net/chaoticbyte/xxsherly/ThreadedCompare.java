package net.chaoticbyte.xxsherly;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Checksum;
import org.apache.commons.codec.digest.XXHash32;

public class ThreadedCompare extends Thread {

    private final List<File> filesToCompare;

    public ThreadedCompare (List<File> pathsToCompare_) {
        this.filesToCompare = pathsToCompare_;
    }

    @Override
    public void run() {
        for (File file : filesToCompare) {

            List<File> fileArray = new ArrayList<>();
            assert fileArray != null;
            fileArray.add(file);

            // Generate Checksum
            try {
                String checksum = getChecksum(file);
                if (App.fileMap.containsKey(checksum)) {
                    fileArray.addAll(App.fileMap.get(checksum));
                    App.fileMap.put(checksum, fileArray);
                } else {
                    App.fileMap.put(checksum, fileArray);
                }
            }
            catch (IOException e) {
                System.err.println("An exception occured while processing the file " + file.getPath());
                System.err.println(e.getMessage());
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
