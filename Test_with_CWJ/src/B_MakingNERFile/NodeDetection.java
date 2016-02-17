package B_MakingNERFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class NodeDetection {

	static final double CHUNK_SCORE = 1.0;

	public void NodeDectector(LinkedHashMap<String, LinkedHashSet<String>> splitSentence_for_each_line,
			String UMLSDictionaryPath, String TriggerDictionaryPath, String EntityOneDictionaryPath, String AnnFileFolder_Function, String AnnFileFolder_Moa, String AnnFileFolder_Toxicity) throws IOException {

		/*
		 * 
		 * Dictionary loading
		 * 
		 */

		// Phenotype Dictionary load
		MapDictionary<String> Phenotype_dictionary = new MapDictionary<String>();
		File PDicfile = new File(UMLSDictionaryPath);
		BufferedReader PDic_br = new BufferedReader(new FileReader(PDicfile));
		String Pline = null;

		System.out.println("Dictionary loading....");
		while ((Pline = PDic_br.readLine()) != null) {
			String[] contents = Pline.split("\t");
			String CUI = contents[0];
			String TUI = contents[1];
			String STR = contents[2];

			Phenotype_dictionary.addEntry(new DictionaryEntry<String>(STR, CUI + "\t" + TUI, CHUNK_SCORE));

		}
		System.out.println("Phenotype Dictionary is sucessfully loaded");

		// Trigger Dictionary load
		MapDictionary<String> Trigger_dictionary = new MapDictionary<String>();
		File TDicfile = new File(TriggerDictionaryPath);
		BufferedReader TDic_br = new BufferedReader(new FileReader(TDicfile));
		String Tline = null;

		System.out.println("Trigger Dictionary loading....");
		while ((Tline = TDic_br.readLine()) != null) {
			String[] contents = Tline.split("\t");
			String word = contents[0];
			String id = contents[1];

			Trigger_dictionary.addEntry(new DictionaryEntry<String>(word, id, CHUNK_SCORE));

		}
		System.out.println("Trigger Dictionary is sucessfully loaded");

		// EntityOne Dictionary load
		MapDictionary<String> EntityOne_dictionary = new MapDictionary<String>();
		File EntityOneDicfile = new File(EntityOneDictionaryPath);
		BufferedReader EntityOneDic_br = new BufferedReader(new FileReader(EntityOneDicfile));
		String EntityOneline = null;
		
		System.out.println("EntityOne Dictionary loading....");
		while ((EntityOneline = EntityOneDic_br.readLine()) != null) {
			String[] contents = EntityOneline.split("\t");
			String EntityOneName = contents[1];
			String EntityOneRef_ID = contents[0];
			EntityOne_dictionary.addEntry(new DictionaryEntry<String>(EntityOneName, EntityOneRef_ID, CHUNK_SCORE));

		}
		System.out.println("EntityOne Dictionary is sucessfully loaded");

		
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("Making annotation files in progress........................ ");
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");

		ExactDictionaryChunker P_dictionaryChunkerTF = new ExactDictionaryChunker(Phenotype_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		ExactDictionaryChunker T_dictionaryChunkerTF = new ExactDictionaryChunker(Trigger_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		ExactDictionaryChunker EntityOne_dictionaryChunkerTF = new ExactDictionaryChunker(EntityOne_dictionary,
				IndoEuropeanTokenizerFactory.INSTANCE, true, false);
		
		LinkedHashSet<String> Ptemp_chunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Pchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> Tchunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> EntityOnechunk_result = new LinkedHashSet<String>();

		for (Entry<String, LinkedHashSet<String>> entry : splitSentence_for_each_line.entrySet()) { // split된 sentence마다 annotated phenotype 출력.

			String key = entry.getKey();
			String[] key_split = key.split("\t");
			String KindofText = key_split[0];
			String dataType = key_split[1];
			String EntityOneRef_ID = key_split[2].toLowerCase().trim();
			String EntityOneName = key_split[3];
			String allType_text = key_split[4];
			
			
			StringBuffer makeReference = new StringBuffer();			
			for (int j = 0; j < EntityOneRef_ID.length(); j++) {
				char c = EntityOneRef_ID.charAt(j);
				String c_string = "" + c;
				if (c_string.equals("[")) {
					break;
				} else {
					makeReference.append(c_string);
				}
			}
			
			String EntityOneReference = makeReference.toString();
			String EntityOneID = EntityOneRef_ID.substring(makeReference.length());
					
			String AnnFileFolder = "D:/JUN/MCMT/REmodule/annotated/";
			
			LinkedHashSet<String> value = new LinkedHashSet<String>();
			value.clear();
			value = entry.getValue();

			// write file path
			
			if(KindofText.equals("Fun")){
				AnnFileFolder= AnnFileFolder_Function;
			}
			else if(KindofText.equals("Moa")){
				AnnFileFolder= AnnFileFolder_Moa;
			}
			else if(KindofText.equals("Tox")){
				AnnFileFolder = AnnFileFolder_Toxicity;
			}
			else{
			}
			
			File files = new File(AnnFileFolder + EntityOneID + ".ann");
			BufferedWriter out = new BufferedWriter(new FileWriter(files, true));

			out.write("ID_OriginalText_Reference:" + dataType + "\t" + EntityOneID + "\t" + EntityOneName + "\t" + EntityOneReference.toUpperCase() + "\t" + allType_text);
			out.newLine();

			// each split sentence in an line.
			for (String val : value) {
				out.write("SplitSentence:" + val);
				out.newLine();

				/*
				 * 
				 * Put in the code
				 * 
				 */
				
				Ptemp_chunk_result.clear();
				Pchunk_result.clear();
				Ptemp_chunk_result = Pchunk(P_dictionaryChunkerTF, val.toLowerCase(), "BROMFED-DM", "9");
				Pchunk_result = filtering(Ptemp_chunk_result);
				for (String p : Pchunk_result) {
					out.write("Phenotype:" + p);
					out.newLine();
				}
				
				/*
				 * 
				 * **********************************************
				 * 
				 */

				Tchunk_result.clear();
				Tchunk_result = Tchunk(T_dictionaryChunkerTF, val.toLowerCase().trim(), "BROMFED-DM", "9");
				for (String t : Tchunk_result) {
					out.write("Trigger:" + t);
					out.newLine();
				}
				
				EntityOnechunk_result.clear();
				EntityOnechunk_result = EntityOnechunk(EntityOne_dictionaryChunkerTF, val.toLowerCase().trim(), "BROMFED-DM", "9");
				for (String e : EntityOnechunk_result) {
					out.write("EntityOne:" + e);
					out.newLine();
				}
					
				// For delimiters in the ann file.
				out.write("@@@");
				out.newLine();
			}
			out.close();
		}
	}

	// Annotated phenotype mention filtering (e.g., cold vs common cold -> common cold)
	static LinkedHashSet<String> filtering(LinkedHashSet<String> hash) {
		LinkedHashSet<String> new_chunk_result = new LinkedHashSet<String>();
		LinkedHashSet<String> new_new_chunk_result = new LinkedHashSet<String>();

		for (String h1 : hash) {
			String[] contents = h1.split("\t");
			String phrase = contents[0];
			String start = contents[1];
			String end = contents[2];
			String cui = contents[3];
			String tui = contents[4];
			String saveEnd = "";
			int counter = 0;
			for (String h2 : hash) {
				String[] contents_new = h2.split("\t");
				if (start.toLowerCase().trim().equals(contents_new[1].toLowerCase().trim())) {
					saveEnd = saveEnd + contents_new[2] + "\t";
					++counter;
				}
			}
			if (counter == 0) {
				int counter_new = 0;
				for (String h3 : hash) {
					String[] contents_new = h3.split("\t");
					if (end.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
						if (Integer.valueOf(start.toLowerCase().trim()) > Integer
								.valueOf(contents_new[1].toLowerCase().trim())) {
							++counter_new;
						}
					}
				}
				if (counter_new == 0) {
					new_chunk_result.add(h1);
				} else {
					continue;
				}
			} else {
				String[] End_contents = saveEnd.split("\t");
				String newEnd = "";
				for (String n : End_contents) {
					newEnd = n;
				}
				int counter_new_new = 0;
				for (String h3 : hash) {
					String[] contents_new = h3.split("\t");
					if (newEnd.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
						if (Integer.valueOf(start.toLowerCase().trim()) > Integer
								.valueOf(contents_new[1].toLowerCase().trim())) {
							++counter_new_new;
						}
					}
				}
				if (counter_new_new == 0) {
					for (String h3 : hash) {
						String[] contents_new = h3.split("\t");
						if (newEnd.toLowerCase().trim().equals(contents_new[2].toLowerCase().trim())) {
							if (start.toLowerCase().trim().equals(contents_new[1].toLowerCase().trim())) {
								new_chunk_result.add(h3);
							}
						}
					}
				} else {
					continue;
				}
			}
		}

		// term1+term2+term3 이상 되있는 경우 term2 빼기.
		for (String str : new_chunk_result) {
			String[] contents = str.split("\t");
			int counter = 0;

			for (String str_new : new_chunk_result) {
				String[] contents2 = str_new.split("\t");
				if (Integer.valueOf(contents[1]) > Integer.valueOf(contents2[1])) {
					if (Integer.valueOf(contents[2]) < Integer.valueOf(contents2[2])) {
						++counter;
					}
				}
			}
			if (counter == 0) {
				new_new_chunk_result.add(str);
			} else {
				continue;
			}
		}
		return new_new_chunk_result;
	}

	static LinkedHashSet<String> Pchunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID)
			throws IOException {
		LinkedHashSet<String> result = new LinkedHashSet<String>();
		result.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words
			if (phrase.length() < 4) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("used")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("tuber")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("root")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("growth")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("other")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("null")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("indicated")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("induced")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("ease")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("arrest")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("symptom")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("distressed")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("painful")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("body")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("energy")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("syndrome")) {
				continue;
			}
			if (phrase.toLowerCase().trim().equals("like")) {
				continue;
			}

			result.add(phrase + "\t" + start + "\t" + end + "\t" + type);
		}
		return result;
	}

	static LinkedHashSet<String> Tchunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID)
			throws IOException {
		LinkedHashMap<String, String> result_map = new LinkedHashMap<String, String>();
		LinkedHashSet<String> result_set = new LinkedHashSet<String>();
		result_map.clear();
		result_set.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words
			if (end + 3 <= text.length()) {
				if (text.toLowerCase().trim().substring(start, end + 3).equals(phrase.toLowerCase().trim() + " by")) {
					continue;
				}
			}
			if (result_map.containsKey(phrase + "\t" + start + "\t" + end)) {
				result_map.put(phrase + "\t" + start + "\t" + end, result_map.get(phrase + "\t" + start + "\t" + end) + "|" + type);
			} else {
				result_map.put(phrase + "\t" + start + "\t" + end, type);
			}
		}

		for (Entry<String, String> entry : result_map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result_set.add(key + "\t" + value);
		}
		return result_set;
	}
	
	static LinkedHashSet<String> EntityOnechunk(ExactDictionaryChunker chunker, String text, String entityname, String entityID) throws IOException {
		LinkedHashMap<String, String> result_map = new LinkedHashMap<String, String>();
		LinkedHashSet<String> result_set = new LinkedHashSet<String>();
		result_map.clear();
		result_set.clear();

		Chunking chunking = chunker.chunk(text);
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			String type = chunk.type();
			double score = chunk.score();
			String phrase = text.substring(start, end);

			// stop words
			if (end + 3 <= text.length()) {
				if (text.toLowerCase().trim().substring(start, end + 3).equals(phrase.toLowerCase().trim() + " by")) {
					continue;
				}
			}
			if (result_map.containsKey(phrase + "\t" + start + "\t" + end)) {
				result_map.put(phrase + "\t" + start + "\t" + end, result_map.get(phrase + "\t" + start + "\t" + end) + "|" + type);
			} else {
				result_map.put(phrase + "\t" + start + "\t" + end, type);
			}
		}

		for (Entry<String, String> entry : result_map.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			result_set.add(key + "\t" + value);
		}
		return result_set;
	}

}
