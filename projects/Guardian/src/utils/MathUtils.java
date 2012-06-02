package utils;

import SimpleOpenNI.XnPoint3D;
import SimpleOpenNI.XnVector3D;
import processing.core.PVector;

public class MathUtils {
	
	private static PVector convert(XnVector3D p) {
		return new PVector(p.getX(), p.getY(), p.getZ());
	}
	
	public static float length(PVector p1, PVector p2) {
		return (float) Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + 
				                 (p1.y - p2.y) * (p1.y - p2.y) + 
				                 (p1.z - p2.z) * (p1.z - p2.z));
	}
	
	public static float dotProduct(PVector p1, PVector p2,
								   PVector q1, PVector q2) {
		return (p1.x - p2.x) * (q1.x - q2.x) +
			   (p1.y - p2.y) * (q1.y - q2.y) +
			   (p1.z - p2.z) * (q1.z - q2.z);
	}
	
	public static float degreeOfColinearity(PVector p1, PVector p2, PVector p3) {
		return dotProduct(p1, p2, p2, p3) / (length(p1, p2) * length(p2, p3));
	}
	
	public static float length(XnVector3D p1, XnVector3D p2) {
		return length(convert(p1), convert(p2));
	}
	
	public static float dotProduct(XnVector3D p1, XnVector3D p2,
								   XnVector3D q1, XnVector3D q2) {
		return dotProduct(convert(p1), convert(p2), convert(q1), convert(q2));
	}
	
	public static float degreeOfColinearity(XnVector3D p1, XnVector3D p2, XnVector3D p3) {
		return degreeOfColinearity(convert(p1), convert(p2), convert(p3));
	}
}