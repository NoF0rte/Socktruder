package burp.swing;

import java.util.List;

public class BListTableModel extends BReadOnlyTableModel {
	public void addRow(String ...row){
		addRow((Object[])row);
	}

	public <T> void addRows(List<T> rows) {
		for (T row : rows) {
			addRow(row.toString());
		}
	}
}
