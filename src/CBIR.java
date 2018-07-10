
/* Project 1
*/

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CBIR extends JFrame {

	private JLabel photographLabel = new JLabel(); // container to hold a large
	private JButton[] button; // creates an array of JButtons
	private JCheckBox[] imageCheckBox = new JCheckBox[101];
	private int[] buttonOrder = new int[101]; // creates an array to keep up
												// with the image order
	private double[] imageSize = new double[101]; // keeps up with the image
													// sizes
	private JCheckBox relevanceCheckBox;
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	private Double[][] intensityMatrix = new Double[101][25];
	private Double[][] colorCodeMatrix = new Double[100][64];
	public Boolean imageRefresh = false;
	private Double[][] featureMatrix = new Double[100][89];
	private Double[] stdDev = new Double[89];
	private Double[] avgArray = new Double[89];

	private Double[][] normMatrix = new Double[100][89];
	private Double[] normWeightMatrix = new Double[89];
	private HashSet<Integer> relevantImages = new HashSet<Integer>();
	DecimalFormat formatter = new DecimalFormat("0.000000000000000000000000000000");

	private Map<Double, LinkedList<Integer>> map;
	int picNo = 0;
	int imageCount = 1; // keeps up with the number of images displayed since
						// the first page.
	int pageNo = 1;

	public static void main(String args[]) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CBIR app = new CBIR();
				app.setVisible(true);
			}
		});
	}

	public CBIR() {
		// The following lines set up the interface including the layout of the
		// buttons and JPanels.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Icon Demo: Please Select an Image");

		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();

		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(2, 3, 5, 5);

		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout2);

		panelTop.setLayout(gridLayout3);
		add(panelTop);
		add(panelBottom1);

		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		buttonPanel.setLayout(gridLayout4);

		panelTop.add(photographLabel);
		panelTop.add(buttonPanel);

		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		JButton intensity = new JButton("Intensity");
		JButton colorCode = new JButton("Color Code");
		JButton IntensityColorCode = new JButton("Intensity + ColorCode");
		relevanceCheckBox = new JCheckBox();
		relevanceCheckBox.setSelected(false);
		relevanceCheckBox.setVisible(false);
		relevanceCheckBox.setText("Relevance");

		buttonPanel.add(intensity);
		buttonPanel.add(colorCode);
		buttonPanel.add(IntensityColorCode);
		buttonPanel.add(previousPage);
		buttonPanel.add(nextPage);
		buttonPanel.add(relevanceCheckBox);

		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());
		intensity.addActionListener(new intensityHandler());
		colorCode.addActionListener(new colorCodeHandler());
		IntensityColorCode.addActionListener(new intensityColorCodeHandler());
		relevanceCheckBox.addActionListener(new relevanceCheckBoxHandler());
		setSize(1200, 750);
		// this centers the frame on the screen
		setLocationRelativeTo(null);

		button = new JButton[101];
		/*
		 * This for loop goes through the images in the database and stores them
		 * as icons and adds the images to JButtons and then to the JButton
		 * array
		 */
		for (int i = 1; i < 101; i++) {
			ImageIcon icon;
			icon = new ImageIcon(getClass().getResource(i + ".jpg"));// Code
			Image image = icon.getImage(); // transform it
			Image newimg = image.getScaledInstance(220, 128,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
			icon = new ImageIcon(newimg);  // transform it back
			
			if (icon != null) {
				button[i] = new JButton(icon);
				button[i].addActionListener(new IconButtonHandler(i, icon));
				buttonOrder[i] = i;
				imageCheckBox[i] = new JCheckBox("Relevant");
				imageCheckBox[i].setSelected(false);
				imageCheckBox[i].setVisible(false);
				imageCheckBox[i].setSize(130, 10);
				imageCheckBox[i].addActionListener(new relevantChkBoxHandler(i));
			}
		}

		for (Double[] row : intensityMatrix) {
			Arrays.fill(row, 0.0);
		}
		for (Double[] row : colorCodeMatrix) {
			Arrays.fill(row, 0.0);
		}
		for (Double[] row : featureMatrix) {
			Arrays.fill(row, 0.0);
		}
		for (Double[] row : normMatrix) {
			Arrays.fill(row, 0.0);
		}
		Arrays.fill(avgArray, 0.0);
		Arrays.fill(stdDev, 0.0);
		// Arrays.fill(normWeightMatrix,(1/(double)89));

		readImageSize();
		readIntensityFile();
		readColorCodeFile();
		createFeatureMatrix();
		displayFirstPage();
	}

	/* This method is written to refresh the images array */
	public void refreshImageArray() {
		if (!imageRefresh) {
			for (int i = 1; i < 101; i++) {
				ImageIcon icon;
				icon = new ImageIcon(getClass().getResource(i + ".jpg"));// Code
				Image image = icon.getImage(); // transform it
				Image newimg = image.getScaledInstance(220, 128,  java.awt.Image.SCALE_SMOOTH); // scale it the smooth way 
				icon = new ImageIcon(newimg);  // transform it back
				
				if (icon != null) {
					button[i] = new JButton(icon);
					button[i].addActionListener(new IconButtonHandler(i, icon));
//					button[i].resize(5, 5);
					buttonOrder[i] = i;
					imageCheckBox[i] = new JCheckBox("Relevant");
					imageCheckBox[i].setSelected(false);
					imageCheckBox[i].setVisible(false);
					imageCheckBox[i].addActionListener(new relevantChkBoxHandler(i));
				}
			}
			imageRefresh = true;
		}
	}
	
	/* This method is written to get minimum value of an double array */
	public static double getMin(Double[] d) {
		double minVal = 999999999999999.0;
		for (double val : d) {
			if (val != 0) {
				minVal = Math.min(minVal, val);
			}
		}
		return minVal;
	}

	/*
	 * This method displays the first twenty images in the panelBottom. The for
	 * loop starts at number one and gets the image number stored in the
	 * buttonOrder array and assigns the value to imageButNo. The button
	 * associated with the image is then added to panelBottom1. The for loop
	 * continues this process until twenty images are displayed in the
	 * panelBottom1
	 */
	private void displayFirstPage() {
		int imageButNo = 0;
		panelBottom1.removeAll();
		for (int i = 1; i < 21; i++) {
			imageButNo = buttonOrder[i];
			panelBottom2 = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.weighty=2;
			panelBottom2.add(button[imageButNo],c);
			c.gridy=1;
			c.gridwidth=128;
			c.weighty=0;
			c.anchor=GridBagConstraints.WEST;
			panelBottom2.add(imageCheckBox[i],c);
			panelBottom1.add(panelBottom2);
			imageCount++;
		}
		panelBottom1.revalidate();
		panelBottom1.repaint();
	}

	/* This method is written to create feature matrix of 89 bins 
	 * first 25 bins are taken from intensity matrix and remaining 64 bins 
	 * are taken from Color code matrix*/
	public void createFeatureMatrix() {

		/*
		 * calculating Feature matrix from intensity Matrix and ColorCode Matrix
		 */
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 25; j++) {
				featureMatrix[i][j] = intensityMatrix[i][j] / imageSize[i];
			}
			for (int j = 0; j < 64; j++) {
				featureMatrix[i][25 + j] = colorCodeMatrix[i][j] / imageSize[i];
			}
		}
		/* calculating average of Feature matrix */
		for (int j = 0; j < 89; j++) {
			double sum = 0.0;
			for (int i = 0; i < 100; i++) {
				sum = sum + featureMatrix[i][j];
			}
			avgArray[j] = sum / 100;
		}
		/* calculating std Deviation matrix */
		for (int j = 0; j < 89; j++) {
			double sum = 0.0;
			for (int i = 0; i < 100; i++) {
				double delta = 0.0;
				delta = featureMatrix[i][j] - avgArray[j];
				sum = sum + (delta * delta);
			}
			stdDev[j] = Math.sqrt(sum / 99);
		}
		/* Executed logic for  0 std deviation*/
		for (int j = 0; j < 89; j++) {
			if (stdDev[j] == 0 && avgArray[j] != 0) {
				double minVal = getMin(stdDev);
				stdDev[j] = 0.5 * minVal;
			}
		}
		/* calculating Normalized matrix */
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 89; j++) {
				if (stdDev[j] == 0) {
					normMatrix[i][j] = 0.0;
				} else {
					normMatrix[i][j] = (featureMatrix[i][j] - avgArray[j]) / stdDev[j];
				}
			}
		}
		/* Printing Normalized matrix */
