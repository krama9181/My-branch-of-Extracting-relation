import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import A_Splitter.SentenceSplitter;
import B_MakingNERFile.NodeDetection;
import C_ExtractRelation.ExtractRelation;

public class Main_REModule {
	
	static final double CHUNK_SCORE = 1.0;
	 
	/*
	 * 
	 * File Path Declaration ( You should change the path )
	 * 
	 */
	
	static String InputTextFile = "D:/JUN/MCMT/End_of_project/tagged_input/taggedData_distinguishMeta.txt"; // input text  
	static String AnnFileFolder_Function = "D:/JUN/MCMT/End_of_project/annotated/about_Function/"; // Folder path whose annotation files will be stored.
	static String AnnFileFolder_Moa = "D:/JUN/MCMT/End_of_project/annotated/about_Moa/";
	static String AnnFileFolder_Toxicity = "D:/JUN/MCMT/End_of_project/annotated/about_Toxicity/";
	static String UMLSDictionaryPath = "D:/JUN/MCMT/REmodule/dictionary/UMLS_DICTIONARY_FOR_20_SEM.txt"; // UMLS dictionary path
	static String TriggerDictionaryPath = "D:/JUN/MCMT/REmodule/dictionary/Integrated_Triggers.txt"; // Trigger dictionary path
	static String RelationResultOutputPath = "D:/JUN/MCMT/End_of_project/Output/output_distinguishMeta_up.tsv"; // Final output path
	
	public static void main(String[] args) throws IOException {
		
		
		SentenceSplitter splitter = new SentenceSplitter();
		NodeDetection node_detector = new NodeDetection();
		ExtractRelation relation = new ExtractRelation();

		/*
		 * 
		 * Enter Input Texts
		 * 
		 */
		File FunctionTextFile = new File(InputTextFile);

		/*
		 * 
		 * Sentence splitter
		 * 
		 */
		LinkedHashMap<String, LinkedHashSet<String>> splitSentence_for_each_line = new LinkedHashMap<String, LinkedHashSet<String>>();
		splitSentence_for_each_line = splitter.Splitter(FunctionTextFile);

		/*
		 * 
		 * Phenotype Detector, Trigger Detector
		 * 
		 */
		node_detector.NodeDectector(splitSentence_for_each_line, UMLSDictionaryPath, TriggerDictionaryPath,	InputTextFile, AnnFileFolder_Function, AnnFileFolder_Moa, AnnFileFolder_Toxicity);

		/*
		 * 
		 * //Relation Extraction
		 * 
		 */
		LinkedHashSet<String> output = new LinkedHashSet<String>();
		File files = new File(RelationResultOutputPath); // print output
		relation.RelationDetector(AnnFileFolder_Function, AnnFileFolder_Moa, AnnFileFolder_Toxicity, files);					
		
	}
}
