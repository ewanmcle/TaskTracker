import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for making a new account. Saves the user through UserDatabase.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class RegisterDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTextField usernameField;
	private JTextField emailField;
	private JPasswordField passwordField;
	private JPasswordField confirmField;

	private UserDatabase users;
	private User createdUser;   // stays null if the dialog is cancelled

	// WindowBuilder uses this constructor to show the design
	public RegisterDialog() {
		this(null, null);
	}

	public RegisterDialog(Frame owner, UserDatabase users) {
		super(owner, "Create Account", true);
		this.users = users;

		setBounds(100, 100, 420, 400);
		Theme.decorate(getRootPane());
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(20, 36, 10, 36));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] {0};
		gbl_contentPanel.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPanel.columnWeights = new double[] {1.0};
		gbl_contentPanel.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);

		JLabel usernameLabel = new JLabel("Username");
		GridBagConstraints gbc_usernameLabel = new GridBagConstraints();
		gbc_usernameLabel.anchor = GridBagConstraints.WEST;
		gbc_usernameLabel.insets = new Insets(0, 0, 4, 0);
		gbc_usernameLabel.gridx = 0;
		gbc_usernameLabel.gridy = 0;
		contentPanel.add(usernameLabel, gbc_usernameLabel);

		usernameField = new JTextField();
		GridBagConstraints gbc_usernameField = new GridBagConstraints();
		gbc_usernameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usernameField.insets = new Insets(0, 0, 10, 0);
		gbc_usernameField.gridx = 0;
		gbc_usernameField.gridy = 1;
		contentPanel.add(usernameField, gbc_usernameField);

		JLabel emailLabel = new JLabel("Email");
		GridBagConstraints gbc_emailLabel = new GridBagConstraints();
		gbc_emailLabel.anchor = GridBagConstraints.WEST;
		gbc_emailLabel.insets = new Insets(0, 0, 4, 0);
		gbc_emailLabel.gridx = 0;
		gbc_emailLabel.gridy = 2;
		contentPanel.add(emailLabel, gbc_emailLabel);

		emailField = new JTextField();
		GridBagConstraints gbc_emailField = new GridBagConstraints();
		gbc_emailField.fill = GridBagConstraints.HORIZONTAL;
		gbc_emailField.insets = new Insets(0, 0, 10, 0);
		gbc_emailField.gridx = 0;
		gbc_emailField.gridy = 3;
		contentPanel.add(emailField, gbc_emailField);

		JLabel passwordLabel = new JLabel("Password");
		GridBagConstraints gbc_passwordLabel = new GridBagConstraints();
		gbc_passwordLabel.anchor = GridBagConstraints.WEST;
		gbc_passwordLabel.insets = new Insets(0, 0, 4, 0);
		gbc_passwordLabel.gridx = 0;
		gbc_passwordLabel.gridy = 4;
		contentPanel.add(passwordLabel, gbc_passwordLabel);

		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.fill = GridBagConstraints.HORIZONTAL;
		gbc_passwordField.insets = new Insets(0, 0, 10, 0);
		gbc_passwordField.gridx = 0;
		gbc_passwordField.gridy = 5;
		contentPanel.add(passwordField, gbc_passwordField);

		JLabel confirmLabel = new JLabel("Confirm password");
		GridBagConstraints gbc_confirmLabel = new GridBagConstraints();
		gbc_confirmLabel.anchor = GridBagConstraints.WEST;
		gbc_confirmLabel.insets = new Insets(0, 0, 4, 0);
		gbc_confirmLabel.gridx = 0;
		gbc_confirmLabel.gridy = 6;
		contentPanel.add(confirmLabel, gbc_confirmLabel);

		confirmField = new JPasswordField();
		GridBagConstraints gbc_confirmField = new GridBagConstraints();
		gbc_confirmField.fill = GridBagConstraints.HORIZONTAL;
		gbc_confirmField.gridx = 0;
		gbc_confirmField.gridy = 7;
		contentPanel.add(confirmField, gbc_confirmField);

		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new EmptyBorder(0, 30, 12, 30));
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);

		JButton createButton = new JButton("Create Account");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createAccount();
			}
		});
		buttonPane.add(createButton);
		getRootPane().setDefaultButton(createButton);
	}

	private void createAccount() {
		String username = usernameField.getText().trim();
		String email = emailField.getText().trim();
		String password = new String(passwordField.getPassword());
		String confirm = new String(confirmField.getPassword());

		if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
			showError("Fill in every field.");
			return;
		}
		if (!email.contains("@")) {
			showError("Enter a valid email address.");
			return;
		}
		if (!password.equals(confirm)) {
			showError("The passwords don't match.");
			return;
		}
		User user = new User(username, email, LoginFrame.hashPassword(password));
		if (!users.addUser(user)) {
			showError("That username or email is already taken.");
			return;
		}
		createdUser = user;
		JOptionPane.showMessageDialog(this, "Account created. You can log in now.");
		dispose();
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(this, message, "Create Account", JOptionPane.ERROR_MESSAGE);
	}

	// the user that was saved, or null if the dialog was cancelled
	public User getCreatedUser() {
		return createdUser;
	}
}
