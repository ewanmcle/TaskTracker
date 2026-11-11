import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class TaskTest {

	private task makeTask(LocalDate due) {
		return new task("Essay", "Write the essay", "ewan", due, task.Priority.HIGH);
	}

	@Test
	void newTaskHasSensibleDefaults() {
		task t = makeTask(LocalDate.now().plusDays(3));
		assertEquals("Essay", t.getName());
		assertEquals("Essay", t.getTitle());
		assertEquals("ewan", t.getCreatedBy());
		assertEquals("ewan", t.getAssignedTo());
		assertEquals(LocalDate.now(), t.getDateCreated());
		assertNull(t.getDateFinished());
		assertEquals(task.Status.NOT_STARTED, t.getStatus());
		assertFalse(t.isComplete());
		assertFalse(t.isCompleted());
	}

	@Test
	void startTaskSetsWip() {
		task t = makeTask(null);
		t.startTask();
		assertEquals(task.Status.WIP, t.getStatus());
		assertFalse(t.isComplete());
	}

	@Test
	void markCompleteSetsStatusAndDate() {
		task t = makeTask(null);
		t.markComplete();
		assertTrue(t.isComplete());
		assertEquals(LocalDate.now(), t.getDateFinished());
	}

	@Test
	void setCompletedTrueAndFalse() {
		task t = makeTask(null);
		t.setCompleted(true);
		assertTrue(t.isCompleted());
		assertNotNull(t.getDateFinished());
		t.setCompleted(false);
		assertFalse(t.isCompleted());
		assertEquals(task.Status.NOT_STARTED, t.getStatus());
		assertNull(t.getDateFinished());
	}

	@Test
	void overdueOnlyWhenPastDueAndNotDone() {
		assertTrue(makeTask(LocalDate.now().minusDays(1)).isOverdue());
		assertFalse(makeTask(LocalDate.now()).isOverdue());
		assertFalse(makeTask(LocalDate.now().plusDays(1)).isOverdue());
		assertFalse(makeTask(null).isOverdue());
		task done = makeTask(LocalDate.now().minusDays(1));
		done.markComplete();
		assertFalse(done.isOverdue());
	}

	@Test
	void belongsToCreatorOrAssignee() {
		task t = makeTask(null);
		t.setAssignedTo("sam");
		assertTrue(t.belongsTo("ewan"));
		assertTrue(t.belongsTo("sam"));
		assertFalse(t.belongsTo("alex"));
	}
}
