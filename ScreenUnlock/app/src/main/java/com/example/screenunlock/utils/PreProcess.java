package com.example.screenunlock.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import joinery.DataFrame;

public class PreProcess {
    public static DataFrame<Object> preProcess(DataFrame<Object> df, Integer length) {
        // add TStamp2
        long startTime = (Long) df.get(0, 2);
        List TStamp = df.col("TStamp");
        List<Object> TStamp2 = new ArrayList<>();
        for (int i = 0; i < TStamp.size(); i++) {
            TStamp2.add(((Number) TStamp.get(i)).doubleValue() - ((Number) startTime).doubleValue());
        }
        df.add("TStamp2", TStamp2);

        df = PreProcess.removeDuplicatedPoint(df);
        df = PreProcess.addSigFeature(df);
        df = PreProcess.normalizeImage(df);
        df = PreProcess.addFirstOrderDerivation(df);
        df = PreProcess.lengthNorm(df, length);
        df = PreProcess.standardizeDf(df);
        return df;
    }

    /**
     * normalize the sign image for position and size.
     *
     * @param df
     * @return df
     */
    public static DataFrame<Object> normalizeImage(DataFrame<Object> df) {
        // size normalization
        double widthX = 500; // width
        double heightY = 200; // height
        double minX = (Double) df.min().col("X").get(0);
        double maxX = (Double) df.max().col("X").get(0);
        double minY = (Double) df.min().col("Y").get(0);
        double maxY = (Double) df.max().col("Y").get(0);

        List X = df.col("X");
        List<Object> normalX = new ArrayList<>();
        for (int i = 0; i < X.size(); i++) {
            normalX.add(widthX * (((double) X.get(i) - minX) / (double) (maxX - minX)));
        }
        df.add("normalX", normalX);

        List Y = df.col("Y");
        List<Object> normalY = new ArrayList<>();
        for (int i = 0; i < Y.size(); i++) {
            normalY.add(heightY * (((Double) Y.get(i) - minY) / (maxY - minY)));
        }
        df.add("normalY", normalY);

        double avernormalX = (Double) df.mean().col("normalX").get(0);
        double avernormalY = (Double) df.mean().col("normalY").get(0);

        // position normalization
        normalX = df.col("normalX");
        for (int i = 0; i < normalX.size(); i++) {
            double v = (Double) normalX.get(i);
            v = v - avernormalX;
            df.set(i, "normalX", v);
        }
        normalY = df.col("normalY");
        for (int i = 0; i < normalY.size(); i++) {
            double v = (Double) normalY.get(i);
            v = v - avernormalY;
            df.set(i, "normalY", v);
        }

        return df;
    }

    /**
     * length normalization
     *
     * @param df  dataframe
     * @param length default is 400
     * @return
     */
    public static DataFrame<Object> lengthNorm(DataFrame<Object> df, Integer length) {
        int data_size = df.length();
        float interval = (float) (data_size - 1) / length;
        float start = 0;
        DataFrame<Object> df_new = new DataFrame<>(df.columns());
        System.out.print(df_new.columns());
        for (float dist = start; dist <= data_size - 1; dist = dist + interval) {
            int first = (int) Math.floor(dist);
            int second = (int) Math.ceil(dist);
            if (second >= data_size) {
                second = data_size - 1;
            }
            double percent = (dist - (float) first) / 1.0;

            Set<Object> colNames = df.columns();

            ArrayList<Double> features = new ArrayList<Double>();
            for (Object o : colNames) {
                String name = (String) o;
                double feature;
                if (name.equals("TStamp") || name.equals("Pres")) {
                    feature = (Long) df.get(first, name)
                            + ((Long) df.get(second, name) - (Long) df.get(first, name)) * percent;
                } else if (name.equals("EndPts")) {
                    feature = 0;
                } else {
                    feature = (Double) df.get(first, name)
                            + ((Double) df.get(second, name) - (Double) df.get(first, name)) * percent;
                }
                features.add(feature);
            }
            df_new.append(features);
            if (df_new.length() >= length) {
                break;
            }
        }
        return df_new;
    }

