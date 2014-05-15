package org.biojava3.test.framework;

import java.util.List;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * Custom XML DifferenceListener which ignores a list of tags
 * @author spencer
 *
 */
public class ElementTextIgnoringDifferenceListener implements DifferenceListener {

	private String[] ignoredNames;

	public ElementTextIgnoringDifferenceListener(String... ignoredNames) {
		this.ignoredNames = ignoredNames;
	}

	public ElementTextIgnoringDifferenceListener(List<String> ignoredNames) {
		this.ignoredNames = new String[ignoredNames.size()];
		for (int i = 0; i < ignoredNames.size(); i++) {
			this.ignoredNames[i] = ignoredNames.get(i);
		}
	}

	@Override
	public int differenceFound(Difference difference) {
		Node controlNode = difference.getControlNodeDetail().getNode();
		String name = null;
		if (controlNode != null && controlNode.getParentNode() != null) {
			name = controlNode.getParentNode().getNodeName();
		}
		for (String ignoredName : ignoredNames) {
			if (ignoredName.equals(name)) {
				return RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
			}
		}
		return RETURN_ACCEPT_DIFFERENCE;
	}

	@Override
	public void skippedComparison(Node control, Node test) {
	}

}