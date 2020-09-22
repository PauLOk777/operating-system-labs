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
        byte free = Util.booleanToByte(true);

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
        if (size > memory.length - headerSize) return null;
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

    @SuppressWarnings("all")
    public Integer memRealloc(int addr, int size) {
        if (size > memory.length - headerSize) return null;
        int headerAddr = addr - 12;
        int memorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, headerAddr, headerAddr + 4));
        if (memorySize == size) return addr;
        if (memorySize > size) {
            int possibleBlocks = (headerSize + memorySize - size) / blockSize;
            int blocksForSize = (int) Math.ceil((headerSize + size) / (double) blockSize);
            if (possibleBlocks > 1) {
                byte[] data = Arrays.copyOfRange(memory, addr, addr + memorySize);
                int memoryForPart = blocksForSize * blockSize - headerSize;
                System.arraycopy(data, memorySize - memoryForPart, memory, addr, memoryForPart);
                System.arraycopy(Util.intToByteArray(memoryForPart), 0, memory, headerAddr, 4);
                int nextHeaderAddr = addr + memoryForPart;
                byte[] currentMemorySize = Util.intToByteArray(blockSize - headerSize);
                byte[] previousMemorySize = Util.intToByteArray(blockSize - headerSize);
                byte free = Util.booleanToByte(true);
                for(int i = 1; i < possibleBlocks - blocksForSize + 1; i++) {
                    System.arraycopy(currentMemorySize, 0, memory, nextHeaderAddr * i, 4);
                    System.arraycopy(previousMemorySize, 0, memory, nextHeaderAddr * i + 4, 4);
                    memory[nextHeaderAddr * i + 11] = free;
                }
            }
            return addr;
        }

        byte[] data = Arrays.copyOfRange(memory, addr, addr + memorySize);
        memory[headerAddr + 11] = Util.booleanToByte(true);
        Integer newAddr = memAlloc(size);
        if (newAddr == null) {
            memory[headerAddr + 11] = Util.booleanToByte(false);
        } else {
            int newMemorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, newAddr - 12 , newAddr - 8));
            System.arraycopy(data, 0, memory, newAddr + newMemorySize - memorySize, memorySize);
        }
        return newAddr;
    }

    public void memFree(int addr) {
        memory[addr - 1] = Util.booleanToByte(true);
        optimizeBlock(addr);
    }

    private void optimizeBlock(int addr) {
        byte[] currentMemorySize = Util.intToByteArray(blockSize - headerSize);
        byte[] previousMemorySize = Util.intToByteArray(blockSize - headerSize);
        byte free = Util.booleanToByte(true);
        int memorySize = Util.byteArrayToInt(Arrays.copyOfRange(memory, addr - 12, addr - 8));
        System.arraycopy(currentMemorySize, 0, memory, addr - 12, 4);
        int possibleBlocks = (memorySize + headerSize) / blockSize;
        int nextHeaderAddr = addr + blockSize - headerSize;
        for (int i = 1; i < possibleBlocks + 1; i++) {
            System.arraycopy(currentMemorySize, 0, memory, nextHeaderAddr * i, 4);
            System.arraycopy(previousMemorySize, 0, memory, nextHeaderAddr * i + 4, 4);
            memory[nextHeaderAddr * i + 11] = free;
        }
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
        Allocator allocator = new Allocator(128, 16);
        allocator.dump();
        int index;
        System.out.println(index = allocator.memAlloc(5));
        allocator.dump();
        System.out.println(allocator.memAlloc(3));
        allocator.dump();
        allocator.memFree(index);
        System.out.println("----------------------");
        allocator.dump();
        System.out.println(index = allocator.memAlloc(1));
        allocator.dump();
        System.out.println(allocator.memRealloc(index, 80));
        allocator.dump();
        allocator.memFree(index);
        System.out.println("----------------------");
        allocator.dump();
        System.out.println(index = allocator.memAlloc(80));
        allocator.dump();
        System.out.println(allocator.memRealloc(index, 5));
        allocator.dump();
    }
}
