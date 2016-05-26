import java.io.BufferedReader;
//import java.io.FileNotFoundException;
//import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class DictionaryCheck {
	private String failResponse = "";
	private String filePath = new File("").getAbsolutePath();
	private String defaultFileName = "csdb.csv";
	private String dictionaryLoc;
	private Map<String, DictionaryEntry> dictionary = new HashMap<String, DictionaryEntry>();
	
	//Reads csdb.csv into a map
	//Map is <First entry, [Entire row]>
	public DictionaryCheck() throws IOException{
		dictionaryLoc = filePath + "/" + defaultFileName;
		buildDictionary();
	}
	
	public DictionaryCheck(String newDictionaryLoc) throws IOException{
		dictionaryLoc = filePath + "/" + newDictionaryLoc;
		buildDictionary();
	}
	
	//Checks entry to see if it should be excluded.
	//Returns true if it should be excluded
	private boolean excluded(String[] entry){
		//Don't include one character filters (numbers or single letters)
		if (entry[0].length() < 2) return true;
		//Only include certain english two char words
		if (entry[0].length() == 2 && entry[1].equals("en") && 
				 !(entry[0].equals("bs") || entry[0].equals("ho") || entry[0].equals("fk") 
				|| entry[0].equals("bj") || entry[0].equals("mf") || entry[0].equals("69")
				|| entry[0].equals("fu") || entry[0].equals("vj") || entry[0].equals("fg")
				)) return true;
		//Don't include words with ' ' (space)
		if (entry[0].contains(" ")) return true;
		//Exclude en words
		//if(entry[1].equals("en")) return true;
		//Don't include the numbers 0-9, or the word spelling of
		//if (entry[3].equals("Numbers")) return true;
		//Don't include medium severity words
		//if (entry[4].equals("medium")) return true;
		//Don't include mild severity words
		//if (entry[4].equals("mild")) return true;
		//Don't include none severity words
		//if (entry[4].equals("none")) return true;
		
		return false;
	}
	
	private void buildDictionary() throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dictionaryLoc), "UTF-8"));
		try{
			//Skip first line of input dictionary, which is the headers
			String line = br.readLine();
			while ((line = br.readLine())  != null){
				//Regex logic
				//Split line into the parts we're interested in
				//Groups:
				//1:	Anything between two "'s with a comma following. The following comma
				//		check is needed because some comments have multiple double "'s.
				//2:	A start " with no end "
				//3:	Empty entries. A ',,'
				//4:	Anything else. Anything that's not a , or newline
				String regex = "(\"[\\s\\S]+?\"(?=,))|(\".+)|((?<=,)(?=,))|([^,\\n]+)";
				Matcher m = Pattern.compile(regex).matcher(line);
				ArrayList<String> al = new ArrayList<String>();
				while(m.find()){
					//If there's a start quote with no end quote that spans multiple lines
					if (m.group(2) != null){
						String splitLines = m.group();
						line = br.readLine();
						String endQuoteRegex = ".+\",";
						Matcher endQuoteMatcher = Pattern.compile(endQuoteRegex).matcher(line);
						//Keep searching new lines until the end quote is found. Add
						//each new line to existing string
						while(!endQuoteMatcher.find()){
							splitLines += (" " + line);
							line = br.readLine();
							endQuoteMatcher = Pattern.compile(endQuoteRegex).matcher(line);
						}
						splitLines += (" " + endQuoteMatcher.group());
						al.add(splitLines);
						//Check the rest of the line (if there is any)
						splitLines = line.substring(endQuoteMatcher.end(),line.length());
						m = Pattern.compile(regex).matcher(splitLines);
					} else {
						al.add(m.group());
					}
				}
				String[] split = new String[al.size()];
				al.toArray(split);
				//Check to see if we should exclude this term from map
				if (!excluded(split)){
					//Put term in map
					DictionaryEntry de = new DictionaryEntry(split);
					dictionary.put(split[0].toLowerCase(), de);
					//Check for variations
					if (!split[2].isEmpty()){
						String[] variations = split[2].split(";");			
						for (int i = 0;i < variations.length;i++){
							String[] newSplit = new String[split.length];
							System.arraycopy(split,0,newSplit,0,split.length);
							newSplit[0] = variations[i].toLowerCase();
							if(!excluded(newSplit)){
								DictionaryEntry de2 = new DictionaryEntry(newSplit);
								dictionary.put(variations[i].toLowerCase(), de2);
							}
						}
					}
				}
			}
		} finally {
			br.close();
		}
	}
	
	public void printDictionary(String outputFileName) throws UnsupportedEncodingException, FileNotFoundException{
		String charSet = "UTF-8";
		PrintStream out = new PrintStream(new FileOutputStream(outputFileName), true, charSet);
		out.println("text,locale,variations,tags,severity,adjective,adverb,collapseDoubles,filterMode,noun,replacePhonetics,verb,definition,entryIgnores");
		for (Map.Entry<String, DictionaryEntry> entry : dictionary.entrySet()){
			String s = Arrays.toString(entry.getValue().data);
			out.println(s.substring(1, s.length() - 1));
		}
		out.close();
	}
	
	public void printCounters(String outputFileName) throws UnsupportedEncodingException, FileNotFoundException{
		String charSet = "UTF-8";
		PrintStream out = new PrintStream(new FileOutputStream(outputFileName), true, charSet);
		out.println("SubString Matches,Exact Matches,Word");
		for (Map.Entry<String, DictionaryEntry> entry : dictionary.entrySet()){
			if(entry.getValue().subStringMatchCounter != 0 || entry.getValue().exactMatchCounter != 0){
				out.println(entry.getValue().subStringMatchCounter + "," + entry.getValue().exactMatchCounter + "," + entry.getKey());
			}
		}
		out.close();
	}
	
	public String[] exactMatch(String word){
		String w = word.toLowerCase();
		if (dictionary.containsKey(w)){
			//Increment counter
			DictionaryEntry de = dictionary.get(w);
			de.exactMatchCounter += 1;
			dictionary.put(w, de);
			return dictionary.get(w).data;
		} else {
			String[] s = {failResponse};
			return s;
		}
	}
	
	public ArrayList<String[]> subStringMatch(String word){
		String w = word.toLowerCase();
		ArrayList<String[]> entries = new ArrayList<String[]>();
		for(Entry<String, DictionaryEntry> entry : dictionary.entrySet()) {
			String key = entry.getKey();
			if (w.contains(key) && !key.equals("")){				
				//Increment counter
				DictionaryEntry de = dictionary.get(key);
				de.subStringMatchCounter += 1;
				dictionary.put(key, de);
				entries.add(dictionary.get(key).data);
			}
		}
		if (entries.size() == 0){
			String[] s = {failResponse};
			entries.add(s);
		}
		return entries;
	}
	
}
