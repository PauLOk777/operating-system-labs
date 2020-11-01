package com.paulok777.lab2;

import java.util.*;

public class Allocator {
    private byte[] buffer;
    private int totalSize;
    private int pageSize;
    private byte[][] pageDescriptors;
    private Map<Integer, List<Integer>> pagesDividedToBlocks = new HashMap<>(); // size - address
    private List<Integer> freePages = new ArrayList<>(); // address

    private static final int PAGE_DESCRIPTOR_SIZE = 16;
    private static final int DEFAULT_SIZE = 256;
    private static final int DEFAULT_PAGE_SIZE = 256;

    public enum PageState {
        FREE,
        DIVIDED,
        OCCUPIED
    }

    public Allocator() {
        this(DEFAULT_SIZE, DEFAULT_PAGE_SIZE);
    }

    public Allocator(int totalSize) {
        this(totalSize, DEFAULT_PAGE_SIZE);
    }

    public Allocator(int totalSize, int pageSize) {
        if (totalSize < pageSize || totalSize % pageSize != 0 || pageSize < 16) {
            throw new IllegalArgumentException();
        }
        HashMap<Integer, Integer> hashMap = new HashMap<>();
        this.totalSize = totalSize;
        this.pageSize = pageSize;
        buffer = new byte[totalSize];
        pageDescriptors = new byte[totalSize / pageSize][PAGE_DESCRIPTOR_SIZE];
        fillFreePagesList();
    }

    private void fillFreePagesList() {
        for (int i = 0; i < totalSize / pageSize; i++) {
            freePages.add(i * pageSize);
        }
    }

    public int memAlloc(int size) {
        return 0;
    }

    public int memRealloc(int addr, int size) {
        return 0;
    }

    public void memFree(int addr) {

    }

    public String dump() {
        StringBuilder dump = new StringBuilder();

        for (int i = 0; i < totalSize / pageSize; i++) {
            byte[] pageDescriptor = pageDescriptors[i];
            int enumOrdinal = Util.byteArrayToInt(Arrays.copyOfRange(buffer, i, i + 4));
            String type = PageState.values()[enumOrdinal].name();
            dump.append("Page ").append(i + 1).append(": ").append(type);
            if (type.equals(PageState.DIVIDED.name())) {
                int countOfFreeBlocks = Util.byteArrayToInt(Arrays.copyOfRange(buffer, i + 8, i + 12));
                int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(buffer, i + 1-2, i + 16));
                dump.append("; count of free blocks: ").append(countOfFreeBlocks);
                dump.append("; block size: ").append(blockSize);
            }
            dump.append('\n');
        }

        return dump.toString();
    }

    public static void main(String[] args) {
        Allocator allocator = new Allocator(1024, 256);
        System.out.println(allocator.dump());
    }
}
