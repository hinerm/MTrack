package swingClasses;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import MTObjects.ResultsMT;
import drawandOverlay.DisplayGraph;
import drawandOverlay.DisplayGraphKalman;
import graphconstructs.KalmanTrackproperties;
import graphconstructs.Trackproperties;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.measure.ResultsTable;
import ij.plugin.frame.RoiManager;
import interactiveMT.Interactive_MTDoubleChannel;
import interactiveMT.Interactive_MTDoubleChannel.ValueChange;
import interactiveMT.Interactive_MTDoubleChannel.Whichend;
import labeledObjects.PlusMinusSeed;
import lineFinder.FindlinesVia;
import lineFinder.LinefinderInteractiveHFHough;
import lineFinder.LinefinderInteractiveHFMSER;
import lineFinder.LinefinderInteractiveHFMSERwHough;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.view.Views;
import trackerType.KFsearch;
import trackerType.TrackModel;
import velocityanalyser.Trackend;
import velocityanalyser.Trackstart;

public class Track {

	final Interactive_MTDoubleChannel parent;

	ArrayList<PlusMinusSeed> plusminusstartlist = new ArrayList<PlusMinusSeed>();
	ArrayList<PlusMinusSeed> plusminusendlist = new ArrayList<PlusMinusSeed>();
	public Track(final Interactive_MTDoubleChannel parent) {

		this.parent = parent;
	}

