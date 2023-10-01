package burp.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;

public class TableRowHighlighter implements TableCellRenderer {
	private final Color highlightColor = new Color(253,253,150);
	private int highlightRow = -1;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		// Create new DefaultTableCellRenderer every time so only the row is changed
		Component c = new DefaultTableCellRenderer().getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		if (isSelected) {
			return c;
		}
		
		if (row == highlightRow) {
			c.setBackground(highlightColor);
		}

		return c;
	}

	public void setHighlightRow(int row) {
		this.highlightRow = row;
	}
}
