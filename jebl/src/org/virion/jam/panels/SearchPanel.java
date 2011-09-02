package org.virion.jam.panels;

import org.virion.jam.mac.Utils;
import org.virion.jam.util.IconUtils;
import org.virion.jam.util.SimpleListener;
import org.virion.jam.util.SimpleListenerManager;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Andrew Rambaut
 * Date: Jul 26, 2004
 * Time: 5:11:59 PM
 */
public class SearchPanel extends JPanel {

	public SearchPanel(final String emptyLabel, final boolean searchAsYouType) {
		this(emptyLabel, null, searchAsYouType);
	}

	public SearchPanel(final String emptyLabel, final JPopupMenu popup, final boolean searchAsYouType) {

		this.continuousSearch = searchAsYouType;

		if (Utils.isMacOSX() && (Utils.getMacOSXVersion().startsWith("10.5") || Utils.getMacOSXVersion().startsWith("10.6") ||
                Utils.getMacOSXVersion().startsWith("10.7"))) {
			// Mac OS X 10.5 implements a search text box natively...
			this.emptyLabel = "";

			findButton = null;
			cancelButton = null;
			searchText = new JTextField();
			searchText.setColumns(12);
		    searchText.putClientProperty("JTextField.variant", "search");
			searchText.putClientProperty("JTextField.Search.FindPopup", popup);
			searchText.putClientProperty("JTextField.Search.FindAction", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					searchTextChanged();
				}
			});
			searchText.putClientProperty("JTextField.Search.CancelAction", new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					clearSearchText();
				}
			});
			add(searchText, BorderLayout.CENTER);

		} else {
			this.emptyLabel = emptyLabel;

			Icon findIcon = IconUtils.getIcon(SearchPanel.class, "images/search/find.png");
			Icon findPopupIcon = IconUtils.getIcon(SearchPanel.class, "images/search/findPopup.png");
			Icon stopIcon = IconUtils.getIcon(SearchPanel.class, "images/search/stop.png");
			Icon stopRolloverIcon = IconUtils.getIcon(SearchPanel.class, "images/search/stopRollover.png");
			Icon stopPressedIcon = IconUtils.getIcon(SearchPanel.class, "images/search/stopPressed.png");

			setLayout(new BorderLayout(0, 0));

			if (popup != null) {
				popup.getSelectionModel().setSelectedIndex(0);
				findButton = new JButton(findPopupIcon);
				findButton.add(popup);
				findButton.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent mouseEvent) {
						Component comp = mouseEvent.getComponent();
						popup.show(comp, 0, comp.getHeight());
					}
				});
			} else {
				findButton = new JButton(findIcon);
				findButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						searchText.requestFocusInWindow();
					}
				});
			}

			findButton.setPreferredSize(new Dimension(findButton.getIcon().getIconWidth(),
					findButton.getIcon().getIconHeight()));

			findButton.putClientProperty("JButton.buttonType", "toolbar");
			findButton.setBorderPainted(false);
			findButton.setOpaque(false);
			// this is required on Windows XP platform -- untested on Macintosh
			findButton.setContentAreaFilled(false);

			JPanel findPanel = new JPanel();
			findPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			findPanel.setOpaque(false);
			findPanel.add(findButton);

			searchText = new JTextField(emptyLabel);
            searchText.setOpaque(false);