	public void Trackobject(final int next) {

		parent.thirdDimensionSize = parent.endtime;

		for (int index = next; index <= parent.endtime; ++index) {

			parent.displayBitimg = false;
			parent.displayWatershedimg = false;
			parent.thirdDimension = index;
			parent.isStarted = true;
			parent.CurrentPreprocessedView = util.CopyUtils.getCurrentPreView(parent.originalPreprocessedimg,
					parent.thirdDimension, parent.thirdDimensionSize);
			parent.CurrentView = util.CopyUtils.getCurrentView(parent.originalimg, parent.thirdDimension,
					parent.endtime);
			parent.updatePreview(ValueChange.THIRDDIMTrack);

			RandomAccessibleInterval<FloatType> groundframe = parent.currentimg;
			RandomAccessibleInterval<FloatType> groundframepre = parent.currentPreprocessedimg;

			if (parent.FindLinesViaMSER) {

				parent.updatePreview(ValueChange.SHOWMSER);

				LinefinderInteractiveHFMSER newlineMser = new LinefinderInteractiveHFMSER(groundframe, groundframepre,
						parent.newtree, parent.thirdDimension);
				parent.returnVector = FindlinesVia.LinefindingMethodHF(groundframe, groundframepre,
						parent.PrevFrameparam, parent.thirdDimension, parent.psf, newlineMser, parent.userChoiceModel,
						parent.Domask, parent.Intensityratio, parent.Inispacing, parent.seedmap, parent.jpb,
						parent.endtime, parent.maxdist, next, parent.numgaussians);
				parent.Accountedframes.add(FindlinesVia.getAccountedframes());

				if (parent.Userframe.size() > 0) {

					parent.returnVectorUser = FindlinesVia.LinefindingMethodHFUser(groundframe, groundframepre,
							parent.Userframe, parent.thirdDimension, parent.psf, newlineMser, parent.userChoiceModel,
							parent.Domask, parent.Intensityratio, parent.Inispacing, parent.jpb, parent.endtime,
							parent.maxdist, next);

				}

			}

			if (parent.FindLinesViaHOUGH) {

				parent.updatePreview(ValueChange.SHOWHOUGH);
				parent.updatePreview(ValueChange.SHOWMSERinHough);
				LinefinderInteractiveHFHough newlineHough = new LinefinderInteractiveHFHough(parent, groundframe,
						groundframepre, parent.Maxlabel, parent.thirdDimension);

				parent.returnVector = FindlinesVia.LinefindingMethodHF(groundframe, groundframepre,
						parent.PrevFrameparam, parent.thirdDimension, parent.psf, newlineHough, parent.userChoiceModel,
						parent.Domask, parent.Intensityratio, parent.Inispacing, parent.seedmap, parent.jpb,
						parent.endtime, parent.maxdist, next, parent.numgaussians);

				parent.Accountedframes.add(FindlinesVia.getAccountedframes());

				if (parent.Userframe.size() > 0) {

					parent.returnVectorUser = FindlinesVia.LinefindingMethodHFUser(groundframe, groundframepre,
							parent.Userframe, parent.thirdDimension, parent.psf, newlineHough, parent.userChoiceModel,
							parent.Domask, parent.Intensityratio, parent.Inispacing, parent.jpb, parent.endtime,
							parent.maxdist, next);

				}

			}

			if (parent.FindLinesViaMSERwHOUGH) {

				parent.updatePreview(ValueChange.SHOWMSER);
				LinefinderInteractiveHFMSERwHough newlineMserwHough = new LinefinderInteractiveHFMSERwHough(groundframe,
						groundframepre, parent.newtree, parent.thirdDimension, parent.thetaPerPixel,
						parent.rhoPerPixel);
				parent.returnVector = FindlinesVia.LinefindingMethodHF(groundframe, groundframepre,
						parent.PrevFrameparam, parent.thirdDimension, parent.psf, newlineMserwHough,
						parent.userChoiceModel, parent.Domask, parent.Intensityratio, parent.Inispacing, parent.seedmap,
						parent.jpb, parent.endtime, parent.maxdist, next, parent.numgaussians);

				parent.Accountedframes.add(FindlinesVia.getAccountedframes());

				if (parent.Userframe.size() > 0) {

					parent.returnVectorUser = FindlinesVia.LinefindingMethodHFUser(groundframe, groundframepre,
							parent.Userframe, parent.thirdDimension, parent.psf, newlineMserwHough,
							parent.userChoiceModel, parent.Domask, parent.Intensityratio, parent.Inispacing, parent.jpb,
							parent.endtime, parent.maxdist, next);

				}

			}

			parent.NewFrameparam = parent.returnVector.getB();

			ArrayList<Trackproperties> startStateVectors = parent.returnVector.getA().getA();
			ArrayList<Trackproperties> endStateVectors = parent.returnVector.getA().getB();

			if (parent.returnVectorUser != null) {
				parent.UserframeNew = parent.returnVectorUser.getB();
				ArrayList<Trackproperties> userStateVectors = parent.returnVectorUser.getA();
				parent.AllUser.add(userStateVectors);
				parent.Userframe = parent.UserframeNew;
			}

			parent.detcount++;
			util.DrawingUtils.Trackplot(parent.detcount, parent.returnVector, parent.returnVectorUser,
					parent.AllpreviousRois, parent.colorLineTrack, parent.colorTrack, parent.inactiveColor,
					parent.overlay, parent.maxghost);

			parent.PrevFrameparam = parent.NewFrameparam;

			parent.Allstart.add(startStateVectors);
			parent.Allend.add(endStateVectors);

		}

		if (parent.Allstart.get(0).size() > 1) {
			ImagePlus impstartsec = ImageJFunctions.show(parent.originalimg);
			final Trackstart trackerstart = new Trackstart(parent.Allstart, parent.endtime - next);
			trackerstart.process();
			SimpleWeightedGraph<double[], DefaultWeightedEdge> graphstart = trackerstart.getResult();
			DisplayGraph displaygraphtrackstart = new DisplayGraph(impstartsec, graphstart);
			displaygraphtrackstart.getImp();
			impstartsec.draw();
			impstartsec.setTitle("Graph Start A MT");
		}
		if (parent.Allend.get(0).size() > 1) {
			ImagePlus impendsec = ImageJFunctions.show(parent.originalimg);
			final Trackend trackerend = new Trackend(parent.Allend, parent.endtime - next);

			trackerend.process();
			SimpleWeightedGraph<double[], DefaultWeightedEdge> graphend = trackerend.getResult();
			DisplayGraph displaygraphtrackend = new DisplayGraph(impendsec, graphend);
			displaygraphtrackend.getImp();
			impendsec.draw();
			impendsec.setTitle("Graph Start B MT");
		}

		if (parent.returnVectorUser != null && parent.AllUser.get(0).size() > 1) {
			ImagePlus impstartsec = ImageJFunctions.show(parent.originalimg);
			final Trackstart trackerstart = new Trackstart(parent.AllUser, parent.endtime - next);
			trackerstart.process();
			SimpleWeightedGraph<double[], DefaultWeightedEdge> graphstart = trackerstart.getResult();
			DisplayGraph displaygraphtrackstart = new DisplayGraph(impstartsec, graphstart);
			displaygraphtrackstart.getImp();
			impstartsec.draw();
			impstartsec.setTitle("Graph Start User MT");
		}

		ResultsTable rtAll = new ResultsTable();
		int MaxSeedLabel, MinSeedLabel;
		
		double growratestart = 0;
		double growrateend = 0;
		
		if (parent.Allstart.get(0).size() > 0) {
			
			 MaxSeedLabel = parent.Allstart.get(0).get(parent.Allstart.get(0).size() - 1).seedlabel;
			 MinSeedLabel = parent.Allstart.get(0).get(0).seedlabel;
			 
			final ArrayList<Trackproperties> first = parent.Allstart.get(0);

			Collections.sort(first, parent.Seedcomparetrack);

			
			for (int currentseed = MinSeedLabel; currentseed < MaxSeedLabel + 1; ++currentseed) {

			

				double startlengthreal = 0;
				double startlengthpixel = 0;
				for (int index = 0; index < parent.Allstart.size(); ++index) {

					final ArrayList<Trackproperties> thirdDimension = parent.Allstart.get(index);

					for (int frameindex = 0; frameindex < thirdDimension.size(); ++frameindex) {

						final Integer seedID = thirdDimension.get(frameindex).seedlabel;
						final int framenumber = thirdDimension.get(frameindex).Framenumber;
						if (seedID == currentseed) {
							final Integer[] FrameID = { framenumber, seedID };
							final double[] originalpoint = thirdDimension.get(frameindex).originalpoint;
							final double[] newpoint = thirdDimension.get(frameindex).newpoint;
							final double[] oldpoint = thirdDimension.get(frameindex).oldpoint;
							final double[] newpointCal = new double[] {
									thirdDimension.get(frameindex).newpoint[0] * parent.calibration[0],
									thirdDimension.get(frameindex).newpoint[1] * parent.calibration[1] };
							final double[] oldpointCal = new double[] {
									thirdDimension.get(frameindex).oldpoint[0] * parent.calibration[0],
									thirdDimension.get(frameindex).oldpoint[1] * parent.calibration[1] };

							final double lengthrealperframe = util.Boundingboxes.Distance(newpointCal, oldpointCal);
							final double lengthpixelperframe = util.Boundingboxes.Distance(newpoint, oldpoint);
							final double seedtocurrent = util.Boundingboxes.Distancesq(originalpoint, newpoint);
							final double seedtoold = util.Boundingboxes.Distancesq(originalpoint, oldpoint);
							final boolean shrink = seedtoold > seedtocurrent ? true : false;
							final boolean growth = seedtoold > seedtocurrent ? false : true;

							if (shrink) {
								// MT shrank

								startlengthreal -= lengthrealperframe;
								startlengthpixel -= lengthpixelperframe;

							}
							if (growth) {

								// MT grew
								startlengthreal += lengthrealperframe;
								startlengthpixel += lengthpixelperframe;
								growratestart += lengthpixelperframe;

							}

							double[] currentlocationpixel = new double[parent.ndims];

							if (framenumber == parent.thirdDimensionsliderInit)
								currentlocationpixel = originalpoint;
							else
								currentlocationpixel = newpoint;

							double[] currentlocationreal = new double[parent.ndims];

							currentlocationreal = new double[] { currentlocationpixel[0] * parent.calibration[0],
									currentlocationpixel[1] * parent.calibration[1] };

							ResultsMT startMT = new ResultsMT(framenumber, startlengthpixel, startlengthreal, seedID,
									currentlocationpixel, currentlocationreal, lengthpixelperframe, lengthrealperframe);

							parent.startlengthlist.add(startMT);

						}
					}
				}
				
			
		

		
		}
		}
			for (int index = 0; index < parent.startlengthlist.size(); ++index) {

				double[] landt = { parent.startlengthlist.get(index).totallengthpixel,
						parent.startlengthlist.get(index).framenumber, parent.startlengthlist.get(index).seedid };
				parent.lengthtimestart.add(landt);

				rtAll.incrementCounter();
				rtAll.addValue("FrameNumber", parent.startlengthlist.get(index).framenumber);
				rtAll.addValue("Total Length (pixel)", parent.startlengthlist.get(index).totallengthpixel);
				rtAll.addValue("Total Length (real)", parent.startlengthlist.get(index).totallengthreal);
				rtAll.addValue("Track iD", parent.startlengthlist.get(index).seedid);
				rtAll.addValue("CurrentPosition X (px units)",
						parent.startlengthlist.get(index).currentpointpixel[0]);
				rtAll.addValue("CurrentPosition Y (px units)",
						parent.startlengthlist.get(index).currentpointpixel[1]);
				rtAll.addValue("CurrentPosition X (real units)",
						parent.startlengthlist.get(index).currentpointreal[0]);
				rtAll.addValue("CurrentPosition Y (real units)",
						parent.startlengthlist.get(index).currentpointreal[1]);
				rtAll.addValue("Length per frame (px units)",
						parent.startlengthlist.get(index).lengthpixelperframe);
				rtAll.addValue("Length per frame (real units)",
						parent.startlengthlist.get(index).lengthrealperframe);

			}

	
		

				if (parent.Allend.get(0).size() > 0) {

					MaxSeedLabel = parent.Allend.get(0).get(parent.Allend.get(0).size() - 1).seedlabel;
					MinSeedLabel = parent.Allend.get(0).get(0).seedlabel;
					
					for (int currentseed = MinSeedLabel; currentseed < MaxSeedLabel + 1; ++currentseed) {
					double endlengthreal = 0;
					double endlengthpixel = 0;
					for (int index = 0; index < parent.Allend.size(); ++index) {

						final ArrayList<Trackproperties> thirdDimension = parent.Allend.get(index);

						for (int frameindex = 0; frameindex < thirdDimension.size(); ++frameindex) {
							final int framenumber = thirdDimension.get(frameindex).Framenumber;
							final Integer seedID = thirdDimension.get(frameindex).seedlabel;

							if (seedID == currentseed) {
								final Integer[] FrameID = { framenumber, seedID };
								final double[] originalpoint = thirdDimension.get(frameindex).originalpoint;
								final double[] newpoint = thirdDimension.get(frameindex).newpoint;
								final double[] oldpoint = thirdDimension.get(frameindex).oldpoint;

								final double[] newpointCal = new double[] {
										thirdDimension.get(frameindex).newpoint[0] * parent.calibration[0],
										thirdDimension.get(frameindex).newpoint[1] * parent.calibration[1] };
								final double[] oldpointCal = new double[] {
										thirdDimension.get(frameindex).oldpoint[0] * parent.calibration[0],
										thirdDimension.get(frameindex).oldpoint[1] * parent.calibration[1] };

								final double lengthrealperframe = util.Boundingboxes.Distance(newpointCal, oldpointCal);
								final double lengthpixelperframe = util.Boundingboxes.Distance(newpoint, oldpoint);
								final double seedtocurrent = util.Boundingboxes.Distancesq(originalpoint, newpoint);
								final double seedtoold = util.Boundingboxes.Distancesq(originalpoint, oldpoint);
								final boolean shrink = seedtoold > seedtocurrent ? true : false;
								final boolean growth = seedtoold > seedtocurrent ? false : true;

								if (shrink) {

									// MT shrank

									endlengthreal -= lengthrealperframe;
									endlengthpixel -= lengthpixelperframe;

								}

								if (growth) {

									// MT grew

									endlengthreal += lengthrealperframe;
									endlengthpixel += lengthpixelperframe;
									
									growrateend += lengthpixelperframe;

								}

								double[] currentlocationpixel = new double[parent.ndims];

								if (framenumber == parent.thirdDimensionsliderInit)
									currentlocationpixel = originalpoint;
								else
									currentlocationpixel = newpoint;

								double[] currentlocationreal = new double[parent.ndims];

								currentlocationreal = new double[] { currentlocationpixel[0] * parent.calibration[0],
										currentlocationpixel[1] * parent.calibration[1] };

								ResultsMT endMT = new ResultsMT(framenumber, endlengthpixel, endlengthreal, seedID,
										currentlocationpixel, currentlocationreal, lengthpixelperframe,
										lengthrealperframe);

								parent.endlengthlist.add(endMT);

								

							}
						}
					}

				
				
				
				String plusorminusstart = (growratestart > growrateend) ? "Plus" : "Minus" ;
				String plusorminusend = (growratestart > growrateend) ? "Minus" : "Plus" ;
				if (parent.seedmap.get(currentseed) == Whichend.start || parent.seedmap.get(currentseed) == Whichend.end && parent.seedmap.get(currentseed) != Whichend.both ){
					plusorminusstart = "Zeroend";
					plusorminusend = "Zeroend";
					
				}
				PlusMinusSeed pmseedEndA = new PlusMinusSeed(currentseed, plusorminusstart);
				PlusMinusSeed pmseedEndB = new PlusMinusSeed(currentseed, plusorminusend);
				plusminusstartlist.add(pmseedEndA);
				plusminusendlist.add(pmseedEndB);
				
					
				

		                for (int j = 0; j < plusminusendlist.size(); ++j){
						
						if (plusminusendlist.get(j).seedid == currentseed){
					
						try {
							File fichier = new File(
									parent.usefolder + "//" + parent.addToName + "SeedLabel" + currentseed + plusminusendlist.get(j).plusorminus + ".txt");

							FileWriter fw = new FileWriter(fichier);
							BufferedWriter bw = new BufferedWriter(fw);

							bw.write(
									"\tFrame\tLength (px)\tLength (real)\tiD\tCurrentPosX (px)\tCurrentPosY (px)\tCurrentPosX (real)\tCurrentPosY (real)"
											+ "\tdeltaL (px) \tdeltaL (real)  \tCalibrationX  \tCalibrationY  \tCalibrationT \n");

							for (int index = 0; index < parent.endlengthlist.size(); ++index) {

								if (parent.endlengthlist.get(index).seedid == currentseed) {
									if (index > 0
											&& parent.endlengthlist.get(index).currentpointpixel[0] != parent.endlengthlist
													.get(index - 1).currentpointpixel[0]
											&& parent.endlengthlist.get(index).currentpointpixel[1] != parent.endlengthlist
													.get(index - 1).currentpointpixel[1])

										bw.write("\t" + parent.nf.format(parent.endlengthlist.get(index).framenumber) + "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).totallengthpixel) + "\t"+ "\t"
												+ "\t"+ "\t" + parent.nf.format(parent.endlengthlist.get(index).totallengthreal)
												+ "\t" + "\t" + parent.nf.format(parent.endlengthlist.get(index).seedid)
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).currentpointpixel[0])
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).currentpointpixel[1])
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).currentpointreal[0])
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).currentpointreal[1])
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).lengthpixelperframe)
												+ "\t" + "\t"
												+ parent.nf.format(parent.endlengthlist.get(index).lengthrealperframe)
												+ "\t" + "\t"
												+ parent.nf.format(parent.calibration[0])  
												+ "\t" + "\t"
												+ parent.nf.format(parent.calibration[1])  
												+ "\t" + "\t"
												+ parent.nf.format(parent.calibration[2]) + 
												
												"\n");

								}

							}
							bw.close();
							fw.close();

						} catch (IOException e) {
						}
						}
		}
		
		
					
				
				for (int index = 0; index < parent.endlengthlist.size(); ++index) {

					double[] landt = { parent.endlengthlist.get(index).totallengthpixel,
							parent.endlengthlist.get(index).framenumber, parent.endlengthlist.get(index).seedid };
					parent.lengthtimestart.add(landt);

					rtAll.incrementCounter();
					rtAll.addValue("FrameNumber", parent.endlengthlist.get(index).framenumber);
					rtAll.addValue("Total Length (pixel)", parent.endlengthlist.get(index).totallengthpixel);
					rtAll.addValue("Total Length (real)", parent.endlengthlist.get(index).totallengthreal);
					rtAll.addValue("Track iD", parent.endlengthlist.get(index).seedid);
					rtAll.addValue("CurrentPosition X (px units)", parent.endlengthlist.get(index).currentpointpixel[0]);
					rtAll.addValue("CurrentPosition Y (px units)", parent.endlengthlist.get(index).currentpointpixel[1]);
					rtAll.addValue("CurrentPosition X (real units)", parent.endlengthlist.get(index).currentpointreal[0]);
					rtAll.addValue("CurrentPosition Y (real units)", parent.endlengthlist.get(index).currentpointreal[1]);
					rtAll.addValue("Length per frame (px units)", parent.endlengthlist.get(index).lengthpixelperframe);
					rtAll.addValue("Length per frame (real units)", parent.endlengthlist.get(index).lengthrealperframe);

				}

				
		}
				}
			
			
				if (parent.Allstart.get(0).size() > 0) {
					
					 MaxSeedLabel = parent.Allstart.get(0).get(parent.Allstart.get(0).size() - 1).seedlabel;
					 MinSeedLabel = parent.Allstart.get(0).get(0).seedlabel;
				

					
					for (int currentseed = MinSeedLabel; currentseed < MaxSeedLabel + 1; ++currentseed) {

				
			

						for (int j = 0; j < plusminusstartlist.size(); ++j){
							
							if (plusminusstartlist.get(j).seedid == currentseed){

								try {
									File fichier = new File(parent.usefolder + "//" + parent.addToName + "SeedLabel" + currentseed
											+ plusminusstartlist.get(j).plusorminus + ".txt");

									FileWriter fw = new FileWriter(fichier);
									BufferedWriter bw = new BufferedWriter(fw);

									bw.write(
											"\tFrame\tLength (px)\tLength (real)\tiD\tCurrentPosX (px)\tCurrentPosY (px)\tCurrentPosX (real)\tCurrentPosY (real)"
													+ "\tdeltaL (px)  \tdeltaL (real)  \tCalibrationX  \tCalibrationY  \tCalibrationT \n");

									for (int index = 0; index < parent.startlengthlist.size(); ++index) {

										if (parent.startlengthlist.get(index).seedid == currentseed) {

											if (index > 0
													&& parent.startlengthlist
															.get(index).currentpointpixel[0] != parent.startlengthlist
																	.get(index - 1).currentpointpixel[0]
													&& parent.startlengthlist
															.get(index).currentpointpixel[1] != parent.startlengthlist
																	.get(index - 1).currentpointpixel[1])

												bw.write(
														"\t" + parent.nf.format(parent.startlengthlist.get(index).framenumber) + "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).totallengthpixel)
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).totallengthreal)
																+ "\t" + "\t"
																+ parent.nf.format(parent.startlengthlist.get(index).seedid)
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).currentpointpixel[0])
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).currentpointpixel[1])
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).currentpointreal[0])
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).currentpointreal[1])
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).lengthpixelperframe)
																+ "\t" + "\t"
																+ parent.nf.format(
																		parent.startlengthlist.get(index).lengthrealperframe)
																+ "\t" + "\t"
																+  parent.nf.format(parent.calibration[0])  
																+ "\t" + "\t"
																+ parent.nf.format(parent.calibration[1])  
																+ "\t" + "\t"
																+ parent.nf.format(parent.calibration[2]) + 
																
														
														"\n");

										}

									}
								
									bw.close();
									fw.close();

								} catch (IOException e) {
								}
							}
						}


					}
				}
		
		if (parent.returnVectorUser != null && parent.AllUser.get(0).size() > 0) {
			final ArrayList<Trackproperties> first = parent.AllUser.get(0);
			MaxSeedLabel = first.get(first.size() - 1).seedlabel;
			MinSeedLabel = first.get(0).seedlabel;
			Collections.sort(first, parent.Seedcomparetrack);
		
			for (int currentseed = MinSeedLabel; currentseed < MaxSeedLabel + 1; ++currentseed) {
				double startlengthreal = 0;
				double startlengthpixel = 0;
				for (int index = 0; index < parent.AllUser.size(); ++index) {

					final ArrayList<Trackproperties> thirdDimension = parent.AllUser.get(index);

					for (int frameindex = 0; frameindex < thirdDimension.size(); ++frameindex) {

						final Integer seedID = thirdDimension.get(frameindex).seedlabel;
						final int framenumber = thirdDimension.get(frameindex).Framenumber;
						if (seedID == currentseed) {
							
							final Integer[] FrameID = { framenumber, seedID };
							final double[] originalpoint = thirdDimension.get(frameindex).originalpoint;
							final double[] newpoint = thirdDimension.get(frameindex).newpoint;
							final double[] oldpoint = thirdDimension.get(frameindex).oldpoint;
							final double[] newpointCal = new double[] {
									thirdDimension.get(frameindex).newpoint[0] * parent.calibration[0],
									thirdDimension.get(frameindex).newpoint[1] * parent.calibration[1] };
							final double[] oldpointCal = new double[] {
									thirdDimension.get(frameindex).oldpoint[0] * parent.calibration[0],
									thirdDimension.get(frameindex).oldpoint[1] * parent.calibration[1] };

							final double lengthrealperframe = util.Boundingboxes.Distance(newpointCal, oldpointCal);
							final double lengthpixelperframe = util.Boundingboxes.Distance(newpoint, oldpoint);
							final double seedtocurrent = util.Boundingboxes.Distancesq(originalpoint, newpoint);
							final double seedtoold = util.Boundingboxes.Distancesq(originalpoint, oldpoint);
							final boolean shrink = seedtoold > seedtocurrent ? true : false;
							final boolean growth = seedtoold > seedtocurrent ? false : true;

							if (shrink) {
								// MT shrank

								startlengthreal -= lengthrealperframe;
								startlengthpixel -= lengthpixelperframe;

							}
							if (growth) {

								// MT grew
								startlengthreal += lengthrealperframe;
								startlengthpixel += lengthpixelperframe;

							}

							double[] currentlocationpixel = new double[parent.ndims];

							if (framenumber == parent.thirdDimensionsliderInit)
								currentlocationpixel = originalpoint;
							else
								currentlocationpixel = newpoint;

							double[] currentlocationreal = new double[parent.ndims];

							currentlocationreal = new double[] { currentlocationpixel[0] * parent.calibration[0],
									currentlocationpixel[1] * parent.calibration[1] };

							ResultsMT startMT = new ResultsMT(framenumber, startlengthpixel, startlengthreal, seedID,
									currentlocationpixel, currentlocationreal, lengthpixelperframe, lengthrealperframe);

							parent.userlengthlist.add(startMT);

						}
					}
				}
				
				
				
				
				
				
			}

			for (int seedID = MinSeedLabel; seedID <= MaxSeedLabel; ++seedID) {
				double count = 0;
				for (int index = 0; index < parent.userlengthlist.size(); ++index) {
					if (parent.userlengthlist.get(index).seedid == seedID) {

						if (index > 0
								&& parent.userlengthlist.get(index).currentpointpixel[0] != parent.userlengthlist
										.get(index - 1).currentpointpixel[0]
								&& parent.userlengthlist.get(index).currentpointpixel[1] != parent.userlengthlist
										.get(index - 1).currentpointpixel[1])
							count++;

					}
				}
				

				

					try {
						File fichier = new File(parent.usefolder + "//" + parent.addToName + "SeedLabel" + seedID
								+ "-Zeroend" + ".txt");

						FileWriter fw = new FileWriter(fichier);
						BufferedWriter bw = new BufferedWriter(fw);

						bw.write(
								"\tFrame\tLength (px)\tLength (real)\tiD\tCurrentPosX (px)\tCurrentPosY (px)\tCurrentPosX (real)\tCurrentPosY (real)"
										+ "\tdeltaL (px) \tdeltaL (real) \tCalibrationX  \tCalibrationY  \tFrametoSec \n");

						for (int index = 0; index < parent.userlengthlist.size(); ++index) {
							if (parent.userlengthlist.get(index).seedid == seedID) {

								if (index > 0
										&& parent.userlengthlist
												.get(index).currentpointpixel[0] != parent.userlengthlist
														.get(index - 1).currentpointpixel[0]
										&& parent.userlengthlist
												.get(index).currentpointpixel[1] != parent.userlengthlist
														.get(index - 1).currentpointpixel[1])

									bw.write("\t" + parent.nf.format(parent.userlengthlist.get(index).framenumber) + "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).totallengthpixel) + "\t"+ "\t"
											+ "\t" + "\t"+ parent.nf.format(parent.userlengthlist.get(index).totallengthreal)
											+ "\t" + "\t" + parent.nf.format(parent.userlengthlist.get(index).seedid)
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).currentpointpixel[0])
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).currentpointpixel[1])
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).currentpointreal[0])
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).currentpointreal[1])
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).lengthpixelperframe)
											+ "\t" + "\t"
											+ parent.nf.format(parent.userlengthlist.get(index).lengthrealperframe)
											+ "\t" + "\t"
											+ parent.nf.format(parent.calibration[0])  
											+ "\t" + "\t"
											+ parent.nf.format(parent.calibration[1])  
											+ "\t" + "\t"
											+ parent.nf.format(parent.calibration[2]) + 
											
											"\n");

							}

						}
						bw.close();
						fw.close();

					} catch (IOException e) {
					}
				}
			
			for (int index = 0; index < parent.userlengthlist.size(); ++index) {

				double[] landt = { parent.userlengthlist.get(index).totallengthpixel,
						parent.userlengthlist.get(index).framenumber, parent.userlengthlist.get(index).seedid };
				parent.lengthtimeuser.add(landt);

				rtAll.incrementCounter();
				rtAll.addValue("FrameNumber", parent.userlengthlist.get(index).framenumber);
				rtAll.addValue("Total Length (pixel)", parent.userlengthlist.get(index).totallengthpixel);
				rtAll.addValue("Total Length (real)", parent.userlengthlist.get(index).totallengthreal);
				rtAll.addValue("Track iD", parent.userlengthlist.get(index).seedid);
				rtAll.addValue("CurrentPosition X (px units)", parent.userlengthlist.get(index).currentpointpixel[0]);
				rtAll.addValue("CurrentPosition Y (px units)", parent.userlengthlist.get(index).currentpointpixel[1]);
				rtAll.addValue("CurrentPosition X (real units)", parent.userlengthlist.get(index).currentpointreal[0]);
				rtAll.addValue("CurrentPosition Y (real units)", parent.userlengthlist.get(index).currentpointreal[1]);
				rtAll.addValue("Length per frame (px units)", parent.userlengthlist.get(index).lengthpixelperframe);
				rtAll.addValue("Length per frame (real units)", parent.userlengthlist.get(index).lengthrealperframe);

			}

		}

		rtAll.show("Start and End of MT");
		if (parent.lengthtimestart != null)
			parent.lengthtime = parent.lengthtimestart;
		else
			parent.lengthtime = parent.lengthtimeend;
		if (parent.analyzekymo) {
			double lengthcheckstart = 0;
			double lengthcheckend = 0;
			if (parent.lengthtimestart != null) {

				parent.lengthtime = parent.lengthtimestart;
				for (int index = 0; index < parent.lengthtimestart.size(); ++index) {

					int time = (int) parent.lengthtimestart.get(index)[1];

					lengthcheckstart += parent.lengthtimestart.get(index)[0];

					for (int secindex = 0; secindex < parent.lengthKymo.size(); ++secindex) {

						for (int accountindex = 0; accountindex < parent.Accountedframes.size(); ++accountindex) {

							if ((int) parent.lengthKymo.get(secindex)[1] == time
									&& parent.Accountedframes.get(accountindex) == time) {

								float delta = (float) (parent.lengthtimestart.get(index)[0]
										- parent.lengthKymo.get(secindex)[0]);
								float[] cudeltadeltaLstart = { delta, time };
								parent.deltadstart.add(cudeltadeltaLstart);

							}
						}

					}
				}

				/********
				 * The part below removes the duplicate entries in the array dor
				 * the time co-ordinate
				 ********/

				int j = 0;

				for (int index = 0; index < parent.deltadstart.size() - 1; ++index) {

					j = index + 1;

					while (j < parent.deltadstart.size()) {

						if (parent.deltadstart.get(index)[1] == parent.deltadstart.get(j)[1]) {

							parent.deltadstart.remove(index);
						}

						else {
							++j;

						}

					}
				}

				for (int index = 0; index < parent.deltadstart.size(); ++index) {

					for (int secindex = 0; secindex < parent.Accountedframes.size(); ++secindex) {

						if ((int) parent.deltadstart.get(index)[1] == parent.Accountedframes.get(secindex)) {

							parent.netdeltadstart += Math.abs(parent.deltadstart.get(index)[0]);

						}

					}

				}
				parent.deltad = parent.deltadstart;

			}

			if (parent.lengthtimeend != null) {
				parent.lengthtime = parent.lengthtimeend;
				for (int index = 0; index < parent.lengthtimeend.size(); ++index) {

					int time = (int) parent.lengthtimeend.get(index)[1];

					lengthcheckend += parent.lengthtimeend.get(index)[0];

					for (int secindex = 0; secindex < parent.lengthKymo.size(); ++secindex) {

						for (int accountindex = 0; accountindex < parent.Accountedframes.size(); ++accountindex) {

							if ((int) parent.lengthKymo.get(secindex)[1] == time
									&& parent.Accountedframes.get(accountindex) == time) {

								if ((int) parent.lengthKymo.get(secindex)[1] == time
										&& parent.Accountedframes.get(accountindex) == time) {

									float delta = (float) (parent.lengthtimeend.get(index)[0]
											- parent.lengthKymo.get(secindex)[0]);
									float[] cudeltadeltaLend = { delta, time };
									parent.deltadend.add(cudeltadeltaLend);
								}
							}

						}

					}
				}
				/********
				 * The part below removes the duplicate entries in the array dor
				 * the time co-ordinate
				 ********/

				int j = 0;

				for (int index = 0; index < parent.deltadend.size() - 1; ++index) {

					j = index + 1;

					while (j < parent.deltadend.size()) {

						if (parent.deltadend.get(index)[1] == parent.deltadend.get(j)[1]) {

							parent.deltadend.remove(index);
						}

						else {
							++j;

						}

					}
				}

				for (int index = 0; index < parent.deltadend.size(); ++index) {

					for (int secindex = 0; secindex < parent.Accountedframes.size(); ++secindex) {

						if ((int) parent.deltadend.get(index)[1] == parent.Accountedframes.get(secindex)) {

							parent.netdeltadend += Math.abs(parent.deltadend.get(index)[0]);

						}

					}

				}

				parent.deltad = parent.deltadend;
			}

			FileWriter deltaw;
			File fichierKydel = new File(parent.usefolder + "//" + parent.addToName + "MTtracker-deltad" + ".txt");

			try {
				deltaw = new FileWriter(fichierKydel);
				BufferedWriter bdeltaw = new BufferedWriter(deltaw);

				bdeltaw.write("\ttime\tDeltad(pixel units)\n");

				for (int index = 0; index < parent.deltad.size(); ++index) {
					bdeltaw.write("\t" + parent.deltad.get(index)[1] + "\t" + parent.deltad.get(index)[0] + "\n");

				}

				bdeltaw.close();
				deltaw.close();
			}

			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int index = 0; index < parent.deltad.size(); ++index) {

				for (int secindex = 0; secindex < parent.Accountedframes.size(); ++secindex) {

					if ((int) parent.deltad.get(index)[1] == parent.Accountedframes.get(secindex)) {

						parent.netdeltad += Math.abs(parent.deltad.get(index)[0]);

					}

				}

			}
			parent.netdeltad /= parent.deltad.size();

			if (parent.netdeltad > parent.deltadcutoff) {

				parent.redo = true;

			} else
				parent.redo = false;

		}
		if (parent.Kymoimg != null) {
			ImagePlus newimp = parent.Kymoimp.duplicate();
			for (int index = 0; index < parent.lengthtime.size() - 1; ++index) {

				Overlay overlay = parent.Kymoimp.getOverlay();
				if (overlay == null) {
					overlay = new Overlay();
					parent.Kymoimp.setOverlay(overlay);
				}
				Line newline = new Line(parent.lengthtime.get(index)[0], parent.lengthtime.get(index)[1],
						parent.lengthtime.get(index + 1)[0], parent.lengthtime.get(index + 1)[1]);
				newline.setFillColor(parent.colorDraw);

				overlay.add(newline);

				parent.Kymoimp.setOverlay(overlay);
				RoiManager roimanager = RoiManager.getInstance();

				roimanager.addRoi(newline);

			}

			parent.Kymoimp.show();
		}
		parent.displaystack();
		if (parent.displayoverlay) {
			parent.prestack.deleteLastSlice();
			new ImagePlus(parent.addToName + "Overlays", parent.prestack).show();
		}

		DisplayID.displayseeds(parent.addToName,Views.hyperSlice(parent.originalimg, 2, 0), parent.IDALL);
	}

}
