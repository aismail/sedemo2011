consecutive_faces_seen(0).
face_identified("").
!init.

+!init: true <-
	!setup_scenario;
	+infinite;
	
	while(infinite) {
		// ======== Start kinect stuff =============
		updateKinectContext;
	
		getDepthImage(DepthImage);
		displayDepthImage(DepthImage);
		
		trackHands(HandCoordinates);
		displayHandCoordinates(HandCoordinates);
		
		trackSkeleton(Skeleton);
		displaySkeleton(Skeleton);
		
		determineGesture(HandCoordinates, Skeleton, Gesture);
		.println("Gesture = ", Gesture);
		
		if (Gesture == "PointingRightWithRightHand") {
			displayRightArrow;
			sendRightKeyToPresentation;
		}
		
		// ======== End kinect stuff =============
		
		getWebcamFrame(Frame, FaceRect);
		if (not started_face_tracking) {
			!count_faces(FaceRect);
			if (consecutive_faces_seen(F) & F > 10 & not starting_face_tracking) {
				+starting_face_tracking;
				!!start_face_tracking(Frame, FaceRect);
				!!identify_person(Frame);
				-+consecutive_faces_seen(0);
			}
			?face_identified(FaceName);
			displayImage(Frame, FaceRect, FaceName);
		} else {
			continueTrackingFace(Frame, FaceRect2);
			!count_faces(FaceRect2);
			if (consecutive_faces_seen(F) & F > 100) {
				-+consecutive_faces_seen(0);
				!!identify_person(Frame);
			}
			?face_identified(FaceName);
			displayImage(Frame, FaceRect2, FaceName);
		}
	}.
	
+!setup_scenario: true <-
	makeArtifact("webcam", "artifacts.WebcamArtifact", []);
	makeArtifact("imageStreamDisplay", "artifacts.ImageStreamArtifact", []);
	makeArtifact("faceTracker", "artifacts.FaceTrackerArtifact", []);
	makeArtifact("faceIdentifier", "artifacts.FaceIdentifierArtifact", []);
	makeArtifact("display", "artifacts.DisplayArtifact", []);
	getApplet(Applet);
	makeArtifact("kinect", "artifacts.KinectArtifact", []);
	initKinect(Applet, Kinect);
	makeArtifact("handTracker", "artifacts.HandTrackerArtifact", []);
	initHandTracker(Kinect);
	makeArtifact("skeletonTracker", "artifacts.SkeletonTrackerArtifact", []);
	initSkeletonTracker(Kinect);
	giveKinectToDisplay(Kinect);
	makeArtifact("gestureDetector", "artifacts.GestureDetectionArtifact", []);
	makeArtifact("windowManager", "artifacts.WindowManagerArtifact", []);
	.wait(2000);
	enableFaceDetection.
	
+!count_faces(FaceRect): true <-
	getNullFace(NullFace);
	if (FaceRect == NullFace) {
		-+consecutive_faces_seen(0);
	} else {
		?consecutive_faces_seen(X);
		-+consecutive_faces_seen(X+1);
	}.
	
+started_face_tracking: true <-
	disableFaceDetection.

+!start_face_tracking(Frame, FaceRect): true <-
	startTrackingFace(Frame, FaceRect);
	.wait(2000);
	+started_face_tracking.

+!identify_person(Frame): true <-
	identifyFace(Frame, Name);
	-+face_identified(Name);
	.println("Face identified: ", Name).