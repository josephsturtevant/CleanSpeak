
public class DictionaryEntry {
	public String[] data;
	public int exactMatchCounter;
	public int subStringMatchCounter;
	
	public DictionaryEntry(String[] data){
		this.data = new String[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
		exactMatchCounter = 0;
		subStringMatchCounter = 0;
	}
}
