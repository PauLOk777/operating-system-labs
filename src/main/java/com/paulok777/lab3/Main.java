package com.paulok777.lab3;

public class Main {
    public static void main(String[] args) {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += getTimeOriginalFunction();
        }
        System.out.println(sum / 10);

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += getTimeOptimizedFunction();
        }
        System.out.println(sum / 10);
    }

    public static long getTimeOriginalFunction() {
        long begin = System.nanoTime();

        int[][][] a = new int[100][100][100];

        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                for (int k = 0; k < 100; k++)
                {
                    a[k][j][i]++;
                }
            }
        }

        return System.nanoTime() - begin;
    }

    public static long getTimeOptimizedFunction() {
        long begin = System.nanoTime();

        int[][][] a = new int[100][100][100];

        for (int i = 0; i < 100; i++)
        {
            for (int j = 0; j < 100; j++)
            {
                for (int k = 0; k < 100; k++)
                {
                    a[i][j][k]++;
                }
            }
        }

        return System.nanoTime() - begin;
    }
}
