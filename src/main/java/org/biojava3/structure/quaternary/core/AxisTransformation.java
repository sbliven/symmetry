package org.biojava3.structure.quaternary.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.biojava3.structure.quaternary.geometry.MomentsOfInertia;
import org.biojava3.structure.quaternary.geometry.SuperPosition;

public class AxisTransformation {
	private static Vector3d X_AXIS = new Vector3d(1,0,0);
	private static Vector3d Y_AXIS = new Vector3d(0,1,0);
	private static Vector3d Z_AXIS = new Vector3d(0,0,1);

	private Subunits subunits = null;
	private RotationGroup rotationGroup = null;

	private Matrix4d transformationMatrix = new Matrix4d();
	private Matrix4d reverseTransformationMatrix = new Matrix4d();
	private Vector3d principalRotationAxis = new Vector3d();
	private Vector3d referenceAxis = new Vector3d();
	private Vector3d[] principalAxesOfInertia = null;

	private Vector3d minBoundary = new Vector3d();
	private Vector3d maxBoundary = new Vector3d();
	private double xyRadiusMax = Double.MIN_VALUE;

	boolean modified = true;

	public AxisTransformation(Subunits subunits, RotationGroup rotationGroup) {
		this.subunits = subunits;
		this.rotationGroup = rotationGroup;
		if (subunits == null) {
			throw new IllegalArgumentException("AxisTransformation: Subunits are null");
		} else if (rotationGroup == null) {
			throw new IllegalArgumentException("AxisTransformation: Subunits are null");
		} else if (subunits.getSubunitCount() == 0) {
			throw new IllegalArgumentException("AxisTransformation: Subunits is empty");
		} else if (rotationGroup.getOrder() == 0) {
			throw new IllegalArgumentException("AxisTransformation: RotationGroup is empty");
		}
	}

	public Matrix4d getTransformation() {
		run();   
		return transformationMatrix;
	}
	
	public Matrix3d getRotationMatrix() {
		run();
		Matrix3d m = new Matrix3d();
		transformationMatrix.getRotationScale(m);
	    return m;
	}
	
	public Matrix4d getReverseTransformation() {
		run();   
		return reverseTransformationMatrix;
	}
	
	public Vector3d getPrincipalRotationAxis() {
		run();
		return principalRotationAxis;
	}
	
	public Vector3d getRotationReferenceAxis() {
		run();
		return referenceAxis;
	}
	
	public Vector3d[] getPrincipalAxesOfInertia() {
		run();
		return principalAxesOfInertia;
	}
	
	public Vector3d getDimension() {
		run();
		Vector3d dimension = new Vector3d();
		dimension.sub(maxBoundary, minBoundary);
		dimension.scale(0.5);
		return dimension;
	}

	/**
	 * Returns the radius for drawing the minor rotation axis in the xy-plane
	 * @return double radius in xy-plane
	 */
	public double getXYRadius() {
		run();
		return xyRadiusMax;
	}

	public Subunits getSubunits() {
		return subunits;
	}

	private void run () {
		if (modified) {
			calcPrincipalRotationAxis();
			calcPrincipalAxes();
			calcReferenceAxis();
			calcTransformation();
			calcBoundaries();
			modified = false;
		}
	}

	private Matrix4d calcTransformation() {
		if (subunits == null || subunits.getSubunitCount() == 0) {
			// if the structure doesn't contain protein subunits, i.e.,
			// a DNA structure, return the identity matrix.
			transformationMatrix.setIdentity();
		} else if (rotationGroup.getPointGroup().equals("C1")) {
			transformationMatrix = getTransformationByInertiaAxes();
//		} else if (rotationGroup.getPointGroup().equals("C2")) {
//			transformationMatrix = getTransformationByC2SymmetryAxes();;
		} else {
			transformationMatrix = getTransformationBySymmetryAxes();
			// for cyclic geometry, set a canonical view for the Z direction
			if (rotationGroup.getPointGroup().startsWith("C")) {
				setZDirection(transformationMatrix);
			}
		}

		return transformationMatrix;
	}

	private Matrix4d getTransformationBySymmetryAxes() {
		calcReferenceAxis();

		Point3d[] refPoints = new Point3d[2];
		refPoints[0] = new Point3d(principalRotationAxis);
		refPoints[1] = new Point3d(referenceAxis);


		//  y,z axis centered at the centroid of the subunits
		Point3d[] coordPoints = new Point3d[2];
		coordPoints[0] = new Point3d(Z_AXIS);
		coordPoints[1] = new Point3d(Y_AXIS);

		Matrix4d matrix = alignAxes(refPoints, coordPoints);

		calcReverseMatrix(matrix);

		return matrix;
	}

