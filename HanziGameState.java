package tai.arima.hanzidriller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class HanziGameState implements Serializable {
	
	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	public Stack<HanziCard> deck; // this will hold all the cards the user will study and feed into the study list as users master cards
	public ArrayList<HanziCard> currentStudyList; // this will be the active list where the user is learning new hanzi
	public ArrayList<HanziCard> reviewList; // this list will hold all cards the user needs to review
	public ArrayList<HanziCard> bookmarkStudyList; // this is to save the state of the study list when the user goes into a review session 
	public ArrayList<HanziCard> masteryList; // this is where we will save cards once they have been "mastered"
	public String userName = "";
	public int[]reviewListIndicies;
	public int totalCardsIndex;
	public Map<String, Integer> userSettings = new HashMap<String, Integer>();
	
	// maybe make a getter and setter later fix me, be lazy for now
	
	HanziGameState () {
		
	}
	
	HanziGameState (Stack<HanziCard> deck, ArrayList<HanziCard> currentStudyList, ArrayList<HanziCard> reviewList, ArrayList<HanziCard> bookmarkStudyList, ArrayList<HanziCard> masteryList, int[]reviewListIndicies, Map<String, Integer> userSettings, int totalCardsIndex, String userName) {
	//HanziGameState (Stack<HanziCard> deck, ArrayList<HanziCard> currentStudyList, ArrayList<HanziCard> reviewList, ArrayList<HanziCard> bookmarkStudyList, ArrayList<HanziCard> masteryList, String userName) {
		this.deck = deck;
		this.currentStudyList = currentStudyList;
		this.reviewList = reviewList;
		this.bookmarkStudyList = bookmarkStudyList;
		this.masteryList = masteryList;
		this.userName = userName;
		this.reviewListIndicies = reviewListIndicies;
		this.totalCardsIndex = totalCardsIndex;
		this.userSettings = userSettings;
	}
	
	public void saveState(HanziGameState gameState) {
		
		try
        {   
            //Saving of object in a file
            FileOutputStream saveFile = new FileOutputStream(userName + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(saveFile);
             
            
            // Method for serialization of object
            out.writeObject(gameState);
            out.close();
            saveFile.close();
  
        }
          
        catch(Exception ex)
        {
            System.out.println("IOException is caught");
        }
		
	}
	
	public HanziGameState loadState (String userName) {
		HanziGameState loadGameState = new HanziGameState();
		try {
				
	            // Reading the object from a file
	            FileInputStream loadFile = new FileInputStream(userName + ".ser");
	            ObjectInputStream in = new ObjectInputStream(loadFile);
	  
	            // Method for deserialization of object
	            loadGameState = (HanziGameState)in.readObject();
	            
	            in.close();
	            loadFile.close();
	            
	        }
	  
	        catch (IOException ex) {
	            System.out.println("IOException caught");
	        }
	  
	        catch (ClassNotFoundException ex) {
	            System.out.println("ClassNotFoundException caught");
	        }

		return loadGameState;
		
		
		
	}
	
	
	
}
