package org.biojava3.test.framework;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.xml.sax.SAXException;

public class XMLComparisonHelper {

	// notice the side effects here
	static {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
		XMLUnit.setIgnoreAttributeOrder(true);
	}
	
	/**
	 * Returns true if and only if the two XML files are <em>similar</em>; that
	 * is, the contain the same elements and attributes regardless of order.
	 *
	 * @param expectedFile
	 * @param actualFile
	 * @param listener
	 * @return
	 */
	public static boolean compareXml(File expectedFile, File actualFile,
			DifferenceListener listener) {
		try {
			FileReader expectedFr = new FileReader(expectedFile);
			FileReader actualFr = new FileReader(actualFile);
			Diff diff = new Diff(expectedFr, actualFr);
			if (listener != null)
				diff.overrideDifferenceListener(listener);
			// ignore order
			// look at element, id, and weight (weight is a nested element)
			diff.overrideElementQualifier(new RecursiveElementNameAndTextQualifier());
			final boolean isSimilar = diff.similar();
			if (!isSimilar)
				printDetailedDiff(diff, System.err);
			expectedFr.close();
			actualFr.close();
			return isSimilar;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Prints the results of a 
	 * @param diff
	 * @param ps
	 */
	public static void printDetailedDiff(Diff diff, PrintStream ps) {
		DetailedDiff detDiff = new DetailedDiff(diff);
		for (Object object : detDiff.getAllDifferences()) {
			Difference difference = (Difference) object;
			ps.println(difference);
		}
	}

}
