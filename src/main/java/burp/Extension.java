package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.Registration;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.websocket.*;

public class Extension implements BurpExtension, WebSocketCreatedHandler {

	private static final String extensionName = "WS Fuzzer";
	private MontoyaApi api;

	@Override
	public void initialize(MontoyaApi api) {
		this.api = api;

		Config.setInstance(new Config(api));

        //Register web socket handler with Burp.
        api.websockets().registerWebSocketCreatedHandler(this);

		SuiteTab.set(new SuiteTab(api));

		api.userInterface().registerSuiteTab(extensionName, SuiteTab.get());
		api.extension().setName(extensionName);
	}

	@Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
		if (!webSocketCreated.toolSource().isFromTool(ToolType.PROXY, ToolType.REPEATER)) {
			return;
		}
		
		WebSocket socket = webSocketCreated.webSocket();
        
		SocketMessageListener listener = new SocketMessageListener(socket, webSocketCreated.upgradeRequest().url());
		Registration registration = socket.registerMessageHandler(listener);

		listener.setRegistration(registration);
    }
}
