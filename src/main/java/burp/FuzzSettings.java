package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.WebSocket;

public class FuzzSettings {
	private int delay = 1000;
	public int getDelay(){
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}

	private Position[] positions;
	public Position[] getPositions() {
		return positions;
	}
	public void setPositions(Position[] positions) {
		this.positions = positions;
	}

	private String message;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	private WebSocket socket;
	public WebSocket getSocket() {
		return socket;
	}
	public void setSocket(WebSocket socket) {
		this.socket = socket;
	}

	private final MontoyaApi api;
	public MontoyaApi getApi() {
		return api;
	}

	public FuzzSettings(MontoyaApi api, WebSocket socket) {
		this.socket = socket;
		this.api = api;
	}
}
