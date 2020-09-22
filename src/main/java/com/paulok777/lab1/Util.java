package com.paulok777.lab1;

import java.nio.ByteBuffer;

public class Util {
    public static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte booleanToByte(boolean value) {
        return value ? (byte) 1 : (byte) 0;
    }

    public static boolean byteToBoolean(byte value) {
        return value == 1;
    }
}
