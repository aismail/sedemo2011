package artifacts;

import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnVFlowRouter;
import SimpleOpenNI.XnVSessionManager;
import processing.core.PApplet;
import processing.core.PImage;
import utils.Kinect;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class KinectArtifact extends Artifact {
	private Kinect kinect;
	private PApplet applet;
	
	@OPERATION void initKinect(PApplet applet, OpFeedbackParam<Kinect> kinect) {
		this.applet = applet;
		
		SimpleOpenNI  context;
		XnVSessionManager sessionManager;
		XnVFlowRouter flowRouter;
		
		context = new SimpleOpenNI(applet, SimpleOpenNI.RUN_MODE_MULTI_THREADED);
		context.setMirror(true); 
		context.enableDepth();
		context.enableGesture();
		context.enableHands();
		context.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
		context.start();
		
		sessionManager = context.createSessionManager("Click,Wave", "RaiseHand");
		flowRouter = new XnVFlowRouter();
		sessionManager.AddListener(flowRouter);
		
		this.kinect = new Kinect();
		this.kinect.setContext(context);
		this.kinect.setFlowRouter(flowRouter);
		this.kinect.setSessionManager(sessionManager);
		kinect.set(this.kinect);
	}
	
	@OPERATION void updateKinectContext() {
		kinect.getContext().update();
		kinect.getContext().update(kinect.getSessionManager());
	}
	
	@OPERATION void getDepthImage(OpFeedbackParam<PImage> image) {
		image.set(kinect.getContext().depthImage());
	}
}