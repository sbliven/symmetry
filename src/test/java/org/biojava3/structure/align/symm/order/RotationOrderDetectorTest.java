/**
 * 
 */
package org.biojava3.structure.align.symm.order;

import static org.junit.Assert.*;

import java.io.IOException;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.StructureTools;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.RotationAxis;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.CeSymm;
import org.junit.Before;
import org.junit.Test;

/**
 * @author spencer
 *
 */
public class RotationOrderDetectorTest {
	private RotationOrderDetector detector;
	private CeSymm ce;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {


		ce = new CeSymm();
		detector = new RotationOrderDetector(8);

		ScopFactory.setScopDatabase(ScopFactory.VERSION_1_75A);
	}

	@Test
	public void testFitHarmonics() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.fitHarmonics(ca1, axis);
		expectedHarmonics = new double[] { 0,
				1.218482, 2.110836, 0.7203669, 0.8226358,
				0.6092911, 0.6339138, 0.4439472, 0.4737434,
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.fitHarmonics(ca1, axis);
		expectedHarmonics = new double[] { 0,
				0.5176411, 0.5359353, 0.4928912, 0.5044149,
				0.4031307, 1.915722, 0.4049375, 0.4456366
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}
	@Test
	public void testCalculateOrderByHarmonics() throws IOException, StructureException, OrderDetectionFailedException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		int order;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonics(alignment, ca1);

		assertEquals(name,2,order);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonics(alignment, ca1);

		assertEquals(name,6,order);

		ce = new CeSymm();// work around bug

		name = "1TIM.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonics(alignment, ca1);

		assertEquals(name,1,order);// tough case
	}
	@Test
	public void testFitHarmonicsFloating() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.fitHarmonicsFloating(ca1, axis);
		expectedHarmonics = new double[] { 2.287581,
				0.6300994, 1.51542, 0.1546644, 0.2601547,
				0.08553783, 0.1208464, -0.02471022, 0.01952981
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.fitHarmonicsFloating(ca1, axis);
		expectedHarmonics = new double[] { 1.779942,
				0.09846244, 0.1032024, 0.07649375, 0.07587491,
				-0.007642349, 1.494255, 0.002693317, 0.03414484
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

		ce = new CeSymm();// work around bug

		name = "1TIM.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.fitHarmonicsFloating(ca1, axis);
		expectedHarmonics = new double[] { 2.366679013015843,
				0.3193482859534293, 0.269333714179555, 0.23173514628285755, 0.22683425484995692,
				0.05867574677375045, 0.27108803524720687, 0.05479899729977149, 0.2218018710680044
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}
	@Test
	public void testCalculateOrderByHarmonicsFloating() throws IOException, StructureException, OrderDetectionFailedException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		int order;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonicsFloating(alignment, ca1);

		assertEquals(name,2,order);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonicsFloating(alignment, ca1);

		assertEquals(name,6,order);

		ce = new CeSymm();// work around bug

		name = "1TIM.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);

		order = detector.calculateOrderHarmonicsFloating(alignment, ca1);

		assertEquals(name,1,order);// tough case

	}


	@Test
	public void testTrySingleHarmonicsFloatingByAmp() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleHarmonicsFloatingByAmp(ca1, axis);
		expectedHarmonics = new double[] { 
				0.1739415, 1.246599, -0.3484241, -0.2464376,
				-0.2841697, -0.2528177, -0.250818, -0.2106903
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleHarmonicsFloatingByAmp(ca1, axis);
		expectedHarmonics = new double[] { 
				-0.1288947, -0.2341662, -0.1283006, -0.2218122,
				-0.1814559, 1.445211, -0.1186493, -0.163856
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

		ce = new CeSymm();// work around bug

		name = "1TIM.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleHarmonicsFloatingByAmp(ca1, axis);
		expectedHarmonics = new double[] {
				0.09557174170270101, 0.008787223181015748, 0.01162835744518009, -0.0020224117414387264, 
				-0.13940992848672834, 0.10491704811734609, -0.07861549769491497, 0.11139096427869842
		};
		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}
	@Test
	public void testTrySingleHarmonicsFloatingBySSE() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleHarmonicsFloatingBySSE(ca1, axis);
		expectedHarmonics = new double[] { 
				0.4411113, 0.1688462, 0.4289372, 0.436751,
				0.4336008, 0.4358288, 0.4358056, 0.4384935
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}

	

	@Test
	public void testTrySingleCuspByAmp() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleCuspByAmp(ca1, axis);
		expectedHarmonics = new double[] { 
				0.3485579, 1.079108, -0.3344876, -0.2502484,
				-0.2343293, -0.2120265, -0.1883948, -0.166058
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);


		ce = new CeSymm();// work around bug

		name = "d1ijqa1";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleCuspByAmp(ca1, axis);
		expectedHarmonics = new double[] { 
				-0.172995, -0.1505527, 0.1552747, -0.2200142,
				-0.1770285, 1.18373, -0.1158369, -0.1520574
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

		ce = new CeSymm();// work around bug

		name = "1TIM.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleCuspByAmp(ca1, axis);
		expectedHarmonics = new double[] {
				0.09215688421043856, 0.01954699457484211, 0.024678702567020566, 0.015512762093906297,
				-0.1207419292219559, 0.0775398445014336, -0.06753395575944238, 0.08965700567291392
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}
	@Test
	public void testTrySingleCuspBySSE() throws IOException, StructureException {
		String name;

		// Perform alignment to determine axis
		Atom[] ca1, ca2;
		AFPChain alignment;
		RotationAxis axis;
		double[] coefs,expectedHarmonics;

		name = "1MER.A";
		ca1 = StructureTools.getAtomCAArray(StructureTools.getStructure(name));
		ca2 = StructureTools.cloneCAArray(ca1);
		alignment = ce.align(ca1, ca2);
		axis = new RotationAxis(alignment);

		coefs = detector.trySingleCuspBySSE(ca1, axis);
		expectedHarmonics = new double[] { 
				0.4411113, 0.1688462, 0.4289372, 0.436751,
				0.4336008, 0.4358288, 0.4358056, 0.4384935
		};

		assertArrayEquals(name,expectedHarmonics,coefs,1e-4);

	}

}