    private static DataFrame<Object> standardizeDf(DataFrame<Object> df) {
        List<Double> TStamp2;
        TStamp2 = (List<Double>) (List) df.col("TStamp2");

        for (int i = 0; i < df.size(); i++) {
            Double meanVal = ((Number) df.mean().col(i).get(0)).doubleValue();
            Double stdVal = ((Number) df.stddev().col(i).get(0)).doubleValue();
            for (int j = 0; j < df.length(); j++) {
                if (meanVal == 0 && stdVal == 0) { // for special case, like pressure having no value
                    df.set(j, i, 0);
                } else {
                    // System.out.print(meanVal + "," + stdVal);
                    Number e = (Number) df.get(j, i);
                    e = (e.doubleValue() - meanVal) / stdVal;
                    df.set(j, i, (Object) e);
                }
            }
        }
        return df;
        // DataFrame<Object> normalized_df = (df - df.mean()) / df.stddev();
        // TStamp2 = df['TStamp2'].tolist()
        // TStamp = df['TStamp'].tolist()
        // normalized_df=(df-df.mean())/df.std()
        // # normalized_df=(df-df.min())/(df.max()-df.min())
        // normalized_df['TStamp2'] = TStamp2
        // normalized_df['TStamp'] = TStamp

        // return normalized_df;
    }

    /**
     * add new features to signals
     * @param df
     * @return
     */
    private static DataFrame<Object> addSigFeature(DataFrame<Object> df) {
        ArrayList<Double> dX = PreProcess.derivation((List<Object>) df.col("X")); // dx
        ArrayList<Double> dY = PreProcess.derivation((List<Object>) df.col("Y")); // dy
        ArrayList<Double> Vel = new ArrayList<Double>(Collections.nCopies(df.length(), 0.0)); // velocity
        ArrayList<Double> Angle = new ArrayList<Double>(Collections.nCopies(df.length(), 0.0)); // angle
        Integer T = df.length() - 1;
        Integer t = 1;
        while (t <= T) {
            Vel.set(t, Math.sqrt(dX.get(t) * dX.get(t) + dY.get(t) * dY.get(t)));
            if (dY.get(t) != 0 && dX.get(t) != 0) {
                Angle.set(t, Math.atan(dY.get(t) / dX.get(t)));
            } else if (dX.get(t) == 0) {
                Angle.set(t, Math.atan(dY.get(t) / 0.01));
            } else {
                Angle.set(t, 0.0);
            }

            t += 1;
        }

        ArrayList<Double> dAngle = PreProcess.derivation((List<Object>) (ArrayList) Angle);
        ArrayList<Double> dVel = PreProcess.derivation((List<Object>) (ArrayList) Vel);
        ArrayList<Double> Logcr = new ArrayList<Double>(Collections.nCopies(df.length(), 0.0));
        ArrayList<Double> Tam = new ArrayList<Double>(Collections.nCopies(df.length(), 0.0)); // 加速度 Acceleration

        t = 1;
        while (t <= T) {
            Logcr.set(t, Math.log((Math.abs(Vel.get(t)) + 0.01) / ((Math.abs(dAngle.get(t)) + 0.01))));
            Tam.set(t, Math.sqrt(dVel.get(t) * dVel.get(t) + Vel.get(t) * Vel.get(t) * dAngle.get(t) * dAngle.get(t)));
            t += 1;
        }

        df.add("Angle", Arrays.<Object> asList(Angle.toArray()));
        df.add("Vel", Arrays.<Object> asList(Vel.toArray()));
        df.add("Logcr", Arrays.<Object> asList(Logcr.toArray()));
        df.add("Tam", Arrays.<Object> asList(Tam.toArray()));
        return df;
    }

