import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class User {

	private int userId; // id from the database
	private String username;
	private String email;
	private String passwordHash; // never store the plain password
	private LocalDate dateCreated;

	// constructor for making a new user (id gets set after it's saved to the database)
	public User(String username, String email, String passwordHash) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.dateCreated = LocalDate.now();
	}

	// constructor for when we load a user from the database
	public User(int userId, String username, String email, String passwordHash, LocalDate dateCreated) {
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
		this.dateCreated = dateCreated;
	}

	// builds a User from the current row of a JDBC ResultSet
	// column names match the users table in schema.sql
	public static User fromResultSet(ResultSet results) throws SQLException {
		Date created = results.getDate("date_created");
		return new User(
				results.getInt("id"),
				results.getString("username"),
				results.getString("email"),
				results.getString("password_hash"),
				created == null ? null : created.toLocalDate());
	}

	// getters and setters

	public int getId() {
		return userId;
	}

	public void setId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public LocalDate getDateCreated() {
		return dateCreated;
	}

	// leaves the password out on purpose
	@Override
	public String toString() {
		return username + " | " + email + " | Joined: " + dateCreated;
	}

}
