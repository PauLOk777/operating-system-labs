package com.paulok777.lab4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Planner {

    private int[][] matrix;
    private int tasks;
    private int resources;

    public Planner(int tasks, int resources) {
        this.tasks = tasks;
        this.resources = resources;
        matrix = new int[tasks][resources];
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
        List<Integer> optimizedPairs = new ArrayList<>();

        searchAndOptimizeImportantNodes(0, optimizedPairs);

        for (int i = 0; i < tasks; i++) {
            if (!optimizedPairs.contains(i)) {
                for (int j = 0; j < resources; j++) {
                    if (matrix[i][j] > 0) {
                        reduceMatrix(i, j);
                        printMatrix();
                        optimizedPairs.add(i);
                        searchAndOptimizeImportantNodes(i + 1, optimizedPairs);
                    }
                }
            }
        }
    }

    private void searchAndOptimizeImportantNodes(int startTask, List<Integer> optimizedPairs) {
        for (int i = startTask; i < tasks; i++) {
            int resourceIndex = getResourceIndex(i);
            if (resourceIndex != -1) {
                reduceMatrix(i, resourceIndex);
                printMatrix();
                optimizedPairs.add(i);
                continue;
            }

            int taskIndex = getTaskIndex(i);
            if (taskIndex != -1) {
                reduceMatrix(taskIndex, i);
                printMatrix();
                optimizedPairs.add(taskIndex);
            }
        }
    }

    private int getResourceIndex(int currentTask) {
        int indexOfResource = -1;
        for (int j = 0; j < resources; j++) {
            if (matrix[currentTask][j] > 0 && indexOfResource == -1) {
                indexOfResource = j;
            }

            if (matrix[currentTask][j] > 0) {
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
            }

            if (matrix[j][currentResource] > 0) {
                return -1;
            }
        }

        return indexOfTask;
    }

    public Map<Integer, Integer> getRelationTaskResource() {
        Map<Integer, Integer> relation = new HashMap<>();

        for(int i = 0; i < tasks; i++) {
            for (int j = 0; j < resources; j++) {
                if (matrix[i][j] > 0) {
                    relation.put(i, j);
                    break;
                }
            }
        }

        return relation;
    }

    public void printRelationTaskResource(Map<Integer, Integer> relation) {
        System.out.println("Task -> Resource");
        for (Map.Entry<Integer, Integer> pair: relation.entrySet()) {
            System.out.println(pair.getKey() + " -> " + pair.getValue());
        }
    }

    private void reduceMatrix(int row, int column) {
        for (int i = 0; i < tasks; i++) {
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

    public static void main(String[] args) {
        Planner planner = new Planner(5, 5);
        planner.printMatrix();
//        planner.randomFill();
        planner.fillByExample();
        planner.printMatrix();
        planner.plan();
        planner.printRelationTaskResource(planner.getRelationTaskResource());
    }
}
