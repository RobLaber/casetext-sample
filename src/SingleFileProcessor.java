
import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Class to scan and extract Citations from a single source file.  This class is the main 
 * workhorse for the casetext code sample exercise.
 *
 * Some methods in this class assume that the source files can be scanned using UTF-8 encoding.
 * This may not be valid, but it seems to work okay.
 */
public class SingleFileProcessor {
	
	/** 
	* These are the indices of the various capturing groups in the CITATION_REGEX below.
	* They must be kept in sync with that regex.
	*/
	private static final int FIRST_NUMBER_GROUP = 1;
	private static final int LAST_NUMBER_GROUP = 3;
	private static final int TEXT_GROUP = 2;
	
	/** 
	* This regex is a very wide net used to catch citations.  The values 2 and 37 occurring 
	* in the second group of this regex come from the shortest and longest reporter names, 
	* plus some additional characters for things like ", at page ".  The values 1 and 5 occurring
	* in the first and third groups of the regex come from the fact that both volume and page numbers 
	* should be between 1 and 5 digits long.  These values are guesstimates, and are possibly too generous.
	*/
	private static final Pattern CITATION_REGEX = Pattern.compile("(\\d{1,5})(\\D{2,37})(\\d{1,5})");
	
	/** The file to scan */
	private final File sourceFile;
	
	/** The file to which the data will be written. */
	private final File outputFile;

	private SingleFileProcessor(File sourceFile, File outputFile) {
	
		if (!sourceFile.exists() || sourceFile.isDirectory()) {
			throw new IllegalArgumentException("Illegal path name:  There is no readable file at " + sourceFile.getAbsolutePath());
		}
			
		this.sourceFile = sourceFile;
		this.outputFile = outputFile;
	}
	
	/**
	 * Convenient static entry point for this class. 
	 */
	public static void process(File sourceFile, File outputFile) {
		SingleFileProcessor processor = new SingleFileProcessor(sourceFile, outputFile);
		processor.process();
	}
	
	/**
	 * Process the source file by scanning it and collecting citations.
	 */
	public void process() {
	
		// Map each found citation to the number of times it was found.
		Map<Citation, Integer> citations = countCitations();
		
		FileWriter writer;
		try {
			writer = new FileWriter(outputFile, true);
		} catch (IOException e) {
			System.out.printf(
				"Error opening file %s while processing file %s.  No data has been recorded for this file.",
				outputFile.getAbsolutePath(),
				sourceFile.getAbsolutePath());
			return;	
		}
		
		for (Citation citation : citations.keySet()) {
			try {
				writer.append(formatCitationEntry(citation, citations.get(citation)));
			} catch(IOException e) {
				System.out.printf(
					"Error writing to file %s while processing file %s.  No data has been recorded for this file.",
					outputFile.getAbsolutePath(),
					sourceFile.getAbsolutePath());
				return;	
			}
		}
		try {
			writer.close();	
		} catch(IOException e) {
			System.out.printf(
				"Error writing to file %s while processing file %s.  No data has been recorded for this file.",
				outputFile.getAbsolutePath(),
				sourceFile.getAbsolutePath());
			return;			
		}
	}
	
	/** 
	 * Count the number of occurrences of a Citation in the fileScanner's source File.
	 * This method will only count Citations whose getReporter() method returns an element of 
	 * allowedReporters.  Return a Map whose keySet() is a set containing each encountered Citation,
	 * and whose corresponding values are the number of times that the Citation was encountered.
	 *
	 * This method assumes that the files can be Scanned using UTF-8 encoding.
	 */
	private Map<Citation, Integer> countCitations() {
	
		Scanner scanner;
		try{
			// UTF-8 encoding is an assumption.  Seems reasonable though
			scanner = new Scanner(sourceFile, "utf-8");
		} catch(FileNotFoundException e) {
			System.out.printf("Error scanning file %s.  No values for this file were recorded.", sourceFile.getAbsolutePath());
			return new HashMap<>();
		}
		
		// There are third party utilities to scan in files to Strings, but we can do it in pure java for project simplicity
		scanner.useDelimiter("\\Z");

		String fileString = scanner.next();
		scanner.close();
		
		Map<Citation, Integer> citationCounts = new HashMap<>();
		
		Matcher citationMatcher = CITATION_REGEX.matcher(fileString);
		
		// Index of the most recently matched group.  This is used to "back-up" the matcher.
		int endIndex = 0;
		
		while (citationMatcher.find(endIndex)) {
		
			String citationCandidate = citationMatcher.group();

			// Try to match the candidate to a new or existing citation.
			Optional<Citation> matchedCitation = matchCitation(citationMatcher, citationCounts.keySet());
			
			// should we consider the possibility that the terminal digit group in this match
			// is actually the initial digit group in a subsequent match?
			boolean rescanMatchEnd = true;
			
			if (matchedCitation.isPresent()) {
				updateMap(citationCounts, matchedCitation.get());
				// If we found a citation we don't need to look at the last integer again.
				rescanMatchEnd = false;
			}
		
			// "back up" the matcher if we should rescan the terminal digit group.
			endIndex = rescanMatchEnd ? 
				citationMatcher.end() - citationMatcher.group(LAST_NUMBER_GROUP).length() :
				citationMatcher.end();	
		}	
		return citationCounts;	
	}

