package openGL.picking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import assignment7.Label;

public class LabelingPanelNorth extends JPanel implements ActionListener {

	/**
	 * GUI (top row pane)
	 */
	private static final long serialVersionUID = 1L;
	private JTextField labelName;
	private JButton setLabelButton;
	private JButton removeLabelButton;

	ArrayList<LabelingListener> listeners;

	public LabelingPanelNorth() {

		// setup gui elements (top row)
		setLabelButton = new JButton("set label");
		removeLabelButton = new JButton("remove label");
		labelName = new JTextField("", 15);

		// add action
		setLabelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				for (LabelingListener l : listeners) {
					Label label = null;
					if (!labelName.getText().equals(""))
						label = new Label(labelName.getText());
					l.labelCurrentVertex(label);
				}
			}
		});

		// remove action
		removeLabelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				for (LabelingListener l : listeners) {
					l.labelCurrentVertex(null);
				}
			}
		});

		// do layout.
		this.add(new JLabel("Label name: "));
		this.add(labelName);
		this.add(setLabelButton);
		this.add(removeLabelButton);
		removeLabelButton.setEnabled(false);
		setLabelButton.setEnabled(false);

		listeners = new ArrayList<>();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	public void addPickingListener(LabelingListener l) {
		this.listeners.add(l);
	}

	public void updateLabelField(String string) {
		labelName.setText(string);
	}

	public void setButtonsActive(boolean b) {
		removeLabelButton.setEnabled(b);
		setLabelButton.setEnabled(b);
	}
}
