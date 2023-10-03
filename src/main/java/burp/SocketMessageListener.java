package burp;

import java.util.ArrayList;
import java.util.regex.Matcher;

import burp.api.montoya.websocket.*;

public class SocketMessageListener implements MessageHandler {
	private final WebSocket socket;
	private final String url;

	public SocketMessageListener(WebSocket socket, String url) {
		this.socket = socket;
		this.url = url;
	}

	private Integer parseIntOrNull(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public TextMessageAction handleTextMessage(TextMessage textMessage) {
		Matcher matcher = Extension.SEND_REGEX.matcher(textMessage.payload());
		if (!matcher.find()) {
			return TextMessageAction.continueWith(textMessage);
		}

		try {
			String name = matcher.group(1).replace("_", " ");
			Integer index = parseIntOrNull(name);
			SuiteTab suiteTab = SuiteTab.get();
			
			if (!name.equals("")) {
				ArrayList<Integer> indicies = suiteTab.getFuzzTabIndices(name);
				if (indicies.size() == 1) {
					int tabIndex = indicies.get(0);
					Dialog.showYesNo(String.format("Update WebSocket for %s tab \"%d - %s\"", Extension.EXTENSION_NAME, tabIndex + 1, name), () -> {
						suiteTab.updateFuzzTab(tabIndex, socket);
					});
				} else if (indicies.size() > 1) {
					// Ask which named tab to update
				} else if (index != null && index.intValue() > 0 && index.intValue() <= suiteTab.fuzzTabCount()) {
					Dialog.showYesNo(String.format("Update WebSocket for %s tab \"%d - %s\"", Extension.EXTENSION_NAME, index.intValue(), suiteTab.getFuzzTabName(index.intValue() - 1)), () -> {
						suiteTab.updateFuzzTab(index.intValue() - 1, socket);
					});
				} else {
					Dialog.showYesNo(String.format("Send WebSocket message to %s with name \"%s\"?", Extension.EXTENSION_NAME, name), () -> {
						suiteTab.addFuzzTab(name, socket, url, textMessage);
					});
				}
			} else {
				Dialog.showYesNo(String.format("Send WebSocket message to %s?", Extension.EXTENSION_NAME), () -> {
					suiteTab.addFuzzTab(socket, url, textMessage);
				});
			}
		} catch (Exception e) {
			Dialog.showError("Error adding fuzz tab: " + e.getMessage());
		}

		return TextMessageAction.drop();
	}

	@Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }
}
