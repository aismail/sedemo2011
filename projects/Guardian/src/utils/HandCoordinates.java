package utils;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Map;

import processing.core.PVector;

public class HandCoordinates {
	
	private Map<Long, LinkedList<Point2D.Float>> coordinates;
	private Map<Long, LinkedList<PVector>> coordinates3D;

	public Map<Long, LinkedList<Point2D.Float>> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(Map<Long, LinkedList<Point2D.Float>> coordinates) {
		this.coordinates = coordinates;
	}

	public void setCoordinates3D(Map<Long, LinkedList<PVector>> coordinates3d) {
		coordinates3D = coordinates3d;
	}
	
	public LinkedList<PVector> getCoordinateHistory() {
		LinkedList<PVector> listMax = new LinkedList<PVector>();
		
		for (Map.Entry<Long, LinkedList<PVector>> entry: coordinates3D.entrySet()) {
			if (entry.getValue().size() > listMax.size())
				listMax = entry.getValue();
		}
		
		return listMax;
	}
}