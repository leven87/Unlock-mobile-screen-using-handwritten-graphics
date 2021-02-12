package com.example.screenunlock.utils;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class RecordFile {
    public static final String RECORD_FILE_NAME = "recordSignPath.txt";
    public static final String RECORD_FILE_NAME_NEW = "recordSignPathNew.txt";

    // acquire the storage path of system picture
    public static final File DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

    /**
     * save sign path to a record file
     *
     * @param signPath
     * @param recordPath
     * @return
     */
    public static Object[] saveNewSignFilePath(String signPath, String recordPath) {
        // check sign file
        File sign = new File(signPath);
        if (!sign.exists()) {
            return new Object[] { false, "The sign file doesn't exist!" };
        }

        // check record file
        File record = new File(recordPath + RecordFile.RECORD_FILE_NAME_NEW);
        if (!record.exists()) {
            try {
                record.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fos = new FileOutputStream(record, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            osw.write(signPath + "\r\n");
            osw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Object[] { true, "" };
    }

    /**
     * Replace the old record file by new file
     */
    public static boolean replace() {
        String oldFilePath = RecordFile.DIR_PATH.getPath() + "/" + RecordFile.RECORD_FILE_NAME;
        String newFilePath = RecordFile.DIR_PATH.getPath() + "/" + RecordFile.RECORD_FILE_NAME_NEW;

        if (RecordFile.deleteFile(oldFilePath) && RecordFile.renameFile(newFilePath, oldFilePath)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);

        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                // System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                // System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean renameFile(String oldname, String newname) {
        File oldfile = new File(oldname);
        File newfile = new File(newname);
        if (!oldfile.exists()) {
            return false;// old file not exists!
        }
        if (!oldname.equals(newname)) {// check new name and old name are not the same
            if (newfile.exists())// check no existed file has the new name
                return false;
            else {
                oldfile.renameTo(newfile);
                return true;
            }
        } else {
            return true;
        }
    }

    public static ArrayList<String> getSignPaths() throws IOException {
        String path = RecordFile.DIR_PATH.getPath() + "/" + RecordFile.RECORD_FILE_NAME;
        File file = new File(path);
        ArrayList<String> signPaths = new ArrayList<>();

        if (!file.exists()) {
            return signPaths;// file not exists!
        }

        InputStreamReader reader = null; // build object reader
        try {
            reader = new InputStreamReader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader br = new BufferedReader(reader); // build an object, whick could transfer the content readable by
                                                        // computer
        String line = br.readLine();
        while (line != null && line != "") {
            signPaths.add(line);
            line = br.readLine(); // read one line each time
        }
        return signPaths;
    }
}
