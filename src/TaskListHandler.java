import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * TaskListHandler is the only class that stores and changes tasks.
 * The tasks live only in the SQLite database (tasks.db), which it reaches through JDBC.
 * Nothing keeps a list of tasks in memory: every method asks the database, and the methods
 * that give back tasks build a brand new ArrayList from the database rows each time.
 *
 * Values use the same types as task.getField():
 *   DATE_CREATED, DATE_FINISHED, DUE_DATE -> LocalDate
 *   PRIORITY                              -> task.Priority
 *   STATUS                                -> task.Status
 *   everything else                       -> String
 */
public class TaskListHandler {

    private Connection connection;

    // uses the default database file (tasks.db)
    public TaskListHandler() {
        this("tasks.db");
    }

    // uses a different database file, handy for JUnit tests
    // the file is created if it isn't there yet
    public TaskListHandler(String dbFile) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            createTable();
        } catch (SQLException e) {
            close();   // don't leave the connection open if making the table failed
            throw new RuntimeException("Couldn't open the database: " + e.getMessage(), e);
        }
    }

    // makes the tasks table if it isn't there yet
    // each column is named after a task.Field (NAME -> name, CREATED_BY -> created_by, ...)
    // dates are saved as text like 2026-11-16, priority and status as their names
    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS tasks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL DEFAULT '', "
                + "description TEXT, "
                + "created_by TEXT NOT NULL DEFAULT '', "
                + "assigned_to TEXT, "
                + "date_created TEXT NOT NULL DEFAULT (date('now', 'localtime')), "
                + "date_finished TEXT, "
                + "due_date TEXT, "
                + "priority TEXT NOT NULL DEFAULT 'LOW', "
                + "status TEXT NOT NULL DEFAULT 'NOT_STARTED')";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    // ---------- adding and removing ----------

    // saves a task as a new row, then gives the task the id the database made for it
    public void addTask(task newTask) {
        if (newTask == null) {
            throw new IllegalArgumentException("Task can't be null");
        }

        // build "INSERT INTO tasks (name, description, ...) VALUES (?, ?, ...)" with one column per field
        task.Field[] allFields = task.Field.values();
        String columns = "";
        String questionMarks = "";
        for (int i = 0; i < allFields.length; i++) {
            if (i > 0) {
                columns = columns + ", ";
                questionMarks = questionMarks + ", ";
            }
            columns = columns + columnName(allFields[i]);
            questionMarks = questionMarks + "?";
        }
        String sql = "INSERT INTO tasks (" + columns + ") VALUES (" + questionMarks + ")";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            // fill in the ? in the same order as the columns
            for (int i = 0; i < allFields.length; i++) {
                task.Field field = allFields[i];
                preparedStatement.setString(i + 1, valueToText(field, newTask.getField(field)));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't add the task: " + e.getMessage(), e);
        }
        newTask.setId(lastInsertedId());
    }

    // makes a new task from its parts, saves it, and gives it back (with its id)
    public task addTask(String name, String description, String createdBy, LocalDate dueDate,
            task.Priority priority) {
        task newTask = new task(name, description, createdBy, dueDate, priority);
        addTask(newTask);
        return newTask;
    }

    // deletes a task, returns true if it was deleted
    public boolean removeTask(int taskId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            preparedStatement.setInt(1, taskId);
            int rowsDeleted = preparedStatement.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't delete the task: " + e.getMessage(), e);
        }
    }

    // ---------- changing tasks ----------

    // changes any one field of a task (like its name), returns false if the task doesn't exist
    // the value has to be the right type for the field (see the top of this file)
    // use changeStatus for the status, so the date finished stays right
    public boolean changeField(int taskId, task.Field field, Object value) {
        String sql = "UPDATE tasks SET " + columnName(field) + " = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, valueToText(field, value));
            preparedStatement.setInt(2, taskId);
            int rowsChanged = preparedStatement.executeUpdate();
            return rowsChanged > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't change the task: " + e.getMessage(), e);
        }
    }

    // assign a task to a user (null means nobody), returns false if the task doesn't exist
    public boolean assignTask(int taskId, String username) {
        return changeField(taskId, task.Field.ASSIGNED_TO, username);
    }

    // changes the status of a task, returns false if the task doesn't exist
    // finishing a task saves today's date, moving it back to unfinished clears the date
    public boolean changeStatus(int taskId, task.Status newStatus) {
        boolean found = changeField(taskId, task.Field.STATUS, newStatus);
        if (!found) {
            return false;
        }
        if (newStatus == task.Status.COMPLETE) {
            changeField(taskId, task.Field.DATE_FINISHED, LocalDate.now());
        } else {
            changeField(taskId, task.Field.DATE_FINISHED, null);
        }
        return true;
    }

    // ---------- getting tasks ----------
    // each of these reads the database and gives back new task objects

    // gives back the task with this id, or null if there isn't one
    public task getTaskById(int taskId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM tasks WHERE id = ?")) {
            preparedStatement.setInt(1, taskId);
            try (ResultSet results = preparedStatement.executeQuery()) {
                if (results.next()) {
                    return taskFromRow(results);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't look up the task: " + e.getMessage(), e);
        }
    }

    // gives back every task, oldest first
    public ArrayList<task> getAllTasks() {
        return runQuery("SELECT * FROM tasks ORDER BY id", null);
    }

    // gives back every task sorted by one field
    // ascending = true goes A to Z / oldest to newest / LOW to HIGH, false is the other way
    // a task with nothing in that field (no date finished, nobody assigned) comes first
    // when ascending and last when descending
    public ArrayList<task> sortBy(task.Field field, boolean ascending) {
        String sortBy;
        if (field == task.Field.PRIORITY) {
            // sorting the text would give HIGH, LOW, MEDIUM, so sort by each one's place in the enum
            sortBy = rankingFor("priority", task.Priority.values());
        } else if (field == task.Field.STATUS) {
            sortBy = rankingFor("status", task.Status.values());
        } else {
            sortBy = columnName(field) + " COLLATE NOCASE";   // NOCASE ignores capital letters
        }

        String direction = "ASC";
        if (!ascending) {
            direction = "DESC";
        }

        // tasks that tie stay in the order they were made
        String sql = "SELECT * FROM tasks ORDER BY " + sortBy + " " + direction + ", id ASC";
        return runQuery(sql, null);
    }

    // gives back every task whose field matches the value, like filterBy(task.Field.STATUS, task.Status.WIP)
    // text ignores capital letters, and a null value finds the tasks where that field is empty
    public ArrayList<task> filterBy(task.Field field, Object value) {
        String text = valueToText(field, value);
        if (text == null) {
            String sql = "SELECT * FROM tasks WHERE " + columnName(field) + " IS NULL ORDER BY id";
            return runQuery(sql, null);
        }
        String sql = "SELECT * FROM tasks WHERE " + columnName(field) + " = ? COLLATE NOCASE ORDER BY id";
        return runQuery(sql, text);
    }

    // how many tasks there are
    public int size() {
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM tasks")) {
            results.next();
            return results.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't count the tasks: " + e.getMessage(), e);
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    // closes the database connection, call this when the program is done with it
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Problem closing the database: " + e.getMessage());
        }
    }

    // ---------- helpers ----------

    // runs a SELECT and turns every row into a task object in a new list
    // parameter fills in the one ? in the sql, or is null when the sql has no ?
    private ArrayList<task> runQuery(String sql, String parameter) {
        ArrayList<task> foundTasks = new ArrayList<task>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                preparedStatement.setString(1, parameter);
            }
            try (ResultSet results = preparedStatement.executeQuery()) {
                while (results.next()) {
                    foundTasks.add(taskFromRow(results));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't load the tasks: " + e.getMessage(), e);
        }
        return foundTasks;
    }

    // makes a task object from the row the ResultSet is on
    private task taskFromRow(ResultSet results) throws SQLException {
        int taskId = results.getInt("id");
        String name = results.getString("name");
        String description = results.getString("description");
        String createdBy = results.getString("created_by");
        String assignedTo = results.getString("assigned_to");
        LocalDate dateCreated = textToDate(results.getString("date_created"));
        LocalDate dateFinished = textToDate(results.getString("date_finished"));
        LocalDate dueDate = textToDate(results.getString("due_date"));
        task.Priority priority = task.Priority.valueOf(results.getString("priority"));
        task.Status status = task.Status.valueOf(results.getString("status"));
        return new task(taskId, name, description, createdBy, assignedTo,
                dateCreated, dateFinished, dueDate, priority, status);
    }

    // asks SQLite for the id it made for the last row added (getGeneratedKeys doesn't work on newer drivers)
    private int lastInsertedId() {
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT last_insert_rowid()")) {
            results.next();
            return results.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get the new task's id: " + e.getMessage(), e);
        }
    }

    // the column for a field is just its name in lowercase, like CREATED_BY -> created_by
    // (this is safe to put in the SQL because field can only be one of the enum values)
    private String columnName(task.Field field) {
        return field.name().toLowerCase();
    }

    // makes SQL that turns each enum name into its place in the enum, so they sort in the right order
    // for example "CASE priority WHEN 'LOW' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'HIGH' THEN 2 END"
    private String rankingFor(String column, Object[] valuesInOrder) {
        String sql = "CASE " + column;
        for (int i = 0; i < valuesInOrder.length; i++) {
            sql = sql + " WHEN '" + valuesInOrder[i] + "' THEN " + i;
        }
        sql = sql + " END";
        return sql;
    }

    // turns text like 2026-11-16 into a date, null stays null
    private LocalDate textToDate(String text) {
        if (text == null) {
            return null;
        }
        return LocalDate.parse(text);
    }

    // turns a value into the text that gets saved in the database
    private String valueToText(task.Field field, Object value) {
        if (value == null) {
            return null;
        }
        switch (field) {
            case DATE_CREATED:
            case DATE_FINISHED:
            case DUE_DATE:
                return ((LocalDate) value).toString();
            case PRIORITY:
                return ((task.Priority) value).name();
            case STATUS:
                return ((task.Status) value).name();
            default:
                return (String) value;
        }
    }
}
