package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;

public class Config {
	public static final String SEND_KEYWORD = "[SOCKTRUDER]";

	private static Config instance;

	public static Config instance() {
		return instance;
	}

	public static void setInstance(Config config) {
		instance = config;
	}

	private final MontoyaApi api;
	private final PersistedObject extensionData;

	public Config(MontoyaApi api) {
		this.api = api;

		this.extensionData = api.persistence().extensionData();
		setDefaults();
	}

	public void setDefaults() {
	}
}
