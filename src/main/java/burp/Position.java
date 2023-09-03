package burp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Position {
	private int index;
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}

	private int start;
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	
	private int end;
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	private List<String> payloads = new ArrayList<>();
	public List<String> getPayloads() {
		return payloads;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{0} - {1}", index, name);
	}

	public String replace(String message, String payload, int offset) {
		String before = message.substring(0, this.start + offset);
		String after = message.substring(end + offset, message.length());
		return before + payload + after;
	}
}
