package NodeDetector;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Position;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

public class Metamap {
	static String preprocessing(String input) {			
		String result = input.replaceAll("[^a-zA-Z0-9 ]{2,}", "__");
		return result;
	}
	
	static String getPhrase(String text, int start, int end) throws IOException {
		if(start < 0 || start > text.length() || text.length() < end){
			return "NULL";
		}	

		String result = text.substring(start, end);
		return result;
	}

	static LinkedHashSet<String> metamap(MetaMapApi api, String input) throws Exception {		
		LinkedHashSet<String> result = new LinkedHashSet<String>();	
//		System.out.println("api instanciated");
		List<Result> resultList = api.processCitationsFromString(input);
		Result NER_result = resultList.get(0);
		
		for (Utterance utterance : NER_result.getUtteranceList()) {
			System.out.println("Utterance:");
			System.out.println(" Utterance text: " + utterance.getString());
			for (PCM pcm : utterance.getPCMList()) {
				for (Mapping map : pcm.getMappingList()) {					
					for (Ev mapEv : map.getEvList()) {						
						Position start_end = mapEv.getPositionalInfo().get(0);
						int start = start_end.getX();
						int end = start + start_end.getY();
						
						String phenotype_name = getPhrase(utterance.getString(), start, end);
						String UMLS_ID = mapEv.getConceptId();
						String UMLS_Symantic_type = mapEv.getSemanticTypes().get(0);
						String tmp = phenotype_name + "\t" + start + "\t" + end + "\t" + UMLS_ID + "\t" + UMLS_Symantic_type;
						System.out.println(tmp);
						result.add(tmp);
					}
				}
			}
		}
		return result;
	}
}