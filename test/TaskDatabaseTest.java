import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskDatabaseTest {

	@TempDir
	Path tempDir;

	private String dbFile;
	private TaskDatabase db;

	@BeforeEach
	void open() {
		dbFile = tempDir.resolve("test.db").toString();
		db = new TaskDatabase(dbFile);
	}

	@AfterEach
	void close() {
		db.close();
	}

	@Test
	void startsEmpty() {
		assertTrue(db.loadAll().isEmpty());
	}

	@Test
	void insertGivesIdsAndRoundTripsEveryField() {
		task a = new task("A", "first", "ewan", LocalDate.of(2026, 12, 1), task.Priority.LOW);
		task b = new task("B", null, "sam", null, task.Priority.HIGH);
		db.insert(a);
		db.insert(b);
		assertTrue(a.getId() > 0);
		assertTrue(b.getId() > a.getId());

		ArrayList<task> loaded = db.loadAll();
		assertEquals(2, loaded.size());
		task la = loaded.get(0);
		assertEquals(a.getId(), la.getId());
		assertEquals("A", la.getName());
		assertEquals("first", la.getDescription());
		assertEquals("ewan", la.getCreatedBy());
		assertEquals("ewan", la.getAssignedTo());
		assertEquals(a.getDateCreated(), la.getDateCreated());
		assertNull(la.getDateFinished());
		assertEquals(LocalDate.of(2026, 12, 1), la.getDueDate());
		assertEquals(task.Priority.LOW, la.getPriority());
		assertEquals(task.Status.NOT_STARTED, la.getStatus());

		task lb = loaded.get(1);
		assertNull(lb.getDescription());
		assertNull(lb.getDueDate());
		assertEquals(task.Priority.HIGH, lb.getPriority());
	}

	@Test
	void updateSavesChanges() {
		task t = new task("A", "d", "ewan", null, task.Priority.MEDIUM);
		db.insert(t);
		t.setName("A2");
		t.setAssignedTo("sam");
		t.markComplete();
		db.update(t);

		task loaded = db.loadAll().get(0);
		assertEquals("A2", loaded.getName());
		assertEquals("sam", loaded.getAssignedTo());
		assertEquals(task.Status.COMPLETE, loaded.getStatus());
		assertEquals(LocalDate.now(), loaded.getDateFinished());
	}

	@Test
	void deleteRemovesRow() {
		task t = new task("A", "d", "ewan", null, task.Priority.MEDIUM);
		db.insert(t);
		db.delete(t.getId());
		assertTrue(db.loadAll().isEmpty());
	}

	@Test
	void dataSurvivesReopening() {
		db.insert(new task("A", "d", "ewan", null, task.Priority.MEDIUM));
		db.close();
		db = new TaskDatabase(dbFile);
		assertEquals(1, db.loadAll().size());
	}
}
