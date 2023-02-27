package burp;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.websocket.*;

public class Extension implements BurpExtension, WebSocketCreatedHandler {

	private MontoyaApi api;

	@Override
	public void initialize(MontoyaApi api) {
		this.api = api;

		api.extension().setName("WS Fuzzer");

		Config.setInstance(new Config(api));

        //Register web socket handler with Burp.
        api.websockets().registerWebSocketCreatedHandler(this);

		UserInterface.create(api);
	}

	@Override
    public void handleWebSocketCreated(WebSocketCreated webSocketCreated) {
        WebSocket socket = webSocketCreated.webSocket();

		if (!webSocketCreated.toolSource().isFromTool(ToolType.PROXY, ToolType.REPEATER)) {
			return;
		}
        
        webSocketCreated.webSocket().registerMessageHandler(new SocketMessageHandler(api, socket));
    }
	
}
