
import java.util.*;
import java.util.stream.*; 
import java.io.*;

/**
 * A purely static class for working with the set of all Reporters.
 */
public class Reporters {

	/** No instantiation of static class. */
	private Reporters() {}

	private static final String PATH_TO_REPORTERS = "data/reporters.txt";
	
	/** A mapping from raw String to Reporter for quick lookup. */
	private static final Map<String, Reporter> reporterDictionary;
	
	/** 
	 * A mapping from Strings that are 'near matches' to the exact reporter name.  This can
	 * help us account for differences in punctuation or typos.
	 *
	 * TODO: In practice, this didn't seem to help very much.  I'll leave it here for anyway
	 * for possible future use.
	 */
	private static final Map<String, Reporter> nearMatchDictionary;

	//  Initialize the dictionaries in a static block.
	static {
		File reporterFile = new File(PATH_TO_REPORTERS);
		
		if (reporterFile.exists() && ! reporterFile.isDirectory()) {
			Scanner fileParser;
			Map<String, Reporter> repMap = new HashMap<>();
			Map<String, Reporter> nearMatchMap = new HashMap<>();
			
			try {
				fileParser = new Scanner(reporterFile);
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Error initializing Scanner.  File not found: " + reporterFile.getAbsolutePath());
			}
			// Split the file on newLines
			fileParser.useDelimiter("\n");
			
			while (fileParser.hasNext()) {
				String trimmedName = fileParser.next().trim();
				Reporter rep = new Reporter(trimmedName);
				repMap.put(trimmedName, rep);
				nearMatchMap.put(Reporter.stripWhiteSpaceAndPunct(trimmedName), rep);
			}
			fileParser.close();
			
			reporterDictionary = repMap;
			nearMatchDictionary = nearMatchMap;
		}
		
		else {
			throw new IllegalStateException("File at path " + PATH_TO_REPORTERS + " is either a directory or doesn't exist.");
		}
	}
	/** Return a collection of all Reporters. */
	public static Collection<Reporter> getAllReporters() {
		return reporterDictionary.values();
	}
	
	/**
	 * Try to find a Reporter whose name matches the trimmed input String.  If no reporter is found,
	 * return Optional.empty().  Otherwise, return an Optional whose .get() returns the matched
	 * Reporter.
	 *
	 */
	public static Optional<Reporter> findMatch(String name) {
	
		Reporter possibleMatch = reporterDictionary.get(name.trim());
		
		if (possibleMatch != null) {
			return Optional.of(possibleMatch);
		}
		return Optional.empty();		
	}
	
	/**
	 * If the input String does not exactly match a known Reporter, we can try to account
	 * for typos and inconsistent punctuation here.
	 *
	 * TODO: As stated above, this method does not really add anything, but perhaps it could be
	 * amended to be more useful.
	 */
	public static Optional<Reporter> findNearMatch(String name) {
	
		Reporter possibleMatch = nearMatchDictionary.get(Reporter.stripWhiteSpaceAndPunct(name));
		
		if (possibleMatch != null) {
			System.out.printf("String \"%s\" is a near match for reporter \"%s\".\n", name, possibleMatch.getCanonicalName());
			return Optional.of(possibleMatch);
		}
		return Optional.empty();
	}
}