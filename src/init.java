import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
//import java.util.Arrays;

//Requires one argument, name of input file list
//Second argument of dictionary name optional, defaults to csdb.csv
public class init {
	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
		if (args.length == 0){
			System.out.println("Requires inputFileName argument");
			System.out.println("");
			System.out.println("Usage:");
			System.out.println("init inputFileName (dictionaryFileName)");
			return;
		} else if (args.length > 3){
			System.out.println("Too many arguments");
			return;
		}
		String testWordList = args[0];
		String filePath = new File("").getAbsolutePath();
		String testWordListLoc = filePath + "/" + testWordList;
		String outputFileName = "out.csv";
		String altOutputFileName = "altOut.csv";
		String countersOutputFileName = "counters.csv";
		String dictionaryOutputFileName = "dictionaryUsed.csv";
		boolean altOutput = false;
		String charSet = "UTF-8";
		int outputCounter = 100;

		//Output changed to UTF-8 to better represent characters
		PrintStream out = new PrintStream(new FileOutputStream(outputFileName), true, charSet);
		PrintStream altOut = new PrintStream(new FileOutputStream(altOutputFileName), true, charSet);
		try {
			DictionaryCheck dictionary;
			switch (args.length){
				case 3: 
					dictionary = new DictionaryCheck(args[1]);
					altOutput = true;
					break;
				case 2:
					dictionary = new DictionaryCheck(args[1]);
					break;
				default:
					dictionary = new DictionaryCheck();
			}
			if (args.length == 2){
				dictionary = new DictionaryCheck(args[1]);
			} else {
				dictionary = new DictionaryCheck();
			}
			if (altOutput){
				
				altOut.println("Words not filtered");
			}
			FileInputStream fIn = new FileInputStream(testWordListLoc);
			BufferedReader br = new BufferedReader(new InputStreamReader(fIn, charSet));
			String word;
			//Output to .csv
			//Output is Word,exact/not_exact,language,Type,severity,substring,language,type,severity,substring,...
			out.println("Word,Match Type,Language,Type,Severity,Substring Match,Language,Type,Severity,Substring Match,...");
			int wordCount = 0;
			int matchedWordCount = 0;
			String s = "";
			System.out.print("Checking word ");
			while((word = br.readLine()) != null){
				if(++wordCount % outputCounter == 0){			
					if (wordCount > outputCounter + 1){
						for (int i = 0;i < s.length();i++){
							System.out.print("\b");
						}
					}
					s = Integer.toString(wordCount);
					System.out.print(s);
				}
				String[] match = dictionary.exactMatch(word);
				ArrayList<String[]> al = dictionary.subStringMatch(word);
				//If exactMatch and subStringMatch return nothing
				if (match[0].isEmpty() && al.get(0)[0].isEmpty()){
					//Print to alternate out
					if(altOutput){
						altOut.println(word);
					}
					continue;
				}
				if (match[0].isEmpty()){
					out.print(word + ",not_exact,,,");
				} else {
					out.print(word + ",exact," + match[1] + "," + match[3] + "," + match[4]);
				}
				//Check if there's any substring matches
				if (!al.get(0)[0].isEmpty()){
					for (String[] entry : al){
						//Don't reprint exact match as substring match
						if (entry.equals(match)) continue;
						out.print("," + entry[0] + "," + entry[1] + "," + entry[3] + "," + entry[4]);
					}
				}
				out.print("\n");
				//Will only get this far if either exactMatch or subStringMatch returned anything.
				matchedWordCount++;
			}
			fIn.getChannel().position(0);
			System.out.println("");
			System.out.println("Total words checked: " + wordCount);
			System.out.println("Total words flagged: " + matchedWordCount);
			fIn.close();
			br.close();
			altOut.close();
			//Print the dictionary we used
			dictionary.printDictionary(dictionaryOutputFileName);
			//Print the counters
			dictionary.printCounters(countersOutputFileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		out.close();
	}

}
