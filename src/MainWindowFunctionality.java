import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * MainWindowFunctionality is what the buttons, menus and controls on the main window do.
 * MainWindowFrame only builds the window, and each of its controls calls one method here.
 * All changes to tasks go through TaskListHandler.
 */
public class MainWindowFunctionality {

	private MainWindowFrame frame;        // the main window, used to read the controls and fill the table
	private String currentUser;
	private TaskListHandler tasks;
	private UserDatabaseHandler users;
	private ArrayList<task> shownTasks;   // the tasks in the table, in the same order as the rows

	public MainWindowFunctionality(MainWindowFrame frame, String currentUser, TaskListHandler tasks, UserDatabaseHandler users) {
		this.frame = frame;
		this.currentUser = currentUser;
		this.tasks = tasks;
		this.users = users;
		this.shownTasks = new ArrayList<task>();
	}

	// ---------- filling the table ----------

	// Show box, My tasks only box and Sort by box:
	// rebuilds the table from the saved tasks using what those three controls are set to
	public void refreshTable() {
		if (tasks == null || !frame.isFinishedBuilding()) {
			return;   // the window is still being built
		}

		task.Field sortField = sortChoiceToField(frame.getSortChoice());
		boolean ascending = true;
		if (sortField == task.Field.PRIORITY) {
			ascending = false;   // show HIGH priority first
		}
		ArrayList<task> sorted = tasks.sortBy(sortField, ascending);

		String statusFilter = frame.getStatusFilterChoice();
		boolean myTasksOnly = frame.isMyTasksOnly();

		shownTasks.clear();
		frame.clearTable();
		int inProgress = 0;
		int completed = 0;
		for (task currentTask : sorted) {
			task.Status status = (task.Status) currentTask.getField(task.Field.STATUS);
			String createdBy = (String) currentTask.getField(task.Field.CREATED_BY);
			String assignedTo = (String) currentTask.getField(task.Field.ASSIGNED_TO);

			// skip tasks that don't match the Show box
			if (!MainWindowFrame.ALL_STATUSES.equals(statusFilter) && !status.name().equals(statusFilter)) {
				continue;
			}
			// skip other people's tasks when My tasks only is checked
			if (myTasksOnly && !currentUser.equals(createdBy) && !currentUser.equals(assignedTo)) {
				continue;
			}

			String assignedText = assignedTo;
			if (assignedTo == null) {
				assignedText = TaskForm.NOBODY;
			}

			shownTasks.add(currentTask);
			Object[] row = new Object[] {
					currentTask.getField(task.Field.NAME),
					currentTask.getField(task.Field.DESCRIPTION),
					currentTask.getField(task.Field.DATE_CREATED),
					currentTask.getField(task.Field.PRIORITY),
					status,
					assignedText,
					createdBy
			};
			frame.addTableRow(row);

			if (status == task.Status.WIP) {
				inProgress++;
			} else if (status == task.Status.COMPLETE) {
				completed++;
			}
		}

		if (shownTasks.size() == 1) {
			frame.setCountText("1 task");
		} else {
			frame.setCountText(shownTasks.size() + " tasks");
		}
		frame.setProgressText(inProgress + " in progress, " + completed + " completed");
	}

	// ---------- task buttons and menu items ----------

	// New Task button and Task > New Task menu item
	public void newTask() {
		TaskForm taskForm = new TaskForm(frame, null, currentUser, usernames());
		taskForm.setVisible(true);   // waits here until the form is closed
		if (!taskForm.isSaved()) {
			return;
		}
		task createdTask = tasks.addTask(taskForm.getTaskName(), taskForm.getDescriptionText(), currentUser, null, taskForm.getPriority());
		tasks.assignTask(createdTask.getId(), taskForm.getAssignee());
		tasks.changeStatus(createdTask.getId(), taskForm.getStatus());
		refreshTable();
	}

