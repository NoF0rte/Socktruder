package burp.swing;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ToClientModel extends BReadOnlyTableModel {
	private ArrayList<Row> rows = new ArrayList<>();
	private Class<?>[] types = new Class[]{
		Integer.class, String.class, Integer.class, LocalDateTime.class
	};

	public ToClientModel() {
		this.addColumn("#");
		this.addColumn("Message");
        this.addColumn("Length");
		this.addColumn("Time");
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	@Override
	public void clear() {
		super.clear();
		rows.clear();
	}

	public void addRow(String message) {
		Row row = new Row(rows.size() + 1, message);
		rows.add(row);

		this.addRow(row.toRow());
	}

	public Row getRow(int row) {
		return this.rows.get(row);
	}

	public ArrayList<Row> getRows(){
		return this.rows;
	}
	
	public class Row {
		public int num;
		public String message;
		public int length;
		public LocalDateTime time;

		public Row(int num, String message) {
			this.num = num;
			this.message = message;
			this.length = message.length();
			this.time = LocalDateTime.now();
		}

		private Object[] toRow() {
			return new Object[]{num, message, length, time};
		}
	}
}
