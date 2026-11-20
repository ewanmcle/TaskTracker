import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * The main window: a table of tasks with buttons to add, edit, change and delete them.
 * All changes go through TaskList.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class MainWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String ALL_STATUSES = "All statuses";
	private static final String[] COLUMNS = {"Name", "Description", "Created", "Priority", "Status", "Assigned to", "Created by"};

	private JPanel contentPane;
	private JTable table;
	private DefaultTableModel tableModel;
	private JComboBox<String> statusFilterBox;
	private JComboBox<String> sortBox;
	private JCheckBox myTasksCheck;
	private JLabel countLabel;
	private JLabel progressLabel;
	private JLabel userLabel;
	private JCheckBoxMenuItem darkModeItem;
	private JToggleButton darkModeButton;

	private String currentUser;
	private TaskList tasks;
	private UserDatabaseHandler users;
	private ArrayList<task> shownTasks = new ArrayList<task>();   // the tasks in the table, same order as the rows

	// WindowBuilder uses this constructor to show the design
	public MainWindow() {
		this("ewan", new TaskList(), null);
	}

	public MainWindow(String currentUser, TaskList tasks, UserDatabaseHandler users) {
		this.currentUser = currentUser;
		this.tasks = tasks;
		this.users = users;

		setTitle("TaskTracker | " + currentUser);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1040, 640);
		Theme.decorate(getRootPane());

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem logOutItem = new JMenuItem("Log Out");
		logOutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				logOut();
			}
		});
		fileMenu.add(logOutItem);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		fileMenu.add(exitItem);

		JMenu taskMenu = new JMenu("Task");
		menuBar.add(taskMenu);

		JMenuItem newTaskItem = new JMenuItem("New Task...");
		newTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				newTask();
			}
		});
		taskMenu.add(newTaskItem);

		JMenuItem editTaskItem = new JMenuItem("Edit Task...");
		editTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				editTask();
			}
		});
		taskMenu.add(editTaskItem);

		JMenuItem deleteTaskItem = new JMenuItem("Delete Task");
		deleteTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				deleteTask();
			}
		});
		taskMenu.add(deleteTaskItem);

		JMenu viewMenu = new JMenu("View");
		menuBar.add(viewMenu);

		darkModeItem = new JCheckBoxMenuItem("Dark Mode");
		darkModeItem.setSelected(Theme.isDark());
		darkModeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK));
		darkModeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setDarkMode(darkModeItem.isSelected());
			}
		});
		viewMenu.add(darkModeItem);

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About TaskTracker");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(MainWindow.this, "TaskTracker\nA simple team task tracker.");
			}
		});
		helpMenu.add(aboutItem);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(8, 8, 0, 8));
		contentPane.setLayout(new BorderLayout(0, 8));
		setContentPane(contentPane);

		// ---------- toolbar ----------

		JPanel toolbarPanel = new JPanel();
		contentPane.add(toolbarPanel, BorderLayout.NORTH);
		toolbarPanel.setLayout(new BorderLayout(0, 0));

		JPanel actionsPanel = new JPanel();
		FlowLayout fl_actionsPanel = (FlowLayout) actionsPanel.getLayout();
		fl_actionsPanel.setAlignment(FlowLayout.LEFT);
		toolbarPanel.add(actionsPanel, BorderLayout.WEST);

		JButton newButton = new JButton("New Task");
		newButton.putClientProperty("FlatLaf.styleClass", "primary");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				newTask();
			}
		});
		actionsPanel.add(newButton);

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				editTask();
			}
		});
		actionsPanel.add(editButton);

		JButton statusButton = new JButton("Change Status...");
		statusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				changeStatus();
			}
		});
		actionsPanel.add(statusButton);

		JButton assignButton = new JButton("Assign...");
		assignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				assignTask();
			}
		});
		actionsPanel.add(assignButton);

		JButton deleteButton = new JButton("Delete");
		deleteButton.putClientProperty("FlatLaf.styleClass", "danger");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				deleteTask();
			}
		});
		actionsPanel.add(deleteButton);

		JPanel viewPanel = new JPanel();
		FlowLayout fl_viewPanel = (FlowLayout) viewPanel.getLayout();
		fl_viewPanel.setAlignment(FlowLayout.RIGHT);
		toolbarPanel.add(viewPanel, BorderLayout.EAST);

		JLabel showLabel = new JLabel("Show");
		viewPanel.add(showLabel);

		statusFilterBox = new JComboBox<String>();
		statusFilterBox.setModel(new DefaultComboBoxModel<String>(new String[] {ALL_STATUSES, "NOT_STARTED", "WIP", "COMPLETE"}));
		statusFilterBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refreshTable();
			}
		});
		viewPanel.add(statusFilterBox);

		myTasksCheck = new JCheckBox("My tasks only");
		myTasksCheck.setSelected(true);
		myTasksCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refreshTable();
			}
		});
		viewPanel.add(myTasksCheck);

		JLabel sortLabel = new JLabel("Sort by");
		viewPanel.add(sortLabel);

		sortBox = new JComboBox<String>();
		sortBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Name", "Priority", "Status", "Date created"}));
		sortBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				refreshTable();
			}
		});
		viewPanel.add(sortBox);

		// ---------- table ----------

		JScrollPane tableScroll = new JScrollPane();
		contentPane.add(tableScroll, BorderLayout.CENTER);

		tableModel = new DefaultTableModel(new Object[][] {}, COLUMNS) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(false);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					editTask();
				}
			}
		});
		tableScroll.setViewportView(table);

		// ---------- status bar ----------

		JPanel statusBar = new JPanel();
		statusBar.setBorder(new EmptyBorder(2, 4, 4, 4));
		contentPane.add(statusBar, BorderLayout.SOUTH);
		statusBar.setLayout(new BorderLayout(0, 0));

		JPanel countsPanel = new JPanel();
		FlowLayout fl_countsPanel = (FlowLayout) countsPanel.getLayout();
		fl_countsPanel.setHgap(12);
		fl_countsPanel.setAlignment(FlowLayout.LEFT);
		statusBar.add(countsPanel, BorderLayout.WEST);

		countLabel = new JLabel("0 tasks");
		countsPanel.add(countLabel);

		progressLabel = new JLabel("0 in progress, 0 completed");
		countsPanel.add(progressLabel);

		JPanel accountPanel = new JPanel();
		FlowLayout fl_accountPanel = (FlowLayout) accountPanel.getLayout();
		fl_accountPanel.setHgap(10);
		fl_accountPanel.setAlignment(FlowLayout.RIGHT);
		statusBar.add(accountPanel, BorderLayout.EAST);

		userLabel = new JLabel("Signed in as " + currentUser);
		accountPanel.add(userLabel);

		darkModeButton = new JToggleButton("Dark Mode");
		darkModeButton.setSelected(Theme.isDark());
		darkModeButton.setToolTipText("Switch between light and dark mode");
		darkModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setDarkMode(darkModeButton.isSelected());
			}
		});
		accountPanel.add(darkModeButton);

		refreshTable();
	}

	// ---------- filling the table ----------

	// rebuilds the table from the TaskList using the Show, My tasks and Sort by controls
	private void refreshTable() {
		if (tasks == null || tableModel == null || sortBox == null || statusFilterBox == null || myTasksCheck == null) {
			return;   // still being built
		}
		task.Field sortField = sortFieldName((String) sortBox.getSelectedItem());
		boolean ascending = sortField != task.Field.PRIORITY;   // show HIGH priority first
		ArrayList<task> sorted = tasks.sortBy(sortField, ascending);

		String statusFilter = (String) statusFilterBox.getSelectedItem();
		boolean mineOnly = myTasksCheck.isSelected();

		shownTasks.clear();
		tableModel.setRowCount(0);
		int inProgress = 0;
		int completed = 0;
		for (task currentTask : sorted) {
			task.Status status = (task.Status) currentTask.getField(task.Field.STATUS);
			String createdBy = (String) currentTask.getField(task.Field.CREATED_BY);
			String assignedTo = (String) currentTask.getField(task.Field.ASSIGNED_TO);
			if (!ALL_STATUSES.equals(statusFilter) && !status.name().equals(statusFilter)) {
				continue;
			}
			if (mineOnly && !currentUser.equals(createdBy) && !currentUser.equals(assignedTo)) {
				continue;
			}
			shownTasks.add(currentTask);
			tableModel.addRow(new Object[] {
					currentTask.getField(task.Field.NAME),
					currentTask.getField(task.Field.DESCRIPTION),
					currentTask.getField(task.Field.DATE_CREATED),
					currentTask.getField(task.Field.PRIORITY),
					status,
					assignedTo == null ? TaskForm.NOBODY : assignedTo,
					createdBy
			});
			if (status == task.Status.WIP) {
				inProgress++;
			} else if (status == task.Status.COMPLETE) {
				completed++;
			}
		}
		countLabel.setText(shownTasks.size() + (shownTasks.size() == 1 ? " task" : " tasks"));
		progressLabel.setText(inProgress + " in progress, " + completed + " completed");
	}

	// turns the Sort by choice into the task.Field TaskList.sortBy wants
	private task.Field sortFieldName(String choice) {
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
		int row = table.getSelectedRow();
		if (row < 0 || row >= shownTasks.size()) {
			JOptionPane.showMessageDialog(this, "Select a task first.");
			return null;
		}
		return shownTasks.get(row);
	}

	// everyone a task can be assigned to
	private String[] usernames() {
		if (users == null) {
			return new String[] {currentUser};
		}
		ArrayList<User> all = users.getAllUsers();
		String[] names = new String[all.size()];
		for (int i = 0; i < all.size(); i++) {
			names[i] = all.get(i).getUsername();
		}
		return names;
	}

	// ---------- actions ----------

	private void newTask() {
		TaskForm taskForm = new TaskForm(this, null, currentUser, usernames());
		taskForm.setVisible(true);
		if (!taskForm.isSaved()) {
			return;
		}
		task created = tasks.addTask(taskForm.getTaskName(), taskForm.getDescriptionText(), currentUser, null, taskForm.getPriority());
		tasks.assignTask(created.getId(), taskForm.getAssignee());
		tasks.changeStatus(created.getId(), taskForm.getStatus());
		refreshTable();
	}

	private void editTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		TaskForm taskForm = new TaskForm(this, chosenTask, currentUser, usernames());
		taskForm.setVisible(true);
		if (!taskForm.isSaved()) {
			return;
		}
		chosenTask.setField(task.Field.NAME, taskForm.getTaskName());
		chosenTask.setField(task.Field.DESCRIPTION, taskForm.getDescriptionText());
		chosenTask.setField(task.Field.PRIORITY, taskForm.getPriority());
		tasks.assignTask(chosenTask.getId(), taskForm.getAssignee());
		if (taskForm.getStatus() != chosenTask.getField(task.Field.STATUS)) {
			tasks.changeStatus(chosenTask.getId(), taskForm.getStatus());
		}
		refreshTable();
	}

	private void changeStatus() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		task.Status picked = (task.Status) JOptionPane.showInputDialog(this, "New status for \"" + chosenTask.getField(task.Field.NAME) + "\":",
				"Change Status", JOptionPane.PLAIN_MESSAGE, null, task.Status.values(), chosenTask.getField(task.Field.STATUS));
		if (picked != null && picked != chosenTask.getField(task.Field.STATUS)) {
			tasks.changeStatus(chosenTask.getId(), picked);
			refreshTable();
		}
	}

	private void assignTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		String[] names = usernames();
		String[] choices = new String[names.length + 1];
		choices[0] = TaskForm.NOBODY;
		System.arraycopy(names, 0, choices, 1, names.length);
		String assigned = (String) chosenTask.getField(task.Field.ASSIGNED_TO);
		String current = assigned == null ? TaskForm.NOBODY : assigned;
		String picked = (String) JOptionPane.showInputDialog(this, "Assign \"" + chosenTask.getField(task.Field.NAME) + "\" to:",
				"Assign Task", JOptionPane.PLAIN_MESSAGE, null, choices, current);
		if (picked != null) {
			tasks.assignTask(chosenTask.getId(), TaskForm.NOBODY.equals(picked) ? null : picked);
			refreshTable();
		}
	}

	private void deleteTask() {
		task chosenTask = selectedTask();
		if (chosenTask == null) {
			return;
		}
		int answer = JOptionPane.showConfirmDialog(this, "Delete \"" + chosenTask.getField(task.Field.NAME) + "\"? This can't be undone.",
				"Delete Task", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (answer == JOptionPane.YES_OPTION) {
			tasks.removeTask(chosenTask.getId());
			refreshTable();
		}
	}

	// switches the whole app between dark and light, keeping the menu item and button in sync
	private void setDarkMode(boolean dark) {
		Theme.setDark(dark);
		darkModeItem.setSelected(dark);
		darkModeButton.setSelected(dark);
	}

	private void logOut() {
		LoginFrame login = new LoginFrame(users, tasks);
		login.setVisible(true);
		dispose();
	}
}
