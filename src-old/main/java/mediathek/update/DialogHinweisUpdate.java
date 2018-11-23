


package mediathek.update;

import mediathek.config.Konstanten;
import mediathek.gui.HyperlinkButton;
import mediathek.gui.actions.UrlHyperlinkAction;
import mediathek.tool.EscapeKeyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.net.URISyntaxException;

@SuppressWarnings("serial")
public class DialogHinweisUpdate extends JDialog {

	private static final Logger logger = LogManager.getLogger(DialogHinweisUpdate.class);

	public DialogHinweisUpdate(JFrame parent, String ttext) {
		super(parent, true);

		initComponents();

		EscapeKeyHandler.installHandler(this, this::dispose);

		jButtonOk.addActionListener(e -> dispose());
		jTextArea1.setText(ttext);

		btnWebsite.addActionListener(e -> {
			try {
				UrlHyperlinkAction.openURL(parent, killURLdl);
			} catch (URISyntaxException ex) {
				logger.error(ex);
			}
		});
	}

	/** This method is called from within the constructor to
	* initialize the form.
	* WARNING: Do NOT modify this code. The content of this method is
	* always regenerated by the Form Editor.
	*/
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	// Generated using JFormDesigner non-commercial license
	private void initComponents() {
		JScrollPane jScrollPane1 = new JScrollPane();
		jTextArea1 = new JTextArea();
		jButtonOk = new JButton();
		btnWebsite = new HyperlinkButton();

		//======== this ========
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Programminformationen");
		Container contentPane = getContentPane();

		//======== jScrollPane1 ========
		{

			//---- jTextArea1 ----
			jTextArea1.setEditable(false);
			jTextArea1.setColumns(20);
			jTextArea1.setLineWrap(true);
			jTextArea1.setRows(5);
			jTextArea1.setText("\n\n");
			jTextArea1.setWrapStyleWord(true);
			jScrollPane1.setViewportView(jTextArea1);
		}

		//---- jButtonOk ----
		jButtonOk.setText("Schlie\u00dfen");

		//---- btnWebsite ----
		btnWebsite.setText("Link zur Webseite");

		GroupLayout contentPaneLayout = new GroupLayout(contentPane);
		contentPane.setLayout(contentPaneLayout);
		contentPaneLayout.setHorizontalGroup(
		contentPaneLayout.createParallelGroup()
		.addGroup(contentPaneLayout.createSequentialGroup()
		.addContainerGap()
		.addGroup(contentPaneLayout.createParallelGroup()
		.addComponent(jScrollPane1)
		.addComponent(btnWebsite, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 379, Short.MAX_VALUE)
		.addComponent(jButtonOk)))
		.addGap(5, 5, 5))
		);
		contentPaneLayout.setVerticalGroup(
		contentPaneLayout.createParallelGroup()
		.addGroup(GroupLayout.Alignment.TRAILING, contentPaneLayout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(btnWebsite, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
		.addComponent(jButtonOk)
		.addContainerGap())
		);
		pack();
		setLocationRelativeTo(getOwner());
	}// </editor-fold>//GEN-END:initComponents
	// Variables declaration - do not modify//GEN-BEGIN:variables
	// Generated using JFormDesigner non-commercial license
	private JTextArea jTextArea1;
	private JButton jButtonOk;
	private HyperlinkButton btnWebsite;
	// End of variables declaration//GEN-END:variables
}
