import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void newUserConstructor() {
		User u = new User("ewan", "ewan@example.com", "hash123");
		assertEquals(0, u.getId());
		assertEquals("ewan", u.getUsername());
		assertEquals("ewan@example.com", u.getEmail());
		assertEquals("hash123", u.getPasswordHash());
		assertEquals(LocalDate.now(), u.getDateCreated());
	}

	@Test
	void toStringHidesPassword() {
		User u = new User("ewan", "ewan@example.com", "secrethash");
		assertFalse(u.toString().contains("secrethash"));
		assertTrue(u.toString().contains("ewan"));
	}

	@Test
	void fromResultSetReadsRow() throws Exception {
		try (Connection c = DriverManager.getConnection("jdbc:sqlite::memory:");
				Statement st = c.createStatement()) {
			st.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT, email TEXT, "
					+ "password_hash TEXT, date_created DATE)");
			try (PreparedStatement ps = c.prepareStatement("INSERT INTO users VALUES (7, 'sam', 's@x.com', 'h', ?)")) {
				ps.setDate(1, java.sql.Date.valueOf(LocalDate.of(2026, 1, 15)));
				ps.executeUpdate();
			}
			try (ResultSet rs = st.executeQuery("SELECT * FROM users")) {
				assertTrue(rs.next());
				User u = User.fromResultSet(rs);
				assertEquals(7, u.getId());
				assertEquals("sam", u.getUsername());
				assertEquals("s@x.com", u.getEmail());
				assertEquals("h", u.getPasswordHash());
				assertEquals(LocalDate.of(2026, 1, 15), u.getDateCreated());
			}
		}
	}
}
