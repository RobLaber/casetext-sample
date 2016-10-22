
import java.io.*;
import java.util.*;
import java.util.stream.*;
 
/**
 * A Reporter is an entity whose name appears on the list of valid reporters provided by casetext as part
 * of the code sample exercise.  
 */
public class Reporter {

	/** The name of the reporter, as it appears in reporters.txt */
	private final String name;
	
	/** Hold on to the trimmed name for convenience.  This is the name with no whitespace, commas or periods. */
	private final String trimmedName;
		
	public Reporter(String name) {
		this.name = name.trim();
		this.trimmedName = stripWhiteSpaceAndPunct(name);
	}
	
	public String getTrimmedName() {
		return trimmedName;
	}
	
	/**
	 * Return the name of the reporter without any excessive whitespace.
	 */
	public String getCanonicalName() {
		return name.replaceAll("(\\p{javaWhitespace}|\\p{Space})+", " ");
	}
	
	/**
	 * Get rid of whitespace, commas and periods. This method should probably live in some kind of Utils file. 
	 */
	public static String stripWhiteSpaceAndPunct(String s) {
		return s.replaceAll("[\\.\\w,]", "");
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Reporter) {
			return getCanonicalName().equals(((Reporter) o).getCanonicalName());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getCanonicalName().hashCode();
	}
} 
