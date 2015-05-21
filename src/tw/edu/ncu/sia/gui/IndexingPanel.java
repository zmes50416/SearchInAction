package tw.edu.ncu.sia.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import tw.edu.ncu.sia.index.DocIndexing;
import tw.edu.ncu.sia.index.IndexStatus;
import tw.edu.ncu.sia.util.Config;
import tw.edu.ncu.sia.util.DocFolder;
import tw.edu.ncu.sia.util.ServerUtil;


public class IndexingPanel extends JPanel implements Observer{
	
	private static final long serialVersionUID = 1L;
	private JPanel ctlPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JTextArea textArea= new JTextArea(1,100);
	private JTextField docNames = new JTextField(20);
	private JRadioButton rb1 = new JRadioButton("EN", true);
	private JRadioButton rb2 = new JRadioButton("zhTW"); 
	DocIndexing indexer = new DocIndexing();
	public void init(){
		this.setLayout(new BorderLayout());
		this.add(ctlPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(bottomPanel, BorderLayout.SOUTH);
		textArea.setLineWrap(true);
		// Make sure the last line is always visible
		textArea.setCaretPosition(textArea.getDocument().getLength());
		indexer.addObserver(this);
		addCompoents();
		//add a status bar
		this.add(new StatusPanel(textArea), BorderLayout.SOUTH);
	}
	
	private void addCompoents(){
		//control panel
		ctlPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		ctlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		ctlPanel.setLayout(new BoxLayout(ctlPanel,BoxLayout.Y_AXIS));
		//show code base folder in msgTextArea
		textArea.append("Current docfolder:\n" + " " + Config.docfolder);
		//panel1
		JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblIcon = new JLabel(new ImageIcon(getClass().getResource(
		"icons\\icon.png")));
		JLabel lbMsg = new JLabel("Show docs in docfolder. ");
		JButton btShowCodebase = new JButton("Execute");
		panel1.add(lblIcon);
		panel1.add(lbMsg);
		panel1.add(btShowCodebase);
		ctlPanel.add(panel1);
		
		//panel4
		JPanel panel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JLabel lblIcon3 = new JLabel(new ImageIcon(getClass().getResource(
		"icons\\icon.png")));
		JLabel lbMsg3 = new JLabel("Index doc(s): [separated by ','] ");
		panel4.add(lblIcon3);
		panel4.add(lbMsg3);
		ctlPanel.add(panel4);
		
		//panel5
		JPanel panel5 = new JPanel(new FlowLayout(FlowLayout.CENTER));

		// add radiobutton to buttongroup
		ButtonGroup bg = new ButtonGroup();
		bg.add(rb1);
		bg.add(rb2);
		JButton btIndexing = new JButton("Index");
		JButton btIndexingAll = new JButton("Index All");
		JButton retryButton = new JButton("Retry");

		//panel5.add(lblIcon3);
		//panel5.add(lbMsg3);
		panel5.add(rb1);
		panel5.add(rb2);
		panel5.add(docNames);
		panel5.add(btIndexing);
		panel5.add(btIndexingAll);
		panel5.add(retryButton);
		ctlPanel.add(panel5);
		retryButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				
				textArea.append(System.lineSeparator()+"Start Retry error documents");
				indexer.retryError();
				
			}
			
		});
		//Show docs in docfolder
		ActionListener showDocFolderListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DocFolder.show(textArea);
			}
		};
		btShowCodebase.addActionListener(showDocFolderListener);
		
	
		//Index single project
		ActionListener indexDocListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Make sure the last line is always visible
				textArea.setCaretPosition(textArea.getDocument().getLength());
				if(docNames.getText().equals("")){
					JOptionPane.showMessageDialog(null, "Invalid doc name! Try again!!");
				}else{
					if(docNames.getText().trim().contains(",")){
						for(String docName : docNames.getText().trim().split(",")){
							File docDir = new File(Config.docfolder+"/"+docName);
							if(docDir.exists()){
								textArea.append("Start indexing:"+docName+System.lineSeparator());
								indexer.preProcess(docDir);
							}else{
								textArea.append(docDir.getAbsolutePath()
										+ " does not exist");
							}
						}
					}else{
						File docDir = new File(Config.docfolder+"/"+docNames);
						if(docDir.exists()){
							textArea.append("Start indexing:"+docNames+System.lineSeparator());
							indexer.preProcess(docDir);
						}else{
							textArea.append(docNames+ " does not exist");
						}
					}
				}
			}
		};
		btIndexing.addActionListener(indexDocListener);
		btIndexingAll.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				textArea.setCaretPosition(textArea.getDocument().getLength());
				
				File baseDir = new File(Config.docfolder);
				if(baseDir !=null){
					File[] files = baseDir.listFiles();
					for(File documentDir:files){
						indexer.preProcess(documentDir);
					}
				}
				
			}
			
		});
	}
	
	public HashSet<String> getCodebaseProj(){
		File dir = new File(Config.docfolder);
		File[] files = dir.listFiles();
		if (files == null) {
		    return null;
		} else {
			HashSet<String> h = new HashSet<String>(); 
		    for (int i=0; i<files.length; i++) {
		    	if(files[i].isDirectory()){
		    		h.add(files[i].getName());
		    	}
		    }
		    return h;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		String message = (String)arg;
		this.textArea.append(System.lineSeparator());
		this.textArea.append(message);	
		this.textArea.append(System.lineSeparator());
	}
	

}
