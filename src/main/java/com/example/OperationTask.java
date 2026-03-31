package com.example;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationTask implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final Integer taskId;
    private final Integer roomNumber;
    private final TaskCategory category;
    private final String assignedTo;
    private final String notes;
    private final LocalDateTime createdAt;
    private final TaskState taskState;
    private final LocalDateTime completedAt;

    public OperationTask(
            Integer taskId,
            Integer roomNumber,
            TaskCategory category,
            String assignedTo,
            String notes,
            LocalDateTime createdAt,
            TaskState taskState,
            LocalDateTime completedAt) {
        this.taskId = taskId;
        this.roomNumber = roomNumber;
        this.category = category;
        this.assignedTo = assignedTo;
        this.notes = notes;
        this.createdAt = createdAt;
        this.taskState = taskState;
        this.completedAt = completedAt;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TaskState getTaskState() {
        return taskState;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public OperationTask markInProgress() {
        return new OperationTask(taskId, roomNumber, category, assignedTo, notes, createdAt, TaskState.IN_PROGRESS, completedAt);
    }

    public OperationTask markCompleted() {
        return new OperationTask(taskId, roomNumber, category, assignedTo, notes, createdAt, TaskState.COMPLETED, LocalDateTime.now());
    }

    public String getCreatedAtText() {
        return createdAt == null ? "-" : createdAt.format(TIMESTAMP_FORMATTER);
    }

    public String getCompletedAtText() {
        return completedAt == null ? "-" : completedAt.format(TIMESTAMP_FORMATTER);
    }

    @Override
    public String toString() {
        return "#" + taskId
                + " | Room " + roomNumber
                + " | " + category.getDisplayName()
                + " | " + taskState.getDisplayName()
                + " | Staff: " + assignedTo
                + " | Notes: " + notes
                + " | Created: " + getCreatedAtText()
                + " | Completed: " + getCompletedAtText();
    }
}