	private void calcReferenceAxis() {
		referenceAxis = getMinorRotationAxis();
		if (referenceAxis == null) {
			referenceAxis = getSubunitReferenceAxisNew();
		}
		// make sure reference axis is perpendicular
		referenceAxis.cross(principalRotationAxis, referenceAxis);
		referenceAxis.cross(referenceAxis, principalRotationAxis);
	}
	
	private Matrix4d getTransformationByC2SymmetryAxes() {
	calcReferenceAxis();
		
		Point3d[] refPoints = new Point3d[2];
		refPoints[0] = new Point3d(principalRotationAxis);
		refPoints[1] = new Point3d(referenceAxis);
		
		Point3d[] coordPoints = new Point3d[2];
		coordPoints[0] = new Point3d(Y_AXIS);
		coordPoints[1] = new Point3d(X_AXIS);
 
    	Matrix4d matrix = alignAxes(refPoints, coordPoints);  
		calcReverseMatrix(matrix);

		return matrix;
	}
	
	private Matrix4d alignAxes(Point3d[] refPoints, Point3d[] coordPoints) {
		Vector3d v1 = new Vector3d(refPoints[0]);
		Vector3d v2 = new Vector3d(coordPoints[0]);
		Matrix4d m1 = new Matrix4d();
		AxisAngle4d a = new AxisAngle4d();
		Vector3d axis = new Vector3d();
		axis.cross(v1,v2);
		a.set(axis, v1.angle(v2));
		m1.set(a);
		for (int i = 0; i < 2; i++) {
			Point3d v = refPoints[i];
			m1.transform(v);
		}
		
		v1 = new Vector3d(refPoints[1]);
		v2 = new Vector3d(coordPoints[1]);
		axis.cross(v1,v2);
		a.set(axis, v1.angle(v2));
		Matrix4d m2 = new Matrix4d();
		m2.set(a);
		for (Point3d v: refPoints) {
			m2.transform(v);
		}

		if (SuperPosition.rmsd(refPoints, coordPoints) > 0.01) {
			System.out.println("Warning: AxisTransformation: axes alignment is off. RMSD: " + SuperPosition.rmsd(refPoints, coordPoints));
		}
		m2.mul(m1);
		return m2;
	}

	private void calcReverseMatrix(Matrix4d matrix) {
		reverseTransformationMatrix.set(matrix);
		// reset translation vector
		reverseTransformationMatrix.setColumn(3, 0, 0, 0, 1);
		reverseTransformationMatrix.transpose();
		// set translation vector to centroid
		Point3d centroid = subunits.getCentroid();
		reverseTransformationMatrix.setColumn(3, centroid.x, centroid.y, centroid.z, 1);
//		System.out.println("ReverseTransformation: " + reverseTransformationMatrix);
//		System.out.println("Centroid: " + subunits.getCentroid());
	}

	/**
	 * Returns vector from largest subunit to centroid of complex
	 * TODO would it be better to use an inertia axis instead if it is orthogonal to the
	 * principal rotation axis??
	 * @return
	 */	
	private Vector3d getSubunitReferenceAxisNew() {
		if (rotationGroup.getPointGroup().equals("C2")) {
			Vector3d vr = new Vector3d(subunits.getCentroid());
			vr.sub(subunits.getOriginalCenters().get(0));
			vr.normalize();
			return vr;
		}		
		
		Vector3d vmin = null;
		double dotMin = 1.0;
		for (Vector3d v: principalAxesOfInertia) {
			if (Math.abs(principalRotationAxis.dot(v)) < dotMin) {
				dotMin = Math.abs(principalRotationAxis.dot(v));
				vmin = new Vector3d(v);
			}
		}
		return vmin;
	}


	/**
	 * Returns a transformation matrix for prisms to draw for Cn structures.
	 * The center in this matrix is the geometric center, rather then the centroid
	 * In Cn structures those are usually not the same.
	 * @return
	 */
	public Matrix4d calcPrismTransformation() {
		run();
	    Matrix4d geometricCentered = new Matrix4d(reverseTransformationMatrix);
		// reset translation vector
		geometricCentered.setColumn(3, 0, 0, 0, 1);
		// set translation vector to centroid
		Point3d geometricCenter = calcGeometricCenter();
		
		geometricCentered.setColumn(3, geometricCenter.x, geometricCenter.y, geometricCenter.z, 1);
//		System.out.println("GeometricCentered: " + geometricCentered);
//		System.out.println("Centroid: " + subunits.getCentroid());
		return geometricCentered;
	}

