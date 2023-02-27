package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.websocket.*;

public class SocketMessageHandler implements MessageHandler {

	private final MontoyaApi api;
	private final WebSocket socket;

	private Fuzzer fuzzer = null;
	private Thread fuzzThread = null;

	public SocketMessageHandler(MontoyaApi api, WebSocket socket) {
		this.api = api;
		this.socket = socket;
	}

	@Override
    public TextMessageAction handleTextMessage(TextMessage textMessage) {
		if (!Config.instance().enabled()) {
			return TextMessageAction.continueWith(textMessage);
		}

        String payload = textMessage.payload();

		// If the message is to the client, just log it
        if (textMessage.direction() == Direction.SERVER_TO_CLIENT) {
            this.api.logging().logToOutput(String.format("Client <- Server: %s", payload));
            return TextMessageAction.continueWith(textMessage);
        }

        this.api.logging().logToOutput(String.format("Client -> Server: %s", payload));

		if (fuzzer != null && !fuzzer.running()) {
			fuzzer = null;
			fuzzThread = null;
		}

		if ((fuzzer != null && fuzzer.running()) || !payload.contains(Config.instance().fuzzKeyword())) {
			return TextMessageAction.continueWith(payload);
		}

        this.api.logging().logToOutput("Payload fuzz keyword");

		fuzzer = new Fuzzer(api, socket, payload);
		fuzzThread = new Thread(fuzzer);

        fuzzThread.start();

        return TextMessageAction.drop();
    }

    @Override
    public BinaryMessageAction handleBinaryMessage(BinaryMessage binaryMessage) {
        return BinaryMessageAction.continueWith(binaryMessage);
    }
	
}
