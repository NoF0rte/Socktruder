package burp;

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
		if (textMessage.payload().contains(Config.SEND_KEYWORD)) {
			SuiteTab.get().addFuzzTab(socket, url, textMessage);
			if (registration.isRegistered()) {
				registration.deregister();
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
