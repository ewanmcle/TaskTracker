import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * TaskDatabase does all the SQL for the tasks table (SQLite through JDBC).
 * Only TaskList should use this class. Nothing else in the project talks to the database,
 * everything else goes through TaskList.
 */
class TaskDatabase {

    private Connection connection;

    // opens the database file (it gets created if it isn't there) and makes the table
    TaskDatabase(String dbFile) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            createTable();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't open the database: " + e.getMessage(), e);
        }
    }

    // column names match fromResultSet() in task.java
    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS tasks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "description TEXT, "
                + "created_by TEXT NOT NULL, "
                + "assigned_to TEXT, "
                + "date_created DATE NOT NULL, "
                + "date_finished DATE, "
                + "due_date DATE, "
                + "priority TEXT NOT NULL, "
                + "status TEXT NOT NULL)";
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    // gets every task in the table
    ArrayList<task> loadAll() {
        ArrayList<task> result = new ArrayList<task>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM tasks ORDER BY id")) {
            while (rs.next()) {
                result.add(task.fromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't load the tasks: " + e.getMessage(), e);
        }
        return result;
    }

    // saves a new task and gives it the id the database made for it
    void insert(task t) {
        String sql = "INSERT INTO tasks (name, description, created_by, assigned_to, date_created, "
                + "date_finished, due_date, priority, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, t);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't save the task: " + e.getMessage(), e);
        }
    }

    // overwrites the saved row with whatever the task looks like right now
    void update(task t) {
        String sql = "UPDATE tasks SET name = ?, description = ?, created_by = ?, assigned_to = ?, "
                + "date_created = ?, date_finished = ?, due_date = ?, priority = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, t);
            ps.setInt(10, t.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't update the task: " + e.getMessage(), e);
        }
    }

    // deletes the row with this id
    void delete(int id) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't delete the task: " + e.getMessage(), e);
        }
    }

    void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Problem closing the database: " + e.getMessage());
        }
    }

    // fills in the first 9 ? for insert and update (same order in both)
    private void fillStatement(PreparedStatement ps, task t) throws SQLException {
        ps.setString(1, t.getName());
        ps.setString(2, t.getDescription());
        ps.setString(3, t.getCreatedBy());
        ps.setString(4, t.getAssignedTo());
        setDate(ps, 5, t.getDateCreated());
        setDate(ps, 6, t.getDateFinished());
        setDate(ps, 7, t.getDueDate());
        ps.setString(8, t.getPriority().name());
        ps.setString(9, t.getStatus().name());
    }

    // dates can be null (no due date, not finished yet)
    private void setDate(PreparedStatement ps, int index, LocalDate date) throws SQLException {
        if (date == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(date));
        }
    }
}
