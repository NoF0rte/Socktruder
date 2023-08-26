package burp;

import java.util.ArrayList;
import java.util.List;

public class Position {
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

	public void replace(String content, int offset) {
	}
}
