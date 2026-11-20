import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Pop-up window for adding a new task or editing one.
 * It only collects the values. MainWindow reads them with the getters and updates the TaskList,
 * so the TaskList rules (like setting the date finished) stay in one place.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class TaskForm extends JDialog {

	private static final long serialVersionUID = 1L;

	public static final String NOBODY = "Nobody";

	private final JPanel contentPanel = new JPanel();
	private JTextField nameField;
	private JTextArea descriptionArea;
	private JComboBox<task.Priority> priorityBox;
	private JComboBox<task.Status> statusBox;
	private JComboBox<String> assignedBox;
	private JLabel createdByValue;
	private JLabel dateCreatedValue;
	private JLabel dateFinishedValue;

	private boolean saved = false;

	// WindowBuilder uses this constructor to show the design
	public TaskForm() {
		this(null, null, "", new String[0]);
	}

	// editing = the task to edit, or null to make a new one
	// usernames = everyone a task can be assigned to
	public TaskForm(Frame owner, task editing, String currentUser, String[] usernames) {
		super(owner, editing == null ? "New Task" : "Edit Task", true);

		setBounds(100, 100, 480, 470);
		Theme.decorate(getRootPane());
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(20, 24, 10, 24));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {110, 0};
		gbl_contentPanel.rowHeights = new int[] {0, 90, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[] {0.0, 1.0};
		gbl_contentPanel.rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);

		JLabel nameLabel = new JLabel("Name");
		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
		gbc_nameLabel.anchor = GridBagConstraints.WEST;
		gbc_nameLabel.insets = new Insets(0, 0, 10, 10);
		gbc_nameLabel.gridx = 0;
		gbc_nameLabel.gridy = 0;
		contentPanel.add(nameLabel, gbc_nameLabel);

		nameField = new JTextField();
		GridBagConstraints gbc_nameField = new GridBagConstraints();
		gbc_nameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nameField.insets = new Insets(0, 0, 10, 0);
		gbc_nameField.gridx = 1;
		gbc_nameField.gridy = 0;
		contentPanel.add(nameField, gbc_nameField);

		JLabel descriptionLabel = new JLabel("Description");
		GridBagConstraints gbc_descriptionLabel = new GridBagConstraints();
		gbc_descriptionLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_descriptionLabel.insets = new Insets(4, 0, 10, 10);
		gbc_descriptionLabel.gridx = 0;
		gbc_descriptionLabel.gridy = 1;
		contentPanel.add(descriptionLabel, gbc_descriptionLabel);

		JScrollPane descriptionScroll = new JScrollPane();
		GridBagConstraints gbc_descriptionScroll = new GridBagConstraints();
		gbc_descriptionScroll.fill = GridBagConstraints.BOTH;
		gbc_descriptionScroll.insets = new Insets(0, 0, 10, 0);
		gbc_descriptionScroll.gridx = 1;
		gbc_descriptionScroll.gridy = 1;
		contentPanel.add(descriptionScroll, gbc_descriptionScroll);

		descriptionArea = new JTextArea();
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		descriptionScroll.setViewportView(descriptionArea);

		JLabel priorityLabel = new JLabel("Priority");
		GridBagConstraints gbc_priorityLabel = new GridBagConstraints();
		gbc_priorityLabel.anchor = GridBagConstraints.WEST;
		gbc_priorityLabel.insets = new Insets(0, 0, 10, 10);
		gbc_priorityLabel.gridx = 0;
		gbc_priorityLabel.gridy = 2;
		contentPanel.add(priorityLabel, gbc_priorityLabel);

		priorityBox = new JComboBox<task.Priority>();
		priorityBox.setModel(new DefaultComboBoxModel<task.Priority>(task.Priority.values()));
		priorityBox.setSelectedItem(task.Priority.MEDIUM);
		GridBagConstraints gbc_priorityBox = new GridBagConstraints();
		gbc_priorityBox.anchor = GridBagConstraints.WEST;
		gbc_priorityBox.insets = new Insets(0, 0, 10, 0);
		gbc_priorityBox.gridx = 1;
		gbc_priorityBox.gridy = 2;
		contentPanel.add(priorityBox, gbc_priorityBox);

		JLabel statusLabel = new JLabel("Status");
		GridBagConstraints gbc_statusLabel = new GridBagConstraints();
		gbc_statusLabel.anchor = GridBagConstraints.WEST;
		gbc_statusLabel.insets = new Insets(0, 0, 10, 10);
		gbc_statusLabel.gridx = 0;
		gbc_statusLabel.gridy = 3;
		contentPanel.add(statusLabel, gbc_statusLabel);

		statusBox = new JComboBox<task.Status>();
		statusBox.setModel(new DefaultComboBoxModel<task.Status>(task.Status.values()));
		GridBagConstraints gbc_statusBox = new GridBagConstraints();
		gbc_statusBox.anchor = GridBagConstraints.WEST;
		gbc_statusBox.insets = new Insets(0, 0, 10, 0);
		gbc_statusBox.gridx = 1;
		gbc_statusBox.gridy = 3;
		contentPanel.add(statusBox, gbc_statusBox);

		JLabel assignedLabel = new JLabel("Assigned to");
		GridBagConstraints gbc_assignedLabel = new GridBagConstraints();
		gbc_assignedLabel.anchor = GridBagConstraints.WEST;
		gbc_assignedLabel.insets = new Insets(0, 0, 10, 10);
		gbc_assignedLabel.gridx = 0;
		gbc_assignedLabel.gridy = 4;
		contentPanel.add(assignedLabel, gbc_assignedLabel);

		assignedBox = new JComboBox<String>();
		GridBagConstraints gbc_assignedBox = new GridBagConstraints();
		gbc_assignedBox.anchor = GridBagConstraints.WEST;
		gbc_assignedBox.insets = new Insets(0, 0, 10, 0);
		gbc_assignedBox.gridx = 1;
		gbc_assignedBox.gridy = 4;
		contentPanel.add(assignedBox, gbc_assignedBox);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 2;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(4, 0, 10, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 5;
		contentPanel.add(separator, gbc_separator);

		JLabel createdByLabel = new JLabel("Created by");
		GridBagConstraints gbc_createdByLabel = new GridBagConstraints();
		gbc_createdByLabel.anchor = GridBagConstraints.WEST;
		gbc_createdByLabel.insets = new Insets(0, 0, 6, 10);
		gbc_createdByLabel.gridx = 0;
		gbc_createdByLabel.gridy = 6;
		contentPanel.add(createdByLabel, gbc_createdByLabel);

		createdByValue = new JLabel("-");
		GridBagConstraints gbc_createdByValue = new GridBagConstraints();
		gbc_createdByValue.anchor = GridBagConstraints.WEST;
		gbc_createdByValue.insets = new Insets(0, 0, 6, 0);
		gbc_createdByValue.gridx = 1;
		gbc_createdByValue.gridy = 6;
		contentPanel.add(createdByValue, gbc_createdByValue);

		JLabel datesLabel = new JLabel("Created / finished");
		GridBagConstraints gbc_datesLabel = new GridBagConstraints();
		gbc_datesLabel.anchor = GridBagConstraints.WEST;
		gbc_datesLabel.insets = new Insets(0, 0, 0, 10);
		gbc_datesLabel.gridx = 0;
		gbc_datesLabel.gridy = 7;
		contentPanel.add(datesLabel, gbc_datesLabel);

		JPanel datesPanel = new JPanel();
		FlowLayout fl_datesPanel = (FlowLayout) datesPanel.getLayout();
		fl_datesPanel.setHgap(0);
		fl_datesPanel.setVgap(0);
		fl_datesPanel.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_datesPanel = new GridBagConstraints();
		gbc_datesPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_datesPanel.gridx = 1;
		gbc_datesPanel.gridy = 7;
		contentPanel.add(datesPanel, gbc_datesPanel);

		dateCreatedValue = new JLabel("-");
		datesPanel.add(dateCreatedValue);

		JLabel slashLabel = new JLabel("  /  ");
		datesPanel.add(slashLabel);

		dateFinishedValue = new JLabel("Not finished");
		datesPanel.add(dateFinishedValue);

		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new EmptyBorder(0, 18, 12, 18));
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				save();
			}
		});
		buttonPane.add(saveButton);
		getRootPane().setDefaultButton(saveButton);

		fillIn(editing, currentUser, usernames);
	}

	// puts the task's current values (or the defaults for a new task) into the fields
	private void fillIn(task editing, String currentUser, String[] usernames) {
		assignedBox.addItem(NOBODY);
		for (String name : usernames) {
			assignedBox.addItem(name);
		}
		if (editing == null) {
			createdByValue.setText(currentUser);
			assignedBox.setSelectedItem(currentUser);
			return;
		}
		nameField.setText((String) editing.getField(task.Field.NAME));
		descriptionArea.setText((String) editing.getField(task.Field.DESCRIPTION));
		priorityBox.setSelectedItem(editing.getField(task.Field.PRIORITY));
		statusBox.setSelectedItem(editing.getField(task.Field.STATUS));
		String assigned = (String) editing.getField(task.Field.ASSIGNED_TO);
		assignedBox.setSelectedItem(assigned == null ? NOBODY : assigned);
		createdByValue.setText((String) editing.getField(task.Field.CREATED_BY));
		dateCreatedValue.setText(String.valueOf(editing.getField(task.Field.DATE_CREATED)));
		if (editing.getField(task.Field.DATE_FINISHED) != null) {
			dateFinishedValue.setText(editing.getField(task.Field.DATE_FINISHED).toString());
		}
	}

	private void save() {
		if (nameField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Give the task a name.", getTitle(), JOptionPane.WARNING_MESSAGE);
			return;
		}
		saved = true;
		dispose();
	}

	// true if the user pressed Save
	public boolean isSaved() {
		return saved;
	}

	public String getTaskName() {
		return nameField.getText().trim();
	}

	public String getDescriptionText() {
		return descriptionArea.getText().trim();
	}

	public task.Priority getPriority() {
		return (task.Priority) priorityBox.getSelectedItem();
	}

	public task.Status getStatus() {
		return (task.Status) statusBox.getSelectedItem();
	}

	// the username picked, or null for Nobody
	public String getAssignee() {
		String picked = (String) assignedBox.getSelectedItem();
		return NOBODY.equals(picked) ? null : picked;
	}
}
