package org.qsardb.conversion.table;

public class RowFilter {

	private Column column;
	private String pattern;

	public RowFilter(String expression) {
		String[] a = expression.split("=");
		column = new Column(a[0].trim());
		pattern = a[1].trim();
	}

	RowFilter() {
	}

	public boolean include(Row row) {
		if (column == null) {
			return true;
		}

		Cell cell = row.getValues().get(column);
		return cell.getText().equals(pattern);
	}
}
