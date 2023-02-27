package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.*;
import java.util.regex.Pattern;

public class SocketMessageHandler implements MessageHandler {

	private final MontoyaApi api;
	private final WebSocket socket;

	private Fuzzer fuzzer = null;
	private Thread fuzzThread = null;
	private String successRegex = "";

	public SocketMessageHandler(MontoyaApi api, WebSocket socket) {
		this.api = api;
		this.socket = socket;
	}

	public TextMessageAction handleServerToClientMessage(TextMessage textMessage) {
		String payload = textMessage.payload();
		this.api.logging().logToOutput(String.format("Client <- Server: %s", payload));

		if (fuzzer != null && fuzzer.running() && successRegex != "" && Pattern.matches(successRegex, payload)) {
			api.logging().logToOutput("Success regex matched! Stopping fuzzing...");
			fuzzer.stop();
		}

		return TextMessageAction.continueWith(textMessage);
	}

	public TextMessageAction handleClientToServerMessage(TextMessage textMessage) {
		String payload = textMessage.payload();
		
		this.api.logging().logToOutput(String.format("Client -> Server: %s", payload));

		if (fuzzer != null && !fuzzer.running()) {
			fuzzer = null;
			fuzzThread = null;
			successRegex = "";
		}

		if ((fuzzer != null && fuzzer.running()) || !payload.contains(Config.instance().fuzzKeyword())) {
			return TextMessageAction.continueWith(payload);
		}

        this.api.logging().logToOutput("Payload fuzz keyword found");

		successRegex = Config.instance().successRegex();
		fuzzer = new Fuzzer(api, socket, payload);
		fuzzThread = new Thread(fuzzer);

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