    /**
     * # calculate the derivation of the discrete sequence
     * @param signal_l  List<Object>
     * @return
     */
    private static ArrayList<Double> derivation(List<Object> signal_l) {
        // List to ArrayList
        ArrayList<Object> signal = new ArrayList<>(signal_l.size());
        signal.addAll(signal_l);

        Integer T = signal.size() - 1;
        ArrayList<Double> dsignal = new ArrayList<Double>(Collections.nCopies(signal.size(), 0.0));
        dsignal.set(0, (2 * (Double) signal.get(2) + (Double) signal.get(1) - 3 * (Double) signal.get(0)) / 5.0);
        dsignal.set(1, (2 * (Double) signal.get(3) + (Double) signal.get(2) - 2 * (Double) signal.get(1)
                - (Double) signal.get(0)) / 6.0);
        Integer t = 2;

        while (t <= T - 2) {
            dsignal.set(t, (2 * (Double) signal.get(t + 2) + (Double) signal.get(t + 1) - (Double) signal.get(t - 1)
                    - 2 * (Double) signal.get(t - 2)) / 10.0);
            t += 1;
        }

        dsignal.set(T - 1, ((Double) signal.get(T) - (Double) signal.get(T - 2) + 2 * (Double) signal.get(T - 1)
                - 2 * (Double) signal.get(T - 3)) / 6.0);
        dsignal.set(T,
                (3 * (Double) signal.get(T) - (Double) signal.get(T - 1) - 2 * (Double) signal.get(T - 2)) / 5.0);

        return dsignal;
    }

    /**
     * remove duplicated point
      * @param df
     * @return
     */
    private static DataFrame<Object> removeDuplicatedPoint(DataFrame<Object> df) {
        DataFrame<Object> df_new = new DataFrame<>(df.columns());
        double old_x = (Double) df.get(0, "X");
        double old_y = (Double) df.get(0, "Y");

        for (int i = 0; i < df.length(); i++) {
            if ((Double) df.get(i, "X") != old_x || (Double) df.get(i, "Y") != old_y || i == 0) {
                df_new.append(df.row(i));
                old_x = (Double) df.get(i, "X");
                old_y = (Double) df.get(i, "Y");
            }
            // update the EndPts if the point is
            else if ((Double) df.get(i, "X") == old_x && (Double) df.get(i, "Y") == old_y
                    && (Long) df.get(i, "EndPts") == 1) {
                df_new.set(df_new.length() - 1, "EndPts", 1);
            }
        }
        return df_new;
    }

    /*
     * calculate the first order derivation for all 6 feature
     */
    private static DataFrame<Object> addFirstOrderDerivation(DataFrame<Object> df) {
        // ArrayList<Double> dX = PreProcess.derivation((List<Object>) df.col("X")); // dx
        ArrayList<Double> d_X = PreProcess.derivation((List<Object>) df.col("X"));
        ArrayList<Double> d_Y = PreProcess.derivation((List<Object>) df.col("Y"));
        ArrayList<Double> d_Angle = PreProcess.derivation((List<Object>) df.col("Angle"));
        ArrayList<Double> d_Vel = PreProcess.derivation((List<Object>) df.col("Vel"));
        ArrayList<Double> d_Logcr = PreProcess.derivation((List<Object>) df.col("Logcr"));
        ArrayList<Double> d_Tam = PreProcess.derivation((List<Object>) df.col("Tam"));

        df.add("d_X", Arrays.<Object> asList(d_X.toArray()));
        df.add("d_Y", Arrays.<Object> asList(d_Y.toArray()));
        df.add("d_Angle", Arrays.<Object> asList(d_Angle.toArray()));
        df.add("d_Vel", Arrays.<Object> asList(d_Vel.toArray()));
        df.add("d_Logcr", Arrays.<Object> asList(d_Logcr.toArray()));
        df.add("d_Tam", Arrays.<Object> asList(d_Tam.toArray()));
        return df;
    }
}
