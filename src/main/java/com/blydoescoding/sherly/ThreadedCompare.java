package com.blydoescoding.sherly;

import java.io.*;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

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
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        FileInputStream inputStream = new FileInputStream(file);

        byte[] dataBytes = new byte[1024];
        int unread = 0;
        while ((unread = inputStream.read(dataBytes)) != -1) {
            messageDigest.update(dataBytes, 0, unread);
        }

        inputStream.close();

        // get digest & create hexadecimal represenation
        byte[] digestBytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for (byte digestByte : digestBytes) {
            stringBuilder.append(Integer.toString((digestByte & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }
}
