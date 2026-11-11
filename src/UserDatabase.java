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
 * UserDatabase does all the SQL for the users table (SQLite through JDBC).
 * It only works with User objects. It doesn't know about task or TaskList,
 * and they don't know about it.
 *
 * It uses the same database file as TaskList (tasks.db) but has its own connection and its own table.
 * Passwords are never handled here, the User object already holds the hash.
 */
public class UserDatabase {

    private Connection connection;

    // uses the default database file (tasks.db)
    public UserDatabase() {
        this("tasks.db");
    }

    // uses a different database file, handy for JUnit tests
    public UserDatabase(String dbFile) {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
            createTable();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't open the database: " + e.getMessage(), e);
        }
    }

    // column names match fromResultSet() in User.java
    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "date_created DATE NOT NULL)";
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    // saves a new user and gives it the id the database made for it
    // returns false (and saves nothing) if the username or email is already taken
    public boolean addUser(User newUser) {
        if (newUser == null) {
            throw new IllegalArgumentException("User can't be null");
        }
        if (usernameExists(newUser.getUsername()) || emailExists(newUser.getEmail())) {
            return false;
        }
        String sql = "INSERT INTO users (username, email, password_hash, date_created) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(ps, newUser);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    newUser.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't save the user: " + e.getMessage(), e);
        }
        return true;
    }

    // find a user by username, returns null if we can't find them (used for logging in)
    public User findByUsername(String username) {
        return findOne("SELECT * FROM users WHERE username = ?", username);
    }

    // find a user by id, returns null if we can't find them
    public User findById(int id) {
        return findOne("SELECT * FROM users WHERE id = ?", Integer.valueOf(id));
    }

    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    public boolean emailExists(String email) {
        return findOne("SELECT * FROM users WHERE email = ?", email) != null;
    }

    // gets every user
    public ArrayList<User> getAllUsers() {
        ArrayList<User> result = new ArrayList<User>();
        try (Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (rs.next()) {
                result.add(User.fromResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't load the users: " + e.getMessage(), e);
        }
        return result;
    }

    // overwrites the saved row with whatever the user looks like right now
    // returns false if the user was never saved or the new username/email belongs to someone else
    public boolean updateUser(User changed) {
        if (changed == null || findById(changed.getId()) == null) {
            return false;
        }
        User sameUsername = findByUsername(changed.getUsername());
        if (sameUsername != null && sameUsername.getId() != changed.getId()) {
            return false;
        }
        User sameEmail = findOne("SELECT * FROM users WHERE email = ?", changed.getEmail());
        if (sameEmail != null && sameEmail.getId() != changed.getId()) {
            return false;
        }
        String sql = "UPDATE users SET username = ?, email = ?, password_hash = ?, date_created = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            fillStatement(ps, changed);
            ps.setInt(5, changed.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't update the user: " + e.getMessage(), e);
        }
        return true;
    }

    // deletes the user with this id, returns false if there wasn't one
    public boolean deleteUser(int id) {
        if (findById(id) == null) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't delete the user: " + e.getMessage(), e);
        }
        return true;
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

    // runs a query that has one ? and gives back the first user it finds (or null)
    private User findOne(String sql, Object value) {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return User.fromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't look up the user: " + e.getMessage(), e);
        }
        return null;
    }

    // fills in the first 4 ? for insert and update (same order in both)
    private void fillStatement(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getUsername());
        ps.setString(2, u.getEmail());
        ps.setString(3, u.getPasswordHash());
        LocalDate created = u.getDateCreated();
        if (created == null) {
            ps.setNull(4, Types.DATE);
        } else {
            ps.setDate(4, Date.valueOf(created));
        }
    }
}