	public Point3d calcGeometricCenter() {
		Point3d geometricCenter = new Point3d();
	
//		if (rotationGroup.getPointGroup().equals("C1")) {
		if (rotationGroup.getPointGroup().startsWith("C")) {
			// geometric center not set at all: 2ZP9 ok, 2X6A misaligned
			// w/ geometric center: 2X6A ok, 2ZP9  misaligned
			geometricCenter.set(getDimension());
			geometricCenter.add(minBoundary);
			
			// TODO does this transformation include the translational component??
			transformationMatrix.transform(geometricCenter);
		}
		
		geometricCenter.add(subunits.getCentroid());
		return geometricCenter;
	}
	
	public Point3d getCentroid() {
		return new Point3d(subunits.getCentroid());
	}
	
	/**
	 * Calculates the min and max boundaries of the structure after it has been
	 * transformed into its canonical orientation.
	 */
	private void calcBoundaries() {
		if (subunits == null || subunits.getSubunitCount() == 0) {
			return;
		}
		
		minBoundary.x = Double.MAX_VALUE;
		maxBoundary.x = Double.MIN_VALUE;
		minBoundary.y = Double.MAX_VALUE;
		maxBoundary.x = Double.MIN_VALUE;
		minBoundary.z = Double.MAX_VALUE;
		maxBoundary.z = Double.MIN_VALUE;

		Point3d probe = new Point3d();
		Point3d centroid = subunits.getCentroid();

		for (Point3d[] list: subunits.getTraces()) {
			for (Point3d p: list) {
				// TODO does transformation matrix contain translation already??
				probe.sub(p, centroid); // apply transformation at origin of coordinate system
//				probe.set(p);
				transformationMatrix.transform(probe);

				minBoundary.x = Math.min(minBoundary.x, probe.x);
				maxBoundary.x = Math.max(maxBoundary.x, probe.x);
				minBoundary.y = Math.min(minBoundary.y, probe.y);
				maxBoundary.y = Math.max(maxBoundary.y, probe.y);
				minBoundary.z = Math.min(minBoundary.z, probe.z);
				maxBoundary.z = Math.max(maxBoundary.z, probe.z);
				xyRadiusMax = Math.max(xyRadiusMax, Math.sqrt(probe.x*probe.x + probe.y * probe.y));
			}
		}
//		System.out.println("Min: " + minBoundary);
//		System.out.println("Max: " + maxBoundary);
	}
	
	/**
	 * 
	 */
	public List<List<Integer>> getOrbits() {
		double[] depth = getOrbitDepth();
		List<List<Integer>> orbits = calcOrbits();

		// calculate the mean depth of orbit along z-axis
        Map<Double, List<Integer>> depthMap = new TreeMap<Double, List<Integer>>();
		for (int i = 0; i < orbits.size(); i++) {
			double meanDepth = 0;
			List<Integer> orbit = orbits.get(i);
			for (int subunit: orbit) {
				meanDepth += depth[subunit];
			}
		    meanDepth /= orbit.size();
		    if (depthMap.get(meanDepth) != null) {
//		    	System.out.println("Conflict in depthMap");
		    	meanDepth += 0.01;
		    }
		    depthMap.put(meanDepth, orbit);
		}
		
		// now fill orbits back into list order by depth
		orbits.clear();
		for (List<Integer> orbit: depthMap.values()) {
			orbits.add(orbit);
		}
		return orbits;
	}
	
	private double[] getOrbitDepth() {	
		int n = subunits.getSubunitCount();
        double[] depth = new double[n];        
		Point3d probe = new Point3d();

		for (int i = 0; i < n; i++) {
			Point3d p= subunits.getCenters().get(i);
			probe.set(p);
			transformationMatrix.transform(probe);
			probe.add(minBoundary);
			depth[i] = probe.z;
		}
		return depth;
	}
	
