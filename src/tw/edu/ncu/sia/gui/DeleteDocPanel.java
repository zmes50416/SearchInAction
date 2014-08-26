package tw.edu.ncu.sia.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tw.edu.ncu.sia.util.ServerUtil;


public class DeleteDocPanel extends JPanel {
	
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
		textArea.setLineWrap(true);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
		addCompoents();
		//add a status bar
		this.add(new StatusPanel(textArea), BorderLayout.SOUTH);
	}
	
	private void addCompoents(){
		//control panel
		ctlPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		ctlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ctlPanel.setLayout(new BoxLayout(ctlPanel,BoxLayout.Y_AXIS));
		
		//panel1
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblIcon = new JLabel(new ImageIcon(getClass().getResource(
		"icons\\icon.png")));
		JLabel lbMsg = new JLabel("Delete a doc by ID: ");
		final JTextField jfID = new JTextField(18);
		JButton btDelete = new JButton("Delete");
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel1.add(lblIcon);
		panel1.add(lbMsg);
		panel1.add(jfID);
		panel1.add(btDelete);
		ctlPanel.add(panel1);
		
		//panel2
		JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblIcon2 = new JLabel(new ImageIcon(getClass().getResource(
		"icons\\icon.png")));
		JLabel lbMsg1 = new JLabel("Delete a folder (name without '/'): ");
		final JTextField jfPName = new JTextField(18);
		JButton btDelete2 = new JButton("Delete");
		panel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		panel2.add(lblIcon2);
		panel2.add(lbMsg1);
		panel2.add(jfPName);
		panel2.add(btDelete2);
		ctlPanel.add(panel2);
		
		JPanel panelDeleteAll = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton btnDeleteAll = new JButton("Delete All");
		panelDeleteAll.add(new JLabel("Delete All Index!!:"));
		panelDeleteAll.add(btnDeleteAll);
		this.ctlPanel.add(panelDeleteAll);
		
		ActionListener delListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure the last line is always visible
				textArea.setCaretPosition(textArea.getDocument().getLength());
				
				if(jfID.getText()==null || jfID.getText().equals("")){
					textArea.append("Id is invalid!");
				}else{
					// create a solr Server object
					//ServerUtil sol = new ServerUtil();
					try {
						textArea.append("Connecting to server...");
						ServerUtil.testServerConnected();
						textArea.append("\nDeleting source file: " + jfID.getText());
						ServerUtil.deleteDocByID(jfID.getText());
						textArea.append("\nTask is done.");
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		btDelete.addActionListener(delListener);
		
		ActionListener delFolderListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure the last line is always visible
				textArea.setCaretPosition(textArea.getDocument().getLength());
				
				if(jfPName.getText()==null || jfPName.getText().equals("")){
					textArea.append("Project name is invalid!");
				}else{
					// create a solr Server object
					
					try {
						textArea.append("Connecting to server...");
						ServerUtil.testServerConnected();
						textArea.append("\nDeleting source project: " + jfPName.getText());
						ServerUtil.deleteSingleProject(jfPName.getText());
						textArea.append("\nTask is done.");
						
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		};
		btDelete2.addActionListener(delFolderListener);
		
		btnDeleteAll.addActionListener(new ActionListener(){
			
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Make sure the last line is always visible
				textArea.setCaretPosition(textArea.getDocument().getLength());
				 
				if(JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, "Are You sure Delete All DOCUMENT?!", "Warning",JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)){
				textArea.append("Connecting to server...");
				try {
					ServerUtil.deleteAllDoc();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				textArea.append("\nTask is done.");
				}
				
			} 
		});
	}
}
