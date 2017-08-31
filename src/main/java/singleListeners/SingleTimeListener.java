package singleListeners;

import java.awt.Label;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import ij.IJ;

import interactiveMT.Interactive_MTSingleChannel;
import interactiveMT.Interactive_MTSingleChannel.ValueChange;
import updateListeners.Markends;

public class SingleTimeListener implements AdjustmentListener {
	final Label label;
	final String string;
	Interactive_MTSingleChannel parent;
	final float min, max;
	final int scrollbarSize;

	final JScrollBar deltaScrollbar;

	public SingleTimeListener(final Interactive_MTSingleChannel parent, final Label label, final String string, final float min, final float max,
			final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;

		this.deltaScrollbar = deltaScrollbar;
		deltaScrollbar.addMouseMotionListener(new SingleNonStandardMouseListener(parent, ValueChange.THIRDDIMmouse));
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		parent.thirdDimension = (int) parent.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize);

		deltaScrollbar
				.setValue(parent.computeScrollbarPositionFromValue(parent.thirdDimension, min, max, scrollbarSize));

		label.setText(string +  " = "  + parent.thirdDimension);

		shownew();

	}
	
	public void shownew() {

		if (parent.thirdDimension > parent.thirdDimensionSize) {
			IJ.log("Max frame number exceeded, moving to last frame instead");
			parent.thirdDimension = parent.thirdDimensionSize;
			parent.CurrentView = util.CopyUtils.getCurrentView(parent.originalimg, parent.thirdDimension,
					parent.thirdDimensionSize);
			parent.CurrentPreprocessedView = util.CopyUtils.getCurrentPreView(parent.originalPreprocessedimg,
					parent.thirdDimension, parent.thirdDimensionSize);
		} else {

			parent.CurrentView = util.CopyUtils.getCurrentView(parent.originalimg, parent.thirdDimension,
					parent.thirdDimensionSize);
			parent.CurrentPreprocessedView = util.CopyUtils.getCurrentPreView(parent.originalPreprocessedimg,
					parent.thirdDimension, parent.thirdDimensionSize);
		}

		
		
	

		
	}
}