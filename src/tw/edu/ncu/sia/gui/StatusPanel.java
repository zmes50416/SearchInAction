package tw.edu.ncu.sia.gui;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class StatusPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private JTextArea textArea;
	
	public StatusPanel() {
	}

	public StatusPanel(JTextArea msgTextArea){
		this.textArea = msgTextArea;
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		final JLabel btLarger = new JLabel("L");
		final JLabel btSmaller = new JLabel("S");
		final JLabel btClear = new JLabel(" #Clear");
		this.add(new JLabel("#Font Size:"));
		this.add(btSmaller);
		this.add(btLarger);
		this.add(btClear);
		Font f = StatusPanel.this.textArea.getFont();
		StatusPanel.this.textArea.setFont(new Font(f.getFontName(),f.getStyle(),f.getSize()+5));
		
		// function listener
		MouseListener functionListener = new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				JLabel label = (JLabel) e.getSource();
				if (label == btLarger) {
					Font f = StatusPanel.this.textArea.getFont();
					StatusPanel.this.textArea.setFont(new Font(f.getFontName(),f.getStyle(),f.getSize()+1));
				}else if (label == btSmaller){
					Font f = StatusPanel.this.textArea.getFont();
					StatusPanel.this.textArea.setFont(new Font(f.getFontName(),f.getStyle(),f.getSize()-1));
				}else {
					StatusPanel.this.textArea.setText("");
				}
			}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {}
			
		};
		btLarger.addMouseListener(functionListener);
		btSmaller.addMouseListener(functionListener);
		btClear.addMouseListener(functionListener);
	}
}
