package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.Direction;
import burp.api.montoya.websocket.WebSocket;

public class Settings {
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

	private final WebSocket socket;
	public WebSocket getSocket() {
		return socket;
	}

	private final Direction direction;
	public Direction getDirection() {
		return direction;
	}

	private final MontoyaApi api;
	public MontoyaApi getApi() {
		return api;
	}

	public Settings(MontoyaApi api, WebSocket socket, Direction direction) {
		this.socket = socket;
		this.api = api;
		this.direction = direction;
	}
}
