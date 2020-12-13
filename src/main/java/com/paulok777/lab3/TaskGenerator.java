package com.paulok777.lab3;

public class TaskGenerator {

    private RequestService requestService;
    private int id;
    private int minPriority;
    private int maxPriority;

    public TaskGenerator(RequestService requestService, int minPriority, int maxPriority) {
        this.requestService = requestService;
        this.minPriority = minPriority;
        this.maxPriority = maxPriority;
        id = 0;
    }

    public void addRandomTaskToRequestService() {
        Job job = generateTask();
        System.out.println("Generated task: " + job);
        requestService.addJob(job);
    }

    private Job generateTask() {
        int priority = (int) (Math.random() * (maxPriority - minPriority + 1) + minPriority);
        return new Job(id++, priority);
    }

    public void stopService() {
        requestService.stopService();
    }
}
