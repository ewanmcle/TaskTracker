import java.time.LocalDate;
import java.util.ArrayList;

/**
 * TaskList is the list of task objects the program works with.
 * It can add, find, remove and change tasks, and it can sort and filter them.
 *
 * Every change to a task goes through this class, and each change is also saved to the
 * SQLite database through DatabaseHandler, so the list and the database always match.
 * A TaskList made with a DatabaseHandler starts by loading every task already saved.
 * A TaskList made without one (new TaskList()) only keeps tasks in memory, which is
 * handy for WindowBuilder's Design tab and for tests.
 */
public class TaskList {

    private ArrayList<task> tasks;
    private DatabaseHandler database;   // null when the tasks are only kept in memory
    private int nextId;                 // the id the next new task gets when there is no database

    // keeps the tasks in memory only, nothing is saved
    public TaskList() {
        tasks = new ArrayList<task>();
        database = null;
        nextId = 1;
    }

    // saves every change to the database, and starts by loading every task already saved there
    public TaskList(DatabaseHandler database) {
        tasks = new ArrayList<task>();
        this.database = database;
        nextId = 1;
        loadFromDatabase();
    }

    // ---------- loading ----------

    // makes a task object for every row in the database and puts it in the list
    private void loadFromDatabase() {
        ArrayList<Integer> taskIds = database.getAllIds();
        for (int taskId : taskIds) {
            task loadedTask = new task("", "", "", null, task.Priority.LOW);
            loadedTask.setId(taskId);
            // copy every field from the database row into the task
            for (task.Field field : task.Field.values()) {
                loadedTask.setField(field, database.getField(taskId, field));
            }
            tasks.add(loadedTask);
        }
    }

    // ---------- adding tasks ----------

    // adds a task that was already made and gives it an id
    // with a database, the database makes the id and every field gets saved
    public void addTask(task newTask) {
        if (newTask == null) {
            throw new IllegalArgumentException("Task can't be null");
        }
        if (newTask.getId() != 0 && findById(newTask.getId()) != null) {
            throw new IllegalArgumentException("A task with that id already exists");
        }

        if (database != null) {
            // the database makes a new row and its id, then every field is saved into it
            int newId = database.insertRow();
            newTask.setId(newId);
            saveAllFields(newTask);
        } else {
            // no database, so the list hands out the ids itself
            if (newTask.getId() == 0) {
                newTask.setId(nextId);
                nextId++;
            } else if (newTask.getId() >= nextId) {
                nextId = newTask.getId() + 1;
            }
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
    public task findById(int taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == taskId) {
                return tasks.get(i);
            }
        }
        return null;
    }

    // delete a task from the list and the database, returns true if it was deleted
    public boolean removeTask(int taskId) {
        task found = findById(taskId);
        if (found == null) {
            return false;
        }
        if (database != null) {
            database.deleteRow(taskId);
        }
        tasks.remove(found);
        return true;
    }

    // ---------- changing tasks ----------
    // each of these changes the task and then saves what changed

    // assign a task to a user, returns false if the task doesn't exist
    public boolean assignTask(int taskId, String username) {
        task found = findById(taskId);
        if (found == null) {
            return false;
        }
        found.setField(task.Field.ASSIGNED_TO, username);
        saveField(found, task.Field.ASSIGNED_TO);
        return true;
    }

    // changes the status of a task, returns false if the task doesn't exist
    // finishing a task saves today's date, moving it back to unfinished clears the date
    public boolean changeStatus(int taskId, task.Status newStatus) {
        task found = findById(taskId);
        if (found == null) {
            return false;
        }
        found.setField(task.Field.STATUS, newStatus);
        if (newStatus == task.Status.COMPLETE) {
            found.setField(task.Field.DATE_FINISHED, LocalDate.now());
        } else {
            found.setField(task.Field.DATE_FINISHED, null);
        }
        saveField(found, task.Field.STATUS);
        saveField(found, task.Field.DATE_FINISHED);
        return true;
    }

    // changes any one field of a task (like its name) and saves it,
    // returns false if the task doesn't exist
    // the value has to be the right type for the field (see task.getField)
    // use changeStatus for the status, so the date finished stays right
    public boolean changeField(int taskId, task.Field field, Object value) {
        task found = findById(taskId);
        if (found == null) {
            return false;
        }
        found.setField(field, value);
        saveField(found, field);
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
        for (int round = 0; round < sorted.size() - 1; round++) {
            for (int position = 0; position < sorted.size() - 1 - round; position++) {
                task first = sorted.get(position);
                task second = sorted.get(position + 1);
                int result = compareTasks(first, second, field);
                if (!ascending) {
                    result = -result;
                }
                if (result > 0) {
                    sorted.set(position, second);
                    sorted.set(position + 1, first);
                }
            }
        }
        return sorted;
    }

    // compares two tasks by one field
    // negative = a is first, positive = b is first, 0 = same
    private int compareTasks(task firstTask, task secondTask, task.Field field) {
        // priority and status are compared as enums, because alphabetical order
        // (HIGH, LOW, MEDIUM) is not the real order (LOW, MEDIUM, HIGH)
        if (field == task.Field.PRIORITY) {
            task.Priority priorityA = (task.Priority) firstTask.getField(field);
            task.Priority priorityB = (task.Priority) secondTask.getField(field);
            return priorityA.compareTo(priorityB);
        }
        if (field == task.Field.STATUS) {
            task.Status statusA = (task.Status) firstTask.getField(field);
            task.Status statusB = (task.Status) secondTask.getField(field);
            return statusA.compareTo(statusB);
        }
        // everything else is compared as lowercase text
        return firstTask.getFieldAsText(field).compareTo(secondTask.getFieldAsText(field));
    }

    // ---------- saving ----------

    // saves one field of a task to the database (does nothing when there is no database)
    private void saveField(task changedTask, task.Field field) {
        if (database == null) {
            return;
        }
        database.setField(changedTask.getId(), field, changedTask.getField(field));
    }

    // saves every field of a task to the database
    private void saveAllFields(task changedTask) {
        for (task.Field field : task.Field.values()) {
            saveField(changedTask, field);
        }
    }

    // ---------- other ----------

    public int size() {
        return tasks.size();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}