	// Edit button, Task > Edit Task menu item, and double clicking a row
	public void editTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		TaskForm taskForm = new TaskForm(frame, chosenTask, currentUser, usernames());
		taskForm.setVisible(true);   // waits here until the form is closed
		if (!taskForm.isSaved()) {
			return;
		}
		tasks.changeField(chosenTask.getId(), task.Field.NAME, taskForm.getTaskName());
		tasks.changeField(chosenTask.getId(), task.Field.DESCRIPTION, taskForm.getDescriptionText());
		tasks.changeField(chosenTask.getId(), task.Field.PRIORITY, taskForm.getPriority());
		tasks.assignTask(chosenTask.getId(), taskForm.getAssignee());
		if (taskForm.getStatus() != chosenTask.getField(task.Field.STATUS)) {
			tasks.changeStatus(chosenTask.getId(), taskForm.getStatus());
		}
		refreshTable();
	}

	// Change Status button
	public void changeStatus() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		String message = "New status for \"" + chosenTask.getField(task.Field.NAME) + "\":";
		Object currentStatus = chosenTask.getField(task.Field.STATUS);
		task.Status picked = (task.Status) JOptionPane.showInputDialog(frame, message, "Change Status",
				JOptionPane.PLAIN_MESSAGE, null, task.Status.values(), currentStatus);
		if (picked != null && picked != currentStatus) {
			tasks.changeStatus(chosenTask.getId(), picked);
			refreshTable();
		}
	}

	// Assign button
	public void assignTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}

		// the choices are "Nobody" followed by every username
		String[] names = usernames();
		String[] choices = new String[names.length + 1];
		choices[0] = TaskForm.NOBODY;
		for (int i = 0; i < names.length; i++) {
			choices[i + 1] = names[i];
		}

		String assignedTo = (String) chosenTask.getField(task.Field.ASSIGNED_TO);
		String currentChoice = assignedTo;
		if (assignedTo == null) {
			currentChoice = TaskForm.NOBODY;
		}

		String message = "Assign \"" + chosenTask.getField(task.Field.NAME) + "\" to:";
		String picked = (String) JOptionPane.showInputDialog(frame, message, "Assign Task",
				JOptionPane.PLAIN_MESSAGE, null, choices, currentChoice);
		if (picked == null) {
			return;   // Cancel was pressed
		}

		String newAssignee = picked;
		if (TaskForm.NOBODY.equals(picked)) {
			newAssignee = null;
		}
		tasks.assignTask(chosenTask.getId(), newAssignee);
		refreshTable();
	}

	// Delete button and Task > Delete Task menu item
	public void deleteTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		String message = "Delete \"" + chosenTask.getField(task.Field.NAME) + "\"? This can't be undone.";
		int answer = JOptionPane.showConfirmDialog(frame, message, "Delete Task",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			tasks.removeTask(chosenTask.getId());
			refreshTable();
		}
	}

	// ---------- other menu items and buttons ----------

	// Dark Mode button and View > Dark Mode menu item
	// switches the whole app, and keeps the button and menu item showing the same thing
	public void setDarkMode(boolean dark) {
		Theme.setDark(dark);
		frame.setDarkModeSelected(dark);
	}

	// File > Log Out: goes back to the login window
	public void logOut() {
		LoginFrame loginFrame = new LoginFrame(users, tasks);
		loginFrame.setVisible(true);
		frame.dispose();
	}

	// File > Exit
	public void exit() {
		System.exit(0);
	}

	// Help > About TaskTracker
	public void showAbout() {
		JOptionPane.showMessageDialog(frame, "TaskTracker\nA simple team task tracker.");
	}

	// ---------- helpers ----------

	// turns the Sort by choice into the task.Field that TaskListHandler.sortBy wants
	private task.Field sortChoiceToField(String choice) {
		if ("Priority".equals(choice)) {
			return task.Field.PRIORITY;
		}
		if ("Status".equals(choice)) {
			return task.Field.STATUS;
		}
		if ("Date created".equals(choice)) {
			return task.Field.DATE_CREATED;
		}
		return task.Field.NAME;
	}

	// the task on the selected row, or null (with a message) if nothing is selected
	private task selectedTask() {
		int row = frame.getSelectedRow();
		if (row < 0 || row >= shownTasks.size()) {
			JOptionPane.showMessageDialog(frame, "Select a task first.");
			return null;
		}
		return shownTasks.get(row);
	}

	// everyone a task can be assigned to
	private String[] usernames() {
		if (users == null) {
			String[] onlyMe = new String[1];
			onlyMe[0] = currentUser;
			return onlyMe;
		}
		ArrayList<User> allUsers = users.getAllUsers();
		String[] names = new String[allUsers.size()];
		for (int i = 0; i < allUsers.size(); i++) {
			names[i] = allUsers.get(i).getUsername();
		}
		return names;
	}
}
