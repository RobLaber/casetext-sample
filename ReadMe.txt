
CitationFinder

Author : Rob Laber
Date   : November 10, 2015

Casetext code sample exercise.
(Spec here: https://drive.google.com/file/d/0B9uCbWb-bHGteG1sVTJTTW5FakdSNHlfTGJhRzhNRjhtM1NZ/view?usp=sharing)

This program does not require any third party libraries, but it does require a java 8 compiler.

Instructions for running CitationFinder:
- It appears you've already decompressed and opened the .zip archive.
- From the command line, navigate to the directory where the uncompressed files are.
- Use javac to compile all java files (a simple "javac *.java" should work).
- Run the program by entering "java CitationFinder" on the command line.
- The program will produce a .csv file in the same directory called "CitationFinderOutput.csv".

The main idea behind the implementation of the CitationFinder is to use regular expressions to 
match chunks of text that look like they may be citations.  Each citation candidate is then
checked against the list of Reporters provided in the problem spec.  For each file, we store a  
collection of citations and the number of times that citation was found in the file.  This collection
is then written to a file.

CitationFinder is a multi-threaded application, utilizing the natural parallelizability of
the task.  The complete collection of source files is broken up into a set of subcollections
of roughly equal size which are processed in parallel.  The set of files in each subcollection 
is processed sequentially.

As with any real-world data, the source files do contain some noise in the form of unexpected 
characters.  CitationFinder provides some robustness against this noise in the form of finding 
"near matches".  While I believe that this functionality can be fine-tuned to improve accuracy,
my empirical testing indicated that none of my naive near-matching attempts proved very useful.

I have made a number of assumptions in the implementation of CitationFinder.  Most of them are
documented in the code in the form of TODOs. 

Some pros of my implementation:
- It is my belief that I haven't gotten too many false negatives.  The regex I used will match any
  reasonably well-formed citation, either abbreviated or canonical.  
- Parallel processing improves performance.  I've been clocking the runtime at a little under
  4 minutes per thread (~30 sec total runtime on my MacBook pro), which I think is reasonably fast.

There are some issues with CitationFinder that I am aware of or concerned about:

- Regexes are rigid.  While I think the regex used in this implementation is pretty robust,
  it may be the case the regex is not the right tool for noisy data or data that may not
  be formatted correctly.
 
- I think that this program could be made faster.  In particular, I feel like there are ways
  to improve the i/o aspects of this implementation. 

I considered other implementations that I'll briefly detail here:

- Stream each file and look for integers.  When an integer is found, interpret it as the volume number,
  and continue streaming.  Determine whether the next characters could match any Reporter name.  If a match is found, 
  continue looking for a possible page number or abbreviated citation pattern.  We could be fairly generous in trying to 
  match the page number since the presence of a volume-reporter combination would indicate that it is likely that
  we are looking at a citation.  The set of Reporters could be stored in a Trie data structure, which would
  pair nicely with the streaming characters.
  
- I ran into a skewed distribution of workloads when I divided up the set of files among the 
  different threads.  Some threads ran quite a bit longer than others.  To handle this, one 
  option would have been to use a pool of unprocessed files from which each thread could draw
  when it finished processing the previous file.  This would minimize thread dead-time.
