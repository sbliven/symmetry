package org.biojava3.structure.align.symm.benchmark.comparison.order;

import org.biojava3.structure.align.symm.census2.Result;

/**
 * A method to determine order, assuming the result is already broadly "significant" (e.g. TM-score >= 0.4).
 * @author dmyersturnbull
 */
public interface OrderDetermination {

	int getOrder(Result result);
	
}