	/**
	 * Returns a list of list of subunit ids that form an "orbit", i.e. they
	 * are transformed into each other during a rotation around the principal symmetry axis (z-axis)
	 * @return
	 */
	private List<List<Integer>> calcOrbits() {
		int n = subunits.getSubunitCount();
//		for (int i = 0; i < n; i++) {
//			System.out.println("ChainId: " + subunits.getChainIds().get(i));
//		}
		int fold = rotationGroup.getRotation(0).getFold();

		List<List<Integer>> orbits = new ArrayList<List<Integer>>();
		Set<Integer> used = new HashSet<Integer>();
//		for (int j = 0; j < fold; j++) {
//			System.out.println("Rotation angle: " +rotationGroup.getRotation(j).getAxisAngle());
//			System.out.println(rotationGroup.getRotation(j).getPermutation());
//		}
		
		List<Integer> inOrder = rotationGroup.getRotation(0).getPermutation();
		
		// for simple Cn group, order the orbits in rotation order for coloring
		if (rotationGroup.getOrder() > 1  && n == fold && rotationGroup.getPointGroup().startsWith("C")) {
		    inOrder = deconvolute();
		}
			
		for (int i = 0; i < n; i++) {
			if (! used.contains(i)) {
				// determine the equivalent subunits
				List<Integer> orbitElements = new ArrayList<Integer>();
				for (int j = 0; j < fold; j++) {
					List<Integer> permutation = rotationGroup.getRotation(j).getPermutation();
					orbitElements.add(permutation.get(i));
					used.add(permutation.get(i));
				}

//				System.out.println("OrbitElements: " + orbitElements);

				// order subunits in rotation order
				List<Integer> orbit = new ArrayList<Integer>(orbitElements.size());
				for (Integer p: inOrder) {
					if (orbitElements.contains(p)) {
						orbit.add(p);
					}
				}
				orbits.add(orbit);
			}
		}
//		System.out.println("Orbits: ");
//		for (List<Integer> orbit: orbits) {
//			System.out.println(orbit);
//		}
		return orbits;
	}
	
	private List<Integer> deconvolute() {
		// get first rotation in rotation group (by definition the first rotation with smallest angle)
		List<Integer> permutation = rotationGroup.getRotation(1).getPermutation();

		List<Integer> inRotationOrder = new ArrayList<Integer>(permutation.size());
		inRotationOrder.add(0);
		for (int i = 0; i < permutation.size()-1; i++) {
			int next = permutation.get(inRotationOrder.get(i));
			if (next == 0) {
				next = i+1;
			}
		    inRotationOrder.add(next);
			
//			System.out.println("inrotationorder: " + inRotationOrder);
		}
		return inRotationOrder;
	}
	
	/*
	 * Modifies the rotation part of the transformation axis for
	 * a Cn symmetric complex, so that the narrower end faces the
	 * viewer, and the wider end faces away from the viewer. Example: 3LSV
	 */
	private Matrix4d setZDirection(Matrix4d matrix) {
		calcBoundaries();

		// TODO can we just use the z-min/z-max distances to create canonical view?
		Point3d probe = new Point3d();
		Point3d centroid = subunits.getCentroid();
		// TODO centroid required?
//		centroid = new Point3d();

		// find the center along the z-axis
		double center = minBoundary.z + (maxBoundary.z - minBoundary.z)/2;
		double sum1 = 0;
		double sum2 = 0;
		for (Point3d[] list: subunits.getTraces()) { // loop over all subunits
			for (Point3d p: list) {			
				// align points with z-axis (principal rotation axis)
				probe.sub(p, centroid);
//				probe.set(p);
				transformationMatrix.transform(probe);
				
				// calculate the distance square for each
				// point from the z-axis. Sum up the distance 
				// squares for front and back half of the structure.
				if (probe.z < center) {
					sum1 += probe.x*probe.x + probe.y*probe.y;
				} else {
					sum2 += probe.x*probe.x + probe.y*probe.y;
				}
			}
		}

		// if the wider part (large sum) faces the front, apply
		// a 180 degree rotation around the y-axis so that the 
		// narrower part faces the viewer. This new orientation 
		// provides the least occluded view along the z-axis.
//		System.out.println("setZDirection: " + sum1 + "/" + sum2);
		if (sum2 > sum1) {
			Matrix4d rot = new Matrix4d();
			rot.rotY(-Math.PI);
			rot.mul(matrix);
			matrix.set(rot);
//			System.out.println("Changing z direction");
		}

		return matrix;
	}

	/**
	 * Returns a normalized vector that represents the
	 * principal rotation axis.
	 * @return principal rotation axis
	 */
	private void calcPrincipalRotationAxis() {
		Rotation rotation = rotationGroup.getRotation(0); // the rotation around the principal axis is the first rotation
		AxisAngle4d axisAngle = rotation.getAxisAngle();
		principalRotationAxis = new Vector3d(axisAngle.x, axisAngle.y, axisAngle.z);
		principalRotationAxis.normalize();
	}

