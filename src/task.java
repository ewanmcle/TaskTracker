import java.time.LocalDate;

/**
 * A task is one thing on the to do list.
 * This class only holds the data for a task. Saving and loading is done somewhere else.
 */
public class task {

    // the different statuses a task can have
    public enum Status {
        NOT_STARTED, WIP, COMPLETE
    }

    // the different urgency levels a task can have
    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private int id;               // the database gives this, 0 means it hasn't been saved yet
    private String name;
    private String description;
    private String createdBy;     // username of the person who made the task
    private String assignedTo;    // username of the person doing the task, null if nobody yet
    private LocalDate dateCreated;
    private LocalDate dateFinished;   // null until the task is finished
    private Priority priority;    // the urgency level
    private Status status;

    // constructor for making a brand new task
    public task(String name, String description, String createdBy, Priority priority) {
        this.id = 0;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.assignedTo = null;
        this.dateCreated = LocalDate.now();
        this.dateFinished = null;
        this.priority = priority;
        this.status = Status.NOT_STARTED;
    }

    // constructor for when we load a task that was already saved
    public task(int id, String name, String description, String createdBy, String assignedTo,
            LocalDate dateCreated, LocalDate dateFinished, Priority priority, Status status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.dateCreated = dateCreated;
        this.dateFinished = dateFinished;
        this.priority = priority;
        this.status = status;
    }

    // getters and setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(LocalDate dateFinished) {
        this.dateFinished = dateFinished;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name + " | " + description + " | Priority: " + priority
                + " | Status: " + status + " | Assigned to: " + assignedTo;
    }
}
