package utils;

import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnVFlowRouter;
import SimpleOpenNI.XnVSessionManager;

public class Kinect {
	private SimpleOpenNI context;
	private XnVSessionManager sessionManager;
	private XnVFlowRouter flowRouter;
	
	public SimpleOpenNI getContext() {
		return context;
	}
	public void setContext(SimpleOpenNI context) {
		this.context = context;
	}
	public XnVSessionManager getSessionManager() {
		return sessionManager;
	}
	public void setSessionManager(XnVSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	public XnVFlowRouter getFlowRouter() {
		return flowRouter;
	}
	public void setFlowRouter(XnVFlowRouter flowRouter) {
		this.flowRouter = flowRouter;
	}
}