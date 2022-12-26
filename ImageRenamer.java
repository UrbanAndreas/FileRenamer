import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;

public class ImageRenamer {

	private JFrame frame;
	private JPanel panel;
	private JTextField sourceTextField;
	private JButton selectSourceButton;
	private JTable fileTable;
	private JPopupMenu popupMenu;
	private JMenuItem deleteOne;
	private JMenuItem deleteAll;
	private DefaultTableModel model;
	private JTextField newFileNameTextField;
	private JButton renameButton;

	private static final String CONFIRMRENAME = "Umbenennen";
	private static final String DATE = "Datum";
	private static final String DATEFORMAT = "yyyy.MM.dd HH:mm:ss";
	private static final String DELETEALLFILES = "Alle löschen";
	private static final String DELETESELECTEDFILES = "Auswahl löschen";
	private static final String DIFFERENTDIRECTORIES = "Die Dateien müssen im selben Verzeichnis liegen!";
	private static final String FILENAME = "Dateiname";
	private static final String FILEPATH = "Dateipfad";
	private static final String NEWFILENAME = "Neuer Dateiname";
	private static final String NOSELECTION = "Keine Datei ausgewählt!";
	private static final String PATHDELIMITER = "\\";
	private static final String RENAMEFILES = "Dateien umbenennen";
	private static final String RENAMESUCCESSFUL = "Die Dateien wurden erfolgreich umbenannt!";
	private static final String SELECTPATH = "Pfad auswählen";
	private static final String TYPE = "Dateityp";

	/**
	 * Creates a JFileChooser with which files can be added to the jTable.
	 * 
	 * @author uran
	 */
	private void selectFiles() {

		JFileChooser selectFiles = new JFileChooser();
		selectFiles.setMultiSelectionEnabled(true);
		int option = selectFiles.showOpenDialog(null);

		if (option == JFileChooser.APPROVE_OPTION) {

			if (fileTable.getRowCount() != 0) {

				checkDirectory(selectFiles);

			} else {

				addValuesToTable(selectFiles);
			}

		} else if (option == JFileChooser.CANCEL_OPTION) {

			if (fileTable.getRowCount() != 0) {
				return;
			}

			model.setRowCount(0);
			sourceTextField.setText(FILEPATH);
			sourceTextField.setEnabled(false);
			newFileNameTextField.setEnabled(false);
		}
	}

	/**
	 * Checks whether there already are files from a different directory in the
	 * jTable. Only files from the same directory are allowed!
	 * 
	 * @param selectFiles
	 * 
	 * @author uran
	 */
	private void checkDirectory(JFileChooser selectFiles) {

		String tablePath = fileTable.getValueAt(0, 1).toString()
				.substring(0, fileTable.getValueAt(0, 1).toString().lastIndexOf('.'))
				.substring(0, fileTable.getValueAt(0, 1).toString()
						.substring(0, fileTable.getValueAt(0, 1).toString().lastIndexOf('.')).lastIndexOf('\\'));

		String directoryPath = selectFiles.getSelectedFile().toString().substring(0,
				selectFiles.getSelectedFile().toString().lastIndexOf('\\'));

		if (tablePath != directoryPath) {
			JOptionPane.showMessageDialog(frame, DIFFERENTDIRECTORIES);
		}
	}

	/**
	 * Adds the selectedFiles to the jTable.
	 * 
	 * @param selectFiles
	 * 
	 * @author uran
	 */
	private void addValuesToTable(JFileChooser selectFiles) {

		File[] fileArray = selectFiles.getSelectedFiles();
		String sourcePath = fileArray[0].getParent();

		sourceTextField.setText(sourcePath);
		sourceTextField.setEnabled(true);
		newFileNameTextField.setEnabled(true);
		renameButton.setEnabled(true);

		for (int i = 0; i < fileArray.length; i++) {
			Vector<String> v = new Vector<>();

			String name;
			String path;
			String type = "";

			name = fileArray[i].getName();
			path = fileArray[i].getPath();

			int j = name.lastIndexOf('.');
			if (j >= 0) {
				type = name.substring(j + 1);
			}

			Path filePath = Paths.get(path);
			try {
				BasicFileAttributes attr = Files.readAttributes(filePath, BasicFileAttributes.class);

				Date formattedDate = new Date(attr.creationTime().to(TimeUnit.MILLISECONDS));
				SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
				String date = String.valueOf(dateFormat.format(formattedDate));

				v.add(name);
				v.add(path);
				v.add(type);
				v.add(date);

				model.addRow(v);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(frame, "Das Erstellungsdatum der Datei "
						+ fileTable.getValueAt(i, 1).toString() + " konnte nicht gefunden werden.");
			}
		}
	}

