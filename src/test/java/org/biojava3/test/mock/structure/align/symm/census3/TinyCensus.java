package org.biojava3.test.mock.structure.align.symm.census3;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.align.model.AFPChain;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopDomain;
import org.biojava.bio.structure.scop.ScopFactory;
import org.biojava3.structure.align.symm.census3.run.AfpChainCensusRestrictor;
import org.biojava3.structure.align.symm.census3.run.Census;

/**
 * Mock Census containing a single domain
 *
 */
public class TinyCensus extends Census {

	private String[] domains;
	
	public TinyCensus(String... domains) {
		super();
		this.domains = domains;
	}

	@Override
	protected AfpChainCensusRestrictor getSignificance() {
		return new AfpChainCensusRestrictor() {
			@Override
			public boolean isPossiblySignificant(AFPChain afpChain) {
				return true;
			}
		};
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