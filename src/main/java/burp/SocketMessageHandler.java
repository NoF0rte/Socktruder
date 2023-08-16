package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SocketMessageHandler implements MessageHandler {

	private final MontoyaApi api;
	private final WebSocket socket;

	private Runner runner = null;
	private Thread fuzzThread = null;
	private List<Config.Fuzz> fuzzItems = new ArrayList<>();

	public SocketMessageHandler(MontoyaApi api, WebSocket socket) {
		this.api = api;
		this.socket = socket;
	}

	public TextMessageAction handleServerToClientMessage(TextMessage textMessage) {
		String payload = textMessage.payload();
		this.api.logging().logToOutput(String.format("Client <- Server: %s", payload));

		if (runner != null && runner.running() && fuzzItems.stream().anyMatch(x -> x.successMatch(payload))) {
			api.logging().logToOutput("Success regex matched! Stopping fuzzing...");
			runner.stop();
		}

		return TextMessageAction.continueWith(textMessage);
	}

	public TextMessageAction handleClientToServerMessage(TextMessage textMessage) {
		String payload = textMessage.payload();
		
		this.api.logging().logToOutput(String.format("Client -> Server: %s", payload));

		if (runner != null && !runner.running()) {
			runner = null;
			fuzzThread = null;
			fuzzItems.clear();
		}

		if (runner != null && runner.running()) {
			return TextMessageAction.continueWith(payload);
		}

		List<Config.Fuzz> matches = Config.instance().fuzzList().stream().filter(x -> x.keywordMatch(payload)).collect(Collectors.toList());
		if (matches.size() == 0) {
			return TextMessageAction.continueWith(payload);
		}

		fuzzItems.addAll(matches);

        this.api.logging().logToOutput("Payload fuzz keyword found");

		runner = new Runner(api, socket, fuzzItems, payload);
		fuzzThread = new Thread(runner);

        fuzzThread.start();

        return TextMessageAction.drop();
	}

	@Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
		if (!Config.instance().enabled()) {
			return TextMessageAction.continueWith(textMessage);
		}

        if (textMessage.direction() == Direction.SERVER_TO_CLIENT) {
            return handleServerToClientMessage(textMessage);
        }

        return handleClientToServerMessage(textMessage);
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }
}