	/**
	 * Returns a normalized vector that represents the
	 * minor rotation axis, or null if there is no minor rotation axis in
	 * the case of cyclic symmetry.
	 * @return minor rotation axis, return null if minor rotation axis does not exists
	 */
	private Vector3d getMinorRotationAxis() {
		// find axis that is not the rotation principal axis (direction = 1)
		if (rotationGroup.getPointGroup().equals("T")) {
			return getReferenceAxisTetrahedral();
		} else if (rotationGroup.getPointGroup().equals("O")) {
			return getReferenceAxisOctahedral();
		} else if (rotationGroup.getPointGroup().equals("I")) {
			return getReferenceAxisIcosahedral();		
		}
    	int maxFold = rotationGroup.getRotation(0).getFold();
    	// TODO how about D2, where minor axis = 2 = principal axis??
		for (int i = 0; i < rotationGroup.getOrder(); i++) {
			if (rotationGroup.getRotation(i).getDirection() == 1 && rotationGroup.getRotation(i).getFold() < maxFold) {
				AxisAngle4d axisAngle = rotationGroup.getRotation(i).getAxisAngle();
				Vector3d v = new Vector3d(axisAngle.x, axisAngle.y, axisAngle.z);
				v.normalize();
//				System.out.println("Minor rotation axis: " + rotationGroup.getRotation(i).getFold());
				return v;
			}
		}
		return null; 
	}
	
	private Vector3d getReferenceAxisTetrahedral() {
		for (int i = 0; i < rotationGroup.getOrder(); i++) {
				AxisAngle4d axisAngle = rotationGroup.getRotation(i).getAxisAngle();
				Vector3d v = new Vector3d(axisAngle.x, axisAngle.y, axisAngle.z);
				double d = v.dot(principalRotationAxis);
				if (rotationGroup.getRotation(i).getFold() == 3) {
					// the dot product 0 is between to adjacent 3-fold axes
					if (d > 0.3 && d < 0.9) {
						return v;
					}
				}
		}
		return null;
	}
	
	private Vector3d getReferenceAxisOctahedral() {
		for (int i = 0; i < rotationGroup.getOrder(); i++) {
				AxisAngle4d axisAngle = rotationGroup.getRotation(i).getAxisAngle();
				Vector3d v = new Vector3d(axisAngle.x, axisAngle.y, axisAngle.z);
				double d = v.dot(principalRotationAxis);
				if (rotationGroup.getRotation(i).getFold() == 4) {
					// the dot product 0 is between to adjacent 4-fold axes
					if (d >= 0 && d < 0.1 ) {
						return v;
					}
				}
		}
		return null;
	}
	
	private Vector3d getReferenceAxisIcosahedral() {
		for (int i = 0; i < rotationGroup.getOrder(); i++) {
				AxisAngle4d axisAngle = rotationGroup.getRotation(i).getAxisAngle();
				Vector3d v = new Vector3d(axisAngle.x, axisAngle.y, axisAngle.z);
				double d = v.dot(principalRotationAxis);
				if (rotationGroup.getRotation(i).getFold() == 5) {
					// the dot product of 0.447.. is between to adjacent 5-fold axes
					if (d > 0.447 && d < 0.448) {
						return v;
					}
				}
		}
		return null;
	}

	private void calcPrincipalAxes() {
		MomentsOfInertia moi = new MomentsOfInertia();

		for (Point3d[] list: subunits.getTraces()) {
			for (Point3d p: list) {
				moi.addPoint(p, 1.0);
			}
		}
		principalAxesOfInertia = moi.getPrincipalAxes();
	}

	private Matrix4d getTransformationByInertiaAxes() {
		Point3d[] refPoints = new Point3d[2];
		refPoints[0] = new Point3d(principalAxesOfInertia[0]);
		refPoints[1] = new Point3d(principalAxesOfInertia[1]);

		Point3d[] coordPoints = new Point3d[2];
		coordPoints[0] = new Point3d(Y_AXIS);
		coordPoints[1] = new Point3d(X_AXIS);
		
		// align inertia axis with y-x axis
		Matrix4d matrix = alignAxes(refPoints, coordPoints);
		if (SuperPosition.rmsd(refPoints, coordPoints) > 0.01) {
			System.out.println("Warning: aligment with coordinate system is off. RMSD: " + SuperPosition.rmsd(refPoints, coordPoints));
		}
		
		calcReverseMatrix(matrix);
		return matrix;
	}

}