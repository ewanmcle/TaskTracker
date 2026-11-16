import java.awt.Color;
import java.awt.Window;
import java.util.prefs.Preferences;

import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

/**
 * Sets up the FlatLaf look and feel and switches between light and dark mode.
 * The choice is saved, so the app opens in the same mode next time.
 *
 * Styles the windows can use with button.putClientProperty("FlatLaf.styleClass", ...):
 *   "primary"  filled accent button (like New Task)
 *   "danger"   red text button (like Delete)
 */
public final class Theme {

	private static final String DARK_MODE_KEY = "darkMode";

	private Theme() {
	}

	// call once at the start of main, before any window is made
	public static void setupSaved() {
		// lets macOS draw the title bars in the matching light or dark style
		System.setProperty("apple.awt.application.appearance", "system");
		apply(prefs().getBoolean(DARK_MODE_KEY, false));
	}

	public static boolean isDark() {
		return FlatLaf.isLafDark();
	}

	// switches every open window to dark (true) or light (false) and remembers the choice
	public static void setDark(boolean dark) {
		if (dark == isDark()) {
			return;
		}
		apply(dark);
		FlatLaf.updateUI();
		for (Window w : Window.getWindows()) {
			if (w instanceof RootPaneContainer) {
				decorate(((RootPaneContainer) w).getRootPane());
			}
		}
		prefs().putBoolean(DARK_MODE_KEY, dark);
	}

	// call in each window's constructor so its macOS title bar blends with the theme
	public static void decorate(JRootPane rootPane) {
		rootPane.putClientProperty("apple.awt.transparentTitleBar", Boolean.TRUE);
	}

	private static void apply(boolean dark) {
		if (dark) {
			FlatMacDarkLaf.setup();
		} else {
			FlatMacLightLaf.setup();
		}

		// rounded corners like the mockup
		UIManager.put("Component.arc", 8);
		UIManager.put("Button.arc", 8);
		UIManager.put("TextComponent.arc", 8);
		UIManager.put("ScrollBar.thumbArc", 999);
		UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));

		// table: taller rows, light lines between rows, striped rows
		UIManager.put("Table.rowHeight", 26);
		UIManager.put("Table.showHorizontalLines", true);
		UIManager.put("Table.showVerticalLines", false);
		UIManager.put("TableHeader.height", 28);
		Color tableBg = UIManager.getColor("Table.background");
		UIManager.put("Table.alternateRowColor", dark ? brighten(tableBg, 8) : brighten(tableBg, -8));

		// button styles
		UIManager.put("[style]Button.primary",
				"background: $Button.default.background; foreground: $Button.default.foreground;"
				+ " borderColor: $Button.default.borderColor; focusedBorderColor: $Button.default.focusedBorderColor;"
				+ " hoverBackground: $Button.default.hoverBackground; pressedBackground: $Button.default.pressedBackground;"
				+ " font: bold");
		UIManager.put("[style]Button.danger", "foreground: " + (dark ? "#FF8A80" : "#A1261E"));
	}

	// makes a color lighter (positive amount) or darker (negative amount)
	private static Color brighten(Color c, int amount) {
		if (c == null) {
			return null;
		}
		return new Color(clamp(c.getRed() + amount), clamp(c.getGreen() + amount), clamp(c.getBlue() + amount));
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}

	private static Preferences prefs() {
		return Preferences.userRoot().node("TaskTracker");
	}
}
