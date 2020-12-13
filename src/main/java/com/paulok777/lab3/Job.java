package com.paulok777.lab3;

public class Job implements Runnable {

    private static final int MIN_TIME_OF_JOB = 1;
    private static final int MAX_TIME_OF_JOB = 32;

    private int id;
    private int priority;
    private long createdTime;

    public Job(int id, int priority) {
        this.id = id;
        this.priority = priority;
        createdTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        System.out.println("Starting executing: " + toString());
        try {
            int randomTime = (int) (Math.random() * (MAX_TIME_OF_JOB - MIN_TIME_OF_JOB + 1) + MIN_TIME_OF_JOB);
            Thread.sleep(randomTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Finished: " + toString());
    }

    public int getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", priority=" + priority +
                '}';
    }
}
