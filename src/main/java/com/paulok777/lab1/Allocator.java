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

    public Integer memAlloc(int size) {
        for (int i = 0; i < this.size;) {
            boolean free = Util.byteToBoolean(memory[i + 11]);
            int memorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, i, i + 4));
            int offset = headerSize + memorySize;
            if (!free) {
                i += offset;
                continue;
            }
            if (size <= memorySize) {
                memory[i + 11] = Util.booleanToByte(false);
                return i + 12;
            }
            int startAddress = i + 12;
            int nextHeader = i + headerSize + memorySize;
            for (int j = nextHeader; j < this.size;) {
                boolean currentHeaderFree = Util.byteToBoolean(memory[j + 11]);
                int currentHeaderMemorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, j, j + 4));
                if (!currentHeaderFree) {
                    i = j + headerSize + currentHeaderMemorySize;
                    break;
                }
                memorySize += currentHeaderMemorySize + headerSize;
                if (memorySize >= size) {
                    memory[startAddress - 1] = Util.booleanToByte(false);
                    byte[] sizeInBytes = Util.intToByteArray(memorySize);
                    System.arraycopy(sizeInBytes, 0, memory, i, 4);
                    return startAddress;
                }
                j += headerSize + currentHeaderMemorySize;
            }
        }
        return null;
    }

    public Integer memRealloc(int addr, int size) {
        return null;
    }

    public void memFree(int addr) {
        memory[addr - 1] = Util.booleanToByte(true);
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
        Allocator allocator = new Allocator(64, 16);
        allocator.dump();
        int firstIndex;
        System.out.println(firstIndex = allocator.memAlloc(5));
        allocator.dump();
        System.out.println(allocator.memAlloc(3));
        allocator.dump();
        allocator.memFree(firstIndex);
        allocator.dump();
        System.out.println(allocator.memAlloc(21));
        allocator.dump();
    }
}
