package artifacts;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import SimpleOpenNI.XnPoint3D;
import SimpleOpenNI.XnVHandPointContext;
import SimpleOpenNI.XnVPointControl;
import processing.core.PVector;
import utils.HandCoordinates;
import utils.Kinect;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class HandTrackerArtifact extends Artifact {
	private Kinect kinect;
	private PointTracker pointTracker;
	
	private static class PointTracker extends XnVPointControl {
		private static final int MAX_POINTS = 30;
		private Map<Long, LinkedList<PVector>> handCoordinates;
		private Kinect kinect;
		
		public PointTracker(Kinect kinect) {
			this.kinect = kinect;
			handCoordinates = new HashMap<Long, LinkedList<PVector>>();
		}
		
		public void OnPointCreate(XnVHandPointContext ctx) {
			long handId = ctx.getNID();
			XnPoint3D pos = ctx.getPtPosition();
			add(handId, pos.getX(), pos.getY(), pos.getZ());
		}
		
		public void OnPointUpdate(XnVHandPointContext ctx) {
			long handId = ctx.getNID();
			XnPoint3D pos = ctx.getPtPosition();
			add(handId, pos.getX(), pos.getY(), pos.getZ());
		}
		
		public void OnPointDestroy(long handId) {
			del(handId);
		}
		
		private void add(long handId, float x, float y, float z) {
			if (!handCoordinates.containsKey(handId)) {
				handCoordinates.put(handId, new LinkedList<PVector>());
			}
			LinkedList<PVector> handCoordinateVector = handCoordinates.get(handId);
			if (handCoordinateVector.size() == MAX_POINTS)
				handCoordinateVector.removeLast();
			handCoordinateVector.push(new PVector(x, y, z));
		}
		
		private void del(long handId) {
			if (handCoordinates.containsKey(handId))
				handCoordinates.remove(handId);
		}

		public HandCoordinates getHandCoordinates() {
			HandCoordinates coordinates = new HandCoordinates();
			Map<Long, LinkedList<Point2D.Float>> result = new HashMap<Long, LinkedList<Point2D.Float>>();
			for (Map.Entry<Long, LinkedList<PVector>> entry: handCoordinates.entrySet()) {
				result.put(entry.getKey(), new LinkedList<Point2D.Float>());
				for (PVector coord: entry.getValue()) {
					PVector screenCoord = new PVector();
					kinect.getContext().convertRealWorldToProjective(coord, screenCoord);
					result.get(entry.getKey()).add(new Point2D.Float(screenCoord.x, screenCoord.y));
				}
			}
			coordinates.setCoordinates(result);
			coordinates.setCoordinates3D(handCoordinates);
			return coordinates;
		}
	}
	
	@OPERATION void initHandTracker(Kinect kinect) {
		this.kinect = kinect;
		this.pointTracker = new PointTracker(kinect);
		this.kinect.getFlowRouter().SetActive(this.pointTracker);
	}
	
	@OPERATION void trackHands(OpFeedbackParam<HandCoordinates> handCoordinates) {
		handCoordinates.set(pointTracker.getHandCoordinates());
	}
}