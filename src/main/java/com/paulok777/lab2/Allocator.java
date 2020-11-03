package com.paulok777.lab2;

import java.util.*;

public class Allocator {
    private final byte[] buffer;
    private final int totalSize;
    private final int pageSize;
    private final byte[][] pageDescriptors;
    private final Map<Integer, List<Integer>> pagesDividedToBlocks = new HashMap<>(); // size - address
    private final List<Integer> freePages = new ArrayList<>(); // address

    private static final int PAGE_DESCRIPTOR_SIZE = 16;
    private static final int DEFAULT_SIZE = 256;
    private static final int DEFAULT_PAGE_SIZE = 256;
    private static final byte[] RESET_ARRAY = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

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
        if (pagesDividedToNecessaryBlockSize != null && !pagesDividedToNecessaryBlockSize.isEmpty()) { // we have page with necessary block
            int addr = pagesDividedToNecessaryBlockSize.get(0);
            return getAddrWhenPageWithNecessaryBlockExist(addr);
        } else if (size + 4 > pageSize / 2 && size + 4 <= freePages.size() * pageSize) { // make multi page block
            int numberOfPages = necessaryBlockSize / pageSize;
            int[] addresses = new int[numberOfPages];

            setNewDescriptorsMultiPageBlock(addresses, numberOfPages, necessaryBlockSize);

            return addresses[0];
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

    private void setNewDescriptorsMultiPageBlock(int[] addresses, int numberOfPages, int necessaryBlockSize) {
        for (int i = 0; i < numberOfPages; i++) {
            addresses[i] = freePages.remove(0);
        }

        for (int i = 0; i < numberOfPages; i++) {
            byte[] type = Util.intToByteArray(PageState.OCCUPIED.ordinal());
            byte[] sizeOfBlock = Util.intToByteArray(necessaryBlockSize);
            byte[] ordinalNumber = Util.intToByteArray(i);
            byte[] nextAddr = {0, 0, 0, 0};
            if (i != numberOfPages - 1) {
                nextAddr = Util.intToByteArray(addresses[i + 1]);
            }

            byte[] pageDescriptor = pageDescriptors[addresses[i] / pageSize];
            System.arraycopy(type, 0, pageDescriptor, 0, type.length);
            System.arraycopy(sizeOfBlock, 0, pageDescriptor, 4, sizeOfBlock.length);
            System.arraycopy(ordinalNumber, 0, pageDescriptor, 8, ordinalNumber.length);
            System.arraycopy(nextAddr, 0, pageDescriptor, 12, nextAddr.length);
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
        byte[] newCount = Util.intToByteArray(countOfFreeBlocks - 1);

        if (countOfFreeBlocks == 1) { // last block
            Util.deleteValueToBucketIfNotExists(pagesDividedToBlocks, addr);
            System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
        } else {
            byte[] newFirstBlockAddr = Arrays.copyOfRange(buffer, freeBlockAddr, freeBlockAddr + 4);
            System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
            System.arraycopy(newFirstBlockAddr, 0, pageDescriptor, 4, newFirstBlockAddr.length);
        }
        return freeBlockAddr + 4;
    }

    public Integer memRealloc(int addr, int size) {
        int pageNumber = addr / pageSize;
        byte[] pageDescriptor = pageDescriptors[pageNumber];
        int typeBytes = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 0, 4));
        PageState type = PageState.values()[typeBytes];
        int blockSize;
        Integer result;

        switch (type) {
            case FREE:
                return memAlloc(size);
            case DIVIDED:
                blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 12, 16));
                memFree(addr);
                result = memAlloc(size);
                if (result == null) {
                    memAlloc(blockSize - 4);
                    break;
                }
                return result;
            case OCCUPIED:
                blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 4, 8));
                memFree(addr);
                result = memAlloc(size);
                if (result == null) {
                    memAlloc(blockSize);
                    break;
                }
                return result;
        }
        return null;
    }

    public void memFree(int addr) {
        int pageNumber = addr / pageSize;
        byte[] pageDescriptor = pageDescriptors[pageNumber];
        int typeBytes = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 0, 4));
        String type = PageState.values()[typeBytes].name();
        if (type.equals(PageState.DIVIDED.name())) { // page is divided
            int countOfFreeBlocks = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 8, 12));
            int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 12, 16));
            int blockAddr = (addr / blockSize) * blockSize;

            if (countOfFreeBlocks == 0) { // this page was full occupied
                Util.addValueToBucketIfNotExists(pagesDividedToBlocks, blockSize, pageNumber * pageSize);
                byte[] firstFreeBlockAddr = Util.intToByteArray(blockAddr);
                byte[] newCount = Util.intToByteArray(countOfFreeBlocks + 1);

                System.arraycopy(firstFreeBlockAddr, 0, pageDescriptor, 4, firstFreeBlockAddr.length);
                System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
            } else if (countOfFreeBlocks + 1 == pageSize / blockSize) { // we will freed this page
                Util.deleteValueToBucketIfNotExists(pagesDividedToBlocks, pageNumber * pageSize);
                freePages.add(pageNumber * pageSize);
                System.arraycopy(RESET_ARRAY, 0, pageDescriptor, 0, RESET_ARRAY.length);
            } else {
                byte[] firstFreeBlockAddr = Arrays.copyOfRange(pageDescriptor, 4, 8);
                byte[] newCount = Util.intToByteArray(countOfFreeBlocks + 1);
                byte[] newFirstFreeBlockAddr = Util.intToByteArray(blockAddr);

                System.arraycopy(firstFreeBlockAddr, 0, buffer, blockAddr, firstFreeBlockAddr.length);
                System.arraycopy(newFirstFreeBlockAddr, 0, pageDescriptor, 4, newFirstFreeBlockAddr.length);
                System.arraycopy(newCount, 0, pageDescriptor, 8, newCount.length);
            }
        } else if (type.equals(PageState.OCCUPIED.name())) { // page is occupied
            int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 4, 8));
            int pageAddress = pageNumber * pageSize;

            for (int i = 0; i < blockSize / pageSize; i++) {
                pageDescriptor = pageDescriptors[pageAddress / pageSize];
                freePages.add(pageAddress);
                pageAddress = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 12, 16));
                System.arraycopy(RESET_ARRAY, 0, pageDescriptor, 0, RESET_ARRAY.length);
            }
        }
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
            if (type.equals(PageState.OCCUPIED.name())) {
                int blockSize = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 4, 8));
                int ordinalNumber = Util.byteArrayToInt(Arrays.copyOfRange(pageDescriptor, 8, 12));
                dump.append("; block size: ").append(blockSize);
                dump.append("; ordinal number: ").append(ordinalNumber);
            }
            dump.append('\n');
        }

        return dump.toString();
    }

    public static void main(String[] args) {
        Allocator allocator = new Allocator(1024, 256);
        // divide into blocks
        int addr1 = allocator.memAlloc(80);
        int addr2 = allocator.memAlloc(90);
        allocator.memAlloc(25);
        int addr3 = allocator.memAlloc(50);
        allocator.memAlloc(50);
        int addr4 = allocator.memAlloc(50);
        allocator.memAlloc(50);
        System.out.println(allocator.dump());
        allocator.memFree(addr1);
        allocator.memFree(addr3);
        allocator.memFree(addr4);
        System.out.println(allocator.dump());
        allocator.memRealloc(addr2, 40);
        System.out.println(allocator.dump());

        // divide into multi page
        addr1 = allocator.memAlloc(257);
        System.out.println(allocator.dump());
        System.out.println(allocator.memRealloc(addr1, 200));
        System.out.println(allocator.dump());
    }
}
