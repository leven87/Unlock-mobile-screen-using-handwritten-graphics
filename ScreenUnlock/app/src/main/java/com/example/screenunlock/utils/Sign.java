package com.example.screenunlock.utils;

import com.csvreader.CsvReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import joinery.DataFrame;

public class Sign {

    // List onlinePhoneInfo = new ArrayList();
    // ArrayList<ArrayList<Object>> signData;
    public DataFrame<Object> df;
    double minX;
    double minY;
    double maxX;
    double maxY;
    double avernormalX;
    double avernormalY;

    private int dataLength = 0;

    public Sign(String filePath) throws IOException {
        read(filePath);
    }

    public void read(String filePath) throws IOException {
        CsvReader reader = new CsvReader(filePath, ' ', Charset.forName("UTF-8"));
        reader.readHeaders(); // skip header
        reader.readRecord();

        ArrayList<String> cols = new ArrayList<String>(Arrays.asList("X", "Y", "TStamp", "Pres", "EndPts"));
        df = new DataFrame<>(cols);

        // X, Y, TStamp, Pres., EndPts, TStamp2, NormalX, NormalY
        // 0, 1, 2, 3, 4, 5, 6, 7
        while (reader.readRecord()) {
            String[] values = reader.getValues();
            df.append((Arrays.asList(values)));
        }
        reader.close();
        df.convert();

        this.dataLength = df.length();
    }

    public static void main(String[] args) throws IOException {
        String filePath = "J:\\thesis_project\\ScreenUnlock\\app\\src\\main\\assets\\dataset\\202084182520.sig";
        Sign sign = new Sign(filePath);
        DataFrame<Object> re_df = PreProcess.preProcess(sign.getDf(), sign.getDataLength());
        System.out.print(re_df);
    }

    /**
     * print the data frame
     * @param df
     */
    private void printDf(DataFrame<Object> df) {
        Set<Object> indexs = df.index();
        Set<Object> columns = df.columns();
        for (Object index : indexs) {
            for (Object column : columns) {
                System.out.print(df.get(index, column));
                System.out.print("\t");
            }
            System.out.println();
        }
    }

    public DataFrame<Object> getDf() {
        return this.df;
    }

    public int getDataLength() {
        return this.dataLength;
    }

    public DataFrame<Object> preProcess(Integer length) {
        this.df = PreProcess.preProcess(this.df, length);
        return this.df;
    }
}
