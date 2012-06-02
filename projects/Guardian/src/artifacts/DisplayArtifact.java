package artifacts;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JFrame;

import SimpleOpenNI.SimpleOpenNI;

import processing.core.PApplet;
import processing.core.PImage;
import utils.Constants;
import utils.HandCoordinates;
import utils.Kinect;
import utils.Skeleton;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class DisplayArtifact extends Artifact {
	private DisplayArtifactApplet applet;
	private JFrame frame;
	
	public static class DisplayArtifactApplet extends PApplet {
		/**
		 * Generated serial UID
		 */
		private static final long serialVersionUID = -4077388692790706464L;
		private PImage image;
		private Map<Long, LinkedList<Point2D.Float>> handCoordinates;
		private Skeleton skeleton;
		private boolean displayRightArrow;
		private int numTimesDisplayedRightArrow = 0;
		
		public void setup() {
			size(640, 480);
			background(250,0,0);
			smooth();
		}
		
		public void draw() {
			pushStyle();
			noFill();
			setLocation(0, 0);
			if (image != null)
				image(image, 0, 0, image.width, image.height);
			
			int idx = 0;
			if (handCoordinates != null) {
				strokeWeight(2);
				for (Long handId: handCoordinates.keySet()) {
					beginShape();
					switch(idx % 4) {
						case 0: stroke(255, 0, 0); break;
						case 1: stroke(0, 255, 0); break;
						case 2: stroke(0, 0, 255); break;
						case 3: stroke(255, 255, 0); break;
					}
					int pointNum = 0;
					for (Point2D.Float handPosition: handCoordinates.get(handId)) {
						if (pointNum == 0)
							strokeWeight(8);
						else
							strokeWeight(2);
						vertex(handPosition.x, handPosition.y);
						pointNum++;
					}
					endShape();
					idx++;
				}
			}
			popStyle();
			
			if (skeleton != null && skeleton != Constants.NULL_SKELETON)
				skeleton.draw(this);
			
			if (displayRightArrow) {
				PImage arrow = loadImage("/home/aismail/Desktop/right-arrow-inv.png");
				image(arrow, 200, 200);
				numTimesDisplayedRightArrow++;
				if (numTimesDisplayedRightArrow == 3) {
					displayRightArrow = false;
				}
			}
		}

		public void setImage(PImage image) {
			this.image = image;
		}

		public void setHandCoordinates(Map<Long, LinkedList<Point2D.Float>> handCoordinates) {
			this.handCoordinates = handCoordinates;
		}

		public void setSkeleton(Skeleton skeleton) {
			this.skeleton = skeleton;
		}
		
		public void setDisplayRightArrow() {
			this.displayRightArrow = true;
			numTimesDisplayedRightArrow = 0;
		}
		
		//--------------------------------------------------------------------------
		
		public void onNewUser(int userId)
		{
		  System.out.println("onNewUser - userId: " + userId);
		  System.out.println("  start pose detection");
		  
		  context.startPoseDetection("Psi",userId);
		}

		public void onLostUser(int userId)
		{
		  System.out.println("onLostUser - userId: " + userId);
		}

		public void onStartCalibration(int userId)
		{
		  System.out.println("onStartCalibration - userId: " + userId);
		}

		public void onEndCalibration(int userId, boolean successfull)
		{
		  System.out.println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
		  
		  if (successfull) 
		  { 
		    System.out.println("  User calibrated !!!");
		    context.startTrackingSkeleton(userId); 
		  } 
		  else 
		  { 
		    System.out.println("  Failed to calibrate user !!!");
		    System.out.println("  Start pose detection");
		    context.startPoseDetection("Psi",userId);
		  }
		}

		public void onStartPose(String pose,int userId)
		{
		  System.out.println("onStartPose - userId: " + userId + ", pose: " + pose);
		  System.out.println(" stop pose detection");
		  
		  context.stopPoseDetection(userId); 
		  context.requestCalibrationSkeleton(userId, true);
		 
		}

		public void onEndPose(String pose,int userId)
		{
		  System.out.println("onEndPose - userId: " + userId + ", pose: " + pose);
		}
		
		private SimpleOpenNI context;
		public void setKinect(Kinect kinect) {
			this.context = kinect.getContext();
		}
	}
	
	public DisplayArtifact() {
		applet = new DisplayArtifactApplet();
		frame = new JFrame("Kinect depth information");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(applet);
		frame.setSize(640, 480);
		applet.init();
		applet.start();
		frame.setVisible(true);
	}
	
	@OPERATION void getApplet(OpFeedbackParam<PApplet> applet) {
		applet.set(this.applet);
	}
	
	@OPERATION void displayDepthImage(PImage image) {
		applet.setImage(image);
	}
	
	@OPERATION void displayHandCoordinates(HandCoordinates handCoordinates) {
		applet.setHandCoordinates(handCoordinates.getCoordinates());
	}
	
	@OPERATION void displaySkeleton(Skeleton skeleton) {
		applet.setSkeleton(skeleton);
	}
	
	@OPERATION void giveKinectToDisplay(Kinect kinect) {
		applet.setKinect(kinect);
	}
	
	@OPERATION void displayRightArrow() {
		applet.setDisplayRightArrow();
	}
}