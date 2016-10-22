
import java.io.*;
import java.util.Collection;
import java.lang.Runnable;

/**
 * Process multiple files in a single thread.
 */
public class FileBatchProcessor implements Runnable {

	private final Collection<File> filesToProcess;
	
	private final File outputFile;
	
	public FileBatchProcessor(Collection<File> batch, File outputFile) {
	
		assert(outputFile.exists() && !outputFile.isDirectory());
		
		this.filesToProcess = batch;
		this.outputFile = outputFile;	
	}
	
	public void run() {
		
		try {
			outputFile.createNewFile();
		} catch(Exception e) {
			System.out.printf("Error creating file %s.", outputFile.getAbsolutePath());
			return;
		}
		
		// Process the list of files sequentially.
		for (File sourceFile : filesToProcess) {
			SingleFileProcessor.process(sourceFile, outputFile);
		}	
	} 
}