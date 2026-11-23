import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
 * This class only builds the window. What the buttons, menus and controls do is in
 * MainWindowFunctionality.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class MainWindowFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	// the first choice in the Show box, MainWindowFunctionality checks for it
	public static final String ALL_STATUSES = "All statuses";
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

	private MainWindowFunctionality functionality;   // what the buttons, menus and controls do
	private boolean finishedBuilding;                // becomes true once every control exists

	// WindowBuilder uses this constructor to show the design
	public MainWindowFrame() {
		this("ewan", null, null);
	}

	public MainWindowFrame(String currentUser, TaskListHandler tasks, UserDatabaseHandler users) {
		functionality = new MainWindowFunctionality(this, currentUser, tasks, users);

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
				functionality.logOut();
			}
		});
		fileMenu.add(logOutItem);

		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.exit();
			}
		});
		fileMenu.add(exitItem);

		JMenu taskMenu = new JMenu("Task");
		menuBar.add(taskMenu);

		JMenuItem newTaskItem = new JMenuItem("New Task...");
		newTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.newTask();
			}
		});
		taskMenu.add(newTaskItem);

		JMenuItem editTaskItem = new JMenuItem("Edit Task...");
		editTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.editTask();
			}
		});
		taskMenu.add(editTaskItem);

		JMenuItem deleteTaskItem = new JMenuItem("Delete Task");
		deleteTaskItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.deleteTask();
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
				functionality.setDarkMode(darkModeItem.isSelected());
			}
		});
		viewMenu.add(darkModeItem);

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		JMenuItem aboutItem = new JMenuItem("About TaskTracker");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.showAbout();
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
				functionality.newTask();
			}
		});
		actionsPanel.add(newButton);

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.editTask();
			}
		});
		actionsPanel.add(editButton);

		JButton statusButton = new JButton("Change Status...");
		statusButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.changeStatus();
			}
		});
		actionsPanel.add(statusButton);

		JButton assignButton = new JButton("Assign...");
		assignButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.assignTask();
			}
		});
		actionsPanel.add(assignButton);

		JButton deleteButton = new JButton("Delete");
		deleteButton.putClientProperty("FlatLaf.styleClass", "danger");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.deleteTask();
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
				functionality.refreshTable();
			}
		});
		viewPanel.add(statusFilterBox);

		myTasksCheck = new JCheckBox("My tasks only");
		myTasksCheck.setSelected(true);
		myTasksCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.refreshTable();
			}
		});
		viewPanel.add(myTasksCheck);

		JLabel sortLabel = new JLabel("Sort by");
		viewPanel.add(sortLabel);

		sortBox = new JComboBox<String>();
		sortBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Name", "Priority", "Status", "Date created"}));
		sortBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.refreshTable();
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
					functionality.editTask();
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
				functionality.setDarkMode(darkModeButton.isSelected());
			}
		});
		accountPanel.add(darkModeButton);

		finishedBuilding = true;
		functionality.refreshTable();
	}

	// ---------- used by MainWindowFunctionality to read the controls and fill the table ----------

	// true once every control exists, the table is only filled after that
	public boolean isFinishedBuilding() {
		return finishedBuilding;
	}

	// what is picked in the Sort by box, like "Priority"
	public String getSortChoice() {
		return (String) sortBox.getSelectedItem();
	}

	// what is picked in the Show box, like "WIP" or ALL_STATUSES
	public String getStatusFilterChoice() {
		return (String) statusFilterBox.getSelectedItem();
	}

	public boolean isMyTasksOnly() {
		return myTasksCheck.isSelected();
	}

	// the number of the selected row, or -1 if no row is selected
	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public void clearTable() {
		tableModel.setRowCount(0);
	}

	public void addTableRow(Object[] row) {
		tableModel.addRow(row);
	}

	public void setCountText(String text) {
		countLabel.setText(text);
	}

	public void setProgressText(String text) {
		progressLabel.setText(text);
	}

	// keeps the View menu item and the Dark Mode button showing the same thing
	public void setDarkModeSelected(boolean dark) {
		darkModeItem.setSelected(dark);
		darkModeButton.setSelected(dark);
	}
}
