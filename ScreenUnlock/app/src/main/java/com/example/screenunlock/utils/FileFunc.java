package com.example.screenunlock.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

public class FileFunc {

    public static int calFilesMaxLength(ArrayList<String> signPaths) {
        int maxLength = 0;
        for (int i = 0; i < signPaths.size(); i++) {
            // for total number of lines in the File with Files.lines
            try {
                File file = new File(signPaths.get(i));
                if (file.exists()) {
                    long fileLength = file.length();
                    LineNumberReader lineNumberReader = new LineNumberReader(new FileReader(file));
                    lineNumberReader.skip(fileLength);
                    int lines = lineNumberReader.getLineNumber();
                    lineNumberReader.close();
                    if (lines > maxLength) {
                        maxLength = lines;
                    }
                } else {
                    System.out.println("File does not exists!,path:" + signPaths.get(i));
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        return maxLength - 2;
    }
}
