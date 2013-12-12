package openGL.picking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class LabelingPanelSouth extends JPanel implements ActionListener {


	/**
	 * GUI (bottom row pane)
	 */
	private static final long serialVersionUID = 1L;
	private JLabel nLabels;
	private JButton exportButton;
	private JButton importButton;
	private JFileChooser fileChooser;
	private File currentDir;

	ArrayList<LabelingListener> listeners;

	public LabelingPanelSouth() {
		// setup gui elements (top row)
		nLabels = new JLabel("# labels: 0");
		exportButton = new JButton("export");
		importButton = new JButton("import");

		fileChooser = new JFileChooser();

		listeners = new ArrayList<>();

		// export action
		exportButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (currentDir != null)
					fileChooser.setCurrentDirectory(currentDir);
				if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					String fileSaveName = fileChooser.getSelectedFile()
							.getAbsolutePath();
					String path = fileSaveName.substring(0,
							fileSaveName.lastIndexOf("/"));
					currentDir = new File(path);
					for (LabelingListener l : listeners) {
						try {
							l.exportToTxT(fileSaveName);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});

		// import action
		importButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (currentDir != null)
					fileChooser.setCurrentDirectory(currentDir);
				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					String fileOpenName = fileChooser.getSelectedFile()
							.getAbsolutePath();
					String path = fileOpenName.substring(0,
							fileOpenName.lastIndexOf("/"));
					currentDir = new File(path);

					for (LabelingListener l : listeners) {
						try {
							l.importFromTxT(fileOpenName);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
			}
		});

		// do layout.
		this.add(nLabels);
		this.add(exportButton);
		exportButton.setEnabled(false);
		this.add(importButton);
	}

	/**
	 * update number of labeled vertices in gui and enable/disable labeling
	 * buttons
	 * 
	 * @param nLabels
	 */
	public void updateNLabels(int nLabels) {
		this.nLabels.setText("# labels: " + nLabels);
		if (nLabels > 0)
			exportButton.setEnabled(true);
		else
			exportButton.setEnabled(false);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void addPickingListener(LabelingListener l) {
		this.listeners.add(l);
	}

}
