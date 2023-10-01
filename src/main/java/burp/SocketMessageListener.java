package burp;

import javax.swing.JOptionPane;

import burp.api.montoya.core.Registration;
import burp.api.montoya.websocket.*;

public class SocketMessageListener implements MessageHandler {
	private final WebSocket socket;
	private final String url;
	private Registration registration;
	public void setRegistration(Registration registration) {
		this.registration = registration;
	}

	public SocketMessageListener(WebSocket socket, String url) {
		this.socket = socket;
		this.url = url;
	}

	@Override
	public TextMessageAction handleTextMessage(TextMessage textMessage) {
		if (textMessage.payload().contains(Extension.SEND_KEYWORD)) {
			try {
				int res = JOptionPane.showConfirmDialog(null, "Send WebSocket message to " + Extension.EXTENSION_NAME + "?", Extension.EXTENSION_NAME, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (res == JOptionPane.YES_OPTION) {
					SuiteTab.get().addFuzzTab(socket, url, textMessage);
					if (registration.isRegistered()) {
						registration.deregister();
					}
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error adding fuzz tab: " + e.getMessage(), Extension.EXTENSION_NAME, JOptionPane.ERROR_MESSAGE);
			}

			return TextMessageAction.drop();
		}

        return TextMessageAction.continueWith(textMessage);
	}

	@Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }
}
