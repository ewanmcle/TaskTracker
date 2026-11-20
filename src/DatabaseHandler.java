import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * DatabaseHandler is the connection to the SQLite database (using JDBC).
 * It's a table of tasks where you can read or change one value at a time.
 * It doesn't know about TaskList, whatever connects the two calls this class.
 *
 * Values use the same types as task.getField():
 *   DATE_CREATED, DATE_FINISHED, DUE_DATE -> LocalDate
 *   PRIORITY                              -> task.Priority
 *   STATUS                                -> task.Status
 *   everything else                       -> String
 * Every task in the database is found by its id.
 */
public class DatabaseHandler {

    private Connection connection;

    // uses the default database file (tasks.db)
    public DatabaseHandler() {
        this("tasks.db");
    }

    // uses a different database file, handy for JUnit tests
    // the file is created if it isn't there yet
    public DatabaseHandler(String dbFile) {
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

    // ---------- rows ----------

    // adds a new task to the table with the default values and gives back its id
    // use setField after this to fill it in
    public int insertRow() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO tasks DEFAULT VALUES");
            // ask SQLite for the id it just made (getGeneratedKeys doesn't work on newer drivers)
            try (ResultSet results = statement.executeQuery("SELECT last_insert_rowid()")) {
                results.next();
                return results.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't add a task: " + e.getMessage(), e);
        }
    }

    // deletes the task with this id, returns false if there wasn't one
    public boolean deleteRow(int taskId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            preparedStatement.setInt(1, taskId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't delete the task: " + e.getMessage(), e);
        }
    }

    // true if there is a task with this id
    public boolean rowExists(int taskId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT id FROM tasks WHERE id = ?")) {
            preparedStatement.setInt(1, taskId);
            try (ResultSet results = preparedStatement.executeQuery()) {
                return results.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't look up the task: " + e.getMessage(), e);
        }
    }

    // gives back the ids of every task, oldest first
    public ArrayList<Integer> getAllIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT id FROM tasks ORDER BY id")) {
            while (results.next()) {
                ids.add(results.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't load the task ids: " + e.getMessage(), e);
        }
        return ids;
    }

    // ---------- getting and setting one value ----------

    // gives back one value of a task, throws an error if there is no task with that id
    // an empty value (like no due date) comes back as null
    public Object getField(int taskId, task.Field field) {
        String sql = "SELECT " + columnName(field) + " FROM tasks WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, taskId);
            try (ResultSet results = preparedStatement.executeQuery()) {
                if (!results.next()) {
                    throw new IllegalArgumentException("No task with id " + taskId);
                }
                return textToValue(field, results.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't read the task: " + e.getMessage(), e);
        }
    }

    // changes one value of a task, throws an error if there is no task with that id
    // the value has to be the right type for the field (see the top of this file),
    // otherwise this throws a ClassCastException
    public void setField(int taskId, task.Field field, Object value) {
        String text = valueToText(field, value);
        String sql = "UPDATE tasks SET " + columnName(field) + " = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, text);
            preparedStatement.setInt(2, taskId);
            if (preparedStatement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No task with id " + taskId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't change the task: " + e.getMessage(), e);
        }
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

    // the column for a field is just its name in lowercase, like CREATED_BY -> created_by
    // (this is safe to put in the SQL because field can only be one of the enum values)
    private String columnName(task.Field field) {
        return field.name().toLowerCase();
    }

    // turns the text saved in the database back into the right type
    private Object textToValue(task.Field field, String text) {
        if (text == null) {
            return null;
        }
        switch (field) {
            case DATE_CREATED:
            case DATE_FINISHED:
            case DUE_DATE:
                return LocalDate.parse(text);
            case PRIORITY:
                return task.Priority.valueOf(text);
            case STATUS:
                return task.Status.valueOf(text);
            default:
                return text;
        }
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
