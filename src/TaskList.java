import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * TaskList keeps track of all the Task objects.
 * It only stores tasks in memory, the database stuff will be in a different class.
 *
 * Methods we need in the Task class (from the other half of the project):
 *   int getId()
 *   String getTitle()
 *   boolean isCompleted()
 *   void setCompleted(boolean completed)
 *   String getCreatedBy()
 *   String getAssignedTo()
 *   void setAssignedTo(String username)
 *   LocalDate getDueDate()   (only used for sorting, can be null)
 */
public class TaskList {

    private ArrayList<Task> tasks;

    public TaskList() {
        tasks = new ArrayList<Task>();
    }

    // add a task to the list
    public void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task can't be null");
        }
        // make sure there isn't already a task with this id
        if (findById(task.getId()) != null) {
            throw new IllegalArgumentException("A task with that id already exists");
        }
        tasks.add(task);
    }

    // find a task by its id, returns null if we can't find it
    public Task findById(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId() == id) {
                return tasks.get(i);
            }
        }
        return null;
    }

    // delete a task, returns true if it was deleted
    public boolean removeTask(int id) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }
        tasks.remove(task);
        return true;
    }

    // mark a task as complete, returns false if the task doesn't exist
    public boolean markComplete(int id) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }
        task.setCompleted(true);
        return true;
    }

    // assign a task to a user, returns false if the task doesn't exist
    public boolean assignTask(int id, String username) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }
        task.setAssignedTo(username);
        return true;
    }

    // get every task (returns a copy so nobody messes with our real list)
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<Task>(tasks);
    }

    // get tasks that the user created OR that are assigned to them
    public ArrayList<Task> getTasksForUser(String username) {
        ArrayList<Task> result = new ArrayList<Task>();
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (username.equals(t.getCreatedBy()) || username.equals(t.getAssignedTo())) {
                result.add(t);
            }
        }
        return result;
    }

    // extra feature: filter by completed (true) or not completed (false)
    public ArrayList<Task> filterByCompletion(boolean completed) {
        ArrayList<Task> result = new ArrayList<Task>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isCompleted() == completed) {
                result.add(tasks.get(i));
            }
        }
        return result;
    }

    // extra feature: sort by due date (earliest first, no due date goes at the end)
    public ArrayList<Task> getTasksSortedByDueDate() {
        ArrayList<Task> sorted = new ArrayList<Task>(tasks);
        Collections.sort(sorted, new Comparator<Task>() {
            public int compare(Task a, Task b) {
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