package com.paulok777.lab6;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n Inside main()");
        String typeOfShape = new Scanner(System.in).nextLine();

        int i = 0;

        for (; i < Integer.MAX_VALUE; i++) ;

        switch (typeOfShape) {
            case "square":
                drawSquare();
                break;
            case "circle":
                drawCircle();
                break;
        }
    }

    private static void drawSquare() {
        int[][] squarePixels = new int[10000][10000];
        for(int i = 0; i < 10000; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 10000; j++) {
                //process of drawing
                squarePixels[i][j] = i + j;
            }
        }
    }

    private static void drawCircle() {
        int[][] circlePixels = new int[10000][10000];
        for(int i = 0; i < 10000; i++) {
            //process of drawing
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int j = 0; j < 10000; j++) {
                circlePixels[i][j] = i * j;
            }
        }
    }
}
