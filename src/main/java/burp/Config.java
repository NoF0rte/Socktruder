package burp;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;

import javax.swing.event.EventListenerList;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.PersistedObject;
import burp.api.montoya.persistence.Preferences;

public class Config {
	public static final String CUSTOM_LISTS_DIR_PROPERTY = "customListsDir";
	private static final String CUSTOM_LISTS_DIR_KEY = "CUSTOM_LISTS_DIR";

	private static Config instance;
	public static Config get() {
		return instance;
	}
	public static void set(Config config) {
		instance = config;
	}

	private final MontoyaApi api;
	private final PersistedObject projectData;
	private final Preferences globalData;
	private final EventListenerList listenerList = new EventListenerList();

	private ArrayList<File> customLists = new ArrayList<>();
	public ArrayList<File> getCustomLists() {
		return customLists;
	}

	public Config(MontoyaApi api) {
		this.api = api;
		this.projectData = api.persistence().extensionData();
		this.globalData = api.persistence().preferences();

		setDefaults();
	}

	public void setDefaults() {
		populateCustomLists(customListsDir());
	}

	public String customListsDir() {
		return globalData.getString(CUSTOM_LISTS_DIR_KEY);
	}

	public void setCustomListsDir(String value) {
		globalData.setString(CUSTOM_LISTS_DIR_KEY, value);
		populateCustomLists(value);

		fireChangeEvent(CUSTOM_LISTS_DIR_PROPERTY);
	}

	private void populateCustomLists(String path) {
		if (path == null) {
			return;
		}

		customLists.clear();

		File dir = new File(path);
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				continue;
			}

			customLists.add(file);
		}
	}


	private void fireChangeEvent(String option) {
		// Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConfigChangeListener.class) {
                ((ConfigChangeListener)listeners[i + 1]).configChanged(option);;
            }
        }
	}

	public void addChangeListener(ConfigChangeListener listener) {
		listenerList.add(ConfigChangeListener.class, listener);
	}

	public void removeChangeListener(ConfigChangeListener listener) {
		listenerList.remove(ConfigChangeListener.class, listener);
	}

	public interface ConfigChangeListener extends EventListener {
		void configChanged(String option);
	}
}
