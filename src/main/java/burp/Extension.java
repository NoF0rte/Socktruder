package burp;

import java.util.ArrayList;
import java.util.regex.Pattern;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.websocket.*;

public class Extension implements BurpExtension, WebSocketCreatedHandler, ExtensionUnloadingHandler {
	public static final String EXTENSION_NAME = "Socktruder";
	public static final String SEND_KEYWORD = "@Socktruder";
	public static final Pattern SEND_REGEX = Pattern.compile(String.format("%s(?:_([^\\s]*))?", Extension.SEND_KEYWORD), Pattern.MULTILINE);

	private ArrayList<Registration> registrations = new ArrayList<>();

	private MontoyaApi api;

	@Override
	public void initialize(MontoyaApi api) {
		this.api = api;

        //Register web socket handler with Burp.
        api.websockets().registerWebSocketCreatedHandler(this);

		SuiteTab.set(new SuiteTab(api));

		api.userInterface().registerSuiteTab(EXTENSION_NAME, SuiteTab.get());
		api.extension().registerUnloadingHandler(this);
		api.extension().setName(EXTENSION_NAME);
	}

	@Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
		if (!webSocketCreated.toolSource().isFromTool(ToolType.PROXY, ToolType.REPEATER)) {
			return;
		}
		
		WebSocket socket = webSocketCreated.webSocket();
        
		SocketMessageListener listener = new SocketMessageListener(socket, webSocketCreated.upgradeRequest().url());
		registrations.add(socket.registerMessageHandler(listener));
    }

	@Override
	public void extensionUnloaded() {
		for (Registration registration : registrations) {
			if (registration.isRegistered()) {
				registration.deregister();
			}
		}
	}
}
