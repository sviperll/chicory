package com.github.sviperll;

import com.github.sviperll.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class Credentials {
    public static Credentials readPasswordFile(File passwordFile, Charset charset) throws IOException {
        String contents = Files.read(passwordFile, charset);
        String[] fields = contents.trim().split(":", -1);
        if (fields.length != 2)
            throw new IOException("Wrong password file format: there should be at least on line of <user>:<password> format");
        return new Credentials(fields[0], fields[1]);
    }

    private final String userName;
    private final String password;

    public Credentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String userName() {
        return userName;
    }

    public String password() {
        return password;
    }
}
