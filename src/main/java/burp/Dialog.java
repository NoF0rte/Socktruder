package burp;

import javax.swing.JOptionPane;

public class Dialog {
	public static void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message, Extension.EXTENSION_NAME, JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showYesNo(String message, Runnable yesAction) {
		int result = JOptionPane.showConfirmDialog(null, message, Extension.EXTENSION_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			yesAction.run();
		}
	}
	public static void showError(String error) {
		JOptionPane.showMessageDialog(null, error, Extension.EXTENSION_NAME, JOptionPane.ERROR_MESSAGE);
	}
}
