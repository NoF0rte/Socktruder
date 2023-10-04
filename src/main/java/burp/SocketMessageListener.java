package burp;

import java.util.ArrayList;
import java.util.regex.Matcher;

import burp.api.montoya.websocket.*;
import burp.swing.ListItem;

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
			String name = matcher.group(1) != null ? matcher.group(1).replace("_", " ") : "";
			Integer index = parseIntOrNull(name);
			SuiteTab suiteTab = SuiteTab.get();
			
			if (!name.equals("")) {
				ArrayList<ListItem<Integer>> items = new ArrayList<>();
				ArrayList<Integer> indicies = suiteTab.getFuzzTabIndices(name);
				
				if (indicies.size() > 0) {
					for (int i = 0; i < indicies.size(); i++) {
						String display = String.format("Tab: %d - Name: %s", indicies.get(i) + 1, suiteTab.getFuzzTabName(indicies.get(i)));
						items.add(new ListItem<Integer>(display, indicies.get(i)));
					}
				} else if (index != null && index.intValue() > 0 && index.intValue() <= suiteTab.fuzzTabCount()) {
					int i = index.intValue() - 1;
					String display = String.format("Tab: %d - Name: %s", i + 1, suiteTab.getFuzzTabName(i));
					items.add(new ListItem<Integer>(display, i));
				}

				if (items.size() > 0) {
					ListItem<Integer> selected = Dialog.showOkCancelInput(String.format("Update WebSocket for %s tab", Extension.EXTENSION_NAME), items);
					if (selected != null) {
						suiteTab.updateFuzzTab(((ListItem<Integer>)selected).getValue(), socket);
					}
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
