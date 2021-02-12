package com.example.screenunlock.utils;

import android.util.Log;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.dtw.TimeWarpInfo;
import com.fastdtw.dtw.WarpPath;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesPoint;
import com.fastdtw.util.Distances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import joinery.DataFrame;

public class Verify {

    private static double minSampleDistance = 0.0;
    private static double maxSampleDistance = 0.0;
    private static double meanSampleDistance = 0.0;
    private static double stdSampleDistance = 0.0; // 標準差 Standard deviation
    private static double coefVar = 0.0; // 离散系数 Coefficient of Variation
    private static final double COEF_VAR_THRESHOLD = 0.15; // 离散系数阈值 Coefficient of Variation threshold
    private static final double EXTREME_VAR_THRESHOLD = 0.25; // 极值系数阈值 Extreme value coefficient

    public static int dfsTrainingMaxLength = 0;
    public static double extremeVar = 0.0;
    public static double distThreshold = 0.0;
    public static double timeThreshold = 0.0;
    public static boolean referDSetTrained = false;
    public static ArrayList<DataFrame<Object>> dfListsSample = new ArrayList<DataFrame<Object>>();

    /**
     * calculate the dtw distance of  signatures in record file
     * @return
     */

    public static Object[] calDtwDistanceEntrance() throws IOException {
        Verify.referDSetTrained = false;
        ArrayList<String> signPaths = RecordFile.getSignPaths();
        if (signPaths.size() <= 1) {
            return new Object[] { false, "Sorry! Graphic number is not created or not enough!", 0 };
        }

        // calculate max data length of training set
        int dfsMaxLength = FileFunc.calFilesMaxLength(signPaths);
        Verify.dfsTrainingMaxLength = dfsMaxLength;
        dfListsSample = new ArrayList<DataFrame<Object>>();
        ArrayList<Double> dfTimes = new ArrayList<Double>();
        // create dfLists by path
        for (int i = 0; i < signPaths.size(); i++) {
            Sign sign = new Sign(signPaths.get(i));
            sign.preProcess(dfsMaxLength);
            DataFrame<Object> df = sign.getDf();
            dfListsSample.add(df);
            dfTimes.add((double) df.get(df.length() - 1, "TStamp2"));
        }

        ArrayList<Double> distances = new ArrayList<Double>();
        Double distSum = 0.0;
        for (int i = 0; i < dfListsSample.size(); i++) {
            for (int j = i + 1; j <= dfListsSample.size() - 1; j++) {
                Double distance = Verify.calDtwDistance(dfListsSample.get(i), dfListsSample.get(j));
                distances.add(distance);
                distSum += distance;
            }
        }

        CalMath calMath = new CalMath();
        // get statistic of distances
        Verify.meanSampleDistance = distSum / distances.size();
        Verify.minSampleDistance = Collections.min(distances);
        Verify.maxSampleDistance = Collections.max(distances);
        Double[] arr = new Double[distances.size()];
        arr = distances.toArray(arr);
        Verify.stdSampleDistance = calMath.Sample_STD_dev(arr);

        Verify.coefVar = Verify.stdSampleDistance / Verify.meanSampleDistance;
        Verify.extremeVar = (Verify.maxSampleDistance - Verify.minSampleDistance)
                / ((Verify.maxSampleDistance + Verify.minSampleDistance) / 2);

        Log.i("My activity", "Mean Distance: " + Verify.meanSampleDistance);
        Log.i("My activity", "Min Distance: " + Verify.minSampleDistance);
        Log.i("My activity", "Max Distance: " + Verify.maxSampleDistance);
        Log.i("My activity", "Standard deviation: " + Verify.stdSampleDistance);
        Log.i("My activity", "Coefficient of variation: " + Verify.coefVar);
        Log.i("My activity", "Extreme distance coefficient: " + Verify.extremeVar);

        // validate the training data to see if they are qualified
        if (Verify.coefVar > Verify.COEF_VAR_THRESHOLD)
            return new Object[] { false, String.format("Fail! Coefficient of variation is " + Verify.coefVar
                    + ", over threshold! Please re-entry graphics!"), Verify.minSampleDistance };
        else if (Verify.extremeVar > Verify.EXTREME_VAR_THRESHOLD) {
            return new Object[] { false, String.format("Fail! Extreme distance coefficient is " + Verify.extremeVar
                    + ", over threshold! Please re-entry graphics!"), Verify.minSampleDistance };
        }

        // set the time threshold for validate the test sets
        Verify.timeThreshold = 2 * Collections.max(dfTimes);

        // set the distance threshold for validate the test sets
        double buffer = 0.0;
        // 这其实就是离散系数小于0.1，选择均值，反之，选择标准差
        if (Verify.stdSampleDistance * 2 < 0.2 * Verify.meanSampleDistance) {
            buffer = 0.2 * Verify.meanSampleDistance;
            System.out.print("choose mean as buffer");
        } else {
            buffer = 2 * Verify.stdSampleDistance;
            System.out.print("choose SD as buffer");
        }
        Verify.distThreshold = Verify.meanSampleDistance + buffer;
        System.out.print("distThreshold is: : " + Verify.distThreshold);
        Verify.referDSetTrained = true;
        return new Object[] { true, "Success!", Verify.distThreshold };
    }