//		for (int i = 0; i < 2; i++) {
//			for (int j = 0; j < 89; j++) {
//				System.out.print("Bin" + j + "Feature Matrix " + i + " :" + formatter.format(featureMatrix[i][j]));
//				System.out.print(" ~~~~~ stdDev " + i + " :" + formatter.format(stdDev[j]));
//				System.out.println(" ~~~~~ normMatrix " + i + " :" + formatter.format(normMatrix[i][j]));
//			}
//		}
	}

	/* This method is written to calculate normalized weight from the selected images */
	public void calculateNormalizedWgt() {

		/* This there is one one image selected then normalized weight is 1/89 */
		if (relevantImages.size() == 1) {
			for (int i = 0; i < 89; i++) {
				normWeightMatrix[i] = 1 / (double) 89;
				System.out.println("New Normalized Weight:" + formatter.format(normWeightMatrix[i]));
			}
		} else {

			/* if more than one images are selected */

			Arrays.fill(stdDev, 0.0);
			Arrays.fill(avgArray, 0.0);
			double updatedWeightSum = 0.0;
//			double stdDevSum = 0.0;

			for (int i = 0; i < 89; i++) {
				double sum = 0.0;
				double mean = 0.0;
				double stdDevVal = 0.0;

				/* Calculating Sum of all feature values for the bin in current iteration */
				System.out.print(" Normalized Value ");
				for (int j : relevantImages) {
					System.out.print("Bin " + i + " --> " + normMatrix[j - 1][i]);
					sum = sum + normMatrix[j - 1][i];
				}

				/* Calculating Average of all feature values for the bin in current iteration */
				System.out.print(" Sum --> " + formatter.format(sum));
				mean = sum / relevantImages.size();

				avgArray[i] = mean;
				System.out.print(" Mean --> " + formatter.format(avgArray[i]));

				sum = 0.0;
				/* Calculating sum of (delta)^2, where 
				 * delta = bin_value-Average for the 
				 * bin in current iteration */
				for (int j : relevantImages) {
					double delta = 0.0;
					delta = normMatrix[j - 1][i] - mean;
					System.out.print(" Delta --> " + formatter.format(delta));
					sum = sum + (delta * delta);
				}
				System.out.print(" Deltasum --> " + formatter.format(sum) + " Number of Images: " + (relevantImages.size() - 1));
				stdDevVal = sum / (double) (relevantImages.size() - 1);
				stdDev[i] = Math.sqrt(stdDevVal);
				System.out.println("Old stdDev --> " + formatter.format(stdDevVal));
			}
			double newStdVal = .5 * getMin(stdDev);
			/* Calculating normalized weight with the std logic for zero values */
			for (int i = 0; i < 89; i++) {

				double updatedWeight = 0.0;
				if (stdDev[i] == 0 && newStdVal == 0) {
					updatedWeight = 0.0;
				} else if (stdDev[i] == 0 && avgArray[i] == 0) {
					updatedWeight = 0.0;
				} else if (stdDev[i] == 0 && avgArray[i] != 0) {
					stdDev[i] = newStdVal;
					updatedWeight = 1 / stdDev[i];
				} else {
					updatedWeight = 1 / stdDev[i];
				}
				System.out.println("BIN " + i + " : New stdDev --> " + formatter.format(stdDev[i])
						+ " updatedWeight --> " + formatter.format(updatedWeight));
				normWeightMatrix[i] = updatedWeight;
				updatedWeightSum = updatedWeightSum + updatedWeight;
			}
			for (int i = 0; i < 89; i++) {
				normWeightMatrix[i] = normWeightMatrix[i] / updatedWeightSum;
				System.out
						.println("BIN " + i + " : New Normalized Weight --> " + formatter.format(normWeightMatrix[i]));
			}
		}
	}

	/* This method is written to refresh the relevant check boxes */
	public void setRelevance(Boolean displayFirstPage,Boolean showRelevantButton, Boolean showRelevanceButton) {

		relevanceCheckBox.setVisible(showRelevanceButton);
		for (int i = 1; i < 101; i++) {
			
			imageCheckBox[i].setVisible(showRelevantButton);
			if (showRelevantButton){
				imageCheckBox[i].setSelected(relevantImages.contains(buttonOrder[i]));
			}
		}
		if (displayFirstPage) {
			imageCount = 1;
			displayFirstPage();
		}
	}

	/* method to read images and store image size in imageSize array */
	public void readImageSize() {
		BufferedImage image = null;

		for (int i = 0; i < 100; i++) {
			try {

				/* reading image file in loop */
				image = ImageIO.read(getClass().getResource((i + 1) + ".jpg"));

				/*
				 * imagesize is calculated by multiplying height & width of the
				 * image
				 */
				imageSize[i] = (double) (image.getHeight() * image.getWidth());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void calculateFeatureDistance(int pic) {

		ArrayList<Double> distance = new ArrayList<Double>();
		HashMap<Double, LinkedList<Integer>> map = new HashMap<Double, LinkedList<Integer>>();
		double d = 0;

		System.out.println("Calculating Feature Distance");
		/* loop to iterate through all the 100 images */
		for (int i = 0; i < 100; i++) {
			d = 0;
			/* loop to iterate through all the feature bins */
			for (int j = 0; j < 89; j++) {
				if (i == 1) {
					System.out.print("Normalized Weight for bin" + j + "->" + normWeightMatrix[j]);
					System.out.print("Selected Pic feature value" + j + "->" + normMatrix[pic][j]);
					System.out.println("Target Pic feature value" + j + "->" + normMatrix[i][j]);
				}
				d = d + Math.abs(normWeightMatrix[j] * (normMatrix[pic][j] - normMatrix[i][j]));
			}
			System.out.println(pic + "image distance with image" + i + " distance value" + formatter.format(d));
			/*
			 * avoiding duplication of distances by checking if distances is
			 * already stored
			 */
			if (!distance.contains(d)) {
				distance.add(d);
			}

			/*
			 * loading list of images with their distance from the selected
			 * image
			 */
			if (!map.containsKey(d)) {
				LinkedList<Integer> imageList = new LinkedList<Integer>();
				imageList.add(i + 1);
				map.put(d, imageList);
			} else {
				LinkedList<Integer> imageList = map.get(d);
				imageList.add(i + 1);
				map.put(d, imageList);
			}
		}
		/* Sorting the distance collection */
		Collections.sort(distance);

		/*
		 * Iterating through the map in order of ascending distance values &
		 * updating the button order with image numbers
		 */
		for (int z = 0; z < distance.size(); z++) {
			if (map.containsKey(distance.get(z))) {
				for (int m = 0; m < map.get(distance.get(z)).size(); m++) {
					// System.out.println(" intensity Value :
					// "+distance.get(z)+" Image Name:
					// "+map.get(distance.get(z)).get(m));
					buttonOrder[z + 1] = map.get(distance.get(z)).get(m);

					if (relevantImages.contains(z + 1)) {
						imageCheckBox[z + 1].setSelected(true);
					} else {
						imageCheckBox[z + 1].setSelected(false);
					}
				}
			}
		}
		imageRefresh = false;
	}

	/*
	 * Method to calculate Manhattan distance between selected pic and all the
	 * other images using Intensity histogram bins . The distance will be stored
	 * in an arrayList of double datatype. the image names are stored in a
	 * HashMap map along with their distances from the selected image. These
	 * distances are sorted in ascending order and the array object buttonOrder
	 * is updated with the new sequence of images.
	 */
	public void calculateIntensityDistance(int pic) {

		ArrayList<Double> distance = new ArrayList<Double>();
		HashMap<Double, LinkedList<Integer>> map = new HashMap<Double, LinkedList<Integer>>();
		double d = 0;
		double picSize = imageSize[pic];

		/* loop to iterate through all the 100 images */
		for (int i = 0; i < 100; i++) {
			d = 0;
			/* loop to iterate through all the intensity histogram bins */
			for (int j = 0; j < 25; j++) {
				d = d + Math.abs((intensityMatrix[pic][j] / picSize) - (intensityMatrix[i][j] / imageSize[i]));
			}
			/*
			 * avoiding duplication of distances by checking if distances is
			 * already stored
			 */
			if (!distance.contains(d)) {
				distance.add(d);
			}

			/*
			 * loading list of images with their distance from the selected
			 * image
			 */
			if (!map.containsKey(d)) {
				LinkedList<Integer> imageList = new LinkedList<Integer>();
				imageList.add(i + 1);
				map.put(d, imageList);
			} else {
				LinkedList<Integer> imageList = map.get(d);
				imageList.add(i + 1);
				map.put(d, imageList);
			}
		}
		/* Sorting the distance collection */
		Collections.sort(distance);

		/*
		 * Iterating through the map in order of ascending distance values &
		 * updating the button order with image numbers
		 */
		for (int z = 0; z < distance.size(); z++) {
			if (map.containsKey(distance.get(z))) {
				for (int m = 0; m < map.get(distance.get(z)).size(); m++) {
					// System.out.println(" intensity Value :
					// "+distance.get(z)+" Image Name:
					// "+map.get(distance.get(z)).get(m));
					buttonOrder[z + 1] = map.get(distance.get(z)).get(m);
				}
			}
		}
		// for(int n=1;n<=buttonOrder.length-1;n++){
		// System.out.println(buttonOrder[n]);
		// }
		imageRefresh = false;

	}

	/*
	 * Method to calculate Manhattan distance between selected pic and all the
	 * other images using ColoCode histogram bins . The distance will be stored
	 * in an arrayList of double datatype. the image names are stored in a
	 * HashMap map along with their distances from the selected image. These
	 * distances are sorted in ascending order and the array object buttonOrder
	 * is updated with the new sequence of images.
	 */
	public void calculateColorCodeDistance(int pic) {

		ArrayList<Double> distance = new ArrayList<Double>();
		HashMap<Double, LinkedList<Integer>> map = new HashMap<Double, LinkedList<Integer>>();
		double d = 0;
		double picSize = imageSize[pic];

		/* loop to iterate through all the 100 images */
		for (int i = 0; i < 100; i++) {
			d = 0;
			/* loop to iterate through all the color code histogram bins */
			for (int j = 0; j < 64; j++) {
				d = d + Math.abs((colorCodeMatrix[pic][j] / picSize) - (colorCodeMatrix[i][j] / imageSize[i]));
			}
			/*
			 * avoiding duplication of distances by checking if distances is
			 * already stored
			 */
			if (!distance.contains(d)) {
				distance.add(d);
			}

			/*
			 * loading list of images with their distance from the selected
			 * image
			 */
			if (!map.containsKey(d)) {
				LinkedList<Integer> imageList = new LinkedList<Integer>();
				imageList.add(i + 1);
				map.put(d, imageList);
			} else {
				LinkedList<Integer> imageList = map.get(d);
				imageList.add(i + 1);
				map.put(d, imageList);
			}
		}

		/* Sorting the distance collection */
		Collections.sort(distance);

		/*
		 * Iterating through the map in order of ascending distance values &
		 * updating the button order with image numbers
		 */
		for (int z = 0; z < distance.size(); z++) {
			if (map.containsKey(distance.get(z))) {
				for (int m = 0; m < map.get(distance.get(z)).size(); m++) {
					// System.out.println(" intensity Value :
					// "+distance.get(z)+" Image Name:
					// "+map.get(distance.get(z)).get(m));
					buttonOrder[z + 1] = map.get(distance.get(z)).get(m);
				}
			}
		}
		// for(int n=1;n<=buttonOrder.length-1;n++){
		//// System.out.println(buttonOrder[n]);
		// }
		imageRefresh = false;

	}

	/*
	 * This method opens the intensity text file containing the intensity matrix
	 * with the histogram bin values for each image. The contents of the matrix
	 * are processed and stored in a two dimensional array called
	 * intensityMatrix.
	 */
	public void readIntensityFile() {

		BufferedImage image1 = null;

		for (int k = 0; k < 100; k++) {
			try {
				// image1=ImageIO.read(new File("./resources/"+(k+1)+".jpg"));
				image1 = ImageIO.read(getClass().getResource((k + 1) + ".jpg"));

				for (int i = 0; i < image1.getWidth(); i++) {
					for (int j = 0; j < image1.getHeight(); j++) {

						int pixel = image1.getRGB(i, j);
						int red = (pixel >> 16) & 0xff;
						int green = (pixel >> 8) & 0xff;
						int blue = (pixel) & 0xff;

						double intensity = (0.299 * red) + (0.587 * green) + (0.114 * blue);
						// System.out.print("------- >Intensity "+intensity);

						int index = (int) ((intensity / 10));
						if (index == 25) {
							index = 24;
						}

						if (intensityMatrix[k][index] == null) {
							intensityMatrix[k][index] = (double) 1;
						} else {
							intensityMatrix[k][index] = intensityMatrix[k][index] + (double) 1;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// for(int i=0;i<100;i++){
		// System.out.println(intensityMatrix[i][24]);
		// }

	}

	/*
	 * This method opens the color code text file containing the color code
	 * matrix with the histogram bin values for each image. The contents of the
	 * matrix are processed and stored in a two dimensional array called
	 * colorCodeMatrix.
	 */
	private void readColorCodeFile() {
		BufferedImage image1 = null;

		for (int k = 0; k < 100; k++) {
			try {
				// image1=ImageIO.read(new File("./resources/"+(k+1)+".jpg"));
				image1 = ImageIO.read(getClass().getResource((k + 1) + ".jpg"));

				for (int i = 0; i < image1.getWidth(); i++) {
					for (int j = 0; j < image1.getHeight(); j++) {

						int pixel = image1.getRGB(i, j);
						int red = (pixel >> 16) & 0xff;
						int green = (pixel >> 8) & 0xff;
						int blue = (pixel) & 0xff;

						red = (red >> 6) & 0xff;
						green = (green >> 6) & 0xff;
						blue = (blue >> 6) & 0xff;

						double intensity = (0 << 6) | (red << 4) | (green << 2) | (blue);
						// System.out.print("------- >Intensity "+intensity);

						int index = (int) intensity;

						if (colorCodeMatrix[k][index] == null) {
							colorCodeMatrix[k][index] = (double) 1;
						} else {
							colorCodeMatrix[k][index] = colorCodeMatrix[k][index] + (double) 1;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*	 
	 * This class implements an ActionListener for each relevant check box button.
	 *  When an check box button is clicked, the image on the the button is added 
	 *  to the list of relevant images	 */
	private class relevantChkBoxHandler implements ActionListener {

		int picNo = 0;

		public relevantChkBoxHandler(int value) {
			picNo = value;
		}

		public void actionPerformed(ActionEvent e) {
			System.out.println("inside relevantChkBoxHandler");
			if (imageCheckBox[picNo].isSelected()) {
				relevantImages.add(buttonOrder[picNo]);
			} else {
				relevantImages.remove(buttonOrder[picNo]);
			}
			for (int pic : relevantImages) {
				System.out.println("selected Images" + pic);
			}
		}
	}


	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the image on the the button is added to the
	 * photographLabel and the picNo is set to the image number selected and
	 * being displayed.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		ImageIcon iconUsed;

		IconButtonHandler(int i, ImageIcon j) {
			pNo = i;
			iconUsed = j; // sets the icon to the one used in the button
		}

		public void actionPerformed(ActionEvent e) {
			photographLabel.setIcon(iconUsed);
			picNo = pNo;
			relevantImages.clear();
			setRelevance(false,false,false);
		}
	}

	/*
	 * This class implements an ActionListener for the nextPageButton. The last
	 * image number to be displayed is set to the current image count plus 20.
	 * If the endImage number equals 101, then the next page button does not
	 * display any new images because there are only 100 images to be displayed.
	 * The first picture on the next page is the image located in the
	 * buttonOrder array at the imageCount
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if (endImage <= 101) {
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {

					imageButNo = buttonOrder[i];
					panelBottom2 = new JPanel(new GridBagLayout());
					GridBagConstraints c = new GridBagConstraints();
					c.weighty=2;
					panelBottom2.add(button[imageButNo],c);
					c.gridy=1;
					c.gridwidth=128;
					c.weighty=0;
					c.anchor=GridBagConstraints.LINE_START;
					panelBottom2.add(imageCheckBox[i],c);
					panelBottom1.add(panelBottom2);

					// panelBottom1.add(button[imageButNo]);
					imageCount++;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}

	}

	/*
	 * This class implements an ActionListener for the previousPageButton. The
	 * last image number to be displayed is set to the current image count minus
	 * 40. If the endImage number is less than 1, then the previous page button
	 * does not display any new images because the starting image is 1. The
	 * first picture on the next page is the image located in the buttonOrder
	 * array at the imageCount
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if (startImage >= 1) {
				panelBottom1.removeAll();
				/*
				 * The for loop goes through the buttonOrder array starting with
				 * the startImage value and retrieves the image at that place
				 * and then adds the button to the panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					panelBottom2 = new JPanel(new GridBagLayout());
					GridBagConstraints c = new GridBagConstraints();
					c.weighty=2;
					panelBottom2.add(button[imageButNo],c);
					c.gridy=1;
					c.gridwidth=128;
					c.weighty=0;
					c.anchor=GridBagConstraints.LINE_START;
					panelBottom2.add(imageCheckBox[i],c);
					panelBottom1.add(panelBottom2);
					// panelBottom1.add(button[imageButNo]);
					imageCount--;
				}
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}
		}		
	}

	/*
	 * This class implements an ActionListener when the user selects the
	 * intensityHandler button. The image number that the user would like to
	 * find similar images for is stored in the variable pic. pic takes the
	 * image number associated with the image selected and subtracts one to
	 * account for the fact that the intensityMatrix starts with zero and not
	 * one. The size of the image is retrieved from the imageSize array. The
	 * selected image's intensity bin values are compared to all the other
	 * image's intensity bin values and a score is determined for how well the
	 * images compare. The images are then arranged from most similar to the
	 * least.
	 */
	private class intensityHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int pic = (picNo - 1);

			/* start of logic for calculating distance */

			if (pic >= 0 && pic <= 99) {
				refreshImageArray();
				calculateIntensityDistance(pic);
			} else {
				System.out.print("no pic selected");
			}
			imageCount = 1;
			setRelevance(false,false,false);
			displayFirstPage();

			/* start of logic for re-paint the panel */

		}
	} // was missing

	/*
	 * This class implements an ActionListener when the user selects the
	 * colorCode button. The image number that the user would like to find
	 * similar images for is stored in the variable pic. pic takes the image
	 * number associated with the image selected and subtracts one to account
	 * for the fact that the intensityMatrix starts with zero and not one. The
	 * size of the image is retrieved from the imageSize array. The selected
	 * image's intensity bin values are compared to all the other image's
	 * intensity bin values and a score is determined for how well the images
	 * compare. The images are then arranged from most similar to the least.
	 */
	private class colorCodeHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int pic = (picNo - 1);
			// double picSize = imageSize[pic];

			/* start of logic for calculating distance */

			if (pic >= 0 && pic <= 99) {
				refreshImageArray();
				calculateColorCodeDistance(pic);
			} else {
				System.out.print("no pic selected");
			}
			imageCount = 1;
			setRelevance(false,false,false);
			displayFirstPage();
		}
	}// was missing

	/*
	 * This class implements an ActionListener when the user selects the
	 * Intensity ColorCode button. 
	 */
	private class intensityColorCodeHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int pic = (picNo - 1);
			if (pic >= 0 && pic <= 99) {
				refreshImageArray();
				calculateNormalizedWgt();
				calculateFeatureDistance(pic);
			} else {
				System.out.print("no pic selected");
			}
			imageCount = 1;
			setRelevance(true,relevanceCheckBox.isSelected(), true);
		}
	}// was missing

	
	/* This class implements an ActionListener when the user selects the
	 * Relevance check box button. */	
	private class relevanceCheckBoxHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// relevantImages.clear();
			System.out.println(relevanceCheckBox.isSelected());
			if(relevanceCheckBox.isSelected()){
				setRelevance(true,true,true);				
			}else{
				setRelevance(true,false,true);				
			}
		}
	}// was missing
}// was missing