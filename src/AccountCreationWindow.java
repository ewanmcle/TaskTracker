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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Pop-up window for making a new account.
 * This class only builds the window. What the buttons do is in AccountCreationFunctionality.
 * Built so WindowBuilder can open it in the Design tab.
 */
public class AccountCreationWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTextField usernameField;
	private JTextField emailField;
	private JPasswordField passwordField;
	private JPasswordField confirmField;

	private AccountCreationFunctionality functionality;   // what the buttons do

	// WindowBuilder uses this constructor to show the design
	public AccountCreationWindow() {
		this(null, null);
	}

	public AccountCreationWindow(Frame owner, UserDatabaseHandler users) {
		super(owner, "Create Account", true);
		functionality = new AccountCreationFunctionality(this, users);

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
			public void actionPerformed(ActionEvent event) {
				functionality.cancel();
			}
		});
		buttonPane.add(cancelButton);

		JButton createButton = new JButton("Create Account");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				functionality.createAccount();
			}
		});
		buttonPane.add(createButton);
		getRootPane().setDefaultButton(createButton);
	}

	// ---------- used by AccountCreationFunctionality to read the boxes ----------

	public String getEnteredUsername() {
		return usernameField.getText().trim();
	}

	public String getEnteredEmail() {
		return emailField.getText().trim();
	}

	public String getEnteredPassword() {
		return new String(passwordField.getPassword());
	}

	public String getEnteredConfirmPassword() {
		return new String(confirmField.getPassword());
	}

	// the user that was saved, or null if the window was closed without making an account
	public User getCreatedUser() {
		return functionality.getCreatedUser();
	}
}
