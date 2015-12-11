package cz.sodae.doornock.utils;

import java.io.*;
import java.security.*;
import java.security.spec.*;

public class FileLoader
{

    static final String ALGORITHM = "SHA1WithRSA";


    public static byte[] loadFile(String filename) throws IOException {
        File f = new File(filename);
        FileInputStream fis = new FileInputStream(f);
        DataInputStream dis = new DataInputStream(fis);
        byte[] keyBytes = new byte[(int)f.length()];
        dis.readFully(keyBytes);
        dis.close();
        return keyBytes;
    }

}
