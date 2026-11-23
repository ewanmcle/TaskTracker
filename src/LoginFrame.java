import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.border.EmptyBorder;

/**
 * The first window: log in or open the account creation window.
 * This class only builds the window. What the buttons do is in LoginFrameFunctionality.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField usernameField;
	private JPasswordField passwordField;

	private LoginFrameFunctionality functionality;   // what the buttons do

	// WindowBuilder uses this constructor to show the design
	public LoginFrame() {
		this(null, null);
	}

	public LoginFrame(UserDatabaseHandler users, TaskListHandler tasks) {
		functionality = new LoginFrameFunctionality(this, users, tasks);

		setTitle("TaskTracker | Log in");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 420, 370);
		Theme.decorate(getRootPane());
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(24, 36, 24, 36));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JPanel formPanel = new JPanel();
		contentPane.add(formPanel, BorderLayout.CENTER);
		GridBagLayout gbl_formPanel = new GridBagLayout();
		gbl_formPanel.columnWidths = new int[] {0, 0};
		gbl_formPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_formPanel.columnWeights = new double[] {1.0, 1.0};
		gbl_formPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		formPanel.setLayout(gbl_formPanel);

		JLabel titleLabel = new JLabel("TaskTracker");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
		GridBagConstraints gbc_titleLabel = new GridBagConstraints();
		gbc_titleLabel.gridwidth = 2;
		gbc_titleLabel.anchor = GridBagConstraints.WEST;
		gbc_titleLabel.insets = new Insets(0, 0, 4, 0);
		gbc_titleLabel.gridx = 0;
		gbc_titleLabel.gridy = 0;
		formPanel.add(titleLabel, gbc_titleLabel);

		JLabel subtitleLabel = new JLabel("Log in to see your tasks.");
		GridBagConstraints gbc_subtitleLabel = new GridBagConstraints();
		gbc_subtitleLabel.gridwidth = 2;
		gbc_subtitleLabel.anchor = GridBagConstraints.WEST;
		gbc_subtitleLabel.insets = new Insets(0, 0, 14, 0);
		gbc_subtitleLabel.gridx = 0;
		gbc_subtitleLabel.gridy = 1;
		formPanel.add(subtitleLabel, gbc_subtitleLabel);

		JLabel usernameLabel = new JLabel("Username");
		GridBagConstraints gbc_usernameLabel = new GridBagConstraints();
		gbc_usernameLabel.gridwidth = 2;
		gbc_usernameLabel.anchor = GridBagConstraints.WEST;
		gbc_usernameLabel.insets = new Insets(0, 0, 4, 0);
		gbc_usernameLabel.gridx = 0;
		gbc_usernameLabel.gridy = 2;
		formPanel.add(usernameLabel, gbc_usernameLabel);

		usernameField = new JTextField();
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.gridwidth = 2;
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.insets = new Insets(0, 0, 10, 0);
		gbc_usernameField.gridx = 0;
		gbc_usernameField.gridy = 3;
		formPanel.add(usernameField, gbc_usernameField);
		usernameField.setColumns(20);

		JLabel passwordLabel = new JLabel("Password");
		GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
		gbc_passwordLabel.gridwidth = 2;
		gbc_passwordLabel.anchor = GridBagConstraints.WEST;
		gbc_passwordLabel.insets = new Insets(0, 0, 4, 0);
		gbc_passwordLabel.gridx = 0;
		gbc_passwordLabel.gridy = 4;
		formPanel.add(passwordLabel, gbc_passwordLabel);

		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.gridwidth = 2;
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.insets = new Insets(0, 0, 16, 0);
		gbc_passwordField.gridx = 0;
		gbc_passwordField.gridy = 5;
		formPanel.add(passwordField, gbc_passwordField);

		JButton createAccountButton = new JButton("Create an account");
		createAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.openAccountCreation();
			}
		});
		GridBagConstraints gbc_createAccountButton = new GridBagConstraints();
		gbc_createAccountButton.anchor = GridBagConstraints.WEST;
		gbc_createAccountButton.gridx = 0;
		gbc_createAccountButton.gridy = 6;
		formPanel.add(createAccountButton, gbc_createAccountButton);

		JButton loginButton = new JButton("Log In");
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.logIn();
			}
		});
		GridBagConstraints gbc_loginButton = new GridBagConstraints();
		gbc_loginButton.anchor = GridBagConstraints.EAST;
		gbc_loginButton.gridx = 1;
		gbc_loginButton.gridy = 6;
		formPanel.add(loginButton, gbc_loginButton);

		final JCheckBox darkModeCheck = new JCheckBox("Dark mode");
		darkModeCheck.setSelected(Theme.isDark());
		darkModeCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.setDarkMode(darkModeCheck.isSelected());
			}
		});
		GridBagConstraints gbc_darkModeCheck = new GridBagConstraints();
		gbc_darkModeCheck.gridwidth = 2;
		gbc_darkModeCheck.anchor = GridBagConstraints.WEST;
		gbc_darkModeCheck.insets = new Insets(18, 0, 0, 0);
		gbc_darkModeCheck.gridx = 0;
		gbc_darkModeCheck.gridy = 7;
		formPanel.add(darkModeCheck, gbc_darkModeCheck);

		getRootPane().setDefaultButton(loginButton);
	}

	// ---------- used by LoginFrameFunctionality to read and change the screen ----------

	public String getEnteredUsername() {
		return usernameField.getText().trim();
	}

	public String getEnteredPassword() {
		return new String(passwordField.getPassword());
	}

	public void setEnteredUsername(String username) {
		usernameField.setText(username);
	}

	// empties the password box and puts the cursor in it
	public void clearPassword() {
		passwordField.setText("");
		passwordField.requestFocusInWindow();
	}
}
