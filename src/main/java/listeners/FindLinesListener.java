package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingUtilities;

import interactiveMT.Interactive_MTDoubleChannel;
import swingClasses.ProgressSeeds;

public class FindLinesListener implements ActionListener {

	
final Interactive_MTDoubleChannel parent;
	
	public FindLinesListener (final Interactive_MTDoubleChannel parent ){
		
		this.parent = parent;
	}
	
	@Override
	public void actionPerformed(final ActionEvent arg0) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				goSeeds();

			}

		});

	}
	
	
	public void goSeeds() {

		parent.jpb.setIndeterminate(false);

		parent.jpb.setMaximum(parent.max);
		parent.panel.add(parent.label);
		parent.panel.add(parent.jpb);
		parent.frame.add(parent.panel);
		parent.frame.pack();
		parent.frame.setSize(200, 100);
		parent.frame.setLocationRelativeTo(parent.panelCont);
		parent.frame.setVisible(true);

		ProgressSeeds trackMT = new ProgressSeeds(parent);
		trackMT.execute();
		parent.displayBitimg = false;
		parent.displayWatershedimg = false;

	}
}
