import javax.swing.JOptionPane;

/**
 * LoginFrameFunctionality is what the buttons on the login window do.
 * LoginFrame only builds the window, and each of its buttons calls one method here.
 */
public class LoginFrameFunctionality {

	private LoginFrame frame;     // the login window, used to read the boxes and show messages
	private UserDatabaseHandler users;
	private TaskList tasks;

	public LoginFrameFunctionality(LoginFrame frame, UserDatabaseHandler users, TaskList tasks) {
		this.frame = frame;
		this.users = users;
		this.tasks = tasks;
	}

	// Log In button: checks the username and password, then opens the main window
	public void logIn() {
		String username = frame.getEnteredUsername();
		String password = frame.getEnteredPassword();
		if (username.isEmpty() || password.isEmpty()) {
			JOptionPane.showMessageDialog(frame, "Enter your username and password.", "Log in", JOptionPane.WARNING_MESSAGE);
			return;
		}
		boolean passwordCorrect = users.checkPassword(username, password);
		if (!passwordCorrect) {
			JOptionPane.showMessageDialog(frame, "Wrong username or password.", "Log in", JOptionPane.ERROR_MESSAGE);
			frame.clearPassword();
			return;
		}
		MainWindow mainWindow = new MainWindow(username, tasks, users);
		mainWindow.setVisible(true);
		frame.dispose();
	}

	// Create an account button: opens the account creation window on top of the login window
	// if an account was made, its username gets filled in so you only need to type the password
	public void openAccountCreation() {
		AccountCreationWindow accountCreationWindow = new AccountCreationWindow(frame, users);
		accountCreationWindow.setVisible(true);   // waits here until that window is closed
		User createdUser = accountCreationWindow.getCreatedUser();
		if (createdUser != null) {
			frame.setEnteredUsername(createdUser.getUsername());
			frame.clearPassword();
		}
	}

	// Dark mode checkbox
	public void setDarkMode(boolean dark) {
		Theme.setDark(dark);
	}
}
