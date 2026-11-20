import javax.swing.JOptionPane;

/**
 * AccountCreationFunctionality is what the buttons on the account creation window do.
 * AccountCreationWindow only builds the window, and each of its buttons calls one method here.
 */
public class AccountCreationFunctionality {

	private AccountCreationWindow window;   // used to read the boxes and show messages
	private UserDatabaseHandler users;
	private User createdUser;               // stays null until an account is made

	public AccountCreationFunctionality(AccountCreationWindow window, UserDatabaseHandler users) {
		this.window = window;
		this.users = users;
		this.createdUser = null;
	}

	// Create Account button: checks the boxes, then saves the new user
	public void createAccount() {
		String username = window.getEnteredUsername();
		String email = window.getEnteredEmail();
		String password = window.getEnteredPassword();
		String confirmPassword = window.getEnteredConfirmPassword();

		if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
			showError("Fill in every field.");
			return;
		}
		if (!email.contains("@")) {
			showError("Enter a valid email address.");
			return;
		}
		if (!password.equals(confirmPassword)) {
			showError("The passwords don't match.");
			return;
		}

		// addUser hashes the password and saves the user
		// it gives back false if the username or email is already taken
		boolean saved = users.addUser(username, email, password);
		if (!saved) {
			showError("That username or email is already taken.");
			return;
		}

		createdUser = users.findByUsername(username);
		JOptionPane.showMessageDialog(window, "Account created. You can log in now.");
		window.dispose();
	}

	// Cancel button: closes this window, the login window is still open underneath it
	public void cancel() {
		window.dispose();
	}

	// the user that was saved, or null if no account was made
	public User getCreatedUser() {
		return createdUser;
	}

	private void showError(String message) {
		JOptionPane.showMessageDialog(window, message, "Create Account", JOptionPane.ERROR_MESSAGE);
	}
}
