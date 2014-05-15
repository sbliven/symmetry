package org.biojava3.test.mock.structure.align.symm.census2;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.census2.Census;
import org.biojava3.structure.align.symm.census2.Significance;
import org.biojava3.structure.align.symm.census2.SignificanceFactory;

/**
 * Mock Census containing a single domain
 *
 */
@Deprecated
public class TinyCensus extends Census {

	private String[] domains;
	
	public TinyCensus(String... domains) {
		super();
		this.domains = domains;
	}

	@Override
	protected Significance getSignificance() {
		return SignificanceFactory.tmScore(0.0);
	}
	
	@Override
	protected List<ScopDomain> getDomains() {
		List<ScopDomain> domains = new ArrayList<ScopDomain>();
		ScopDatabase scop = ScopFactory.getSCOP(ScopFactory.VERSION_1_75B);
		for (String domain : this.domains) {
			domains.add(scop.getDomainByScopID(domain));
		}
		return domains;
	}

}