package tw.edu.ncu.sia.gui;

import javax.swing.JOptionPane;

public class BrowserPanel {

	public static void open(String url) {
		String osName = System.getProperty("os.name");
		try {
		if (osName.startsWith("Windows"))
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
		else { 
			String[] browsers = {"chrome","firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
			String browser = null;
		for (int count = 0; count < browsers.length && browser == null; count++)
				if (Runtime.getRuntime().exec(new String[] {"which", browsers[count]}).waitFor() == 0)
				browser = browsers[count];
				Runtime.getRuntime().exec(new String[] {browser, url});
			}
		}
		catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error in opening browser" + ":\n" + e.getLocalizedMessage());
		}
	}

}
