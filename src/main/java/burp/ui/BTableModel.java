package burp.ui;

import java.util.List;

import javax.swing.table.DefaultTableModel;

public class BTableModel extends DefaultTableModel {
	public void clear() {
		for (int i = getRowCount() - 1; i >= 0; i--) {
            removeRow(i);
        }
	}

	public void addRow(String ...row){
		addRow((Object[])row);
	}

	public <T> void addRows(List<T> rows) {
		for (T row : rows) {
			addRow(row.toString());
		}
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