	/**
	 * Given a String of the form <int><text><int>, try to match the String to a valid 
	 * Citation.  This essentially means checking whether the part of the string matching 
	 * <text> can be matched to a valid Reporter.
	 */
	private static Optional<Citation> matchCitation(Matcher candidateMatcher, Set<Citation> existingCitations) {

		String textContent = candidateMatcher.group(TEXT_GROUP);

		// Does this appear to be an abbreviated citation?
		boolean isAbbr = false;
		
		// Check for abbreviated citations by checking if the string contains ' at '
		Pattern abbrRegex = Pattern.compile(" at ");
		Matcher abbrMatcher = abbrRegex.matcher(textContent);
		if (abbrMatcher.find()) {
			String matched = abbrMatcher.group();
			isAbbr = true;
			textContent = textContent.substring(0, abbrMatcher.start());
		}
		
		Optional<Reporter> matchedReporter = Reporters.findMatch(textContent);
		
		if (matchedReporter.isPresent()) {
			
			int volume;
			int page;
			
			try {
				volume = Integer.parseInt(candidateMatcher.group(FIRST_NUMBER_GROUP));
			}catch(NumberFormatException e) {
				System.out.printf("Error parsing integer \"volume\" from string \"%s\".\n", candidateMatcher.group(FIRST_NUMBER_GROUP));
				return Optional.empty();
			}	
			try {
				page = Integer.parseInt(candidateMatcher.group(LAST_NUMBER_GROUP));
			}catch(NumberFormatException e) {
				System.out.printf("Error parsing integer \"page\" from string \"%s\".\n", candidateMatcher.group(LAST_NUMBER_GROUP));
				return Optional.empty();
			}
			
			if (isAbbr) {
				// Try to match the abbreviated citation to an existing canonical citation.
				Optional<Citation> matched = getCanonicalCitation(volume, matchedReporter.get(), page, existingCitations);
			
				if (!matched.isPresent()) {
					matched = Optional.of(new Citation(volume, matchedReporter.get(), page));
				}
				return matched;	
			}
			return Optional.of(new Citation(volume, matchedReporter.get(), page));	
		}
		return Optional.empty();
	} 
	
	/**
	 * Given a citation that appears to have be abbreviated, try to match that citation to an existing
	 * canonical citation.  If no corresponding canonical citation can be matched, then this method 
	 * returns Optional.empty(). 
	 */
	private static Optional<Citation> getCanonicalCitation(int volume, Reporter reporter, int page, Set<Citation> existingCitations) {
	
		// TODO: Is seems reasonable to impose an upper limit for the number of pages a case can be in a given volume.
		// In other words, the page number for an abbreviated citation should only be within X pages
		// of its corresponding canonical citation's page number.
		
		// Get all existing citations with the same reporter and volume, and then get the citation with the closest page number  
		// that is less that the abbreviated page number.
		return existingCitations.stream()
			.filter(citation -> (citation.getVolume() == volume && citation.getReporter().equals(reporter) && citation.getPage() <= page))
			.max((c1, c2) -> c1.getPage() - c2.getPage());
	}
	
	/**
	 * Add a Citation to the citationCounter map.
	 */
	private static void updateMap(Map<Citation, Integer> citationCounter, Citation citation) {
	
		if (citationCounter.containsKey(citation)) {
			citationCounter.put(citation, citationCounter.get(citation) + 1);
		}
		else {
			citationCounter.put(citation, 1);
		}
	}
	
	/**
	 * Get a comma separated list containing the file, the Citation, and the number of
	 * times that citation occurs.  The output of this method is suitable for entry as a
	 * line in a csv file.
	 */
	private String formatCitationEntry(Citation citation, int count) {
		return sourceFile.getName() + ", " + citation.format() + ", " + count + "\n";
	}
}