//	    searchText.putClientProperty("Quaqua.TextField.style", "search");
			searchText.setForeground(Color.lightGray);
			searchText.setBorder(null);

			cancelButton = new JButton(stopIcon);
			cancelButton.setRolloverEnabled(true);
			cancelButton.setRolloverIcon(stopRolloverIcon);
			cancelButton.setPressedIcon(stopPressedIcon);
			cancelButton.setPreferredSize(new Dimension(stopIcon.getIconWidth(), stopIcon.getIconHeight()));
			cancelButton.putClientProperty("JButton.buttonType", "toolbar");
			cancelButton.setBorderPainted(false);
			cancelButton.setOpaque(false);
			// this is required on Windows XP platform -- untested on Macintosh
			cancelButton.setContentAreaFilled(false);
            cancelButton.setVisible(false);

			JPanel cancelPanel = new JPanel();
			cancelPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
			cancelPanel.setOpaque(false);
			cancelPanel.add(cancelButton);

			add(findPanel, BorderLayout.WEST);
			add(searchText, BorderLayout.CENTER);
			add(cancelPanel, BorderLayout.EAST);

			setBackground(searchText.getBackground());
			setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.gray));
			setPreferredSize(new Dimension(120, 24));


			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearSearchText();
				}
			});
		}

		searchText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (searchTextEmpty) {
					searchText.setText("");
					searchText.setForeground(Color.black);
				}
			}

			public void focusLost(FocusEvent e) {
				checkSearchTextEmpty();
			}
		});

		searchText.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
			}
		});

		searchText.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				searchTextChanged();
			}

			public void removeUpdate(DocumentEvent e) {
				searchTextChanged();
			}

			public void changedUpdate(DocumentEvent e) {
				searchTextChanged();
			}
		});

		searchText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (!searchTextEmpty) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						if (!continuousSearch) {
							fireSearchStarted();
						}
                        if ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK)!=0)
                            shiftReturnPressedListeners.fire();
                        else
                            returnPressedListeners.fire();
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						clearSearchText();
					}
				}
                else {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						escapePressedWhenEmptyListeners.fire();
					}
                }
				if (e.getKeyCode() == KeyEvent.VK_DOWN) {
					if (comboBox != null) {
						int index = comboBox.getSelectedIndex();
						if (index < comboBox.getItemCount() - 1)
							index++;
						comboBox.setSelectedIndex(index);
						e.consume();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_UP) {
					if (comboBox != null) {
						int index = comboBox.getSelectedIndex();
						if (index > 0)
							index--;
						comboBox.setSelectedIndex(index);
						e.consume();
					}
				}
			}
		});
	}

	private void checkSearchTextEmpty() {
		String text = searchText.getText().trim();
		if (text.length() == 0) {
			searchTextEmpty = true;
		}
		if (searchTextEmpty) {
			searchText.setForeground(Color.lightGray);
			searchText.setText(SearchPanel.this.emptyLabel);
		}
        else {
            searchText.setForeground(Color.black);
        }
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
        if (findButton != null) {
            findButton.setEnabled(enabled);
        }
        if (searchText!=null) {
            searchText.setEnabled(enabled);
            if(!System.getProperty("os.name").startsWith("Mac OS")) {
                setBackground(searchText.getBackground());
            }
        }
        if (cancelButton!=null) {
            cancelButton.setEnabled(enabled);
        }
    }

	public void setToolTipText(String text) {
		super.setToolTipText(text);
		searchText.setToolTipText(text);
        if (findButton != null) {
            findButton.setToolTipText(text);
        }
    }

	public void setFindIcon(Icon icon) {
        if (findButton != null) {
            findButton.setIcon(icon);
        }
	}

	public void addSearchPanelListener(SearchPanelListener listener) {
		listeners.add(listener);
	}

	public void removeDataSourceListener(SearchPanelListener listener) {
		listeners.remove(listener);
	}

	public boolean requestFocusInWindow() {
		return searchText.requestFocusInWindow();
	}

    private SimpleListenerManager returnPressedListeners = new SimpleListenerManager();
    private SimpleListenerManager shiftReturnPressedListeners = new SimpleListenerManager();
    private SimpleListenerManager escapePressedWhenEmptyListeners = new SimpleListenerManager();

    /**
     * Adds a listener to be notified when return/enter is pressed in the text field
     * @param simpleListener the listener.
     */
    public void addReturnPressedListener(SimpleListener simpleListener) {
        returnPressedListeners.add(simpleListener);
    }

    /**
     * Adds a listener to be notified when shift-return/enter is pressed in the text field
     * @param simpleListener the listener.
     */
    public void addShiftReturnPressedListener(SimpleListener simpleListener) {
        shiftReturnPressedListeners.add(simpleListener);
    }

    /**
     * Adds a listener to be notified when Esc is pressed (or the clear button is pressed) in the text field when it is already empty.
     * @param simpleListener the listener.
     */
    public void addEscapePressedWhenEmptyListener(SimpleListener simpleListener) {
        escapePressedWhenEmptyListeners.add(simpleListener);
    }

	public void removeAllDataSourceListeners() {
		listeners.clear();
	}

	public void clearSearchText() {
		searchText.setText("");
		searchTextChanged();
        if (!searchText.hasFocus()) {
            checkSearchTextEmpty();
        }
	}

	private void fireSearchStarted() {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			((SearchPanelListener) i.next()).searchStarted(searchText.getText());
		}
	}

	private void fireSearchStopped() {
		Iterator i = listeners.iterator();
		while (i.hasNext()) {
			((SearchPanelListener) i.next()).searchStopped();
		}
	}

	private ArrayList listeners = new ArrayList();
	private boolean searchTextEmpty = true;
	private String emptyLabel;
	private boolean continuousSearch;
	private final JButton findButton;
	private final JTextField searchText;
	private final JButton cancelButton;

	private JComboBox comboBox;

	public JComboBox getComboBox() {
		return comboBox;
	}

    public void setEmptyLabel(String emptyLabel) {
        this.emptyLabel = emptyLabel;
        if (!searchText.hasFocus()) 
            checkSearchTextEmpty();
    }

    public void setComboBox(JComboBox comboBox) {
		this.comboBox = comboBox;
		if (comboBox != null) {
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					requestFocusInWindow();
					searchTextChanged();
				}
			});
		}
	}

	private void searchTextChanged() {
		if (searchText.isFocusOwner())
			searchTextEmpty = searchText.getText().length() == 0;
		fireSearchTextChanged();
	}

	public void fireSearchTextChanged() {
		if (searchTextEmpty) {
			if (cancelButton != null) {
				cancelButton.setVisible(false);
			}
			if (continuousSearch) {
				fireSearchStopped();
			}
		} else {
			if (cancelButton != null) {
				cancelButton.setVisible(true);
			}
			if (continuousSearch) {
				fireSearchStarted();
			}
		}
	}

    @Override
    public boolean isFocusOwner() {
        return searchText.isFocusOwner();
    }

    public void setSearchText(String filterText) {
        searchTextEmpty = false;
        searchText.setText(filterText);
        checkSearchTextEmpty();
    }
}


