package burp.ui;

import java.awt.Component;

import javax.swing.JTabbedPane;

public class BTabbedPane extends JTabbedPane {
	private int tabNumber = 1;

	public void addTab(Component component) {
		String title = Integer.toString(tabNumber++);

		addTab(title, component);
		setTabComponentAt(indexOfTab(title), new CloseableTabComponent(title, this, () -> {
			if (component instanceof CloseableComponent) {
				CloseableComponent closable = (CloseableComponent)component;
				if (!closable.close()) {
					return;
				}
			}

            removeTabAt(indexOfTab(title));
        }));

		int index = getTabCount() - 1;

		setSelectedIndex(index);
	}
}
