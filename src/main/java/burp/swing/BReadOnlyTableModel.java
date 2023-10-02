package burp.swing;

import javax.swing.table.DefaultTableModel;

public class BReadOnlyTableModel extends DefaultTableModel {
	public void clear() {
		for (int i = getRowCount() - 1; i >= 0; i--) {
            removeRow(i);
        }
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
}