    public static double calDtwDistance(DataFrame<Object> df1, DataFrame<Object> df2) {

        List X1 = df1.col("normalX");
        List Y1 = df1.col("normalY");
        List Angle1 = df1.col("Angle");
        List Vel1 = df1.col("Vel");
        List Logcr1 = df1.col("Logcr");
        List Tam1 = df1.col("Tam");
        List X2 = df2.col("normalX");
        List Y2 = df2.col("normalY");
        List Angle2 = df2.col("Angle");
        List Vel2 = df2.col("Vel");
        List Logcr2 = df2.col("Logcr");
        List Tam2 = df2.col("Tam");

        List d_X1 = df1.col("d_X");
        List d_Y1 = df1.col("d_Y");
        List d_Angle1 = df1.col("d_Angle");
        List d_Vel1 = df1.col("d_Vel");
        List d_Logcr1 = df1.col("d_Logcr");
        List d_Tam1 = df1.col("d_Tam");
        List d_X2 = df2.col("d_X");
        List d_Y2 = df2.col("d_Y");
        List d_Angle2 = df2.col("d_Angle");
        List d_Vel2 = df2.col("d_Vel");
        List d_Logcr2 = df2.col("d_Logcr");
        List d_Tam2 = df2.col("d_Tam");

        TimeSeriesBase.Builder b1 = TimeSeriesBase.builder();
        TimeSeriesBase.Builder b2 = TimeSeriesBase.builder();
        // System.out.print(df1.length());
        // System.out.print(df2.length());
        for (int i = 0; i < df1.length(); i++) {
            TimeSeriesPoint p1 = new TimeSeriesPoint(new double[] { (double) X1.get(i), (double) Y1.get(i),
                    (double) Angle1.get(i), (double) Vel1.get(i), (double) Logcr1.get(i), (double) Tam1.get(i),
                    (double) d_X1.get(i), (double) d_Y1.get(i), (double) d_Angle1.get(i), (double) d_Vel1.get(i),
                    (double) d_Logcr1.get(i), (double) d_Tam1.get(i) });
            b1.add(i, p1);
            TimeSeriesPoint p2 = new TimeSeriesPoint(new double[] { (double) X2.get(i), (double) Y2.get(i),
                    (double) Angle2.get(i), (double) Vel2.get(i), (double) Logcr2.get(i), (double) Tam2.get(i),
                    (double) d_X2.get(i), (double) d_Y2.get(i), (double) d_Angle2.get(i), (double) d_Vel2.get(i),
                    (double) d_Logcr2.get(i), (double) d_Tam2.get(i) });
            b2.add(i, p2);

        }
        TimeSeries ts1 = b1.build();
        TimeSeries ts2 = b2.build();

        TimeWarpInfo result = FastDTW.compare(ts1, ts2, 12, Distances.EUCLIDEAN_DISTANCE);
        double distance = result.getDistance();
        WarpPath dtwPath = result.getPath();
        System.out.println(distance);
        // System.out.print(dtwPath);
        return distance;
    }

