package com.paulok777.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class RequestService extends Thread {

    private List<LinkedBlockingQueue<Job>> priorityListOfFifoQueuesOfJobs;
    private boolean isStopped;

    // experimental variables
    private List<List<Long>> jobsWaitingTimesByPriority;
    private long waitingTime;

    public RequestService() {
        priorityListOfFifoQueuesOfJobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            priorityListOfFifoQueuesOfJobs.add(new LinkedBlockingQueue<>());
        }

        isStopped = false;

        // experimental variables init
        waitingTime = 0;

        jobsWaitingTimesByPriority = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            jobsWaitingTimesByPriority.add(new ArrayList<>());
        }
    }

    public void startService() {
        start();
    }

    private void executeJob(Job job) {
        Thread thread = new Thread(job);
        thread.start();
        try {
            thread.join();
            // experimental line
            jobsWaitingTimesByPriority.get(job.getPriority()).add(System.currentTimeMillis() - job.getCreatedTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Job findJob() {
        for (LinkedBlockingQueue<Job> fifoQueuesOfJob : priorityListOfFifoQueuesOfJobs) {
            if (fifoQueuesOfJob.size() > 0) {
                return fifoQueuesOfJob.poll();
            }
        }
        return null;
    }

    public boolean isFree() {
        for (LinkedBlockingQueue<Job> fifoQueuesOfJob : priorityListOfFifoQueuesOfJobs) {
            if (fifoQueuesOfJob.size() > 0) {
                return false;
            }
        }
        return true;
    }

    public void stopService() {
        while (!isFree());
        isStopped = true;
    }

    public void addJob(Job job) {
        try {
            priorityListOfFifoQueuesOfJobs.get(job.getPriority()).put(job);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     Experimental function
     */
    public long getAvgTimeOfJobsWaiting() {
        return jobsWaitingTimesByPriority
                .stream()
                .map(l -> l.stream().reduce(0L, Long::sum))
                .reduce(0L, Long::sum) /
                jobsWaitingTimesByPriority.stream().map(List::size).reduce(0, Integer::sum);
    }

    /**
     Experimental function
     */
    public long getWaitingTime() {
        return waitingTime / 1_000_000;
    }

    /**
     Experimental function
     */
    public long getAvgTimeOfJobsWaitingByPriority(int priority) {
        return jobsWaitingTimesByPriority
                .get(priority)
                .stream()
                .reduce(0L, Long::sum) / jobsWaitingTimesByPriority.get(priority).size();
    }

    @Override
    public void run() {
        while (!isStopped) {
            long startOfIteration = System.nanoTime(); // experimental
            Job job = findJob();

            if (job != null) {
                executeJob(job);
                continue;
            }

            waitingTime += System.nanoTime() - startOfIteration; // experimental
        }
    }
}
