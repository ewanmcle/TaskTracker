import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * UserDatabaseHandler does all the SQL for the users table (SQLite through JDBC).
 * It only works with User objects. It doesn't know about task or TaskListHandler,
 * and they don't know about it.
 *
 * It uses the same database file as TaskListHandler (tasks.db) but has its own connection and its own table.
 * Passwords come in as plain text and get hashed in here before they are saved,
 * so only the hash is ever stored. Nothing outside this class needs to know about hashing.
 */
public class UserDatabaseHandler {

    private Connection connection;

    // uses the default database file (tasks.db)
    public UserDatabaseHandler() {
        this("tasks.db");
    }

    // uses a different database file, handy for JUnit tests
    public UserDatabaseHandler(String dbFile) {
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
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    // saves a new user, the password is hashed before it is saved
    // returns false (and saves nothing) if the username or email is already taken
    public boolean addUser(String username, String email, String password) {
        if (usernameExists(username) || emailExists(email)) {
            return false;
        }
        String passwordHash = hashPassword(password);
        User newUser = new User(username, email, passwordHash);
        String sql = "INSERT INTO users (username, email, password_hash, date_created) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            fillStatement(preparedStatement, newUser);
            preparedStatement.executeUpdate();
            // newer SQLite drivers don't support getGeneratedKeys(), so ask SQLite for the new id instead
            try (Statement statement = connection.createStatement();
                    ResultSet keys = statement.executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) {
                    newUser.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't save the user: " + e.getMessage(), e);
        }
        return true;
    }

    // find a user by username, returns null if we can't find them
    public User findByUsername(String username) {
        return findOne("SELECT * FROM users WHERE username = ?", username);
    }

    // used for logging in: true if this user exists and the password is right
    // the typed password is hashed and compared to the saved hash
    public boolean checkPassword(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return false;
        }
        String typedPasswordHash = hashPassword(password);
        return user.getPasswordHash().equals(typedPasswordHash);
    }

    // find a user by id, returns null if we can't find them
    public User findById(int userId) {
        return findOne("SELECT * FROM users WHERE id = ?", Integer.valueOf(userId));
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
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT * FROM users ORDER BY id")) {
            while (results.next()) {
                result.add(User.fromResultSet(results));
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            fillStatement(preparedStatement, changed);
            preparedStatement.setInt(5, changed.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't update the user: " + e.getMessage(), e);
        }
        return true;
    }

    // deletes the user with this id, returns false if there wasn't one
    public boolean deleteUser(int userId) {
        if (findById(userId) == null) {
            return false;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
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
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, value);
            try (ResultSet results = preparedStatement.executeQuery()) {
                if (results.next()) {
                    return User.fromResultSet(results);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't look up the user: " + e.getMessage(), e);
        }
        return null;
    }

    // turns a password into a SHA-256 hash so the plain password is never saved
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte oneByte : bytes) {
                hex.append(String.format("%02x", oneByte));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 isn't available", e);
        }
    }

    // fills in the first 4 ? for insert and update (same order in both)
    private void fillStatement(PreparedStatement preparedStatement, User user) throws SQLException {
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setString(2, user.getEmail());
        preparedStatement.setString(3, user.getPasswordHash());
        LocalDate created = user.getDateCreated();
        if (created == null) {
            preparedStatement.setNull(4, Types.DATE);
        } else {
            preparedStatement.setDate(4, Date.valueOf(created));
        }
    }
}
