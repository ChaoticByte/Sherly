package net.chaoticbyte.xxsherly;

import java.io.*;
import java.util.zip.Checksum;
import org.apache.commons.codec.digest.XXHash32;

public class FileChecksum {

    //this is used to get the MD5 String of one of the files (one of them is just fine since they both have the same value)
    public static String getChecksum (File file) throws IOException {

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