	/**
	 * Renames the selectedFiles
	 * 
	 * @author uran
	 */
	private void renameFiles() {

		if (fileTable.getSelectedRowCount() == 0) {
			fileTable.selectAll();
		}

		for (int i = 0; i < fileTable.getSelectedRowCount(); i++) {

			String currentFilePath = fileTable.getValueAt(i, 1).toString()
					.substring(0, fileTable.getValueAt(i, 1).toString().lastIndexOf('.'))
					.substring(0, fileTable.getValueAt(i, 1).toString()
							.substring(0, fileTable.getValueAt(i, 1).toString().lastIndexOf('.')).lastIndexOf('\\'));

			String currentFileType = fileTable.getValueAt(i, 1).toString().substring(
					fileTable.getValueAt(i, 1).toString().lastIndexOf('.'),
					fileTable.getValueAt(i, 1).toString().length());

			String currentFileNumber = String.format("%0" + String.valueOf(fileTable.getRowCount()).length() + "d",
					i + 1);

			String newFileName = "";

			if (fileTable.getRowCount() < 10) {

				newFileName = currentFilePath + PATHDELIMITER + "0" + currentFileNumber + " "
						+ newFileNameTextField.getText() + currentFileType;
			} else {

				newFileName = currentFilePath + PATHDELIMITER + currentFileNumber + " " + newFileNameTextField.getText()
						+ currentFileType;
			}

			boolean isRenamed = (new File(fileTable.getValueAt(i, 1).toString())).renameTo(new File(newFileName));

			if (!isRenamed) {
				JOptionPane.showMessageDialog(frame,
						"Das Bild " + fileTable.getValueAt(i, 1).toString() + " konnte nicht umbenannt werden!");
				break;
			}
		}

		JOptionPane.showMessageDialog(frame, RENAMESUCCESSFUL);

		sourceTextField.setText(FILEPATH);
		sourceTextField.setEnabled(false);
		newFileNameTextField.setText(NEWFILENAME);
		newFileNameTextField.setEnabled(false);
		renameButton.setEnabled(false);
		model.setRowCount(0);
		panel.revalidate();
		panel.repaint();
	}

