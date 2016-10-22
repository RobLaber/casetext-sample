
import java.util.Objects;
 
/**
 * A simple class to represent a (canonical) citation in a source file.
 */
public class Citation {
 
 	private final int volume;
 	private final int page;
 	private final Reporter reporter;
 	
 	public Citation(int volume, Reporter reporter, int page) {
 		this.volume = volume;
 		this.reporter = reporter;
 		this.page = page;
 	}
 	
 	public String format() {
 		// Wrap the citation in quotes for csv formatting.
 		return String.format("\"%d %s %d\"", volume, reporter.getCanonicalName(), page);
 	}
 	
 	public int getPage() {
 		return page;
 	}
 	
 	public int getVolume() {
 		return volume;
 	}
 	
 	public Reporter getReporter() {
 		return reporter;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof Citation) {
 			Citation other = (Citation) o;
 			return getPage() == other.getPage() && getVolume() == other.getVolume() && getReporter().equals(other.getReporter());
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode() {
 		return Objects.hash(getPage(), getVolume(), getReporter());
 	}	
 }