package artifacts;

import SimpleOpenNI.IntVector;
import utils.Constants;
import utils.Kinect;
import utils.Skeleton;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class SkeletonTrackerArtifact extends Artifact {
	private Kinect kinect;
	
	@OPERATION void initSkeletonTracker(Kinect kinect) {
		this.kinect = kinect;
	}
	
	@OPERATION void trackSkeleton(OpFeedbackParam<Skeleton> skeleton) {		
		IntVector users = new IntVector();
		kinect.getContext().getUsers(users);
		
		if (users.size() > 0) {
			int userId = users.get(0);
			if (kinect.getContext().isTrackingSkeleton(userId)) {
				skeleton.set(new Skeleton(kinect, userId));
			} else {
				skeleton.set(Constants.NULL_SKELETON);
			}
		} else {
			skeleton.set(Constants.NULL_SKELETON);
		}
	}
}