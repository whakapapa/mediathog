
package mediathog.gui.dialog;

import mediathog.tool.EscapeKeyHandler;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class DialogOk extends JDialog {
	public DialogOk(Frame parent, boolean modal, JPanel panel, String titel) {
		super(parent, modal);
		initComponents();
		this.setTitle(titel);
		jPanelExtra.setLayout(new BorderLayout());
		jPanelExtra.add(panel);
		this.pack();
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		jButtonBeenden.addActionListener(e -> dispose());

		EscapeKeyHandler.installHandler(this, this::dispose);
	}

	/** This method is called from within the constructor to
	* initialize the form.
	* WARNING: Do NOT modify this code. The content of this method is
	* always regenerated by the Form Editor.
	*/
	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jButtonBeenden = new javax.swing.JButton();
		jPanelExtra = new javax.swing.JPanel();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		jButtonBeenden.setText("Ok");

		jPanelExtra.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout jPanelExtraLayout = new javax.swing.GroupLayout(jPanelExtra);
		jPanelExtra.setLayout(jPanelExtraLayout);
		jPanelExtraLayout.setHorizontalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 0, Short.MAX_VALUE)
		);
		jPanelExtraLayout.setVerticalGroup(
		jPanelExtraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGap(0, 265, Short.MAX_VALUE)
		);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
		.addContainerGap()
		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
		.addComponent(jPanelExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addGroup(layout.createSequentialGroup()
		.addGap(0, 414, Short.MAX_VALUE)
		.addComponent(jButtonBeenden, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)))
		.addContainerGap())
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jPanelExtra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
		.addComponent(jButtonBeenden)
		.addContainerGap())
		);

		pack();
	}// </editor-fold>//GEN-END:initComponents
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton jButtonBeenden;
	private javax.swing.JPanel jPanelExtra;
	// End of variables declaration//GEN-END:variables
}
