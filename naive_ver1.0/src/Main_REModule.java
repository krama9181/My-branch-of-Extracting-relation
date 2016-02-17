import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import EdgeDetector.ExtractRelation;
import LingPipe.SentenceSplitter;
import NodeDetector.NodeDetection;

public class Main_REModule {
	
	static final double CHUNK_SCORE = 1.0;
	 
	
	///chchchchchange////
	
	/*
	 * 
	 * File Path Declaration ( You should change the path )
	 * 
	 */
	static String path = "D:/Desktop/MCMT/";
	
	static String InputTextFile = path + "taggedInput_random200.txt"; // input text  
//	static String InputTextFile = path + "test.txt"; // input text  
	static String AnnFileFolder_Function = path + "annotated/about_Function/"; // Folder path whose annotation files will be stored.
	static String AnnFileFolder_Moa = path + "annotated/about_Moa/";
	static String AnnFileFolder_Toxicity = path + "annotated/about_Toxicity/";
	static String UMLSDictionaryPath = path + "UMLS_DICTIONARY_FOR_20_SEM.txt"; // UMLS dictionary path
	static String TriggerDictionaryPath = path + "Integrated_Triggers.txt"; // Trigger dictionary path
	static String RelationResultOutputPath = path + "output_DCDB.tsv"; // Final output path
	
	public static void main(String[] args) throws Exception {
			
		createFolder();		
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
	
	public static void createFolder() {
		File annotationFolder = new File(path + "annotated");
		if (!annotationFolder.exists())
			annotationFolder.mkdir();
		else
			System.out.println("Already exists!");
		
		File myFolder = new File(annotationFolder + "/" + "about_Function");
		if (!myFolder.exists())
			myFolder.mkdir();
		else
			System.out.println("Already exists!");
		
		myFolder = new File(annotationFolder + "/" + "about_Moa");
		if (!myFolder.exists())
			myFolder.mkdir();
		else
			System.out.println("Already exists!");
		
		myFolder = new File(annotationFolder + "/" + "about_Toxicity");
		if (!myFolder.exists())
			myFolder.mkdir();
		else
			System.out.println("Already exists!");
	}
}
