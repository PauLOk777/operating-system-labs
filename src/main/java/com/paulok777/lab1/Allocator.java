package com.paulok777.lab1;

import java.util.Arrays;

public class Allocator {
    private final int size;
    private final int headerSize = 12;
    private final int blockSize;
    private final byte[] memory;

    public Allocator(int size, int blockSize) {
        if (size < 16) throw new IllegalArgumentException();
        if (blockSize % 4 != 0 && size % blockSize != 0) throw new IllegalArgumentException();
        if (blockSize < 16) throw new IllegalArgumentException();
        this.size = size;
        this.blockSize = blockSize;
        memory = new byte[size];
        createHeaders();
    }

    private void createHeaders() {
        byte[] currentMemorySize = Util.intToByteArray(blockSize - headerSize);
        byte[] previousMemorySize;
        byte free = 1;

        for (int i = 0; i < size; i += blockSize) {
            if (i == 0) {
                previousMemorySize = Util.intToByteArray(0);
            } else {
                previousMemorySize = Util.intToByteArray(blockSize - headerSize);
            }
            System.arraycopy(currentMemorySize, 0, memory, i, 4);
            System.arraycopy(previousMemorySize, 0, memory, i + 4, 4);
            memory[i + 11] = free;
        }
    }

    public void memAlloc(int size) {

    }

    public void memRealloc(int addr, int size) {

    }

    public void memFree(int addr) {

    }

    public void dump() {
        try {
            int headerCounter = 1;
            for (int i = 0; i < size;) {
                StringBuilder headerInfo = new StringBuilder("Header(" + headerCounter++ + "): ");
                headerInfo.append("memory - ");
                int memorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, i, i + 4));
                int previousMemorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, i + 4, i + 8));
                boolean free = Util.byteToBoolean(memory[i + 11]);
                headerInfo.append(memorySize)
                        .append("; previous block memory - ")
                        .append(previousMemorySize)
                        .append("; free - ")
                        .append(free);
                i += headerSize + memorySize;
                System.out.println(headerInfo);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Allocator(64, 16).dump();
    }
}
