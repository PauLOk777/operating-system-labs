package com.paulok777.lab4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {

    private int[][] matrix;
    private int tasks;
    private int resources;

    private List<Integer> optimizedTasks;
    private List<Integer> optimizedResources;

    public Planner(int tasks, int resources) {
        this.tasks = tasks;
        this.resources = resources;
        matrix = new int[tasks][resources];
        optimizedTasks = new ArrayList<>();
        optimizedResources = new ArrayList<>();
    }

    public void printMatrix() {
        for (int i = 0; i < tasks; i++) {
            for (int j = 0; j < resources - 1; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.print(matrix[i][resources - 1] + "\n");
        }
        System.out.println();
    }

    public void randomFill() {
        for (int i = 0; i < tasks; i++) {
            for (int j = 0; j < resources; j++) {
                matrix[i][j] = (int) Math.round(Math.random());
            }
        }
    }

    public void fillByExample() {
        tasks = 8;
        resources = 8;
        matrix = new int[][] {
                {0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 1, 0, 0},
                {1, 1, 1, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 1, 1},
                {0, 1, 1, 1, 1, 1, 0, 1}
        };
    }

    public void plan() {
        searchAndOptimizeImportantNodesByRow(optimizedTasks, optimizedResources);
        searchAndOptimizeImportantNodesByColumn(optimizedTasks, optimizedResources);

        for (int i = 0; i < tasks; i++) {
            if (!optimizedTasks.contains(i)) {
                for (int j = 0; j < resources; j++) {
                    if (!optimizedResources.contains(j) && matrix[i][j] > 0) {
                        reduceMatrix(i, j);
                        printMatrix();
                        optimizedTasks.add(i);
                        optimizedResources.add(j);
                        System.out.println("Optimized tasks list: " + optimizedTasks);
                        System.out.println("Optimized resources list: " + optimizedResources);
                        searchAndOptimizeImportantNodesByRow(optimizedTasks, optimizedResources);
                        searchAndOptimizeImportantNodesByColumn(optimizedTasks, optimizedResources);
                    }
                }
            }
        }
    }

    private void searchAndOptimizeImportantNodesByRow(List<Integer> optimizedTasks, List<Integer> optimizedResources) {
        for (int i = 0; i < tasks; i++) {
            int resourceIndex = getResourceIndex(i);
            if (optimizedResources.contains(resourceIndex)) break;
            if (resourceIndex != -1) {
                System.out.println("RowSearch. TaskIndex: " + i + ". ResourceIndex: " + resourceIndex);
                reduceMatrix(i, resourceIndex);
                printMatrix();
                optimizedTasks.add(i);
                optimizedResources.add(resourceIndex);
                System.out.println("Optimized tasks list: " + optimizedTasks);
                System.out.println("Optimized resources list: " + optimizedResources);
            }
        }
    }

    private void searchAndOptimizeImportantNodesByColumn(List<Integer> optimizedTasks, List<Integer> optimizedResources) {
        for (int i = 0; i < resources; i++) {
            int taskIndex = getTaskIndex(i);
            if (optimizedTasks.contains(taskIndex)) break;
            if (taskIndex != -1) {
                System.out.println("ColumnSearch. ResourceIndex: " + i + ". TaskIndex: " + taskIndex);
                reduceMatrix(taskIndex, i);
                printMatrix();
                optimizedResources.add(i);
                optimizedTasks.add(taskIndex);
                System.out.println("Optimized tasks list: " + optimizedTasks);
                System.out.println("Optimized resources list: " + optimizedResources);
            }
        }
    }

    private void reduceMatrix(int row, int column) {
        for (int i = 0; i < resources; i++) {
            if (i != column) {
                matrix[row][i] = 0;
            }
        }

        for (int i = 0; i < tasks; i++) {
            if (i != row) {
                matrix[i][column] = 0;
            }
        }
    }

    private int getResourceIndex(int currentTask) {
        int indexOfResource = -1;
        for (int j = 0; j < resources; j++) {
            if (matrix[currentTask][j] > 0 && indexOfResource == -1) {
                indexOfResource = j;
            } else if (matrix[currentTask][j] > 0) {
                return -1;
            }
        }
        return indexOfResource;
    }

    private int getTaskIndex(int currentResource) {
        int indexOfTask = -1;
        for (int j = 0; j < tasks; j++) {
            if (matrix[j][currentResource] > 0 && indexOfTask == -1) {
                indexOfTask = j;
            } else if (matrix[j][currentResource] > 0) {
                return -1;
            }
        }

        return indexOfTask;
    }

    public void printRelationTaskResource() {
        System.out.println("Task -> Resource");
        for (int i = 0; i < optimizedResources.size(); i++) {
            System.out.println(optimizedTasks.get(i) + " -> " + optimizedResources.get(i));
        }
    }

    public static void main(String[] args) {
        Planner planner = new Planner(8, 5);
        planner.printMatrix();
        planner.randomFill();
//        planner.fillByExample();
        planner.printMatrix();
        planner.plan();
        planner.printRelationTaskResource();
    }
}
