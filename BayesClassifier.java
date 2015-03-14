package practice;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

/**
 * A concrete implementation of document classification using Multinomial Naive Bayes model.
 * Classification process: classify(feat1,...,featN) = argmax(P(cat)*PROD(P(featI|cat)
 * In this case feature (feat) are words and cat is the document categories.
 * 
 * Uses a class called Category to create objects of all categories 
 *
 * @author Aniket Deshpande
 *
 * @see http://nlp.stanford.edu/IR-book/html/htmledition/naive-bayes-text-classification-1.html#laplace
 * @see http://www.inf.ed.ac.uk/teaching/courses/inf2b/learnnotes/inf2b-learn-note07-2up.pdf
 * 
 * input and output documents are retrieved from and printed out in the workspace folder of the package
 *
 */

public class BayesClassifier {

	public static void main(String[] args) throws IOException{

		List<Category> categoryList = new ArrayList<Category>();

//		reading the both the input datasets
		ArrayList<ArrayList<String>> data = readData();		
	
		ArrayList<String> trainingData = data.get(0);
		ArrayList<String> testData = data.get(1);
		
//		buiding the classifier
    	categoryList = buildClassifier(trainingData, categoryList);
    	 	
//    	classifying the documents
    	ArrayList<String> newTestData = classifyDocuments(testData, categoryList);
    	
//    	printing the output
    	PrintWriter out = new PrintWriter("output2.txt");
    	out.println("bookId \t predictedLabel");
    	for(String tuple:newTestData){
    		String[] fields = tuple.split("\t");
    		out.println(fields[0] + " \t" + fields[12]);
    	}
    	out.close();

}

 /**
 *	Reads input from the 2 input documents
 * 	@return Separate Training and Test datasets including the table of contents from "input2.txt"
 **/

	public static ArrayList<ArrayList<String>> readData() throws FileNotFoundException{

    	File filename = new File("input1.txt");
		Scanner sc = new Scanner(filename);
		String firstWord = sc.next();
    	firstWord = firstWord.replaceAll("N=", "");
    	int trainingSize = Integer.parseInt(firstWord);
    	String line = sc.nextLine();
    	ArrayList<String> trainingData = new ArrayList<String>(); 
    	for(int i=0; i<trainingSize; i++){
    		trainingData.add(sc.nextLine()); 
    	}

		firstWord = sc.next();
    	firstWord = firstWord.replaceAll("M=", "");
    	int testSize = Integer.parseInt(firstWord);
    	line = sc.nextLine();
    	ArrayList<String> testData = new ArrayList<String>(); 
    	for(int i=0; i<testSize; i++){
    		testData.add(sc.nextLine()); 
    	}
    	
    	ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
    	ArrayList<String> newTrainingData = new ArrayList<String>();
    	ArrayList<String> newTestData = new ArrayList<String>();

    	filename = new File("input2.txt");
    	sc = new Scanner(filename);
    	while(sc.hasNextLine()){
    		line = sc.nextLine();
    		line = line.replaceAll("↵|[0-9]*\\([0-9]+\\)", " ");
    		line = line.replaceAll("( [ivxlc]+ )", "");
    		String[] sections = line.split("\t");
    		for(String tuple:trainingData){
    			String[] cols = tuple.split("\t");
    			if(cols[1].trim().equals(sections[0].trim())){
//    				adding another column to the test and training data which includes "table of contents" 
//    				(Intentionally commented out: not included in the final implementation)
//    				tuple = tuple + "\t" + sections[1]; 
    				newTrainingData.add(tuple);
    			}
    		}
    		for(String tuple:testData){
    			String[] cols = tuple.split("\t");
    			if(cols[0].trim().equals(sections[0])){
//    				adding another column to the test and training data which includes "table of contents" 
//    				(Intentionally commented out: not included in the final implementation)
//    				tuple = tuple + "\t" + sections[1];
    				newTestData.add(tuple);
    			}
    		}
    	}    	

    	data.add(newTrainingData);
    	data.add(newTestData);
    	sc.close();
    	return data;
	}
	
    /**
     * Builds the classifier. Creates an array of category objects, each of which has the Vocabulary of features (feat) 
     * and P(feat(i)|C) - the probability of feat(i) occurring in that document class 
     * (done in the class function) - 
     * for 'title', 'author' and 'table of contents' (Only 'title' used in the final implementation')
     * Also calculates the category prior probabilities
     * Features are tokenized and normalized using an English Analyzer
     *
     * @param Training Data 
     * @param A blank ArrayList of Category objects
     * @return Populated ArrayList of Category objects
     */
		
