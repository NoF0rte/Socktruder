package burp;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Config {

	public static final String FUZZ_LIST_KEY = "fuzzList";
	public static final String ENABLED_KEY = "enabled";
	public static final String DELAY_KEY = "delay";

	private static List<Fuzz> fuzzListCache = null;

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
		if (enabled() == null) {
			setEnabled(true);
		}

		if (fuzzList() == null) {
			setFuzzList(new ArrayList<Fuzz>());
		}

		if (delay() == null) {
			setDelay(100);
		}
	}

	public List<Fuzz> fuzzList() {
		if (fuzzListCache == null) {
			String json = extensionData.getString(FUZZ_LIST_KEY);
			if (json == null) {
				return null;
			}

			try {
				Type listType = TypeToken.getParameterized(ArrayList.class, Fuzz.class).getType();
				fuzzListCache = new Gson().fromJson(json, listType);
			} catch (Exception e) {
				api.logging().logToError(String.format("Error deserializing fuzzList from config", e.getMessage()));
				return null;
			}
		}

		return fuzzListCache;
	}

	public void setFuzzList(List<Fuzz> fuzzList) {
		fuzzListCache = fuzzList;

		String json = new Gson().toJson(fuzzListCache);
		extensionData.setString(FUZZ_LIST_KEY, json);
	}

	public synchronized Boolean enabled() {
		return extensionData.getBoolean(ENABLED_KEY);
	}

	public synchronized void setEnabled(boolean enabled) {
		extensionData.setBoolean(ENABLED_KEY, enabled);
	}

	public Integer delay() {
		return extensionData.getInteger(DELAY_KEY);
	}

	public void setDelay(int delay) {
		extensionData.setInteger(DELAY_KEY, delay);
	}

	public class Fuzz {
		private String keyword;
		private String wordlist;
		private String success;

		public Fuzz(){
			
		}

		public String getKeyword() {
			return keyword;
		}
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}

		public String getWordlist() {
			return wordlist;
		}
		public void setWordlist(String wordlist) {
			this.wordlist = wordlist;
		}

		public String getSuccess() {
			return success;
		}
		public void setSuccess(String success) {
			this.success = success;
		}

		public boolean keywordMatch(String payload) {
			return payload.contains(keyword);
		}

		public boolean successMatch(String response) {
			if (success == "") {
				return false;
			}

			return Pattern.matches(success, response);
		}

	}
}
