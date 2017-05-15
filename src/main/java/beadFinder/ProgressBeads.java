package beadFinder;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import beadAnalyzer.DrawPoints;
import ij.IJ;
import interactiveMT.Interactive_PSFAnalyze;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import psf_Tookit.GaussianFitParam;

public class ProgressBeads extends SwingWorker<Void, Void> {
	
	
	final Interactive_PSFAnalyze parent;
	
	public ProgressBeads(final Interactive_PSFAnalyze parent){
		
		this.parent = parent;
	}
	
	
	@Override
	protected Void doInBackground() throws Exception {
		
		
		RandomAccessibleInterval<FloatType> source = parent.currentPreprocessedimg;
		RandomAccessibleInterval<FloatType> target = parent.currentimg;
		
		if (parent.FindBeadsViaMSER){
			
			BeadfinderInteractiveMSER newbeadMser = new BeadfinderInteractiveMSER(source, target, parent.newtree, parent.thirdDimension);
			parent.FittedBeads = FindbeadsVia.BeadfindingMethod(source, newbeadMser, parent.jpb);
			
			
		}
		
		
		if (parent.FindBeadsViaDOG){
			BeadfinderInteractiveDoG newbeadDog = new BeadfinderInteractiveDoG(source, target, parent.sigma, parent.sigma2, parent.peaks, parent.thirdDimension);
			parent.FittedBeads = FindbeadsVia.BeadfindingMethod(source, newbeadDog, parent.jpb);
			
		}
		IJ.log("Fitted Parameters: " );
		
		
		DrawPoints draw = new DrawPoints();
		
		draw.drawPoints(parent.FittedBeads);
		
		
		
		for (int index = 0; index < parent.FittedBeads.size(); ++index){
			
			IJ.log(" Amplitude: " + parent.FittedBeads.get(index).Amplitude);
			IJ.log(" Position X: " + parent.FittedBeads.get(index).location.getDoublePosition(0));
			IJ.log(" Position Y: " + parent.FittedBeads.get(index).location.getDoublePosition(1));
			IJ.log(" Sigma X: " + parent.FittedBeads.get(index).Sigma[0]);
			IJ.log(" Sigma Y: " + parent.FittedBeads.get(index).Sigma[1]);
			IJ.log(" Background: " + parent.FittedBeads.get(index).Background);
		}
		
		return null;
	}
	

	@Override
	protected void done() {
		try {
			parent.jpb.setIndeterminate(false);
			get();
			parent.frame.dispose();
			// JOptionPane.showMessageDialog(jpb.getParent(), "End Points
			// found and overlayed", "Success",
			// JOptionPane.INFORMATION_MESSAGE);
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}