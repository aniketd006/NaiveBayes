package practice;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *	Category Class to hold all category related attributes and functions
 *
 */


public class Category {
	
	private String categoryName;
	private double categoryProbabilty;
	private HashMap<String, Double> wordProbability;
	private HashMap<String, Double> authWordProb;
	private HashMap<String, Double> contentWordProb;
	
	public Category(){
		categoryName = "";
		categoryProbabilty = 0.0d;
		wordProbability = new HashMap<String, Double>();
		authWordProb = new HashMap<String, Double>();
	}
	
	public String getCategoryName(){
		return categoryName;
	}
	
	public double getCategoryProbability(){
		return categoryProbabilty;
	}
	
	public HashMap<String, Double> getWordProbability(){
		return wordProbability;
	}
	
	public HashMap<String, Double> getAuthWordProb(){
		return authWordProb;
	}
	
	public HashMap<String, Double> getContentWordProb(){
		return authWordProb;
	}
	
	public void setCategoryName(String name){
		categoryName=name;
	}
	
	public void setCategoryProbability(double prob){
		categoryProbabilty=prob;
	}
	
	public void setWordProbability(ArrayList<String> wordList, ArrayList<String> allWords){
		wordProbability = probabilityCalculation(wordList, allWords, wordProbability);
	}
	
	public void setAuthWordProb(ArrayList<String> wordList, ArrayList<String> allAuthWords){
		authWordProb = probabilityCalculation(wordList, allAuthWords, authWordProb);
	}
	
	public void setContentWordProb(ArrayList<String> wordList, ArrayList<String> allContentWords){
		authWordProb = probabilityCalculation(wordList, allContentWords, authWordProb);
	}

    /**
     * Calculates P(feat(i)|C) - the probability of feat(i) occurring in that document class - 
     * for 'title', 'author' and 'table of contents' (Only 'title' used in the final implementation')
     *
     * @param List of words appearing in the category documents 
     * @param List of words appearing in all documents across all categories
     * @param Emplty Map of word and corresponding category conditional probability
     * @return Populated Map of word and corresponding category conditional probability
     */
	
	public HashMap<String, Double> probabilityCalculation(ArrayList<String> wordList, ArrayList<String> allWords, HashMap<String, Double> wp){
		double size = (double) wordList.size();
		double count = 0.0d;
		
		double countDistinctVocab = 0.0d;
		HashMap<String, Integer> distWords = new HashMap<String, Integer>();
		
		for(String word:allWords){
			if(!distWords.containsKey(word)){
				countDistinctVocab++;
				distWords.put(word,1);
			}
		}
		
		for(String word:allWords){
			if(!wp.containsKey(word)){
				count = 0;
				for(String nextWord:wordList){
					if(word.equals(nextWord)){
						count++;
					}
				}
				double probability = (count+1)/(size+countDistinctVocab);
				wp.put(word, probability);
			}	
		}
		return wp;		
	}
}
