package com.example.to_dolistapp;

public class Task {
    private long id;
    public String taskName;
    private boolean isCompleted;

    public Task(long id, String taskName, boolean isCompleted) {
        this.id = id;
        this.taskName = taskName;
        this.isCompleted = isCompleted;
    }

    public Task(String taskName) {
        this.taskName = taskName;
        this.isCompleted = false; // Default is not completed
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