	/**
	 * *****************************************GUI*****************************************
	 * 
	 * @author uran
	 */
	private JFrame getFrame() {
		if (frame == null) {
			frame = new JFrame(RENAMEFILES);
			frame.setSize(535, 550);
			frame.setLocationRelativeTo(null);
			frame.add(getPanel());
			frame.setVisible(true);
		}
		return frame;
	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel(new GridBagLayout());
			panel.setSize(535, 550);

			GridBagConstraints conSourceTextField = new GridBagConstraints();
			conSourceTextField.gridx = 0;
			conSourceTextField.gridy = 0;
			conSourceTextField.gridwidth = 3;
			conSourceTextField.fill = GridBagConstraints.BOTH;
			conSourceTextField.insets = new Insets(0, 0, 0, 5);

			GridBagConstraints conSelectSourceButton = new GridBagConstraints();
			conSelectSourceButton.gridx = 3;
			conSelectSourceButton.gridy = 0;
			conSelectSourceButton.fill = GridBagConstraints.BOTH;
			conSelectSourceButton.insets = new Insets(0, 5, 5, 0);

			GridBagConstraints conFileTable = new GridBagConstraints();
			conFileTable.gridx = 0;
			conFileTable.gridy = 1;
			conFileTable.gridwidth = 4;
			conFileTable.fill = GridBagConstraints.BOTH;
			conFileTable.insets = new Insets(5, 0, 5, 0);

			GridBagConstraints conNewFileNameTextField = new GridBagConstraints();
			conNewFileNameTextField.gridx = 0;
			conNewFileNameTextField.gridy = 2;
			conNewFileNameTextField.gridwidth = 3;
			conNewFileNameTextField.fill = GridBagConstraints.BOTH;
			conNewFileNameTextField.insets = new Insets(5, 0, 0, 5);

			GridBagConstraints conRenameButton = new GridBagConstraints();
			conRenameButton.gridx = 3;
			conRenameButton.gridy = 2;
			conRenameButton.fill = GridBagConstraints.BOTH;
			conRenameButton.insets = new Insets(5, 5, 0, 0);

			panel.add(getSourceTextField(), conSourceTextField);
			panel.add(getSourceButton(), conSelectSourceButton);
			panel.add(new JScrollPane(getFileTable()), conFileTable);
			panel.add(getFileNameTextField(), conNewFileNameTextField);
			panel.add(getRenameButton(), conRenameButton);

		}
		return panel;
	}

	private JTextField getSourceTextField() {
		if (sourceTextField == null) {
			sourceTextField = new JTextField(FILEPATH, 30);
			sourceTextField.setEnabled(false);
			sourceTextField.setEditable(true);
			sourceTextField.getText();
		}
		return sourceTextField;
	}

	private JButton getSourceButton() {
		if (selectSourceButton == null) {
			selectSourceButton = new JButton(SELECTPATH);
			selectSourceButton.setFocusable(false);

			selectSourceButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					selectFiles();
				}
			});
		}
		return selectSourceButton;
	}

	private JTable getFileTable() {
		if (fileTable == null) {
			String[] columnNames = { FILENAME, FILEPATH, TYPE, DATE };
			model = new DefaultTableModel(columnNames, 0);
			fileTable = new JTable(model);
			fileTable.setAutoCreateRowSorter(true);
			fileTable.setComponentPopupMenu(getPopupMenu());
			fileTable.setEnabled(true);
		}
		return fileTable;
	}

	private JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.add(getDeleteOne());
			popupMenu.add(getDeleteAll());
		}
		return popupMenu;
	}

	private JMenuItem getDeleteAll() {
		if (deleteAll == null) {
			deleteAll = new JMenuItem();
			deleteAll.setText(DELETEALLFILES);
			deleteAll.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent delAll) {
					model.setRowCount(0);
					sourceTextField.setText(FILEPATH);
					sourceTextField.setEnabled(false);
					newFileNameTextField.setEnabled(false);
					renameButton.setEnabled(false);
				}
			});
		}
		return deleteAll;
	}

	private JMenuItem getDeleteOne() {
		if (deleteOne == null) {
			deleteOne = new JMenuItem();
			deleteOne.setText(DELETESELECTEDFILES);
			deleteOne.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent delOne) {

					try {

						int[] selectedRows = fileTable.getSelectedRows();

						for (int i = 0; i < selectedRows.length; i++) {

							model.removeRow(selectedRows[i] - i);
						}

						if (fileTable.getRowCount() == 0) {
							sourceTextField.setText(FILEPATH);
							sourceTextField.setEnabled(false);
							newFileNameTextField.setEnabled(false);
							renameButton.setEnabled(false);
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, NOSELECTION);
						return;
					}
				}
			});
		}
		return deleteOne;
	}

	private JTextField getFileNameTextField() {
		if (newFileNameTextField == null) {
			newFileNameTextField = new JTextField(NEWFILENAME, 30);
			newFileNameTextField.setEnabled(false);
			newFileNameTextField.setEditable(true);
			newFileNameTextField.getText();
		}
		return newFileNameTextField;
	}

	private JButton getRenameButton() {
		if (renameButton == null) {
			renameButton = new JButton(CONFIRMRENAME);
			renameButton.setFocusable(false);
			renameButton.setEnabled(false);
			renameButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					renameFiles();
				}
			});
		}
		return renameButton;
	}

	private void initializeGui() {
		getFrame();
	}

	public ImageRenamer() {
		initializeGui();
	}

	public static void main(String[] args) {
		new ImageRenamer();
	}
}