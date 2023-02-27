package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;

public class Config {

	public static final String WORDLIST_PATH_KEY = "wordlistPath";
	public static final String ENABLED_KEY = "enabled";
	public static final String FUZZ_KEYWORD_KEY = "fuzz";
	public static final String DELAY_KEY = "delay";
	public static final String SUCCESS_KEY = "success";

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

		if (delay() == null) {
			setDelay(100);
		}

		if (successRegex() == null) {
			setSuccessRegex("");
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

	public String successRegex() {
		return extensionData.getString(SUCCESS_KEY);
	}

	public void setSuccessRegex(String regex) {
		extensionData.setString(SUCCESS_KEY, regex);
	}

	public Integer delay() {
		return extensionData.getInteger(DELAY_KEY);
	}

	public void setDelay(int delay) {
		extensionData.setInteger(DELAY_KEY, delay);
	}
}
