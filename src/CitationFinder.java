
import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.lang.Runnable;
import java.lang.Thread;
 
/**
 * Front-end, command line program for the Casetext citation sample problem.  This program does
 * not require any command line arguments.
 */
public class CitationFinder {

	// TODO: this could possibly be a command line arg.
	private static final String PATH_TO_DATA = "data/casetextCaseFiles";

	public static void main(String[] args) throws FileNotFoundException, IllegalStateException, InterruptedException, IOException {
		
		long startTime = System.currentTimeMillis();
		
		// TODOD: this could also be a command line arg.
		int numThreads = Runtime.getRuntime().availableProcessors();
	
		List<File> sourceFiles = getSourceFiles();
		
		List<List<File>> distributedFiles = divideFiles(sourceFiles, numThreads);
		
		System.out.printf("Using %d threads to process %d files.\n", numThreads, sourceFiles.size()); 
		
		List<Thread> workers = new ArrayList<>();
		
		File[] tempFiles = new File[distributedFiles.size()];
		
		for (int i = 0; i < distributedFiles.size(); i++) {
		
			List<File> batch = distributedFiles.get(i);
			
			File batchOutput = new File("batch" + i + "temp.txt");
			
			tempFiles[i] = batchOutput;
			
			Thread worker = new Thread(new FileBatchProcessor(batch, batchOutput), "batchWorker" + i);
			workers.add(worker);
			worker.start();
		}
		
		for (Thread worker : workers) {
			worker.join();
		}
		long totalTime = System.currentTimeMillis() - startTime;
		consolidateTempFiles(tempFiles);
		
		totalTime = System.currentTimeMillis() - startTime;
		System.out.printf("Total runtime: %f minutes\n", totalTime / 60000.0);
	}
	
	/**
	 * Get the source files.  This assumes that the files are located in the  
	 * directory PATH_TO_DATA.
	 */	 
	 private static List<File> getSourceFiles() {
		System.out.println("Sourcing files...");
		File sourceDir = new File(PATH_TO_DATA);
		return Arrays.asList(sourceDir.listFiles());
	}
	
	/**
	 * Combine all temp files into the final output csv file, and delete the files.
	 */
	private static void consolidateTempFiles(File[] tempFiles) throws IOException {
	
		System.out.println("Consolidating temp files...");
	
		File output = new File("CitationFinderOutput.csv");
		BufferedWriter writer;
		BufferedReader reader;
		try {
			output.createNewFile();
			writer = Files.newBufferedWriter(output.toPath(), StandardCharsets.UTF_8);
		} catch(IOException e) {
			System.out.println("Error creating file writer.");
			return;
		}
		
		for (int i = 0; i < tempFiles.length; i++) {
			File tempFile = tempFiles[i];
			
			try {
				reader = Files.newBufferedReader(tempFile.toPath(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				System.out.printf("Error reading file %s", tempFile.getAbsolutePath());
				writer.close();
				return;
			}
			String line = reader.readLine();
			while(line != null) {
				writer.write(line);
				writer.newLine();
				line = reader.readLine();
			}
			reader.close();
			tempFile.delete();
		}
		writer.flush();
		writer.close();
	}
	
	/**
	 * Divide the list of files up for parallel processing. 
	 */
	private static List<List<File>> divideFiles(List<File> files, int numBins) {
		
		int numPerBin = files.size() / numBins;
		List<List<File>> dividedFiles = new ArrayList<>();
		
		// Initialize dividedFiles to have numBins empty lists.  We've got a fixed set of files,
		// so may as well define the initial capacity.
		for (int i = 0 ; i < numBins; i++) {
			dividedFiles.add(new ArrayList<File>((files.size() / numBins) + 1));
		}
		
		int splitIndex = 0;
		
		// Shuffle the files like we're dealing a poker hand.  Empirical testing indicated
		// that there is a contiguous chunk of files that were taking up most of the 
		// processing time.
		for (int i = 0; i < files.size(); i++) {
			dividedFiles.get(i % numBins).add(files.get(i));
		}
		
		return dividedFiles;
	}
} 