/*
 * Project 1
*/

import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;


public class readImage
{
  int imageCount = 1;
  double intensityBins [] = new double [26];
  double intensityMatrix [][] = new double[100][26];
  double colorCodeBins [] = new double [64];
  double colorCodeMatrix [][] = new double[100][64];

  /*Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
   * getColorCode methods are called.
  */
  public readImage()
  {
    while(imageCount < 101){
      try
      {
        // the line that reads the image file
      ///////////////////  
	///your code///
	//////////////////
      } 
//      catch (IOException e)
    catch (Exception e)
      {
        System.out.println("Error occurred when reading the file.");
      }
    }
    
    writeIntensity();
    writeColorCode();
    
  }
  
  //intensity method 

  public void getIntensity(BufferedImage image, int height, int width){

    /////////////////////
    ///your code///
    /////////////////

  }
  
  //color code method
  public void getColorCode(BufferedImage image, int height, int width){
    /////////////////////
    ///your code///
    /////////////////
  }
  
  
  ///////////////////////////////////////////////
  //add other functions you think are necessary//
  ///////////////////////////////////////////////
  
  //This method writes the contents of the colorCode matrix to a file named colorCodes.txt.
  public void writeColorCode(){
    /////////////////////
    ///your code///
    /////////////////
  }
  
  //This method writes the contents of the intensity matrix to a file called intensity.txt
  public void writeIntensity(){
    /////////////////////
    ///your code///
    /////////////////
  }
  
  public static void main(String[] args)
  {
    new readImage();
  }

}
