package org.biojava3.structure;
import static org.junit.Assert.assertEquals;

import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.align.ce.CeParameters;
import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.align.util.RotationAxis;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.CeSymm;
import org.junit.Test;

/**
 * Tries to reproduce a sporatic behavior of CE-Symm.
 * 
 * With some combinations of classpaths, the following test passes for 
 * 1c4ea0f but fails for 076b05e (as well as more recent versions).
 * In this case, it seems like the maxGapSize parameter is responsible;
 * with setGapSize(0) we get a 4pi/5 rotation, while with 30 we get only 2pi/5
 * (or maybe vice versa).
 * 
 * However, installing various versions of BioJava, ava-core, structurecodec, etc
 * via either maven or eclipse completely messes with the test, causing it to
 * fail under most conditions. It's very confusing, and probably has to do with
 * the inconsistent BioJava versions used by various dependencies.
 * 
 * 
 * Closed: won't fix; irreproducible.
 *
 * @author spencer
 *
 */
public class CeSymmIrreproducibleBugTest {

	@Test
	public void testIt() {

		AtomCache cache = new AtomCache();

		String name = "d1vkde_";
		
		ScopFactory.setScopDatabase(ScopFactory.VERSION_1_75);


		CeSymm ceSymm = new CeSymm();
		CeParameters params = (CeParameters) ceSymm.getParameters();
		if(params == null) params = new CeParameters();
		//params.setDuplicationHint(DuplicationHint.LEFT);
		params.setMaxGapSize(30);
		ceSymm.setParameters(params);
		
		try {
			Atom[] ca1 = cache.getAtoms(name); 
			Atom[] ca2 = cache.getAtoms(name);

			AFPChain afpChain = ceSymm.align(ca1, ca2);
			afpChain.setName1(name);
			afpChain.setName2(name);
			
			System.out.println("MaxGapSize="+((CeParameters)ceSymm.getParameters()).getMaxGapSize());
			RotationAxis axis = new RotationAxis(afpChain);
			double theta = axis.getAngle();
			
			assertEquals(2./5. * 2*Math.PI, theta, .1 );
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
