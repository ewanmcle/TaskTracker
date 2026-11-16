import java.time.LocalDate;
import java.util.ArrayList;

/**
 * TaskList is a list of task objects.
 * It can add, find, remove and change tasks, and it can sort and filter them.
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
    public task addTask(String name, String description, String createdBy, LocalDate dueDate,
            task.Priority priority) {
        task newTask = new task(name, description, createdBy, dueDate, priority);
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
        found.setField(task.Field.ASSIGNED_TO, username);
        return true;
    }

    // changes the status of a task, returns false if the task doesn't exist
    // finishing a task saves today's date, moving it back to unfinished clears the date
    public boolean changeStatus(int id, task.Status newStatus) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        found.setField(task.Field.STATUS, newStatus);
        if (newStatus == task.Status.COMPLETE) {
            found.setField(task.Field.DATE_FINISHED, LocalDate.now());
        } else {
            found.setField(task.Field.DATE_FINISHED, null);
        }
        return true;
    }

    // ---------- getting lists of tasks ----------

    // get every task (returns a copy so nobody messes with our real list)
    public ArrayList<task> getAllTasks() {
        return new ArrayList<task>(tasks);
    }

    // gives back every task that matches the given task in one field
    // match is a task that has the value you are looking for in that field,
    //     the rest of its fields don't matter
    public ArrayList<task> filterBy(task.Field field, task match) {
        ArrayList<task> result = new ArrayList<task>();
        for (int i = 0; i < tasks.size(); i++) {
            if (compareTasks(tasks.get(i), match, field) == 0) {
                result.add(tasks.get(i));
            }
        }
        return result;
    }

    // gives back a sorted copy of the list (the real list stays the same)
    // ascending = true goes A to Z / oldest to newest / LOW to HIGH, false is the other way
    // a task with nothing in that field (no date finished, nobody assigned) comes first
    // when ascending and last when descending
    public ArrayList<task> sortBy(task.Field field, boolean ascending) {
        ArrayList<task> sorted = new ArrayList<task>(tasks);

        // bubble sort: keep swapping neighbors that are in the wrong order
        for (int i = 0; i < sorted.size() - 1; i++) {
            for (int j = 0; j < sorted.size() - 1 - i; j++) {
                task first = sorted.get(j);
                task second = sorted.get(j + 1);
                int result = compareTasks(first, second, field);
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

    // compares two tasks by one field
    // negative = a is first, positive = b is first, 0 = same
    private int compareTasks(task a, task b, task.Field field) {
        // priority and status are compared as enums, because alphabetical order
        // (HIGH, LOW, MEDIUM) is not the real order (LOW, MEDIUM, HIGH)
        if (field == task.Field.PRIORITY) {
            task.Priority priorityA = (task.Priority) a.getField(field);
            task.Priority priorityB = (task.Priority) b.getField(field);
            return priorityA.compareTo(priorityB);
        }
        if (field == task.Field.STATUS) {
            task.Status statusA = (task.Status) a.getField(field);
            task.Status statusB = (task.Status) b.getField(field);
            return statusA.compareTo(statusB);
        }
        // everything else is compared as lowercase text
        return a.getFieldAsText(field).compareTo(b.getFieldAsText(field));
    }

    // ---------- other ----------

    public int size() {
        return tasks.size();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
