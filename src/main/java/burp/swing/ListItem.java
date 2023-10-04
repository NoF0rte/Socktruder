package burp.swing;

public class ListItem<T> {
	private String display;

	private final T value;
	public T getValue() {
		return value;
	}

	public ListItem(String display, T value) {
		this.display = display;
		this.value = value;
	}

	@Override
	public String toString() {
		return display;
	}
}
