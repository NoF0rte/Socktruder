package burp;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.WebSocket;

public class FuzzRunner implements Runnable {

	private final MontoyaApi api;
    private final WebSocket socket;
	private final String message;
	private final int delay;
	private final Dictionary<String, List<String>> payloadsByKeyword = new Hashtable<>();
	
	private boolean stop = false;
	private boolean running = true;

	public FuzzRunner(MontoyaApi api, WebSocket socket, Config.Fuzz[] fuzzItems, String message) {
		this.api = api;
		this.socket = socket;
		this.message = message;
		this.delay = Config.instance().delay();

		for (Config.Fuzz fuzz : fuzzItems) {
			List<String> payloads;
			try {
				payloads = Util.readLines(fuzz.getWordlist());
			} catch (Exception e) {
				api.logging().logToError(String.format("Error reading wordlist: %s", e.getMessage()));
				payloads = new ArrayList<String>();
			}

			payloadsByKeyword.put(fuzz.getKeyword(), payloads);
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
		Enumeration<String> keywords = payloadsByKeyword.keys();
		while (keywords.hasMoreElements()) {
			if (!keepRunning() || !Config.instance().enabled()) {
				break;
			}
			
            String keyword = keywords.nextElement();
            List<String> payloads = payloadsByKeyword.get(keyword);
			for (String payload : payloads) {
				if (!keepRunning() || !Config.instance().enabled()) {
					break;
				}
	
				String msg = message.replace(keyword, payload);
				api.logging().logToOutput(String.format("New payload: %s", msg));
				socket.sendTextMessage(msg);
	
				try {
					Thread.sleep(delay);
				} catch (Exception e) {
					api.logging().logToError(String.format("Error when sleeping %s", e.getMessage()));
				}
			}
        }

		setRunning(false);
	}
}
