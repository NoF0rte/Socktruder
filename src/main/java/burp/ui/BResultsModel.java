package burp.ui;

import java.util.ArrayList;

import burp.api.montoya.websocket.Direction;

public class BResultsModel extends BReadOnlyTableModel {

	private ArrayList<Row> rows = new ArrayList<>();

	public BResultsModel() {
		this.addColumn("#");
		this.addColumn("Direction");
        this.addColumn("Position");
        this.addColumn("Payload");
        this.addColumn("Length");
	}

	public void addResult(Direction direction, int position, String payload, String message) {
		Row row = new Row(rows.size() + 1, direction, position, payload, message);
		rows.add(row);

		this.addRow(row.toRow());
	}

	public Row getRow(int row) {
		return this.rows.get(row);
	}
	
	public class Row {
		public int num;
		public Direction direction;
		public int position;
		public String payload;
		public String message;
		public int length;

		public Row(int num, Direction direction, int position, String payload, String message) {
			this.num = num;
			this.position = position;
			this.payload = payload;
			this.message = message;
			this.length = message.length();
		}

		private Object[] toRow() {
			String dir = "-> To server";
			if (direction == Direction.SERVER_TO_CLIENT) {
				dir = "<- To client";
			}

			return new Object[]{num, dir, position, payload, length };
		}
	}
}
