import java.awt.EventQueue;

/**
 * Main starts the app. Run this class to open the login window.
 */
public class Main {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Theme.setupSaved();
				LoginFrame frame = new LoginFrame(new UserDatabaseHandler(), new TaskListHandler());
				frame.setVisible(true);
			}
		});
	}
}