	public static List<Category> buildClassifier(ArrayList<String> trainingData, List<Category> categoryList) 
			throws IOException{
		
		String fields[] = new String[4];
		String currentCategory = "";		
		ArrayList<String> categoryWordList = new ArrayList<String>();
//		ArrayList<String> categoryAuthWordList = new ArrayList<String>();
//		ArrayList<String> categoryContentWordList = new ArrayList<String>();
		Category newCategory = new Category();
		double categoryDocCount = 0.0d;
		
		ArrayList<String> allWords = new ArrayList<String>();
//		ArrayList<String> allAuthWords = new ArrayList<String>();
//		ArrayList<String> allContentWords = new ArrayList<String>();
		for(String tuple:trainingData){
			fields = tuple.split("\t");
			allWords = createWordList(fields[2], allWords);
//			allAuthWords = createWordList(fields[3], allAuthWords);
//			allContentWords = createWordList(fields[3], allContentWords);
		}
		
		for(String tuple:trainingData){
			fields = tuple.split("\t");
			if(!fields[0].equals(currentCategory)){

				if(categoryWordList.size() != 0){
					newCategory.setWordProbability(categoryWordList, allWords);
//					newCategory.setAuthWordProb(categoryAuthWordList, allAuthWords);
//					newCategory.setContentWordProb(categoryContentWordList, allContentWords);
					newCategory.setCategoryProbability(categoryDocCount/(double)trainingData.size());
					categoryList.add(newCategory);					
				}				

				currentCategory = fields[0];
				newCategory = new Category();
				newCategory.setCategoryName(currentCategory);
				categoryWordList = new ArrayList<String>();
//				categoryAuthWordList= new ArrayList<String>();
//				categoryContentWordList= new ArrayList<String>();
				categoryDocCount = 1;
				
				categoryWordList = createWordList(fields[2], categoryWordList);
//				categoryAuthWordList = createWordList(fields[3], categoryAuthWordList);
//				categoryContentWordList = createWordList(fields[3], categoryContentWordList);
			}	
			
			else{
				categoryWordList = createWordList(fields[2], categoryWordList);
//				categoryAuthWordList = createWordList(fields[3], categoryWordList);
//				categoryContentWordList = createWordList(fields[4], contentWordList);
				categoryDocCount++;
			}
		}
		
		newCategory.setWordProbability(categoryWordList, allWords);
//		newCategory.setAuthWordProb(categoryAuthWordList, allAuthWords);
//		newCategory.setCategoryProbability(categoryDocCount/(double)trainingData.size());
		categoryList.add(newCategory);					
				
		return categoryList;
	}
	
    /**
     * Classifies the documents into a category which has the class conditional probability.
     * Words are selected from 'title', 'author', 'table of contents' columns separately 
     * (only words from 'title' used in the final implementation)
     * for each document in the test dataset and corresponding (P(cat)*PROD(P(feat(i)|cat)) are calculated 
     * Finally, classify(feat1,...,featN) = argmax(P(cat)*PROD(P(feat(i)|cat))
     * 
     * @param Test Data 
     * @param ArrayList of Category objects
     * @return Populated ArrayList of Category objects
     */
	
	public static ArrayList<String> classifyDocuments(ArrayList<String> testData, List<Category> categoryList) throws IOException{
		
		String fields[] = new String[3]; 
		ArrayList<String> newTestData = new ArrayList<String>();

		for(String tuple:testData){
			fields = tuple.split("\t");
			ArrayList<String> tupleWordList = new ArrayList<String>();
//			ArrayList<String> tupleAuthWordList = new ArrayList<String>();
//			ArrayList<String> tupleContentWordList = new ArrayList<String>();
			tupleWordList = createWordList(fields[1], tupleWordList);
//			tupleAuthWordList = createWordList(fields[2], tupleWordList);
//			tupleContentWordList = createWordList(fields[3], tupleWordList);
			
			String maxProbCategory = "";
			double maxProb = 0.0d;
			
			for(Category cat:categoryList){
				HashMap<String, Double> wordMap = cat.getWordProbability();
//				HashMap<String, Double> authWordMap = cat.getAuthWordProb();
//				HashMap<String, Double> contentWordMap = cat.getContentWordProb();
//				double catProb = cat.getCategoryProbability();
				double catProb = 1.0;
				
				for(String word:tupleWordList){
					if(wordMap.containsKey(word)){
						catProb *= wordMap.get(word);
					}
				}
				
//				for(String word:tupleAuthWordList){
//					if(authWordMap.containsKey(word)){
//						catProb *= authWordMap.get(word);
//					}
//				}				
				
//				for(String word:tupleContentWordList){
//				if(contentWordMap.containsKey(word)){
//					catProb *= contentWordMap.get(word);
//				}
//			}				
				
				if(catProb > maxProb){
					maxProb = catProb;
					maxProbCategory = cat.getCategoryName();
				}
				tuple = tuple + "\t" + catProb;
			}
			
			tuple = tuple + "\t" + maxProbCategory;
			newTestData.add(tuple);
		}
		
		return newTestData;
	}

    /**
     * Splits, Tokenizes and then normalizes a String of words 
     * using the English Analyzer and appends it to a given List
     * 
     * @param String of words appearing in a field ('title', 'author' or 'contents') of a document
     * @param List of all processed normalized words in a category
     * @return Populated ArrayList of Category objects
     */
	
	public static ArrayList<String> createWordList(String wordList, ArrayList<String> categoryWordList) throws IOException{
	    Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_45);
		StringReader sr = new StringReader(wordList);
		TokenStream stream = analyzer.tokenStream(null, sr);
		List<String> result = new ArrayList<String>();
		stream.reset();
		while (stream.incrementToken()) {
			result.add(stream.getAttribute(CharTermAttribute.class).toString());
		}
		
		String title = result.toString().replaceAll("\\[|\\]", "");
		String words[] = title.split(", ");
		for (String word:words){
			categoryWordList.add(word);
		}

		analyzer.close();
		return categoryWordList;
	}
}
