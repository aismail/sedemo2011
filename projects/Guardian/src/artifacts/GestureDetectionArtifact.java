package artifacts;

import java.util.LinkedList;

import processing.core.PVector;

import utils.HandCoordinates;
import utils.MathUtils;
import utils.Skeleton;
import cartago.Artifact;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class GestureDetectionArtifact extends Artifact {
	
	/*
	 * Determines the gesture made by the user, based on the hand coordinate history and the skeleton.
	 */
	@OPERATION void determineGesture(HandCoordinates handCoordinates, Skeleton skeleton, OpFeedbackParam<String> gesture) {
		gesture.set("None");
		
		float rightArmColinearity = MathUtils.degreeOfColinearity(skeleton.getRightShoulder().getPosition(),
					   										      skeleton.getRightElbow().getPosition(),
															      skeleton.getRightHand().getPosition());
		
		LinkedList<PVector> rightHandHistory = handCoordinates.getCoordinateHistory();
		if (rightHandHistory.size() < 3) {
			
			return;
		}
		
		float rightHandHistoryColinearity = MathUtils.degreeOfColinearity(rightHandHistory.get(0),
																	      rightHandHistory.get(1),
																	      rightHandHistory.get(2));
		
		if (rightArmColinearity > 0.55 && rightHandHistoryColinearity > 0.75) {
			gesture.set("PointingRightWithRightHand");
		}
	}
}