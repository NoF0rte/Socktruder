package burp;

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
		return String.format("{0} - {1}", index, name);
	}
}
