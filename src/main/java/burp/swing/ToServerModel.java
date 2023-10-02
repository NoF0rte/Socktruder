package burp.swing;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class ToServerModel extends BReadOnlyTableModel {
	private ArrayList<Row> rows = new ArrayList<>();
	private Class<?>[] types = new Class[]{
		Integer.class, Integer.class, String.class, String.class, Integer.class, LocalDateTime.class
	};

	public ToServerModel() {
		this.addColumn("#");
        this.addColumn("Position");
		this.addColumn("Message");
        this.addColumn("Payload");
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

	public void addRow(String message, int position, String payload) {
		Row row = new Row(rows.size() + 1, position, message, payload);
		rows.add(row);

		this.addRow(row.toRow());
	}

	public Row getRow(int row) {
		return this.rows.get(row);
	}

	public ArrayList<Row> getRows() {
		return rows;
	}
	
	public class Row {
		public int num;
		public int position;
		public String message;
		public String payload;
		public int length;
		public LocalDateTime time;

		public Row(int num, int position, String message, String payload) {
			this.num = num;
			this.position = position;
			this.payload = payload;
			this.message = message;
			this.length = message.length();
			this.time = LocalDateTime.now();
		}

		private Object[] toRow() {
			return new Object[]{num, position, message, payload, length, time};
		}
	}
}
