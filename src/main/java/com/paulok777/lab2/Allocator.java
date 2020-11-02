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

    public Integer memAlloc(int size) {
        int necessaryBlockSize = Util.roundNumberToNearestDegreeOfTwo(size + 4);
        List<Integer> pagesDividedToNecessaryBlockSize = pagesDividedToBlocks.get(necessaryBlockSize);
        if (pagesDividedToNecessaryBlockSize == null && freePages.isEmpty()) return null;
        if (pagesDividedToNecessaryBlockSize != null) { // we have page with necessary block
            int addr = pagesDividedToNecessaryBlockSize.get(0);
            return getAddrWhenPageWithNecessaryBlockExist(addr);
        } else if (size + 4 > pageSize / 2 && size + 4 <= freePages.size() * pageSize) { // make multi page block
            freePages.remove(0);
            return 1;
        } else if (!freePages.isEmpty() && size + 4 <= pageSize / 2) { // divide free page to blocks
            int addrOfFreePage = freePages.remove(0);
            Util.addValueToBucketIfNotExists(pagesDividedToBlocks, necessaryBlockSize, addrOfFreePage);
            byte[] pageDescriptor = pageDescriptors[addrOfFreePage / pageSize];

            divideFreePageIntoBlocks(addrOfFreePage, necessaryBlockSize, pageDescriptor);

            byte[] nextAddr = Util.intToByteArray(addrOfFreePage + necessaryBlockSize);
            System.arraycopy(nextAddr, 0, pageDescriptor, 4, nextAddr.length);
            return addrOfFreePage + 4;
        } else {
            return null;
        }
    }

    private void divideFreePageIntoBlocks(int addr, int size, byte[] pageDescriptor) {
        for (int i = 1; i < (pageSize / size); i++) {
            byte[] nextBlock = Util.intToByteArray(addr + size * i);
            System.arraycopy(nextBlock, 0, buffer, addr + size * (i - 1), nextBlock.length);
        }

        byte[] newState = Util.intToByteArray(PageState.DIVIDED.ordinal());
        byte[] firstFreeBlockAddr = Util.intToByteArray(addr);
        byte[] countOfFreeBlocks = Util.intToByteArray(pageSize / size - 1);
        byte[] blockSize = Util.intToByteArray(size);

        System.arraycopy(newState, 0, pageDescriptor, 0, newState.length);
        System.arraycopy(firstFreeBlockAddr, 0, pageDescriptor, 4, firstFreeBlockAddr.length);
        System.arraycopy(countOfFreeBlocks, 0, pageDescriptor, 8, countOfFreeBlocks.length);
        System.arraycopy(blockSize, 0, pageDescriptor, 12, blockSize.length);
    }

    private int getAddrWhenPageWithNecessaryBlockExist(int addr) {
        byte[] pageDescriptor = pageDescriptors[addr / pageSize];
        int freeBlockAddr = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 4, 8));
        int countOfFreeBlocks = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 8, 12));
        int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 12, 16));
        byte[] newCount = Util.intToByteArray(--countOfFreeBlocks);

        if (countOfFreeBlocks == 1) { // last block
            System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
            pagesDividedToBlocks.get(blockSize).remove(0);
        } else {
            byte[] newFirstBlockAddr = Arrays.copyOfRange(buffer, freeBlockAddr, freeBlockAddr + 4);
            System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
            System.arraycopy(newFirstBlockAddr, 0, pageDescriptor, 4, newFirstBlockAddr.length);
        }
        return freeBlockAddr + 4;
    }

    public Integer memRealloc(int addr, int size) {
        return 0;
    }

    public void memFree(int addr) {

    }

    public String dump() {
        StringBuilder dump = new StringBuilder();

        for (int i = 0; i < totalSize / pageSize; i++) {
            byte[] pageDescriptor = pageDescriptors[i];
            int enumOrdinal = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 0, 4));
            String type = PageState.values()[enumOrdinal].name();
            dump.append("Page ").append(i + 1).append(": ").append(type);
            if (type.equals(PageState.DIVIDED.name())) {
                int countOfFreeBlocks = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 8, 12));
                int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 12, 16));
                dump.append("; count of free blocks: ").append(countOfFreeBlocks);
                dump.append("; block size: ").append(blockSize);
            }
            dump.append('\n');
        }

        return dump.toString();
    }

    public static void main(String[] args) {
        Allocator allocator = new Allocator(1024, 256);
        System.out.println(allocator.memAlloc(20));
        System.out.println(allocator.memAlloc(80));
        System.out.println(allocator.memAlloc(25));
        System.out.println(allocator.dump());
    }
}
