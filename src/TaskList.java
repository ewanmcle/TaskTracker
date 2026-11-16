import java.time.LocalDate;
import java.util.ArrayList;

/**
 * TaskList is a list of task objects.
 * It can add, find, remove and change tasks, and it can sort them.
 * Right now the tasks only live in memory, saving them to a database comes later.
 */
public class TaskList {

    private ArrayList<task> tasks;
    private int nextId;   // the id the next new task will get

    public TaskList() {
        tasks = new ArrayList<task>();
        nextId = 1;
    }

    // ---------- adding tasks ----------

    // adds a task that was already made, gives it an id if it doesn't have one yet
    public void addTask(task newTask) {
        if (newTask == null) {
            throw new IllegalArgumentException("Task can't be null");
        }
        if (newTask.getId() == 0) {
            newTask.setId(nextId);
            nextId++;
        } else if (findById(newTask.getId()) != null) {
            throw new IllegalArgumentException("A task with that id already exists");
        } else if (newTask.getId() >= nextId) {
            nextId = newTask.getId() + 1;
        }
        tasks.add(newTask);
    }

    // makes a new task from its parts, adds it, and gives it back
    public task addTask(String name, String description, String createdBy, task.Priority priority) {
        task newTask = new task(name, description, createdBy, priority);
        addTask(newTask);
        return newTask;
    }

    // ---------- finding and removing ----------

