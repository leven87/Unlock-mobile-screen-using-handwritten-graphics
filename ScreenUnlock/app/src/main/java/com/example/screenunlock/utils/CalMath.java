package com.example.screenunlock.utils;

public class CalMath {
    Double Sum(Double[] data) {
        Double sum = 0.0;
        for (int i = 0; i < data.length; i++)
            sum = sum + data[i];
        return sum;
    }

    Double Mean(Double[] data) {
        Double mean = 0.0;
        mean = Sum(data) / data.length;
        return mean;
    }

    // population variance 总体方差
    Double POP_Variance(Double[] data) {
        Double variance = 0.0;
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - Mean(data)), 2));
        }
        variance = variance / data.length;
        return variance;
    }

    // population standard deviation 总体标准差
    Double POP_STD_dev(Double[] data) {
        Double std_dev;
        std_dev = Math.sqrt(POP_Variance(data));
        return std_dev;
    }

    // sample variance 样本方差
    public Double Sample_Variance(Double[] data) {
        Double variance = 0.0;
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - Mean(data)), 2));
        }
        variance = variance / (data.length - 1);
        return variance;
    }

    // sample standard deviation 样本标准差
    public Double Sample_STD_dev(Double[] data) {
        Double std_dev;
        std_dev = Math.sqrt(Sample_Variance(data));
        return std_dev;
    }
}
