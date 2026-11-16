import java.time.LocalDate;
import java.util.HashMap;

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

    // the fields a task can be sorted, filtered, read or changed by
    public enum Field {
        NAME, DESCRIPTION, CREATED_BY, ASSIGNED_TO, DATE_CREATED, DATE_FINISHED, DUE_DATE, PRIORITY, STATUS
    }

    private int id;                   // the database gives this, 0 means it hasn't been saved yet
    private LocalDate dateCreated;
    private LocalDate dateFinished;   // null until the task is finished
    private LocalDate dueDate;        // null if the task has no due date
    private Priority priority;        // the urgency level
    private Status status;

    // all of the fields that are plain text live in here, so they can share one case:
    // NAME, DESCRIPTION, CREATED_BY (username who made it), ASSIGNED_TO (username doing it,
    // null if nobody yet)
    private HashMap<Field, String> textValues;

    // constructor for making a brand new task
    public task(String name, String description, String createdBy, LocalDate dueDate, Priority priority) {
        this.id = 0;
        this.dateCreated = LocalDate.now();
        this.dateFinished = null;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = Status.NOT_STARTED;

        this.textValues = new HashMap<Field, String>();
        textValues.put(Field.NAME, name);
        textValues.put(Field.DESCRIPTION, description);
        textValues.put(Field.CREATED_BY, createdBy);
        textValues.put(Field.ASSIGNED_TO, null);
    }

    // constructor for when we load a task that was already saved
    public task(int id, String name, String description, String createdBy, String assignedTo,
            LocalDate dateCreated, LocalDate dateFinished, LocalDate dueDate, Priority priority,
            Status status) {
        this.id = id;
        this.dateCreated = dateCreated;
        this.dateFinished = dateFinished;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;

        this.textValues = new HashMap<Field, String>();
        textValues.put(Field.NAME, name);
        textValues.put(Field.DESCRIPTION, description);
        textValues.put(Field.CREATED_BY, createdBy);
        textValues.put(Field.ASSIGNED_TO, assignedTo);
    }

    // the id is not one of the fields, it just belongs to the database

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // gives back the value of a field
    // the type depends on the field, so cast it when you use it:
    //   DATE_CREATED, DATE_FINISHED, DUE_DATE -> LocalDate
    //   PRIORITY                    -> Priority
    //   STATUS                      -> Status
    //   everything else             -> String
    public Object getField(Field field) {
        switch (field) {
            case DATE_CREATED:
                return dateCreated;
            case DATE_FINISHED:
                return dateFinished;
            case DUE_DATE:
                return dueDate;
            case PRIORITY:
                return priority;
            case STATUS:
                return status;
            default:
                return textValues.get(field);
        }
    }

    // changes the value of a field
    // the value has to be the right type for the field (see getField),
    // otherwise this throws a ClassCastException
    public void setField(Field field, Object value) {
        switch (field) {
            case DATE_CREATED:
                dateCreated = (LocalDate) value;
                break;
            case DATE_FINISHED:
                dateFinished = (LocalDate) value;
                break;
            case DUE_DATE:
                dueDate = (LocalDate) value;
                break;
            case PRIORITY:
                priority = (Priority) value;
                break;
            case STATUS:
                status = (Status) value;
                break;
            default:
                textValues.put(field, (String) value);
        }
    }

    // gives back any field as lowercase text
    // a field with nothing in it (null) comes back as blank text
    public String getFieldAsText(Field field) {
        Object value = getField(field);
        if (value == null) {
            return "";
        }
        return value.toString().toLowerCase();
    }

    @Override
    public String toString() {
        return textValues.get(Field.NAME) + " | " + textValues.get(Field.DESCRIPTION)
                + " | Due: " + dueDate + " | Priority: " + priority + " | Status: " + status
                + " | Assigned to: " + textValues.get(Field.ASSIGNED_TO);
    }
}
