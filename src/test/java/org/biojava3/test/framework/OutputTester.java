package org.biojava3.test.framework;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.biojava.bio.structure.scop.BerkeleyScopInstallation;
import org.biojava.bio.structure.scop.ScopDatabase;
import org.biojava.bio.structure.scop.ScopFactory;

/**
 * Facilitates tests which require the comparison of output files.
 * 
 * Expected results should be checked in to the expectedDir, which defaults to
 * the 'expected/' resource directory (e.g.
 * ${project.base}/src/test/resources/expected)
 * 
 * @author spencer
 * 
 */
public class OutputTester {

	private String expectedDir;
	private String actualDir;

	public static final String DEFAULT_EXPECTED_DIR = "expected";
	public static final String DEFAULT_ACTUAL_DIR = null;

	private static OutputTester instance = null;



	/**
	 * Construct a new OutputTester. You probably want to get the singleton
	 * instance with {@link #getInstance() instead.}
	 * 
	 * @param expectedDir
	 *            Directory where expected output is saved, relative to the
	 *            resource path. This must be findable by
	 *            {@link Class#getResource(String)}.
	 * @param actualDir
	 *            Directory to save output. If null, a temporary directory will
	 *            be created.
	 */
	protected OutputTester(String expectedDir, String actualDir) {
		if (expectedDir == null) {
			throw new IllegalArgumentException("No expected directory");
		}
		
		try {
			URL url = OutputTester.class.getResource(expectedDir);
			if(url == null) {
				throw new IllegalArgumentException("Expected dir not found ("+expectedDir+")");
			}
			
			File expectedDirFile = new File(url.toURI());
			if( !expectedDirFile.exists()) {
				throw new IllegalArgumentException("Expected dir doesn't exist ("+expectedDir+")");
			}
			
			this.expectedDir = expectedDirFile.getCanonicalPath();
		} catch( URISyntaxException e) {
			throw new IllegalArgumentException("Error finding expected directory ("+expectedDir+")",e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Error finding expected directory ("+expectedDir+")",e);
		}
		
		if (actualDir == null) {
			File tmp = createTempDirectory("actual");
			this.actualDir = tmp.getAbsolutePath();
		} else {
			this.actualDir = actualDir;
		}
	}

	public static OutputTester getInstance() {
		if (instance == null) {
			instance = new OutputTester(DEFAULT_EXPECTED_DIR,
					DEFAULT_ACTUAL_DIR);
		}

		// TODO is this needed or wise?
		// For efficiency, Instantiate ScopFactory and AtomCache
		// Individual tests could choose to override these later, if they set them back
		ScopDatabase scop = ScopFactory.getSCOP();
		if (!scop.getClass().getName().equals(BerkeleyScopInstallation.class.getName())) {
			ScopFactory.setScopDatabase(new BerkeleyScopInstallation()); // ScopDatabase is too hard to mock well
		}

		return instance;
	}

	public File getExpectedFile(String relPath) throws FileNotFoundException {
		return new File(expectedDir,relPath);
	}
	
	public File getActualFile(String relPath) {
		return new File(actualDir,relPath);
	}

	/**
	 * Creates a temporary directory with a name like <prefix>-<time>-<num>
	 * 
	 * @param prefix
	 * @return
	 * @throws IOException
	 *             if the directory cannot be created
	 * @throws IllegalStateException
	 *             if no suitable name can be found
	 */
	public static File createTempDirectory(String prefix) {
		final int maxAttempts = 1000;

		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = "";
		if (prefix != null && !prefix.isEmpty()) {
			baseName = prefix + '-';
		}
		baseName += System.currentTimeMillis() + "-";

		for (int counter = 0; counter < maxAttempts; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory within "
				+ maxAttempts + " attempts (tried through " + baseName
				+ (maxAttempts - 1) + ')');
	}


}
