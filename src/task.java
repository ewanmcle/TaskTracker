import java.time.LocalDate;

public class task {

	// status options for a task
	public enum Status {
		NOT_STARTED, WIP, COMPLETE
	}

	// priority options
	public enum Priority {
		LOW, MEDIUM, HIGH
	}

	private int id; // id from the database
	private String createdBy;
	private String assignedTo;
	private LocalDate dateCreated;
	private LocalDate dateFinished;
	private String name;
	private String description;
	private LocalDate dueDate;
	private Priority priority;
	private Status status;

	// constructor for making a new task
	public task(String name, String description, String createdBy, LocalDate dueDate, Priority priority) {
		this.name = name;
		this.description = description;
		this.createdBy = createdBy;
		this.assignedTo = createdBy; // assign to whoever made it for now
		this.dueDate = dueDate;
		this.priority = priority;
		this.dateCreated = LocalDate.now();
		this.dateFinished = null;
		this.status = Status.NOT_STARTED;
	}

	// constructor for when we load a task from the database
	public task(int id, String name, String description, String createdBy, String assignedTo,
			LocalDate dateCreated, LocalDate dateFinished, LocalDate dueDate, Priority priority, Status status) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.createdBy = createdBy;
		this.assignedTo = assignedTo;
		this.dateCreated = dateCreated;
		this.dateFinished = dateFinished;
		this.dueDate = dueDate;
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

	public String getCreatedBy() {
		return createdBy;
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

	public LocalDate getDateFinished() {
		return dateFinished;
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

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
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

	// other methods

	// sets the task to work in progress
	public void startTask() {
		status = Status.WIP;
	}

	// marks the task as done and saves the date
	public void markComplete() {
		status = Status.COMPLETE;
		dateFinished = LocalDate.now();
	}

	public boolean isComplete() {
		return status == Status.COMPLETE;
	}

	// checks if the task is past its due date and not done yet
	public boolean isOverdue() {
		if (dueDate == null || isComplete()) {
			return false;
		}
		return LocalDate.now().isAfter(dueDate);
	}

	// used for showing the tasks a user made or got assigned
	public boolean belongsTo(String username) {
		return username.equals(createdBy) || username.equals(assignedTo);
	}

	@Override
	public String toString() {
		return name + " | " + description + " | Due: " + dueDate + " | Priority: " + priority
				+ " | Status: " + status + " | Assigned to: " + assignedTo;
	}

}