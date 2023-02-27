package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;

public class Config {

	public static final String WORDLIST_PATH_KEY = "wordlistPath";
	public static final String ENABLED_KEY = "enabled";
	public static final String FUZZ_KEYWORD_KEY = "fuzz";

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
		if (wordlist() == null) {
			setWordlist("");
		}

		if (enabled() == null) {
			setEnabled(true);
		}

		if (fuzzKeyword() == null) {
			setFuzzKeyword("[FUZZ]");
		}
	}

	public String wordlist() {
		return extensionData.getString(WORDLIST_PATH_KEY);
	}

	public void setWordlist(String wordlist) {
		extensionData.setString(WORDLIST_PATH_KEY, wordlist);
	}

	public synchronized Boolean enabled() {
		return extensionData.getBoolean(ENABLED_KEY);
	}

	public synchronized void setEnabled(boolean enabled) {
		extensionData.setBoolean(ENABLED_KEY, enabled);
	}

	public String fuzzKeyword() {
		return extensionData.getString(FUZZ_KEYWORD_KEY);
	}

	public void setFuzzKeyword(String keyword) {
		extensionData.setString(FUZZ_KEYWORD_KEY, keyword);
	}
}
