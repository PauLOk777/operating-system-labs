package com.paulok777.lab2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Util {
    public static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static int byteArrayToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static void addValueToBucketIfNotExists(Map<Integer, List<Integer>> pagesDividedToBlocks,
                                                   int size, int address) {
        List<Integer> pages = pagesDividedToBlocks.get(size);
        if (pages == null) {
            pages = new ArrayList<>();
            pages.add(address);
            pagesDividedToBlocks.put(size, pages);
        } else {
            if (!pages.contains(address)) pages.add(address);
        }
    }

    public static void deleteValueToBucketIfNotExists(Map<Integer, List<Integer>> pagesDividedToBlocks, int address) {
        for (Map.Entry<Integer, List<Integer>> entry : pagesDividedToBlocks.entrySet()) {
            entry.getValue().remove(address);
        }
    }

    public static int roundNumberToNearestDegreeOfTwo(int number) {
        int counter = 0;
        while (number != 0) {
            number >>= 1;
            counter++;
        }
        return (int)Math.pow(2, counter);
    }
}