    // find a task by its id, returns null if we can't find it
    public task findById(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                return tasks.get(i);
            }
        }
        return null;
    }

    // delete a task, returns true if it was deleted
    public boolean removeTask(int id) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        tasks.remove(found);
        return true;
    }

    // ---------- changing tasks ----------

    // assign a task to a user, returns false if the task doesn't exist
    public boolean assignTask(int id, String username) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        found.setAssignedTo(username);
        return true;
    }

    // changes the status of a task, returns false if the task doesn't exist
    // finishing a task saves today's date, moving it back to unfinished clears the date
    public boolean changeStatus(int id, task.Status newStatus) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        found.setStatus(newStatus);
        if (newStatus == task.Status.COMPLETE) {
            found.setDateFinished(LocalDate.now());
        } else {
            found.setDateFinished(null);
        }
        return true;
    }

    // ---------- getting lists of tasks ----------

    // get every task (returns a copy so nobody messes with our real list)
    public ArrayList<task> getAllTasks() {
        return new ArrayList<task>(tasks);
    }

    // get the tasks a user created OR that are assigned to them
    public ArrayList<task> getTasksForUser(String username) {
        ArrayList<task> result = new ArrayList<task>();
        for (int i = 0; i < tasks.size(); i++) {
            task t = tasks.get(i);
            if (username.equals(t.getCreatedBy()) || username.equals(t.getAssignedTo())) {
                result.add(t);
            }
        }
        return result;
    }

    // get only the tasks that have this status
    public ArrayList<task> getTasksByStatus(task.Status status) {
        ArrayList<task> result = new ArrayList<task>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getStatus() == status) {
                result.add(tasks.get(i));
            }
        }
        return result;
    }

    // ---------- filtering ----------

    // gives back every task whose field matches the value
    // field is the same names as sortBy: name, description, createdby, assignedto,
    //     datecreated, datefinished, priority, status
    // value is written as text: dates look like 2026-11-16, priority is LOW/MEDIUM/HIGH,
    //     status is NOT_STARTED/WIP/COMPLETE
    // name, description and usernames ignore capital letters
    // a null value finds the tasks where that field is empty (like no one assigned)
    public ArrayList<task> filterBy(String field, String value) {
        String lowerField = field.toLowerCase();
        task sample = makeSampleTask(lowerField, value);

        ArrayList<task> result = new ArrayList<task>();
        for (int i = 0; i < tasks.size(); i++) {
            if (compareTasks(tasks.get(i), sample, lowerField) == 0) {
                result.add(tasks.get(i));
            }
        }
        return result;
    }

    // makes a throwaway task that only has the one field filled in with the value,
    // so compareTasks can compare each real task against it
    private task makeSampleTask(String field, String value) {
        task sample = new task("", "", "", task.Priority.LOW);
        switch (field) {
            case "name":
                sample.setName(value);
                break;
            case "description":
                sample.setDescription(value);
                break;
            case "createdby":
                sample.setCreatedBy(value);
                break;
            case "assignedto":
                sample.setAssignedTo(value);
                break;
            case "datecreated":
                sample.setDateCreated(toDate(value));
                break;
            case "datefinished":
                sample.setDateFinished(toDate(value));
                break;
            case "priority":
                if (value == null) {
                    throw new IllegalArgumentException("Priority can't be null");
                }
                sample.setPriority(task.Priority.valueOf(value.toUpperCase()));
                break;
            case "status":
                if (value == null) {
                    throw new IllegalArgumentException("Status can't be null");
                }
                sample.setStatus(task.Status.valueOf(value.toUpperCase()));
                break;
            default:
                throw new IllegalArgumentException("Can't filter by: " + field);
        }
        return sample;
    }

    // turns text like 2026-11-16 into a date, null stays null
    private LocalDate toDate(String value) {
        if (value == null) {
            return null;
        }
        return LocalDate.parse(value);
    }

    // ---------- sorting ----------

    // gives back a sorted copy of the list (the real list stays the same)
    // field can be: name, description, createdby, assignedto, datecreated,
    //               datefinished, priority, status
    // ascending = true goes A to Z / oldest to newest / LOW to HIGH, false is the other way
    // a task with nothing in that field (no date finished, nobody assigned) comes first
    // when ascending and last when descending
    public ArrayList<task> sortBy(String field, boolean ascending) {
        String lowerField = field.toLowerCase();
        ArrayList<task> sorted = new ArrayList<task>(tasks);

        // bubble sort: keep swapping neighbors that are in the wrong order
        for (int i = 0; i < sorted.size() - 1; i++) {
            for (int j = 0; j < sorted.size() - 1 - i; j++) {
                task first = sorted.get(j);
                task second = sorted.get(j + 1);
                int result = compareTasks(first, second, lowerField);
                if (!ascending) {
                    result = -result;
                }
                if (result > 0) {
                    sorted.set(j, second);
                    sorted.set(j + 1, first);
                }
            }
        }
        return sorted;
    }

    // compares two tasks by one field (field should be lowercase)
    // negative = a is first, positive = b is first, 0 = same
    private int compareTasks(task a, task b, String field) {
        switch (field) {
            case "name":
                return compareText(a.getName(), b.getName());
            case "description":
                return compareText(a.getDescription(), b.getDescription());
            case "createdby":
                return compareText(a.getCreatedBy(), b.getCreatedBy());
            case "assignedto":
                return compareText(a.getAssignedTo(), b.getAssignedTo());
            case "datecreated":
                return compareDates(a.getDateCreated(), b.getDateCreated());
            case "datefinished":
                return compareDates(a.getDateFinished(), b.getDateFinished());
            case "priority":
                return a.getPriority().compareTo(b.getPriority());
            case "status":
                return a.getStatus().compareTo(b.getStatus());
            default:
                throw new IllegalArgumentException("Can't sort by: " + field);
        }
    }

    // compares two pieces of text, ignoring capital letters
    // no text (null) is treated like blank text, so it comes first
    private int compareText(String a, String b) {
        String first = a;
        String second = b;
        if (first == null) {
            first = "";
        }
        if (second == null) {
            second = "";
        }
        return first.compareToIgnoreCase(second);
    }

    // compares two dates, earlier date comes first
    // no date yet (null) comes before any real date
    private int compareDates(LocalDate a, LocalDate b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        return a.compareTo(b);
    }

    // ---------- other ----------

    public int size() {
        return tasks.size();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
