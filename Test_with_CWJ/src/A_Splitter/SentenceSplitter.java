package A_Splitter;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

import com.aliasi.util.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Use SentenceModel to find sentence boundaries in text */
public class SentenceSplitter {

	static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
	static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();
	static LinkedHashMap<String, LinkedHashSet<String>> sentences_map = new LinkedHashMap<String, LinkedHashSet<String>>();
	
	static void split_allTypeText(String EntityOneID , String EntityOneName , String dataType, String allType_text, String KindofText){
		
		
		LinkedHashSet<String> splitsentence = new LinkedHashSet<String>();

		List<String> tokenList = new ArrayList<String>();
		List<String> whiteList = new ArrayList<String>();
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(allType_text.toCharArray(), 0, allType_text.length());
		tokenizer.tokenize(tokenList, whiteList);

		String[] tokens = new String[tokenList.size()];
		String[] whites = new String[whiteList.size()];
		tokenList.toArray(tokens);
		whiteList.toArray(whites);
		int[] sentenceBoundaries = SENTENCE_MODEL.boundaryIndices(tokens, whites);

		int sentStartTok = 0;
		int sentEndTok = 0;
		for (int i = 0; i < sentenceBoundaries.length; ++i) {
			String temp = "";
			sentEndTok = sentenceBoundaries[i];
			for (int j = sentStartTok; j <= sentEndTok; j++) {
				temp += tokens[j] + whites[j + 1];
			}
			sentStartTok = sentEndTok + 1;

			if (temp.contains("/")) {
				String[] temp_split = temp.split(" / ");
				for (String t : temp_split) {
					if (!t.trim().toLowerCase().equals("null")) {
						// remove phrase whose length is less than 3.
						if (t.length() < 3) {
							continue;
						}
						splitsentence.add(t.toLowerCase().trim());
					}
				}

			} else {
				// remove phrase whose length is less than 3.
				if (temp.length() < 3) {
					continue;
				}
				splitsentence.add(temp.toLowerCase().trim());
			}
		}
			
		if (sentences_map.containsKey(KindofText + "\t" + dataType + "\t"+ EntityOneID + "\t" + EntityOneName + "\t" + allType_text)) {
			sentences_map.put(KindofText + "\t" + dataType + "\t"+ EntityOneID + "\t" + EntityOneName + "\t" + allType_text, splitsentence);
		} else {
			sentences_map.put(KindofText + "\t" + dataType + "\t"+ EntityOneID + "\t" + EntityOneName + "\t" + allType_text, splitsentence);
		}
	}
	
	public LinkedHashMap<String, LinkedHashSet<String>> Splitter(File FunctionTextFile) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(FunctionTextFile));
		String textLine = null;

		

		while ((textLine = br.readLine()) != null) {
			System.out.println(textLine);

			String[] textLine_split = textLine.split("\t");
			String EntityOneID = textLine_split[0];
			String EntityOneName = textLine_split[1];
			String Function_text = textLine_split[2].toLowerCase().trim();
			String Moa_text = textLine_split[3].toLowerCase().trim();
			String Toxicity_text = textLine_split[4].toLowerCase().trim();
			
			if(Function_text.equals("na")){	}
			else{
				split_allTypeText(EntityOneID, EntityOneName, Function_text.substring(0, 4), Function_text.substring(4), "Fun");
			}
			
			if(Moa_text.equals("na")){	}	
			else{
				split_allTypeText(EntityOneID, EntityOneName, Moa_text.substring(0, 4),		 Moa_text.substring(4),		 "Moa");
			}
			
			if(Toxicity_text.equals("na")){}
			else{
				split_allTypeText(EntityOneID, EntityOneName, Toxicity_text.substring(0, 4), Toxicity_text.substring(4), "Tox");
			}

		}
		return sentences_map;
	}
}