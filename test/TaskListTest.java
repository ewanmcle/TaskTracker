import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TaskListTest {

	@TempDir
	Path tempDir;

	private String dbFile;
	private TaskList list;

	@BeforeEach
	void open() {
		dbFile = tempDir.resolve("tasks.db").toString();
		list = new TaskList(dbFile);
	}

	@AfterEach
	void close() {
		list.close();
	}

	private task make(String name, String by, LocalDate due) {
		return new task(name, "desc", by, due, task.Priority.MEDIUM);
	}

	private TaskList reopen() {
		list.close();
		list = new TaskList(dbFile);
		return list;
	}

	@Test
	void startsEmpty() {
		assertTrue(list.isEmpty());
		assertEquals(0, list.size());
	}

	@Test
	void addTaskGivesIdAndSaves() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		assertTrue(t.getId() > 0);
		assertSame(t, list.findById(t.getId()));
		assertEquals(1, reopen().size());
	}

	@Test
	void addNullThrows() {
		assertThrows(IllegalArgumentException.class, () -> list.addTask(null));
	}

	@Test
	void addSameTaskTwiceThrows() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		assertThrows(IllegalArgumentException.class, () -> list.addTask(t));
		assertEquals(1, list.size());
	}

	@Test
	void findByIdMissingReturnsNull() {
		assertNull(list.findById(999));
	}

	@Test
	void removeTaskDeletesFromListAndDatabase() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		assertTrue(list.removeTask(t.getId()));
		assertFalse(list.removeTask(t.getId()));
		assertTrue(reopen().isEmpty());
	}

	@Test
	void markCompleteSaves() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		assertTrue(list.markComplete(t.getId()));
		assertFalse(list.markComplete(999));
		task loaded = reopen().findById(t.getId());
		assertTrue(loaded.isCompleted());
		assertEquals(LocalDate.now(), loaded.getDateFinished());
	}

	@Test
	void assignTaskSaves() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		assertTrue(list.assignTask(t.getId(), "sam"));
		assertFalse(list.assignTask(999, "sam"));
		assertEquals("sam", reopen().findById(t.getId()).getAssignedTo());
	}

	@Test
	void updateTaskSavesSetterChanges() {
		task t = make("A", "ewan", null);
		list.addTask(t);
		t.setName("Renamed");
		t.setPriority(task.Priority.HIGH);
		assertTrue(list.updateTask(t));
		assertFalse(list.updateTask(null));
		assertFalse(list.updateTask(make("not added", "ewan", null)));
		task loaded = reopen().findById(t.getId());
		assertEquals("Renamed", loaded.getName());
		assertEquals(task.Priority.HIGH, loaded.getPriority());
	}

	@Test
	void getAllTasksReturnsCopy() {
		list.addTask(make("A", "ewan", null));
		ArrayList<task> copy = list.getAllTasks();
		copy.clear();
		assertEquals(1, list.size());
	}

	@Test
	void getTasksForUserIncludesCreatedAndAssigned() {
		task mine = make("Mine", "ewan", null);
		task assignedToMe = make("Theirs", "sam", null);
		task other = make("Other", "sam", null);
		list.addTask(mine);
		list.addTask(assignedToMe);
		list.addTask(other);
		list.assignTask(assignedToMe.getId(), "ewan");

		ArrayList<task> result = list.getTasksForUser("ewan");
		assertEquals(2, result.size());
		assertTrue(result.contains(mine));
		assertTrue(result.contains(assignedToMe));
	}

	@Test
	void filterByCompletion() {
		task a = make("A", "ewan", null);
		task b = make("B", "ewan", null);
		list.addTask(a);
		list.addTask(b);
		list.markComplete(a.getId());
		assertEquals(1, list.filterByCompletion(true).size());
		assertSame(a, list.filterByCompletion(true).get(0));
		assertSame(b, list.filterByCompletion(false).get(0));
	}

	@Test
	void sortByDueDatePutsNoDateLast() {
		task late = make("Late", "ewan", LocalDate.of(2026, 12, 20));
		task none = make("None", "ewan", null);
		task early = make("Early", "ewan", LocalDate.of(2026, 12, 1));
		list.addTask(late);
		list.addTask(none);
		list.addTask(early);
		ArrayList<task> sorted = list.getTasksSortedByDueDate();
		assertSame(early, sorted.get(0));
		assertSame(late, sorted.get(1));
		assertSame(none, sorted.get(2));
	}
}
