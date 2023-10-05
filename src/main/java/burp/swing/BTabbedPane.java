package burp.swing;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import javax.swing.Box.Filler;

import com.formdev.flatlaf.icons.FlatTabbedPaneCloseIcon;

public class BTabbedPane extends JTabbedPane {
	private int tabNumber = 1;
	private JPanel trailingContentPanel;
	private JButton addTabButton;

	private Supplier<Component> tabFactory = () -> {
		return new JPanel();
	};

	public void setTabFactory(Supplier<Component> tabFactory) {
		this.tabFactory = tabFactory;
	}

	private int minTabs = 0;
	public void setMinTabs(int min) {
		minTabs = min;
	}

	public void addTab(Component component) {
		String title = Integer.toString(tabNumber++);
		addTab(title, component);
	}

	@Override
	public void addTab(String title, Component component) {
		super.addTab(title, component);

		setTabComponentAt(indexOfTab(title), new TabComponent(title, this, component));

		int index = getTabCount() - 1;
		setSelectedIndex(index);
	}

	private TabComponent getTabComponent(int index) {
		if (index >= getTabCount()) {
			return null;
		}

		return (TabComponent)getTabComponentAt(index);
	}

	public String getTabName(int index) {
		TabComponent tabComponent = getTabComponent(index);
		if (tabComponent == null) {
			return "";
		}

		String name = tabComponent.getText();
		return name;
	}

	public void enableClosableTabs() {
		UIManager.put("TabbedPane.closeForeground", Color.BLACK);
		UIManager.put("TabbedPane.closeHoverForeground", Color.red);
		UIManager.put("TabbedPane.closePressedForeground", Color.red);
		UIManager.put("TabbedPane.closeHoverBackground", new Color(0, true));
        UIManager.put("TabbedPane.closeSize", new Dimension(9,9));
		UIManager.put("TabbedPane.closeCrossPlainSize", 5.0f);
		UIManager.put("TabbedPane.closeCrossFilledSize", 5.0f);
        UIManager.put("TabbedPane.closeIcon", new FlatTabbedPaneCloseIcon());

        putClientProperty("JTabbedPane.tabClosable", true);
        putClientProperty("JTabbedPane.tabCloseToolTipText", "Close");
        putClientProperty("JTabbedPane.tabCloseCallback", (IntConsumer) tabIndex -> {
            closeTabAction(tabIndex);
        });

		updateUI();

		UIManager.put("TabbedPane.closeCrossFilledSize", null);
		UIManager.put("TabbedPane.closeCrossPlainSize", null);
		UIManager.put("TabbedPane.closeForeground", null);
		UIManager.put("TabbedPane.closeHoverForeground", null);
		UIManager.put("TabbedPane.closePressedForeground", null);
		UIManager.put("TabbedPane.closeHoverBackground", null);
        UIManager.put("TabbedPane.closeSize", null);
        UIManager.put("TabbedPane.closeIcon", null);
	}

	public void enableNewTabButton() {
		putClientProperty("JTabbedPane.trailingComponent", trailingContentPanel);
	}

	private void addTabAction() {
		if (tabFactory != null) {
			addTab(tabFactory.get());
		}
	}

	private void closeTabAction(int tabIndex) {
		if (getSelectedIndex() != tabIndex) {
			setSelectedIndex(tabIndex);
			return;
		}

		TabComponent tabComponent = getTabComponent(tabIndex);
		if (tabComponent.getTabContent() instanceof CloseableComponent) {
			CloseableComponent closable = (CloseableComponent)tabComponent.getTabContent();
			if (!closable.close()) {
				return;
			}
		}

		removeTabAt(tabIndex);

		if (minTabs > 0 && getTabCount() < minTabs && tabFactory != null) {
			addTab(tabFactory.get());
		}
	}

	public BTabbedPane() {
		initComponents();
	}

	private void initComponents() {
		addTabButton = new javax.swing.JButton();
		addTabButton.setFont(new java.awt.Font("Cantarell", 0, 15)); // NOI18N
        addTabButton.setText("+");
        addTabButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addTabButton.setPreferredSize(new java.awt.Dimension(20, 20));
        addTabButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTabAction();
            }
        });
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);

		trailingContentPanel = new javax.swing.JPanel();
		trailingContentPanel.setLayout(new java.awt.GridBagLayout());
        trailingContentPanel.add(addTabButton, gridBagConstraints);

		gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;

        trailingContentPanel.add(new Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0)), gridBagConstraints);
	}

	public class TabComponent extends JPanel {
		private final JTabbedPane parent;
		private final AWTEventListener mouseListener;

		private final Component tabContent;
		public Component getTabContent() {
			return tabContent;
		}

		private boolean isActive = true;
		public void setActive(boolean isActive) {
			this.isActive = isActive;
		}

		private JTextField textField;
		public String getText() {
			return textField.getText();
		}

		public TabComponent(String title, JTabbedPane parent, Component tabContent) {
			this.parent = parent;
			this.tabContent = tabContent;

			initComponents();

			textField.setText(title);

			parent.addChangeListener(e -> {
				if (parent.getTabCount() == 0) {
					return;
				}

				Component selected = parent.getTabComponentAt(parent.getSelectedIndex());
				setActive(selected == this);
			});

			mouseListener = new MouseListener(textField);
		}

		private void initComponents() {
			textField = new JTextField();

			textField.setEditable(false);
			textField.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			textField.setMargin(new java.awt.Insets(2, 2, 2, 2));
			textField.setOpaque(true);

			textField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent evt) {
					commit();
				}
			});
			textField.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					clicked(evt);
				}
			});
			textField.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					commit();
				}
			});
			textField.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent evt) {
					revalidate();
				}
			});

			setLayout(new java.awt.GridBagLayout());

			GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			add(textField, gridBagConstraints);
		}

		private void clicked(java.awt.event.MouseEvent evt) {
			if (!isActive) {
				parent.setSelectedIndex(parent.indexOfTabComponent(this));
				return;
			}
	
			if (evt.getClickCount() >= 2) {
				Toolkit.getDefaultToolkit().addAWTEventListener(mouseListener, AWTEvent.MOUSE_EVENT_MASK);

				textField.setEditable(true);
				textField.setFocusable(true);
				textField.grabFocus();
				textField.selectAll();
			}
		}

		private void commit() {
			String text = textField.getText().strip();
			if (text.equals("") || text.isEmpty()) {
				text = " ";
			}
	
			textField.setText(text);
			textField.setEditable(false);
			textField.setFocusable(false);
			Toolkit.getDefaultToolkit().removeAWTEventListener(mouseListener);
		}
	}

	private class MouseListener implements AWTEventListener {
		private Component component;

		public MouseListener(Component component) {
			this.component = component;
		}

		@Override
		public void eventDispatched(AWTEvent event) {
			if(event instanceof MouseEvent){
				MouseEvent evt = (MouseEvent)event;
				if(evt.getID() == MouseEvent.MOUSE_CLICKED){
					Component clicked = evt.getComponent();
					if (clicked != component) {
						clicked.requestFocus();
					}
				}
			}
		}
	}

	public interface CloseListener extends EventListener {
		void close();
	}
}
