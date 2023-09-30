package burp;

import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

public class Runner implements Runnable {
	private final Settings settings;

	private MessageListener messageListener;
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}

	private DoneListener doneListener;
	public void setDoneListener(DoneListener doneListener) {
		this.doneListener = doneListener;
	}
	
	private State state = State.STOPPED;
	private synchronized State getState(){
		return this.state;
	}
	private synchronized void setState(State state) {
		this.state = state;
	}

	private int currentPosition = 0;
	private int currentPayload = 0;

	public Runner(Settings settings) {
		this.settings = settings;
	}

	public synchronized void setDelay(int delay) {
		this.settings.setDelay(delay);
	}

	public synchronized boolean isRunning() {
		return getState() == State.RUNNING;
	}
	public synchronized boolean isPaused() {
		return getState() == State.PAUSED;
	}
	public synchronized boolean isStopped() {
		return getState() == State.STOPPED;
	}

	public synchronized void stop() {
		this.settings.getApi().logging().logToOutput("Fuzzer set to stop");
		if (isPaused()) {
			cleanup();
			return;
		}
		
		setState(State.STOPPED);
	}

	public synchronized void pause() {
		this.settings.getApi().logging().logToOutput("Fuzzer set to pause");
		setState(State.PAUSED);
	}

	public synchronized void resume() {
		this.settings.getApi().logging().logToOutput("Fuzzer set to resume");
		setState(State.RUNNING);
	}

	@Override
	public void run() {
		this.settings.getApi().logging().logToOutput("Fuzzer set to run");
		setState(State.RUNNING);

		Position[] positions = settings.getPositions();
		for (; currentPosition < positions.length; currentPosition++) {
			Position position = positions[currentPosition];
			List<String> payloads = position.getPayloads();

			for (; currentPayload < payloads.size(); currentPayload++) {
				if (!isRunning()) {
					break;
				}

				String payload = payloads.get(currentPayload);
				String message = position.replace(settings.getMessage(), payload, 0);
				message = message.replaceAll(FuzzTab.MARKER, "");

				try {
					settings.getSocket().sendTextMessage(message);
					messageListener.messageSent(new SentEvent(message, payload, currentPosition + 1));
				} catch (Exception e) {
					settings.getApi().logging().logToError(e);
				}

				try {
					Thread.sleep(settings.getDelay());
				} catch (Exception e) {
					settings.getApi().logging().logToError(String.format("Error when sleeping %s", e.getMessage()));
				}
			}

			// Reset the current payload only if we actually got to the end of the payloads
			// Otherwise, we keep at the same position so we can resume
			if (currentPayload >= payloads.size()) {
				currentPayload = 0;
			}

			if (!isRunning()) {
				break;
			}
		}
		
		if (!isPaused()) {
			cleanup();
		}
	}

	private void cleanup() {
		if (!isPaused()) {
			setState(State.STOPPED);
		}
		
		doneListener.done();
	}

	private enum State {
		RUNNING,
		PAUSED,
		STOPPED,
	}

	public interface DoneListener extends EventListener {
		void done();
	}
	public interface MessageListener extends EventListener {
		void messageSent(SentEvent var1);
	}

	public class SentEvent extends EventObject {
		private final String message;
		public String getMessage() {
			return message;
		}

		private final int position;
		public int getPosition() {
			return position;
		}

		private final String payload;
		public String getPayload() {
			return payload;
		}

		public SentEvent(String message, String payload, int position) {
			super(message);

			this.message = message;
			this.payload = payload;
			this.position = position;
		}
	}
}
