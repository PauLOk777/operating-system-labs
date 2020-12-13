package com.paulok777.lab3;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        RequestService requestService = new RequestService();
        TaskGenerator taskGenerator = new TaskGenerator(requestService, 0, 9);
        requestService.startService();

        long startOfService = System.currentTimeMillis();

        for (int i = 0; i < 100; i ++) {
            Thread.sleep(30);
            taskGenerator.addRandomTaskToRequestService();
        }
        taskGenerator.stopService();

        long finishOfService = System.currentTimeMillis();
        System.out.println("Avg time of jobs waiting: " + requestService.getAvgTimeOfJobsWaiting());
        for (int i = 0; i < 10; i++) {
            System.out.println("Avg time of jobs waiting by priority " + i + ": "
                    + requestService.getAvgTimeOfJobsWaitingByPriority(i));
        }
        System.out.println("Service waiting time: " + requestService.getWaitingTime());
        System.out.println("Service life time: " + (finishOfService - startOfService));
    }
}
