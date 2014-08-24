package tw.edu.ncu.sia.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import tw.edu.ncu.sia.util.Config;

public class MainGuiPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	// Main Components
	private JMenuBar mBar;
	private JTabbedPane tPanel = new JTabbedPane();
	private JTextArea textArea;

	// Menu and menu item Components
	private JMenu mMission, mSetting, mHelp;
	private JMenuItem iDelDoc, iQuery, iUrlSetting, iDocFolderSetting, iQuit,
			iAbout, iShowDocFolder, iIndexing, iHome;

	private JScrollPane scrollPane;
	private JPanel topPanel = new JPanel();

	private Font tFont = new Font("Ubuntu", Font.PLAIN, 16);
	private Font mFont = new Font("Ubuntu", Font.PLAIN, 18);

	public void initGui() {
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(topPanel, BorderLayout.NORTH);
		contentPane.add(tPanel, BorderLayout.CENTER);

		initFrame();
		initMenu();
		initTabbedPane();
		initTopPanel();
		initOther();
		addListeners();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	private void initFrame() {
		this.setTitle(Config.title + " " + Config.version);
		ImageIcon ImageIcon = new ImageIcon(getClass().getResource(
				"icons\\logo.png"));
		Image Image = ImageIcon.getImage();
		this.setIconImage(Image);
	}

	private void initTopPanel() {
		// Init TOP Panel
		topPanel.setLayout(new GridLayout(1, 1));
		topPanel.add(mBar);
	}

	private void initMenu() {
		// Menus
		mMission = new JMenu("Mission");
		mMission.setMnemonic(KeyEvent.VK_M);
		mMission.setFont(mFont);
		mSetting = new JMenu("Setting");
		mSetting.setMnemonic(KeyEvent.VK_S);
		mSetting.setFont(mFont);
		mHelp = new JMenu("Help");
		mHelp.setMnemonic(KeyEvent.VK_H);
		mHelp.setFont(mFont);
		// MenuItems: Mission
		iHome = new JMenuItem("Home");
		iHome.setMnemonic(KeyEvent.VK_H);
		iHome.setFont(mFont);
		iShowDocFolder = new JMenuItem("Show DocFolder");
		iShowDocFolder.setMnemonic(KeyEvent.VK_S);
		iShowDocFolder.setFont(mFont);
		iIndexing = new JMenuItem("Indexing Tasks");
		iIndexing.setMnemonic(KeyEvent.VK_I);
		iIndexing.setFont(mFont);
		iDelDoc = new JMenuItem("Del Doc from Index");
		iDelDoc.setMnemonic(KeyEvent.VK_D);
		iDelDoc.setFont(mFont);
		iQuery = new JMenuItem("Query Interface");
		iQuery.setMnemonic(KeyEvent.VK_Q);
		iQuery.setFont(mFont);
		iQuit = new JMenuItem("Exit");
		iQuit.setMnemonic(KeyEvent.VK_E);
		iQuit.setFont(mFont);

		// MenuItems: Setting
		iUrlSetting = new JMenuItem("Server Url");
		iUrlSetting.setMnemonic(KeyEvent.VK_S);
		iUrlSetting.setFont(mFont);
		iDocFolderSetting = new JMenuItem("Doc folder");
		iDocFolderSetting.setMnemonic(KeyEvent.VK_D);
		iDocFolderSetting.setFont(mFont);
		// MenuItems: Help
		iAbout = new JMenuItem("About");
		iAbout.setMnemonic(KeyEvent.VK_A);
		iAbout.setFont(mFont);

		// Create a menuBar object
		mBar = new JMenuBar();
		// Add Menus to MenuBar
		mBar.add(mMission);
		mBar.add(mSetting);
		mBar.add(mHelp);

		// add menuItems to MISSION menu
		mMission.add(iHome);
		mMission.add(iShowDocFolder);
		mMission.add(iIndexing);
		mMission.add(iDelDoc);
		mMission.add(iQuery);
		mMission.add(iQuit);

		// add MenuItems to PREFERENCES menu
		mSetting.add(iUrlSetting);
		mSetting.add(iDocFolderSetting);

		// Add MenuItems to HELP menu
		mHelp.add(iAbout);
	}

	private void initTabbedPane() {
		HomePanel homePanel = new HomePanel();
		homePanel.init();
		tPanel.addTab("Info View", homePanel);
		tPanel.setVisible(true);
		tPanel.setComponentAt(0, homePanel);
	}

	private void initOther() {
		// Tab
		textArea = new JTextArea();
		textArea.setEditable(true);
		textArea.setFont(tFont);
		scrollPane = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	private void addListeners() {
		/*
		 * ACTION LISTENERS
		 */
		ActionListener quitListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component parent = null;
				int ans = JOptionPane.showConfirmDialog(parent,
						"Are you sure you want to quit?");
				if (ans == 0) {
					dispose();
				}
			}
		};
		iQuit.addActionListener(quitListener);

		ActionListener queryListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BrowserPanel.open(Config.hosturl + Config.queryPage);
			}
		};
		iQuery.addActionListener(queryListener);

		ActionListener serverSettingListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Config.setServerURL();
			}
		};
		iUrlSetting.addActionListener(serverSettingListener);

		ActionListener codeBaseSettingListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Config.setDocFolder();
			}
		};
		iDocFolderSetting.addActionListener(codeBaseSettingListener);

		ActionListener aboutListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(null, Config.title + " "
						+ Config.version + "\nCreated by " + Config.author
						+ "\nEmail: " + Config.authorEmail + "\n"
						+ Config.updatedTime, "About " + Config.title,
						JOptionPane.INFORMATION_MESSAGE);
			}
		};
		iAbout.addActionListener(aboutListener);

		ActionListener codeBaseListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				IndexingPanel codeBasePanel = new IndexingPanel();
				codeBasePanel.init();
				tPanel.setComponentAt(0, codeBasePanel);
			}
		};
		iIndexing.addActionListener(codeBaseListener);

		// Delete a source file by ID
		ActionListener delFileListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DeleteDocPanel delFilePanel = new DeleteDocPanel();
				delFilePanel.init();
				tPanel.setComponentAt(0, delFilePanel);
			}
		};
		iDelDoc.addActionListener(delFileListener);

		// show doc folder
		ActionListener showDocFolderListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DocFolderPanel showDocFolderPanel = new DocFolderPanel();
				showDocFolderPanel.init();
				tPanel.setComponentAt(0, showDocFolderPanel);
			}
		};
		iShowDocFolder.addActionListener(showDocFolderListener);

		// go home
		ActionListener homeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				initTabbedPane();
			}
		};
		iHome.addActionListener(homeListener);
	}
}
