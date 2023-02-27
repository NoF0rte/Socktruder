package burp;

import java.util.ArrayList;
import java.util.List;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.WebSocket;

public class Fuzzer implements Runnable {

	private final MontoyaApi api;
    private final WebSocket socket;
	private final String message;
	private final String keyword;

	private List<String> payloads;
	private boolean stop = false;
	private boolean running = true;

	public Fuzzer(MontoyaApi api, WebSocket socket, String message) {
		this.api = api;
		this.socket = socket;
		this.message = message;
		this.keyword = Config.instance().fuzzKeyword();

		try {
			this.payloads = Util.readLines(Config.instance().wordlist());
		} catch (Exception e) {
			api.logging().logToError(String.format("Error reading wordlist: %s", e.getMessage()));
			this.payloads = new ArrayList<String>();
		}
	}

	public synchronized boolean running() {
		return this.running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	public synchronized void stop() {
		this.api.logging().logToOutput("Fuzzer set to stop");
		this.stop = true;
	}

	private synchronized boolean keepRunning() {
        return this.stop == false;
    }

	@Override
	public void run() {
		for (String payload : payloads) {
			if (!keepRunning() || !Config.instance().enabled()) {
				break;
			}

			String msg = message.replace(keyword, payload);
			api.logging().logToOutput(String.format("New payload: %s", msg));
			socket.sendTextMessage(msg);
		}

		setRunning(false);
	}
}
