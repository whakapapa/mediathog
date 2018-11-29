
package mediathog.tool;

import mediathog.config.MVColor;

@SuppressWarnings("serial")
public class TModelColor extends TModel {
	private final Class<?>[] types;

	public TModelColor(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
		types = new Class<?>[MVColor.MVC_MAX];
		for (int i = 0; i < MVColor.MVC_MAX; ++i) {
			switch (i) {
				case MVColor.MVC_COLOR:
				types[i] = MVC.class;
				break;
				default:
				types[i] = String.class;
			}
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}
}