    /**
     * Verify the sample  to see if it is forgery or original
     * @param verifySignPath
     * @para    threshold
     * @return
     */
    public static boolean verifySignature(String verifySignPath) throws IOException {
        ArrayList<String> signPaths = RecordFile.getSignPaths();
        if (signPaths.size() <= 1) {
            return false;
        }

        Sign verifySign = new Sign(verifySignPath);
        verifySign.preProcess(Verify.dfsTrainingMaxLength);
        DataFrame<Object> dfVerify = verifySign.getDf();

        // verify time duration within threshold
        if ((double) dfVerify.get(dfVerify.length() - 1, "TStamp2") > Verify.timeThreshold) {
            return false;
        }

        ArrayList<Double> distances = new ArrayList<Double>();
        Double distSum = 0.0;
        for (int i = 0; i < Verify.dfListsSample.size(); i++) {
            Double distance = Verify.calDtwDistance(Verify.dfListsSample.get(i), dfVerify);
            distances.add(distance);
            distSum += distance;
        }

        // get mean,min,max of distances
        Double distMean = distSum / distances.size();
        Double distMin = Collections.min(distances);
        Double distMax = Collections.max(distances);

        Log.i("My activity", "Time Threshold: " + Verify.timeThreshold);
        Log.i("My activity", "Distance Threshold: " + Verify.distThreshold);
        System.out.println("Test distMean:" + distMean);
        if (distMean <= Verify.distThreshold) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath1 = "J:\\thesis_project\\ScreenUnlock\\app\\src\\main\\assets\\dataset\\202084182520.sig";
        String filePath2 = "J:\\thesis_project\\ScreenUnlock\\app\\src\\main\\assets\\dataset\\202084182525.sig";
        String filePath3 = "J:\\thesis_project\\ScreenUnlock\\app\\src\\main\\assets\\dataset\\202084182532.sig";
        String filePath4 = "J:\\thesis_project\\ScreenUnlock\\app\\src\\main\\assets\\dataset\\202084182540.sig";

        ArrayList<String> signPaths = new ArrayList<String>();
        signPaths.add(filePath1);
        signPaths.add(filePath2);
        // signPaths.add(filePath3);
        // signPaths.add(filePath4);

        int dfsMaxLength = FileFunc.calFilesMaxLength(signPaths);
        Verify.dfsTrainingMaxLength = dfsMaxLength;

        ArrayList<DataFrame<Object>> dfLists = new ArrayList<DataFrame<Object>>();
        // create dfLists by path
        for (int i = 0; i < signPaths.size(); i++) {
            Sign sign = new Sign(signPaths.get(i));
            sign.preProcess(dfsMaxLength);
            DataFrame<Object> df = sign.getDf();
            dfLists.add(df);
        }

        // System.out.print(dfLists.get(1).col("Vel").size());
        // System.out.print(dfLists.get(1).col("Vel"));
        ArrayList<Double> distances = new ArrayList<Double>();
        Double distSum = 0.0;
        for (int i = 0; i < dfLists.size(); i++) {
            for (int j = i + 1; j <= dfLists.size() - 1; j++) {
                Double distance = Verify.calDtwDistance(dfLists.get(i), dfLists.get(j));
                System.out.println(distance);
                distances.add(distance);
                distSum += distance;
            }
        }

        // get mean of distances
        Double distMean = distSum / distances.size();
        System.out.print("Mean Distance: " + distMean);
        // return new Object[]{true, "Success!", distMean * 1.2};
        // System.out.println(distMean);
        // System.out.print(dtwPath);
    }
}
