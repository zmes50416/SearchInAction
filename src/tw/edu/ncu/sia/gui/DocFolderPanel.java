package tw.edu.ncu.sia.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import tw.edu.ncu.sia.util.DocFolder;


public class DocFolderPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JPanel ctlPanel = new JPanel();
	private JTextArea textArea= new JTextArea();
	
	public void init(){
		this.setLayout(new BorderLayout());
		this.add(ctlPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane, BorderLayout.CENTER);
		textArea.setLineWrap( true );
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
		addCompoents();
		//add a status bar
		this.add(new StatusPanel(textArea), BorderLayout.SOUTH);
		
		DocFolder.show(textArea);
	}
	
	private void addCompoents(){
		
		JLabel lblIcon = new JLabel(new ImageIcon(getClass().getResource(
		"icons\\icon.png")));
		JLabel lbIndex = new JLabel("Information:");
		ctlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ctlPanel.add(lblIcon);
		ctlPanel.add(lbIndex);
	}
	

}
