import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * TaskList keeps track of all the task objects.
 * It only stores tasks in memory, the database stuff will be in a different class.
 *
 * Methods we need in the task class (from the other half of the project):
 *   int getId()
 *   String getTitle()
 *   boolean isCompleted()
 *   void setCompleted(boolean completed)
 *   String getCreatedBy()
 *   String getAssignedTo()
 *   void setAssignedTo(String user name)
 *   LocalDate getDueDate()   (only used for sorting, can be null)
 */
public class TaskList {

    private ArrayList<task> tasks;

    public TaskList() {
        tasks = new ArrayList<task>();
    }

    // add a task to the list
    public void addTask(task newTask) {
        if (newTask == null) {
            throw new IllegalArgumentException("Task can't be null");
        }
        // make sure there isn't already a task with this id
        if (findById(newTask.getId()) != null) {
            throw new IllegalArgumentException("A task with that id already exists");
        }
        tasks.add(newTask);
    }

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

    // mark a task as complete, returns false if the task doesn't exist
    public boolean markComplete(int id) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        found.markComplete();
        return true;
    }

    // assign a task to a user, returns false if the task doesn't exist
    public boolean assignTask(int id, String username) {
        task found = findById(id);
        if (found == null) {
            return false;
        }
        found.setAssignedTo(username);
        return true;
    }

    // get every task (returns a copy so nobody messes with our real list)
    public ArrayList<task> getAllTasks() {
        return new ArrayList<task>(tasks);
    }

    // get tasks that the user created OR that are assigned to them
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

    // extra feature: filter by completed (true) or not completed (false)
    public ArrayList<task> filterByCompletion(boolean completed) {
        ArrayList<task> result = new ArrayList<task>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isComplete() == completed) {
                result.add(tasks.get(i));
            }
        }
        return result;
    }

    // extra feature: sort by due date (earliest first, no due date goes at the end)
    public ArrayList<task> getTasksSortedByDueDate() {
        ArrayList<task> sorted = new ArrayList<task>(tasks);
        Collections.sort(sorted, new Comparator<task>() {
            public int compare(task a, task b) {
                if (a.getDueDate() == null && b.getDueDate() == null) {
                    return 0;
                }
                if (a.getDueDate() == null) {
                    return 1;
                }
                if (b.getDueDate() == null) {
                    return -1;
                }
                return a.getDueDate().compareTo(b.getDueDate());
            }
        });
        return sorted;
    }

    public int size() {
        return tasks.size();
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }
}