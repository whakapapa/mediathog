
package mediathog.tool;

import javax.swing.*;

@SuppressWarnings("serial")
public class MVRun extends javax.swing.JDialog {
	public MVRun(JFrame parent, String title) {
		super(parent, false);
		initComponents();
		this.setTitle(title);
		jProgressBar1.setIndeterminate(true);
		jProgressBar1.setStringPainted(true);
		jProgressBar1.setString("warten");
	}

	@Override
	public void dispose() {
		if (SwingUtilities.isEventDispatchThread()) {
			beenden();
		} else {
			SwingUtilities.invokeLater(this::beenden);
		}
	}

	private void beenden() {
		super.dispose();
	}

	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jProgressBar1 = new javax.swing.JProgressBar();

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
		.addContainerGap())
		);
		layout.setVerticalGroup(
		layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		.addGroup(layout.createSequentialGroup()
		.addContainerGap()
		.addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);

		pack();
	}// </editor-fold>//GEN-END:initComponents
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JProgressBar jProgressBar1;
	// End of variables declaration//GEN-END:variables

}
