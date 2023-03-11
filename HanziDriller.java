package tai.arima.hanzidriller;
// Comment
import java.awt.Dimension;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.event.*;

import java.awt.Color;
import java.awt.Font;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import javax.swing.JComponent;

import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TextArea;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.awt.event.ActionEvent;
import java.awt.Button;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;

import java.awt.Dialog.ModalityType;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton; 




/* This will be a relatively simple program to allow users to drill hanzi readings. It works by creating what are essentially flashcards from an Excel file, then 
 * feeding these to the user according to pre-specified limits of how many cards to "learn" at once, and how much drilling to do before entering into a review mode. 
 * I am planning on adding functionality as I go along.
 * 
 */

public class HanziDriller extends JFrame {
	
	//containers for Hanzi cards
	private Stack<HanziCard> deck; // this will hold all the cards the user will study and feed into the study list as users master cards
	private ArrayList<HanziCard> currentStudyList; // this will be the active list where the user is learning new hanzi
	private ArrayList<HanziCard> reviewList; // this list will hold all cards the user needs to review
	private ArrayList<HanziCard> bookmarkStudyList; // this is to save the state of the study list when the user goes into a review session 
	private ArrayList<HanziCard> masteryList; // this is where we will save cards once they have been "mastered"
	
	// elements of the GUI
	private TextArea hanziPromptText; // shows the hanzi prompt the user has to type the reading for
	private TextField answerBox; // the box where the user types the answer
	private TextArea hanziReadingsText; // the box that will show the correct reading of a hanzi after the card has resolved
	private TextArea hanziMeaningsText; // box that will show the English meaning of a hanzi after card has resolved
	private Button nextCardButton; // this button appears when a card has resolved
	private JLabel instructionsLabel; // shows user a prompt for instructions and when they enter a correct answer
	private JTextArea arrayMonitor;
	
	private JLabel cardScoreLabel; // the following several labels are to keep track of variables for debugging, will probably remove most later
	private JLabel studyListSizeLabel; // same
	private JLabel reviewSessionSizeLabel; // same
	private JLabel masteryListLabel; // same
	private JLabel reviewOnLabel;
	private JLabel cardsPlayedLabel;
	private JLabel totalIndexLabel;
	private JLabel lastSeenLabel;
	
	private JLabel userLabel; // displays current user name
	private JButton undoButton; // after scoring of a card, user can hit this button to undo the score and see the card as "fresh" again
	public JLabel fullProgressBar; // this is just a JLabel I have in order to have a place to put my progress bar picture
	
	public JPanel incorrectColorPanel; // this surrounds the answer box with a red rectangle to give the user a more obvious visual response when they enter an incorrect answer
	public JLabel triesRemainingLabel; // shows how many more tries the user can attempt the card before they will automatically be shown the answer
	
	
	
	// variables necessary for methods
	private int incorrectAnswer; // this will keep track of how many times a user has answered incorrectly on a single card to help decide when to show them the answer if they cannot provide the correct answer
	private int cardsPlayed; // this variable notifies the program when to switch back and forth from "review mode" and "study mode". The default setting is to switch to review after 100 cards (assuming the minimum requirement for cards in the review list has been met), and to switch back to study after going through all items in the length of a review session (default setting is 20). This is different from the totalCardIndex, which is never reset. 
	private int currentCardIndex; // this variable keeps track of the current card being studied in the study list
	private boolean reviewOn; // keeps track of whether a user is doing a review session or not. Depending on whether review is on or off, the way cards are scored will be handled differently.
	private String plainReading = ""; // the string where the pinyin will be saved which has the accented character removed and a number placed at the end of the reading to indicate tone
	public int maxTries = 3; // the maximum amount of tries a user is allowed to attempt to answer before they will be shown the correct answer and be forced to continue
	public int studyListSize = 20; // the amount of items allowed in a study list. This is the amount of "new cards" a user drills at once.
	static final String tones = "*āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"; // this string is a container of all possible special characters used to help convert "real" pinyin to plain text characters
	public int switchToReview = 100; // User will be allowed to choose how many cards they play through before review mode is entered. Default is 100. <currently testing with 10, fix later>
	private int totalCardsIndex; // this will keep track of a history of how many cards total have been seen by the user in order to determine when to show the user an old card versus a new card
	private int switchCard = 20; // this keeps track of how many new cards to show a user before switching to an old card; mainly applies to the situation in which the user gets many cards correct upon first viewing, in order to prevent user from only ever seeing new cards despite the fact that old cards are still in the list
	public int reviewSessionSize = 20; // the default review session size (i.e., how many cards from the review list will be shown to the user once they have played enough cards to enter a review sessions); can be changed by the user via the options in settings
	public String userName = null; // user name will be saved to create a profile where the user saves their data
	public int lastScore; // this is used to save the last score the user had for a card in order to enable the functionality of the "undo" button
	public boolean rightAnswer; // boolean used to determine the behavior upon going to the next card
	public boolean inSession = false; // boolean used to make decisions about behavior when using menu options based upon whether there is already a session in place or not
	public int masteryScore = 5; // this is the score the user must get for each hanzi for it to inserted into the mastery list; can be modified by user in settings
	public int moveToReview = 3; // this value determines how many times the user must get the answer correct in a row before it is taken from the current study list, and moved to the review study list
	private int[]reviewListIndicies = new int[reviewSessionSize]; // keeps track of the location in the review list that the review cards came from so that the review list will always be gone through in order
	public boolean initRequired = true; // keeps track of whether the reviewListIndicies array has been initialized yet or not; maybe this is a stupid solution :shrug:
	public String listSelected = "Hanzi by Frequency List -- 0-1000"; // fix this later, have put in something for testing purposes
	
	// stuff pertaining to new features
	public String userSelection = null; // the user selected here will be used to pick the file from which to restore data
	public boolean restoringState = false; // boolean to determine whether to restore a user profile or create a new deck
	public boolean keepDefaults = true; // associated with a checkbox, if users uncheck the box, the settings box will appear as soon as the new profile is created, maybe it's kind of pointless but whatever
	private JButton giveUpButton; // this button instantly reveals a card, counts it as an incorrect score, and prevents user from undoing the score
	public boolean gaveUp = false; // boolean to keep track of whether a user used the giveup button or not
	public boolean instantMastery = true; // this boolean determines whether or not to instantly move a card to the masterylist if user produces a correct answer upon the first viewing of a card
	public final Runnable runnable = (Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation"); // sound effect for when the user gets an incorrect answer
	public int reviewReps = 1; // when the program enters a reviewSession, this will be how many times the program cycles through the items in the reviewSession before going back to the currentStudyList
	Map<String, Integer> userSettings = new HashMap<String, Integer>(); // a key:value map to save user defined settings
	public boolean deckOver = false; // this indicates that the user has completed all the cards in the deck
	public int sessionSizeSaver = reviewSessionSize;
	public boolean endSwitch = false;
	private JLabel masteryLevelLabel;
	public boolean traditional = false;
	
	public void initialize() throws FileNotFoundException {
		inSession = true;
		// Initialization of lists
		currentStudyList = new ArrayList<HanziCard>();
		reviewList = new ArrayList<HanziCard>();
		masteryList = new ArrayList<HanziCard>();
		bookmarkStudyList = new ArrayList<HanziCard>();
		debuggingModeSwitch(false);
		
		if (restoringState) {
			
			// Restores the values from save file and applies them
			HanziGameState restoreGameState = new HanziGameState();
			restoreGameState = restoreGameState.loadState(userSelection);
			deck = (Stack<HanziCard>)restoreGameState.deck.clone(); // -->this seems to work now
			currentStudyList = new ArrayList<>(List.copyOf(restoreGameState.currentStudyList));
			bookmarkStudyList = new ArrayList<>(List.copyOf(restoreGameState.bookmarkStudyList));
			userName = restoreGameState.userName;
			reviewList = new ArrayList<>(List.copyOf(restoreGameState.reviewList));
			masteryList = new ArrayList<>(List.copyOf(restoreGameState.masteryList));
			reviewListIndicies = restoreGameState.reviewListIndicies.clone();
			totalCardsIndex = restoreGameState.totalCardsIndex;
			
			// Restoring user settings
			studyListSize = restoreGameState.userSettings.get("studyListSize");
			reviewSessionSize = restoreGameState.userSettings.get("reviewSessionSize");
			sessionSizeSaver = restoreGameState.userSettings.get("sessionSizeSaver");
			switchToReview = restoreGameState.userSettings.get("switchToReview");
			maxTries = restoreGameState.userSettings.get("maxTries");
			reviewReps = restoreGameState.userSettings.get("reviewReps");
			cardsPlayed = restoreGameState.userSettings.get("cardsPlayed");
			
			instantMastery = restoreGameState.userSettings.get("instantMastery") == 1 ? true : false;
			reviewOn = restoreGameState.userSettings.get("reviewOn") == 1 ? true : false;
			deckOver = restoreGameState.userSettings.get("deckOver") == 1 ? true : false;
			endSwitch = restoreGameState.userSettings.get("endSwitch") == 1 ? true : false;
			traditional = restoreGameState.userSettings.get("traditional") == 1 ? true : false;
			
			
			
			
		} else {
			deck = createDeck(listSelected); // currently messing with this, fix later if needed
		}
		
		this.setVisible(true);
		userLabel.setText("User : " + userName);
		drawProgressBars();
		nextCard();
		
	}
	
	public void drawProgressBars () { // a silly way to show progress bars, but this is the best I could think of
		double masterySize = masteryList.size();
		double deckSize = 1000; // Fix this later! You have to add it as a saved variable in the initialization (maybe add it to the settings array)
		
		int progress = (int) (304 - (304 * (masterySize / deckSize)));
		masteryLevelLabel.setText("Mastery Level: " + (int) (100 * (masterySize / deckSize)) + "%");
		fullProgressBar.setBounds(230, 466, 304 - progress, 22);
		
		repaint();
	}
	
	public Stack createDeck(String filePath) throws FileNotFoundException { // creates a new deck to study when given a formatted Excel file as input
		//takes as an argument a file path, uses this to pick the deck to be created
		// scans through the file determined in the file path, turns each line into a hanzi card and stores these in a stack
		
		Map<String, Integer> top3000 = new HashMap<String, Integer>();
		top3000.put("Hanzi by Frequency List -- 0-1000", 1000);
		top3000.put("Hanzi by Frequency List -- 1001-2000", 2000);
		top3000.put("Hanzi by Frequency List -- 2001-3000", 2999);
		top3000.put("Hanzi by Frequency List -- 3001-4000", 4000);
		top3000.put("Hanzi by Frequency List -- 4001-5000", 5000);
		top3000.put("Hanzi by Frequency List -- 5001-6000", 5999);
		
		Stack<HanziCard> deck = new Stack<HanziCard>(); // this stack will be returned at the end as the deck full of hanzi cards built from the Excel file
		XSSFWorkbook wb = null; 
		if (top3000.get(listSelected) > 3000) {
			filePath = "hanzi6000.xlsx";
		} else {
			filePath = "hanzi3000.xlsx"; // change this later
		}
		
		FileInputStream fis = new FileInputStream(filePath); 
		
		
		
		
		
		int charsToUse = top3000.get(listSelected);

		try {
			wb = new XSSFWorkbook(fis);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		Sheet hanziSheet = wb.getSheetAt(0);
		
		//for (int r = hanziSheet.getFirstRowNum(); r <= hanziSheet.getLastRowNum(); r++) { // this for loop iterates through the rows of the Excel sheet from beginning
		// for (int r = hanziSheet.getLastRowNum(); r >= hanziSheet.getFirstRowNum(); r--) { // this for loop iterates through the rows from the end, better for frequency lists since then the most frequent will be at top of stack
		for (int r = charsToUse; r >= (charsToUse - 1000); r--) { // experimenting with loop determined by numb of chars to use passed by J	list selection
			Row row = hanziSheet.getRow(r);
			 HanziCard nextCard = new HanziCard(); // a new card is created for each row, the values of the card are added cell by cell
			 for (int c = row.getFirstCellNum(); c <= row.getLastCellNum(); c++) { // this for loop iterates through the cells of each row
				 Cell cell = row.getCell(c);
                 if (cell != null) {
                	 switch (c) { // this switch is used to decide the behavior based on the data in the Excel which is sorted by column
                	 	case 0:
                	 		nextCard.hanzi = cell.getStringCellValue();
                	 		break;
                	 	case 1: // implementing traditional
                	 		nextCard.traditional = cell.getStringCellValue();
                	 		break;
                	 	case 2:
                	 		nextCard.reading = cell.getStringCellValue(); // experimenting, fix change later
                	 		nextCard.multReads.add(convertPinyin(cell.getStringCellValue())); // fix change later
                	 		break;
                	 	case 3:
                	 		String moreReadings = cell.getStringCellValue();
                	 		if (moreReadings != null) {
                	 			nextCard.reading += ", " + moreReadings; // Testing this now here, see if it breaks the system.
                	 		}
                	 		handleMultipleReadings(moreReadings, nextCard);
                	 		break;
                	 	case 4:
                	 		nextCard.meaning = cell.getStringCellValue();
                	 		break;
                	 }
                 }
			 }
			 deck.push(nextCard); // after adding the values to the Hanzi card, it is pushed to the deck and the loop proceeds to the next row
		}
		return deck;
	}
	
	public String handleMultipleReadings (String nextReading, HanziCard theCard) { // in the case the hanzi has multiple readings in the fourth column of the Excel file, this will deal with that
		if (nextReading.isBlank() || nextReading == null) {
			return null;
		} else if (nextReading.indexOf(",") == -1) {
			theCard.multReads.add(convertPinyin(nextReading));
			return null;
		} else {
			theCard.multReads.add(convertPinyin(nextReading.substring(0, nextReading.indexOf(","))));
			String passString = nextReading.substring((nextReading.indexOf(",") +1));
			return handleMultipleReadings(passString, theCard);
		}
	}
	
	// this method converts pinyin with accented characters into plain text with a number after the syllable for the tone. The method exists both in this class, and the HanziCard class. It's supposed to be like that, at least for now.
	public String convertPinyin(String convertMe) { // this function is used to convert "real" pinyin to plain text (i.e., no special characters (tāng -> tang1))
		int vowelPicker = -1;
		String fixedPinyin = "";
		
		for (int i = 0; i < convertMe.length(); i++) { // to iterate through the reading string character by character
			if (tones.indexOf(convertMe.charAt(i)) != -1) {
				 fixedPinyin = convertMe.substring(0, i);
				 vowelPicker = tones.indexOf(convertMe.charAt(i));
				 if (vowelPicker / 4.0 <= 1) {						// this string of if statements is based upon the positioning of vowels in the string tones
					 fixedPinyin += "a" + convertMe.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 2) {
					 fixedPinyin += "e" + convertMe.substring(i+1);
					 break;
				 }
				 else if (vowelPicker  / 4.0 <= 3) {
					 fixedPinyin += "i" + convertMe.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 4) {
					 fixedPinyin += "o" + convertMe.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 5) {
					 fixedPinyin += "u" + convertMe.substring(i+1);
					 break;
				 }
				 else {
					 fixedPinyin += "v" + convertMe.substring(i+1);
					 break;
				 }
			}
		}
		if (vowelPicker == -1) { // in the case that no special characters were found in the reading, the original reading will be returned
			return convertMe;
		}
		switch(vowelPicker % 4) { // in the case that a special character was replaced with a plain character, the original tone will be added to the end of the string based upon the ordering of the characters in the string tones
			case(1) :
				fixedPinyin += "1";
				break;
			case(2) :
				fixedPinyin += "2";
				break;
			case(3) :
				fixedPinyin += "3";
				break;
			case(0) :
				fixedPinyin += "4";
				break;
		}
		return fixedPinyin;
	}
	
	public void nextCard() { // this method will update the GUI for the next card and add a new card if the deck is not empty
		if (deck.isEmpty() && currentStudyList.isEmpty()) { // This block applies to two situations: (1) the first time the user has studied through all cards in the deck; (2) when a user has mastered all the cards in the reviewlist
			if (reviewList.isEmpty()) { // when the user complets the review list, it should allow the user to continue studying by dumping everything from the masteryList into the reviewList, and deleting the masteryList
				JOptionPane.showMessageDialog(null, "You have mastered all the hanzi in your review list! Your list has now been arranged to help you review the hanzi that were most difficult for you. Keep studying or move on to a new challenge!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
				reviewList = new ArrayList<>(List.copyOf(masteryList));// turn the reviewlist into the masterylist
				sortReviewList(); // the list will then be sorted by order, which should put all the relatively "weak" items back at the front, things that were instantly mastered will be at the end because their score was instantly put at 10
				masteryList = new ArrayList<HanziCard>();
				// make a boolean here to identify this state (?)
			}
			if (!deckOver) { // this silly bit of code ensures that the first time the user goes completes the deck, the program can catch this and produce a "one time ever" behavior.
				endSwitch = true;
			}
			checkReviewState();
			
		}
		triesRemainingLabel.setVisible(false);
		gaveUp = false; 
		giveUpButton.setEnabled(true);
		
		/* On second thought, I don't think I need this code if I just remove the bit where I take things out of the current study list during review
		 * 
		 * while (reviewOn && currentStudyList.get(currentCardIndex) == null) { // this bit of code should handle for the case in which the user is doing multiple reps in review and they master one card
			if (currentCardIndex >= currentStudyList.size()) {
				currentCardIndex = 0;
			} else {
				currentCardIndex++;
			}
		} */
		
		//>>>>>>>>>>>>>>>>>>>>>>>>>
		// setting up a Jtextarea to monitor the content of each array dynamically, for debugging, removing later
		String currentListContents = "";
		String reviewListContents = "";
		String bookmarkContents = "";

		for (HanziCard card : currentStudyList) {
			if (card != null) {
				currentListContents += card.hanzi + " ";
			} else {
				currentListContents += "X ";
			}
			
		}
		for (HanziCard card : reviewList) {
			if (card != null) {
				reviewListContents += card.hanzi + " ";
			} else {
				reviewListContents += "X ";
			}
		}
		for (HanziCard card : bookmarkStudyList) {
			bookmarkContents += card.hanzi + " ";
		}
		arrayMonitor.setText("currentStudyList:\r\n" + currentListContents + "\r\nreviewList:\r\n" + reviewListContents + "\r\nreviewListIndicies:\r\n" + Arrays.toString(reviewListIndicies) + "\r\nbookmarkStudyList:\r\n" + bookmarkContents + "\r\n");
		// end monitor section
		//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
		
		
		if (totalCardsIndex % 25 == 0 && inSession) { // automatically saves the game state every 25 cards to provide protection against the case that someone's computer crashes and the application does not exit properly
			saveProgress();
		}

		
		
		// undoButton.setVisible(false); // undoButton will not be visible before user makes an attempt at answering
		undoButton.setEnabled(false);
		
		
		totalCardsIndex++; // the total card index will help keep track of how long it has been since an old card has been seen so the user doesn't have to do too many new cards in a row
		
		nextCardButton.setVisible(false); // next card button should only appear once card has been "resolved"
		answerBox.setEnabled(true); // the answer box reappears after user proceeds to next card
		instructionsLabel.setText("Type the pinyin (including tone) of the hanzi shown.");
		
		// This statement applies to the case that there are no cards left in deck or reviewList
		if (currentStudyList.isEmpty() && reviewList.isEmpty() && deck.isEmpty()) { // if the user has gone through all cards in study list, review list, and no cards left in deck
			instructionsLabel.setText("You have mastered all the hanzi in your study list.");
			return;
		}
		
		// This statement applies to the case where review is not in session and there are still cards in the deck
		if ((currentStudyList.isEmpty() || currentStudyList.size() < studyListSize) && !deck.isEmpty() && !reviewOn) { // this should add a new hanzi card to the current study list whenever there are less than 20 items (or whatever size has been chosen for the studyListSize)
			currentStudyList.add(0, deck.pop());
			currentCardIndex = 0; // whenever a new card is added, this ensures it is the next card to appear
		}
		
	
		
		if (currentStudyList.size() > 1 && !reviewOn) { // this will make sure that the card switches to an "old card" eventually instead of always just showing a new card if the user gets several new cards correct on the first response
			switchCard = studyListSize;
			for (int i = 0; i < currentStudyList.size(); i++) { 
				if (currentStudyList.get(i).lastSeen == -1) {
					currentStudyList.get(i).lastSeen = totalCardsIndex;
				}
				if ((totalCardsIndex - currentStudyList.get(i).lastSeen) > switchCard) {
					switchCard = totalCardsIndex - currentStudyList.get(i).lastSeen;
					currentCardIndex = i;
				}
			}
		}
		
		// Sets the components of the GUI for the current card
		if (traditional) {
			hanziPromptText.setText((currentStudyList.get(currentCardIndex).traditional)); // this sets the text of the hanzi prompt to the hanzi value of the current card
		} else {
			hanziPromptText.setText((currentStudyList.get(currentCardIndex).hanzi)); // this sets the text of the hanzi prompt to the hanzi value of the current card
		}
		hanziReadingsText.setText(""); // when the user sees a new card, the readings and meaning text areas will be cleared
		hanziMeaningsText.setText("");
		cardsPlayed++; // this is incremented after a user has entered a valid input, whether their response was correct or incorrect 
		cardsPlayedLabel.setText("Cards played: " + cardsPlayed);
		answerBox.setText(""); // clear text of answer box
		answerBox.requestFocus(); // makes sure answer box has cursor in it so user can start typing
		
		// The following lines are just to keep track of this while debugging, fix/remove later
		cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
		studyListSizeLabel.setText("Study list size: " + currentStudyList.size());
		reviewSessionSizeLabel.setText("Review list size: " + reviewList.size());
		masteryListLabel.setText("Mastery list size: " + masteryList.size());
		lastSeenLabel.setText("Last seen: " + currentStudyList.get(currentCardIndex).lastSeen);
		totalIndexLabel.setText("TCI : " + totalCardsIndex);
		
		
		
	}
	
	public void newTestResponse() { // this method checks if the user has entered the correct response and keeps track of some important stuff to determine what to do to the current card
		lastScore = currentStudyList.get(currentCardIndex).score; // before scoring the card, the last score is saved in case the user makes a mistake and wants to undo their score
		currentStudyList.get(currentCardIndex).lastSeen = totalCardsIndex; // this will keep track of the "last seen" variable, which will be used to make decisions about whether to show a card based upon how long it has been since the user has last seen the card (this affects the behavior of other methods)
		String userInput = answerBox.getText().trim(); // this gets the text the user inputs into the answer box textfield 
		
		
		// the following codeblock is to control the behavior of if the user decides to use the "give up" button, instantly revealing the card
		// if they choose to instantly reveal the card, they will be prevented from using the undo button, to prevent users from "cheating" (they're really only cheating themselves...)
		if (!gaveUp) {
			rightAnswer = currentStudyList.get(currentCardIndex).multReads.contains(userInput);
			undoButton.setEnabled(true);
		} else {
			rightAnswer = false;
			undoButton.setEnabled(false);
			giveUpButton.setEnabled(false);
			(currentStudyList.get(currentCardIndex)).incorrectScore();
		}
		
		
		if (rightAnswer) {
			triesRemainingLabel.setVisible(false);
			incorrectColorPanel.setVisible(false);
			giveUpButton.setEnabled(false);
			currentStudyList.get(currentCardIndex).correctScore(instantMastery, reviewOn); // the "reviewOn" state is passed as an argument, as scoring works differently depending on whether review is on or off
			answerBox.setEnabled(false); // hides the answer box after correct answer input
			nextCardButton.setVisible(true); // this button will only appear after getting a correct answer
			nextCardButton.requestFocus(); // this will enable a user to press space to continue
			//answerBox.setText(""); // clears the answer box so it is empty for next response

			hanziReadingsText.setText(currentStudyList.get(currentCardIndex).reading); // when the user has answered correctly, the readings and meanings will appear in their respective text areas
			hanziMeaningsText.setText(currentStudyList.get(currentCardIndex).meaning);
			instructionsLabel.setText("Correct. Press space to continue.");
			
			// this is just for testing purposes fix/remove later!!!
			cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
			studyListSizeLabel.setText("Study list size: " + currentStudyList.size());
			reviewSessionSizeLabel.setText("Review list size: " + reviewList.size());
			masteryListLabel.setText("Mastery list size: " + masteryList.size());
		} else { // when the user has not entered the correct answer
			incorrectAnswer++; // this keeps track of incorrect answers -- once the user has attempted the max tries, the card will be revealed
			incorrectColorPanel.setVisible(true);
			triesRemainingLabel.setText("Tries remaining: " + (maxTries - incorrectAnswer));
			triesRemainingLabel.setVisible(true);
			
			if (runnable != null) runnable.run(); // this plays the default "oops" sound for Windows machines
			
			if (incorrectAnswer >= maxTries) { // when the user has entered the incorrect answer too many times, they will be shown the correct answer and be forced to continue to the next card
				giveUpButton.setEnabled(false);
				incorrectColorPanel.setVisible(false);
				answerBox.setEnabled(false);
				nextCardButton.setVisible(true); // this button will appear after user exhausts their tries
				nextCardButton.requestFocus(); // this will enable a user to press space to continue
				
				answerBox.setText(""); // clears the answer box so it is empty for next response
				hanziReadingsText.setText(currentStudyList.get(currentCardIndex).reading); // when the user has answered correctly, the readings and meanings will appear in their respective text areas
				hanziMeaningsText.setText(currentStudyList.get(currentCardIndex).meaning);
				if (gaveUp) {
					instructionsLabel.setText("Try to get it next time! Press space to continue.");
					triesRemainingLabel.setText("Tries remaining: " + 0);
				} else {
					instructionsLabel.setText("You have run out of tries. Press space to continue.");
				}
				
				
				// this is just for testing purposes fix/remove later!!!
				cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
				studyListSizeLabel.setText("Study list size: " + currentStudyList.size());
				reviewSessionSizeLabel.setText("Review list size: " + reviewList.size());
				masteryListLabel.setText("Mastery list size: " + masteryList.size());
			} else { // answer is incorrect but the user has more tries available
				(currentStudyList.get(currentCardIndex)).incorrectScore(); // sets the score to 0
				cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
				instructionsLabel.setText("You have entered the incorrect answer");
				answerBox.requestFocus();
				answerBox.selectAll();
			}
		}
	}
	
	public void correctResponse() {
		
		incorrectAnswer = 0;
		cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
		
		if (currentStudyList.get(currentCardIndex).score >= masteryScore) {
			cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
			if (reviewOn) { // this should indicate that the card was mastered during a review session and mark an item to be removed from the review list once the review session is over by changing its value to null in the review list
				reviewList.set(reviewListIndicies[currentCardIndex], null);
			}
			masteryList.add(currentStudyList.get(currentCardIndex));
			
			drawProgressBars();
			if (!reviewOn) {
				currentStudyList.remove(currentCardIndex);
			}
			masteryListLabel.setText("Mastery list size: " + masteryList.size());
			
			
			
		} else if ((currentStudyList.get(currentCardIndex).score) >= moveToReview && !reviewOn) { // Once a user achieves a score of 3, the card is removed from the current study list and added to the review study list
			reviewList.add(currentStudyList.get(currentCardIndex));
			currentStudyList.remove(currentCardIndex);
		}

		currentCardIndex++; // increments when a user gets a correct answer 
		
		checkReviewState();
		
		if (currentCardIndex > currentStudyList.size() - 1) { // If the user has gone through all the cards in the current study list, the card index is set back to 0
			currentCardIndex = 0;
		}
	}
	
	public void incorrectResponse() {
		currentCardIndex++;
		incorrectAnswer = 0;
		checkReviewState();
		if (currentCardIndex > currentStudyList.size() - 1) { // If the user has gone through all the cards in the current study list, the card index is set back to 0
			currentCardIndex = 0;
		}
	}
	
	public void checkReviewState() { // this method should keep track of when to enter review and also determines the end state once the user has gone through all their cards 	
		
		if (endSwitch || deckOver) { // this is maybe a stupid way of doing things, but it works for now
			
			
			if (!deckOver) { // this block of the code should keep the user in a perpetual state of review once the deck is over, with the order of the cards shuffled every time the user goes through the deck
				deckOver = true; // this will probably have to be passed to the gamestate
				reviewOn = true;
				reviewOnLabel.setText("Review on: " + reviewOn);
				if (!reviewList.isEmpty()) { // the following message should appear every time except in the case that the user mastered every card in the deck upon first viewing, meaning no cards would have ever entered the reviewDeck up to this point
					JOptionPane.showMessageDialog(null, "You have studied all the hanzi in your list. Keep reviewing every day until you've mastered them all!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
				}
				initRequired = true;
				// sort the review list by score and last seen
				// sortReviewList();
				setUpIndicies();
				cardsPlayed = 0;
				currentCardIndex = 0;
			}
			
			if (cardsPlayed >= reviewSessionSize) { // remove any mastered items from reviewList
				reviewList.removeIf(Objects::isNull); // remove any items from the reviewlist that were mastered in the last round
				reviewListIndicies = new int [reviewList.size()];
				for (int i = 0; i < reviewList.size(); i++) {
					reviewListIndicies[i] = i;
				}
			}
			
			if (totalCardsIndex % 100 == 0) { // resort the cards every 100 cards played
				sortReviewList(); // I think this is the culprit which is giving the weird behavior with multiple cards getting put into the review list
			}
			
			// !!!!!!!!!!!!! Instead of making separate behavior for the review, I can use the same behavior, just not have it exit review if deckOver
			// all I need to do is here, if cardsPlayed == reviewSessionSize -- > setUpIndicies
			// I can have it sort the reviewlist every switchToReview cards in any case
			
			if (cardsPlayed >= reviewSessionSize) {
				cardsPlayed = 0;
				currentCardIndex = 0;
				setUpIndicies(); // the above two lines should maybe be added to this method if they always apply
			}
			reviewOnLabel.setText("Review on: " + reviewOn);
			return;
		} // maybe the below is also best handled with an else if
		
		// the below code all applies to the case where the deck hasn't been exhausted yet.
		if ((!reviewOn) && (cardsPlayed >= switchToReview) && (!reviewList.isEmpty())) { //  Once the user has gone through switchToReview cards, they will enter a review session as long as the reviewList is not empty
			// enter review
			reviewOn = true;
			bookmarkStudyList = new ArrayList<>(List.copyOf(currentStudyList)); // saves the currentStudyList so we can return to this at the end of review session
			
			
			setUpIndicies();
			
			cardsPlayed = 0;
			currentCardIndex = 0;
		} else if ((reviewOn && cardsPlayed >= reviewSessionSize * reviewReps)) { // turns the review off after reviewSessionSize cards played as long as the study list is not empty
			reviewList.removeIf(Objects::isNull); // if an item is mastered during a review session, the card is added to the mastery list, then nullified from the reviewList, here all nullified cards will be removed
			reviewOn = false;
			cardsPlayed = 0;
			currentStudyList = new ArrayList<>(List.copyOf(bookmarkStudyList)); // switches currentStudyList back to the list it contained before review began
		}
		
		reviewOnLabel.setText("Review on: " + reviewOn);
	}
	
	public void setUpIndicies() { // this method sets up an array which will be used to tell the system which items from the review list to present to the user. This system ensures that the reviewlist is iterated over in order and randomized for each set.
		if (reviewSessionSize < sessionSizeSaver) {
			reviewSessionSize = sessionSizeSaver;
		}
		if (reviewList.size() < reviewSessionSize) { // if the reviewList has less items than a reviewSession, the reviewSessionSize will be changed to match the reviewListSize
			sessionSizeSaver = reviewSessionSize;    // and this will also make sure the reviewListIndicies size is adjusted 
			reviewSessionSize = reviewList.size();
			initRequired = true;
		} 
		
		
		if (initRequired) { // the first time a review session begins, the reviewListIndicies will be initialized
			reviewListIndicies = new int [reviewSessionSize];
			for (int i = 0; i < reviewSessionSize; i++) {
				reviewListIndicies[i] = i;
			}
			initRequired = false;
		} else { // this means the reviewListIndicies has already been initialized, in which case there are three possibilities 
			if ((reviewListIndicies[reviewSessionSize - 1] + 1) > reviewList.size()) { // this is the first case where the last index holds a value larger than the current review list. In this case, we simply need to reset the reviewListIndicies to 0 to 19
				for (int i = 0; i < reviewSessionSize; i++) {
					reviewListIndicies[i] = i; // should fill the array with  values 0 to 19
				}
			} else if ((reviewListIndicies[reviewSessionSize - 1] + 1) < reviewList.size()) { // this is second the case where the reviewListInidicies last index is less than the length of the reviewList
				int indexTracker = reviewListIndicies[reviewSessionSize - 1] + 1;
				for (int i = 0; i < reviewSessionSize; i++) {
					if (indexTracker >= reviewList.size()) {
						indexTracker = 0;
					}
					reviewListIndicies[i] = indexTracker;
					indexTracker++;
				}
			} // in the third case, this means that the last index of reviewListIndicies is equivalent to the last index of the studyList, and therefore nothing needs to be done
		}
		  
		for (int i = 0; i < reviewSessionSize - 1; i++) { // this bit of code will shuffle the indicies, it's the Fischer-Yates Shuffle, it should leave the last index untouched, which the code above relies upon to make sure the indicies go forward
			int randomIndex = (int)Math.floor(Math.random() * (i + 1));
	        Integer temp = reviewListIndicies[i];
	        reviewListIndicies[i] = reviewListIndicies[randomIndex];
	        reviewListIndicies[randomIndex] = temp;
		}
		
		// this block of code makes sure that the currentStudyList is set up to handle the size of the review session
		if (currentStudyList.size() < reviewSessionSize) {
			while (currentStudyList.size() < reviewSessionSize) {
				currentStudyList.add(null);
			}
		} else if (reviewSessionSize < currentStudyList.size()){
			while (currentStudyList.size() > reviewSessionSize) {
				currentStudyList.remove(0);
			}
		} 
		
		for (int i = 0; i < reviewSessionSize; i++) { // this should assign each item of the currentStudyList to the appropriate item from the reviewList in accordance with the indicies stored in the reviewListIndicies array
			currentStudyList.set(i, reviewList.get(reviewListIndicies[i])); 
		}
	}
	
	public void sortReviewList() { // sorts the reviewList first according to score, then according to lastseen
		
		for (int i = 0; i < reviewList.size(); i++) {
			for (int j = 0; j < i; j++) {
				if (reviewList.get(i).score < reviewList.get(j).score) { // sorting by score here
					HanziCard temp = reviewList.get(j);
					reviewList.set(j, reviewList.get(i));
					for (int k = j + 1; k <= i; k++) {
						HanziCard temp2 = reviewList.get(k);
						reviewList.set(k, temp);
						temp = temp2;
					}
				} else if (reviewList.get(i).score == reviewList.get(j).score) {
					// sort by is last seen
					if (reviewList.get(i).lastSeen > reviewList.get(j).lastSeen) {
						HanziCard temp = reviewList.get(j);
						reviewList.set(j, reviewList.get(i));
						for (int k = j + 1; k <= i; k++) {
							HanziCard temp2 = reviewList.get(k);
							reviewList.set(k, temp);
							temp = temp2;	
						}
					}
				}
			}
		}
		initRequired = true; // this applies to the setUpIndicies method
	}
	
	public void saveProgress() { // serializes the user data in a .ser file so that the user can pick back up right where they left off. Saves happen when the program is exited, when the user is switched, and automatically every 25 cards, to provide a backup in the case that the program is closed as result of a system crash or similar
		userSettings.put("studyListSize", studyListSize);
		userSettings.put("reviewSessionSize", reviewSessionSize);
		userSettings.put("switchToReview", switchToReview);
		userSettings.put("maxTries", maxTries);
		userSettings.put("instantMastery", instantMastery ? 1 : 0);
		userSettings.put("reviewReps", reviewReps);
		userSettings.put("cardsPlayed", cardsPlayed); // this isn't a user setting, but it fits here, so I'll include it here to reduce the amount of separate arguments that must be sent to save the state
		userSettings.put("reviewOn", reviewOn ? 1 : 0); // same as above
		userSettings.put("deckOver", deckOver ? 1 : 0);
		userSettings.put("endSwitch", endSwitch ? 1 : 0);
		userSettings.put("traditional", traditional ? 1 : 0);
		userSettings.put("sessionSizeSaver", sessionSizeSaver);
		
		HanziGameState gameState = new HanziGameState(deck, currentStudyList, reviewList, bookmarkStudyList, masteryList, reviewListIndicies, userSettings, totalCardsIndex, userName);
		gameState.saveState(gameState);
	}
	
	public void debuggingModeSwitch(boolean visible) {
		arrayMonitor.setVisible(visible);
		cardScoreLabel.setVisible(visible);; 
		studyListSizeLabel.setVisible(visible); 
		reviewSessionSizeLabel.setVisible(visible); 
		masteryListLabel.setVisible(visible); 
		reviewOnLabel.setVisible(visible);
		cardsPlayedLabel.setVisible(visible);
		totalIndexLabel.setVisible(visible);
		lastSeenLabel.setVisible(visible);
	}
	

	
	public HanziDriller() throws FileNotFoundException {
		setResizable(false);
		setTitle("Hanzi Driller");
		ImageIcon image = new ImageIcon("kanjilogo.png");
		setIconImage(image.getImage());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		// Make a menu bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		JMenuItem userMenuItem = new JMenuItem("Change User");
		userMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selection = JOptionPane.showConfirmDialog(userMenuItem, "This will save and end your current study session. Would you like to continue?",
			               plainReading, JOptionPane.YES_NO_OPTION,
			               JOptionPane.QUESTION_MESSAGE, new ImageIcon("robotDrillerCartoonSMALL.png")); // since this image is used in multiple places, you should declare it in one place so all classes can use it fix later
				 if (selection == JOptionPane.YES_OPTION) {
					restoringState = false;
					HanziWelcomeScreen welcomeScreen = new HanziWelcomeScreen();
					welcomeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
					welcomeScreen.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					welcomeScreen.setVisible(true);
					saveProgress();
					inSession = false;
					dispose();
		         } 
			}
		});
		optionsMenu.add(userMenuItem);
		

		
		/* Removing this feature for now since implementing it would be a bit more complicated. Maybe I will fix or return to it later.
		JMenuItem deckMenuItem = new JMenuItem("Change Deck");
		deckMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HanziExistingUser changeDeck = new HanziExistingUser();
				changeDeck.setModalityType(ModalityType.APPLICATION_MODAL);
				changeDeck.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				changeDeck.setVisible(true);
				
				dispose();
			}
		});
		optionsMenu.add(deckMenuItem); */
		
		JMenuItem settingsMenuItem = new JMenuItem("Settings");
		settingsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HanziSettings settings = new HanziSettings();
		        settings.setModalityType(ModalityType.APPLICATION_MODAL);
		        settings.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		        settings.setVisible(true);
			}
		});
		optionsMenu.add(settingsMenuItem);
		
		JMenu mnNewMenu = new JMenu("Help");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNewMenuItem = new JMenuItem("About");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				HanziAbout aboutPage = new HanziAbout();
		        aboutPage.setModalityType(ModalityType.APPLICATION_MODAL);
		        aboutPage.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		        aboutPage.setVisible(true);
			}
		});
		mnNewMenu.add(mntmNewMenuItem);
		
		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Troubleshoot");
		mnNewMenu.add(mntmNewMenuItem_1);
		
		
		
		answerBox = new TextField();
		answerBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newTestResponse();
			}
		});
		answerBox.setBounds(269, 235, 222, 22);
		getContentPane().add(answerBox);
		
		JLabel readingsLabel = new JLabel("Reading(s):");
		readingsLabel.setBounds(617, 44, 72, 14);
		getContentPane().add(readingsLabel);
		
		JLabel meaningsLabel = new JLabel("English meaning(s):");
		meaningsLabel.setBounds(617, 142, 118, 14);
		getContentPane().add(meaningsLabel);
		
		hanziReadingsText = new TextArea("", 0, 0, 3);
		hanziReadingsText.setBackground(Color.WHITE);
		hanziReadingsText.setEditable(false);
		hanziReadingsText.setBounds(617, 64, 208, 72);
		getContentPane().add(hanziReadingsText);
		
		hanziMeaningsText = new TextArea("", 0, 0, TextArea.SCROLLBARS_NONE);
		hanziMeaningsText.setEditable(false);
		hanziMeaningsText.setBackground(Color.WHITE);
		hanziMeaningsText.setBounds(617, 162, 208, 72);
		getContentPane().add(hanziMeaningsText);
		
		hanziPromptText = new TextArea("漢", 0, 0, TextArea.SCROLLBARS_NONE);
		hanziPromptText.setEditable(false);
		hanziPromptText.setFont(new Font("Microsoft Tai Le", Font.PLAIN, 150));
		hanziPromptText.setBackground(Color.WHITE);
		hanziPromptText.setBounds(303, 20, 164, 155);
		
		getContentPane().add(hanziPromptText);
		
		nextCardButton = new Button("Press space to go to next card");
		nextCardButton.setVisible(false);
		nextCardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// The following behavior was added in order to account for the functionality of the undo button
				if (rightAnswer) {
					correctResponse();
				} else {
					incorrectResponse();
				}
				nextCard();
			}
		});
		nextCardButton.setBounds(289, 263, 178, 22);
		getContentPane().add(nextCardButton);
		
		cardScoreLabel = new JLabel("Card score:  ");
		cardScoreLabel.setBounds(28, 235, 93, 14);
		getContentPane().add(cardScoreLabel);
		
		studyListSizeLabel = new JLabel("Study list size:  ");
		studyListSizeLabel.setBounds(28, 290, 111, 14);
		getContentPane().add(studyListSizeLabel);
		
		reviewSessionSizeLabel = new JLabel("Review List size:  ");
		reviewSessionSizeLabel.setBounds(28, 356, 126, 14);
		getContentPane().add(reviewSessionSizeLabel);
		
		masteryListLabel = new JLabel("Mastery list size: 0");
		masteryListLabel.setBounds(28, 407, 126, 14);
		getContentPane().add(masteryListLabel);
		
		cardsPlayedLabel = new JLabel("Cards played: 0");
		cardsPlayedLabel.setBounds(28, 432, 99, 14);
		getContentPane().add(cardsPlayedLabel);
		
		reviewOnLabel = new JLabel("Review on: false");
		reviewOnLabel.setBounds(28, 468, 111, 14);
		getContentPane().add(reviewOnLabel);
		
		totalIndexLabel = new JLabel("TCI : 0");
		totalIndexLabel.setBounds(28, 315, 82, 14);
		getContentPane().add(totalIndexLabel);
		
		lastSeenLabel = new JLabel("Last seen: X");
		lastSeenLabel.setBounds(28, 265, 126, 14);
		getContentPane().add(lastSeenLabel);
		
		userLabel = new JLabel("User:  ");
		userLabel.setBounds(28, 25, 186, 14);
		getContentPane().add(userLabel);
		
		undoButton = new JButton("Undo Score (F5)");
		Action undoHotkey = new AbstractAction("Undo Score (F5)") {
		    @Override
		    public void actionPerformed(ActionEvent evt) {
		    	nextCardButton.setVisible(false); // next card button should only appear once card has been "resolved"
				answerBox.setEnabled(true); // the answer box reappears after user proceeds to next card
				instructionsLabel.setText("Type the pinyin (including tone) of the hanzi shown.");
				currentStudyList.get(currentCardIndex).undoScore(lastScore);
				if (!rightAnswer) {
					incorrectAnswer--;
				}
				incorrectColorPanel.setVisible(false);
				triesRemainingLabel.setVisible(false);
				hanziReadingsText.setText("");
				hanziMeaningsText.setText("");
				cardScoreLabel.setText("Card score: " + currentStudyList.get(currentCardIndex).score);
				answerBox.requestFocus();
				answerBox.selectAll();
				giveUpButton.setEnabled(true);
				undoButton.setEnabled(false);
		    }
		};
		String key = "Undo";
		undoButton.setAction(undoHotkey);
		 
		undoHotkey.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		 
		undoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( // adds the F5 hotkey to the Undo button
		        KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), key);
		 
		undoButton.getActionMap().put(key, undoHotkey);
		
		undoButton.setBounds(289, 311, 188, 22);
		getContentPane().add(undoButton);
		
		arrayMonitor = new JTextArea();
		arrayMonitor.setText("currentStudyList:\r\n\r\nreviewList:\r\n\r\nreviewListIndicies:\r\n\r\nbookmarkStudyList:\r\n\r\n");
		arrayMonitor.setBounds(624, 240, 186, 224);
		getContentPane().add(arrayMonitor);
		
		giveUpButton = new JButton("Reveal Card (F3)");
		giveUpButton.setBounds(289, 352, 188, 23);
		getContentPane().add(giveUpButton);
		
		Action giveUp = new AbstractAction("Reveal Card (F3)") {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
		    public void actionPerformed(ActionEvent evt) {
		    	incorrectAnswer = maxTries;
		    	gaveUp = true;
		    	newTestResponse();
				
		    }
		};
		String key2 = "Give Up";
		giveUpButton.setAction(giveUp);
		
		fullProgressBar = new JLabel("");
		// fullProgressBar.setIcon(new ImageIcon("C:\\Users\\magne\\eclipse-workspace\\HanziDriller\\fullbar.png")); // change this to not be an absolute path FIX!
		fullProgressBar.setIcon(new ImageIcon("fullbar.png"));
		fullProgressBar.setBounds(230, 466, 304, 22);
		getContentPane().add(fullProgressBar);
		
		JLabel emptyProgressBar = new JLabel("");
		emptyProgressBar.setIcon(new ImageIcon("emptybar.png")); // change this to not be an absolute path FIX!
		emptyProgressBar.setBounds(230, 466, 304, 22);
		getContentPane().add(emptyProgressBar);
		
		triesRemainingLabel = new JLabel("Tries Remaining: 10");
		triesRemainingLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		triesRemainingLabel.setBounds(332, 181, 118, 14);
		getContentPane().add(triesRemainingLabel);
		
		JButton debugSwitchButton = new JButton("Debug Switch");
		debugSwitchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (arrayMonitor.isVisible()) {
					debuggingModeSwitch(false);
				} else {
					debuggingModeSwitch(true);
				}
			}
		});
		debugSwitchButton.setBounds(679, 475, 131, 23);
		getContentPane().add(debugSwitchButton);
		
		instructionsLabel = new JLabel("Type the pinyin (including tone) of the hanzi shown.");
		instructionsLabel.setBounds(230, 206, 291, 14);
		getContentPane().add(instructionsLabel);
		instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
		incorrectColorPanel = new JPanel();
		incorrectColorPanel.setBorder(new LineBorder(new Color(255, 0, 0), 4));
		incorrectColorPanel.setBounds(212, 196, 337, 108);
		getContentPane().add(incorrectColorPanel);
		
		masteryLevelLabel = new JLabel("Mastery Level: 0%");
		masteryLevelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		masteryLevelLabel.setBounds(332, 446, 118, 14);
		getContentPane().add(masteryLevelLabel);
		incorrectColorPanel.setVisible(false);
		triesRemainingLabel.setVisible(false);
		 
		giveUp.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		 
		giveUpButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( // adds the F5 hotkey to the Undo button
		        KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), key2);
		
		giveUpButton.getActionMap().put(key2, giveUp);
		
		setSize(new Dimension(870,570));
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // found these two lines on StackOverflow to make sure window is centered on appearing
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
		
		Runtime.getRuntime().addShutdownHook(new Thread() { // the game state will save when the user exits the application
	        public void run(){
	        	if (inSession) {
	        		saveProgress();
	        	}
	        }
	    });
		
	}
	
public class HanziAbout extends JDialog {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		HanziAbout() {
			setResizable(false);
			setTitle("About HanziDriller");
			ImageIcon image = new ImageIcon("kanjilogo.png");
			setIconImage(image.getImage());
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			
			JLabel windowTitleLabel = new JLabel("About");
			windowTitleLabel.setFont(new Font("Tahoma", Font.PLAIN, 24));
			windowTitleLabel.setBounds(185, 11, 100, 50);
			windowTitleLabel.setHorizontalAlignment(JLabel.CENTER);
			getContentPane().add(windowTitleLabel);
			
			JLabel programDetailsLabel = new JLabel("HanziDriller Program ©2022-2023 Tai Arima");
			programDetailsLabel.setFont(new Font("Tahoma", Font.PLAIN, 14));
			programDetailsLabel.setBounds(10, 75, 350, 20);
			getContentPane().add(programDetailsLabel);
			
			setSize(new Dimension(500,570));
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // makes sure window is centered
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			setVisible(true);
			
			
		}
}

	public class HanziSettings extends JDialog {
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTextField studySizeField; 
		private JTextField reviewSizeField;
		private JTextField beforeReviewField;
		private JTextField maxTriesField;
		private JTextField reviewRepsField;
		
		
		
		HanziSettings() {
			setResizable(false);
			setTitle("Hanzi Driller");
			ImageIcon image = new ImageIcon("kanjilogo.png");
			setIconImage(image.getImage());
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			
			JLabel windowTitleLabel = new JLabel("Settings");
			windowTitleLabel.setFont(new Font("Tahoma", Font.PLAIN, 24));
			windowTitleLabel.setBounds(185, 11, 100, 50);
			windowTitleLabel.setHorizontalAlignment(JLabel.CENTER);
			getContentPane().add(windowTitleLabel);
			
			JLabel studySizeLabel = new JLabel("Study List Size");
			studySizeLabel.setBounds(10, 75, 100, 14);
			getContentPane().add(studySizeLabel);
			
			JLabel reviewSizeLabel = new JLabel("Review Session Size");
			reviewSizeLabel.setBounds(10, 115, 125, 14);
			getContentPane().add(reviewSizeLabel);
			
			JLabel beforeReviewLabel = new JLabel("Cards Before Review");
			beforeReviewLabel.setBounds(10, 158, 148, 14);
			getContentPane().add(beforeReviewLabel);
			
			JLabel maxTriesLabel = new JLabel("Max tries per card");
			maxTriesLabel.setBounds(10, 199, 125, 14);
			getContentPane().add(maxTriesLabel);
			
			JLabel reviewRepsLabel = new JLabel("Repetitions per card during review");
			reviewRepsLabel.setBounds(10, 243, 200, 14);
			getContentPane().add(reviewRepsLabel);
			
			reviewRepsField = new JTextField();
			reviewRepsField.setText(Integer.toString(reviewReps));
			reviewRepsField.setHorizontalAlignment(SwingConstants.CENTER);
			reviewRepsField.setColumns(10);
			reviewRepsField.setBounds(223, 243, 38, 20);
			getContentPane().add(reviewRepsField);
			
			maxTriesField = new JTextField();
			maxTriesField.setText(Integer.toString(maxTries));
			maxTriesField.setHorizontalAlignment(SwingConstants.CENTER);
			maxTriesField.setColumns(10);
			maxTriesField.setBounds(157, 199, 38, 20);
			getContentPane().add(maxTriesField);
			
			studySizeField = new JTextField();
			studySizeField.setHorizontalAlignment(SwingConstants.CENTER);
			studySizeField.setText(Integer.toString(studyListSize));
			studySizeField.setBounds(157, 75, 38, 20);
			getContentPane().add(studySizeField);
			studySizeField.setColumns(10);
			
			reviewSizeField = new JTextField();
			reviewSizeField.setHorizontalAlignment(SwingConstants.CENTER);
			reviewSizeField.setText(Integer.toString(reviewSessionSize));
			reviewSizeField.setBounds(157, 115, 38, 20);
			getContentPane().add(reviewSizeField);
			reviewSizeField.setColumns(10);
			
			beforeReviewField = new JTextField();
			beforeReviewField.setText(Integer.toString(switchToReview));
			beforeReviewField.setHorizontalAlignment(SwingConstants.CENTER);
			beforeReviewField.setBounds(157, 158, 38, 20);
			getContentPane().add(beforeReviewField);
			beforeReviewField.setColumns(10);
			
			JCheckBox instantMasteryCheck = new JCheckBox("Count cards as mastered if correct on first viewing");
			instantMasteryCheck.setSelected(instantMastery);
			instantMasteryCheck.setBounds(10, 300, 383, 23);
			instantMasteryCheck.addItemListener(new ItemListener() {
		         public void itemStateChanged(ItemEvent e) {
		             instantMastery = e.getStateChange()==1;
		          }
		       });
			getContentPane().add(instantMasteryCheck);
			
			JRadioButton simplifiedButton = new JRadioButton("Simplified", !traditional);
	        JRadioButton traditionalButton = new JRadioButton("Traditional", traditional);
	        simplifiedButton.setBounds(10, 343, 383, 23);
	        traditionalButton.setBounds(10, 363, 383, 23);
	        
	        ButtonGroup characterSetButton = new ButtonGroup();
	        characterSetButton.add(simplifiedButton);
	        characterSetButton.add(traditionalButton);
	        getContentPane().add(traditionalButton);
	        getContentPane().add(simplifiedButton);
	        
	        
			
			JButton applyButton = new JButton("Apply and Close");
			applyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (traditionalButton.isSelected()) {
						traditional = true;
						hanziPromptText.setText((currentStudyList.get(currentCardIndex).traditional));
					} else {
						traditional = false;
						hanziPromptText.setText((currentStudyList.get(currentCardIndex).hanzi));
					}
					String studySize = studySizeField.getText();
					String reviewSize = reviewSizeField.getText();
					String beforeReview = beforeReviewField.getText();
					String maxTriesString = maxTriesField.getText();
					String revReps = reviewRepsField.getText();
					try {
						// make a setter for these (?)
						int checkForReinit = reviewSessionSize;
						studyListSize = Integer.parseInt(studySize); // Parses user input for integers to make sure they didn't enter bogus values
						reviewSessionSize = Integer.parseInt(reviewSize);
						sessionSizeSaver = reviewSessionSize; // this line is critical to avoid weird behavior
						switchToReview = Integer.parseInt(beforeReview);
						maxTries = Integer.parseInt(maxTriesString);
						reviewReps = Integer.parseInt(revReps);
						if (checkForReinit != reviewSessionSize) { // if the user has changed the setting for reviewSessionSize, a new array will have to be created for reviewListIndicies
							initRequired = true;
							reviewListIndicies = new int [reviewSessionSize];
						}
						dispose();
					} catch (Exception f) {
						JOptionPane.showMessageDialog(null, "You have not entered valid values for the settings you have changed. Please use integer values only.", "Oops!", JOptionPane.ERROR_MESSAGE);
						
					}
					
				}
			});
			applyButton.setBounds(350, 497, 128, 23);
			getContentPane().add(applyButton);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			cancelButton.setBounds(250, 497, 89, 23);
			getContentPane().add(cancelButton);
			
			JButton defaultsButton = new JButton("Restore Default Settings");
			defaultsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					studySizeField.setText("20");
					reviewSizeField.setText("20");
					beforeReviewField.setText("100");
					maxTriesField.setText("3");
					reviewRepsField.setText("1");
				}
			});
			defaultsButton.setBounds(10, 497, 185, 23);
			getContentPane().add(defaultsButton);
			
			setSize(new Dimension(500,570));
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // makes sure window is centered
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			setVisible(true);
		}
	}
	
	public class HanziWelcomeScreen extends JDialog {
		
		private JButton pickUserButton;
		private JButton newUserButton;

		private static final long serialVersionUID = 1L;
		private JLabel lblNewLabel;
		
		
		
		HanziWelcomeScreen() {
			
			setResizable(false);
			setTitle("Hanzi Driller");
			ImageIcon image = new ImageIcon("kanjilogo.png");
			setIconImage(image.getImage());
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			this.setSize(461, 349);
			
			pickUserButton = new JButton("Existing User");
			pickUserButton.setBounds(369, 254, 115, 23);
			getContentPane().add(pickUserButton);
			pickUserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					HanziExistingUser changeDeck = new HanziExistingUser();
					changeDeck.setModalityType(ModalityType.APPLICATION_MODAL);
					changeDeck.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					changeDeck.setVisible(true);
					dispose();
				}
			});
			
			
			newUserButton = new JButton("New User");
			newUserButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new HanziNewUser();
					dispose();
				}
			});
			newUserButton.setBounds(369, 309, 115, 23);
			getContentPane().add(newUserButton);
			
			JLabel welcomeLabel = new JLabel("Welcome to Hanzi Driller");
			welcomeLabel.setFont(new Font("Georgia", Font.PLAIN, 24));
			welcomeLabel.setBounds(289, 129, 273, 90);
			getContentPane().add(welcomeLabel);
			
			lblNewLabel = new JLabel("");
			lblNewLabel.setIcon(new ImageIcon("robotDrillerCartoonSMALL.png"));
			lblNewLabel.setBounds(369, 341, 115, 167);
			getContentPane().add(lblNewLabel);
			setSize(new Dimension(870,570));
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // centers window
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			setVisible(true);
			
		}
	}
	
	public class HanziNewUser extends JDialog {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTextField userNameField;
		
		HanziNewUser() {
			
			
			inSession = false;
			
			Map<String, String> fileKeys = new HashMap<String, String>();
			// fileKeys.put("Top 3000 Characters Frequency List", "C:/Users/magne/eclipse-workspace/HanziDriller/src/tai/arima/hanzidriller/hanzi3000.xlsx"); // change this to not be an absolute path FIX!
			fileKeys.put("Top 3000 Characters Frequency List", "hanzi3000.xlsx");
			String fileList[] = {"Hanzi by Frequency List -- 0-1000", "Hanzi by Frequency List -- 1001-2000", "Hanzi by Frequency List -- 2001-3000", "Hanzi by Frequency List -- 3001-4000", "Hanzi by Frequency List -- 4001-5000", "Hanzi by Frequency List -- 5001-6000"};
			/* Map<String, Integer> top3000 = new HashMap<String, Integer>();
			top3000.put("Hanzi by Frequency List -- 0-1000", 1000);
			top3000.put("Hanzi by Frequency List -- 1001-2000", 2000);
			top3000.put("Hanzi by Frequency List -- 2001-3000", 3000); */
			// start here
			
			setResizable(false);
			setSize(new Dimension(870,570));
			setTitle("Hanzi Driller");
			ImageIcon image = new ImageIcon("kanjilogo.png");
			setIconImage(image.getImage());
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			
			JLabel chooseNameLabel = new JLabel("Enter a User Name: ");
			chooseNameLabel.setBounds(28, 64, 129, 14);
			getContentPane().add(chooseNameLabel);
			
			JLabel pickListLabel = new JLabel("Pick a list of hanzi to study: ");
			pickListLabel.setBounds(28, 128, 188, 14);
			getContentPane().add(pickListLabel);
			
			userNameField = new JTextField();
			userNameField.setBounds(151, 61, 178, 20);
			getContentPane().add(userNameField);
			userNameField.setColumns(10);
			
			JList deckList = new JList(fileList); // fix this later
			deckList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			deckList.setBorder(new LineBorder(new Color(0, 0, 0)));
			deckList.setBounds(211, 127, 350, 197);
			getContentPane().add(deckList);
			deckList.addListSelectionListener(new ListSelectionListener() { 
			    @Override
			    public void valueChanged(ListSelectionEvent e) {
			      if (deckList.getSelectedValue() != null) {
			    	  final List<String> selectedValuesList = deckList.getSelectedValuesList();
			    	  listSelected = selectedValuesList.get(0); 
			      }
			    }
			  });
			
			JCheckBox acceptSettingsCheck = new JCheckBox("Accept Default Settings?");
			acceptSettingsCheck.setSelected(true);
			acceptSettingsCheck.setBounds(28, 360, 217, 23);
			getContentPane().add(acceptSettingsCheck);
			acceptSettingsCheck.addItemListener(new ItemListener() {
		         public void itemStateChanged(ItemEvent e) {
		             keepDefaults = e.getStateChange()==1;
		          }
		       });
			JButton createProfileButton = new JButton("Create Profile and Start Studying!");
			createProfileButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (listSelected == null) {
						JOptionPane.showMessageDialog(null, "You have not chosen a list of characters to study!", "Oops!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						userName = userNameField.getText();
						if (userName == null || userName.isBlank()) {
							JOptionPane.showMessageDialog(null, "You have not entered a valid user name.", "Oops!", JOptionPane.ERROR_MESSAGE);
							return;
						}
						if (new File(userName + ".ser").isFile()) {
							JOptionPane.showMessageDialog(null, "There is already an existing user with this user name. Please choose a unique user name.", "Oops!", JOptionPane.ERROR_MESSAGE);
							return;
						}
						initialize();
						if (!keepDefaults) {
							HanziSettings settings = new HanziSettings();
					        settings.setModalityType(ModalityType.APPLICATION_MODAL);
					        settings.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					        settings.setVisible(true);
						}
						
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					dispose();
				}
			});
			createProfileButton.setBounds(529, 497, 264, 23);
			getContentPane().add(createProfileButton);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new HanziWelcomeScreen();
					dispose();
				}
			});
			cancelButton.setBounds(402, 497, 89, 23);
			getContentPane().add(cancelButton);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // center window
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			setVisible(true);
		}
	}
	
public class HanziExistingUser extends JDialog {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTextField userNameField;
		
		
		
		HanziExistingUser() {
			
			
			ArrayList<String> userList = new ArrayList<String>();
			String currentDirectory = Paths.get("").toAbsolutePath().toString();
			File[] files = new File(currentDirectory).listFiles(new FilenameFilter() 
			{                     
				@Override                     
				public boolean accept(File dir, String name) {
					return name.endsWith(".ser");                     
				}                 
			}); 
			for (File file : files) {
			    if (file.isFile()) {
			    	userList.add(file.getName());
			    }
			}
			int i = 0;
			for (String s : userList) {
				s = s.substring(0, s.lastIndexOf('.'));
				userList.set(i, s);
				i++;
			}
			
			
			Map<String, String> fileKeys = new HashMap<String, String>();
			// fileKeys.put("Top 3000 Characters Frequency List", "C:/Users/magne/eclipse-workspace/HanziDriller/src/tai/arima/hanzidriller/hanzi3000.xlsx");
			fileKeys.put("Top 3000 Characters Frequency List", "hanzi3000.xlsx");
			
			String fileList[] = {"Top 3000 Characters Frequency List"};
			
			setResizable(false);
			setSize(new Dimension(870,570));
			setTitle("Hanzi Driller");
			ImageIcon image = new ImageIcon("kanjilogo.png");
			setIconImage(image.getImage());
			setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(null);
			
			JLabel chooseNameLabel = new JLabel("User Name: " + userName);
			chooseNameLabel.setBounds(28, 64, 129, 14);
			if (inSession) {
				getContentPane().add(chooseNameLabel);
			}
			
			
			JLabel pickListLabel = new JLabel("Choose user profile: ");
			if (inSession) {
				pickListLabel.setText("Pick a list of hanzi to study: ");
			}
			pickListLabel.setBounds(28, 128, 188, 14);
			getContentPane().add(pickListLabel);
			
			
			
			JList existingUsers = new JList(userList.toArray()); // fix this later
			// deckList.setBorder(new LineBorder(new Color(0, 0, 0)));
			// deckList.setBounds(211, 127, 253, 197);
			// getContentPane().add(deckList);
			JScrollPane scrollPane = new JScrollPane(existingUsers);
			scrollPane.setBorder(new LineBorder(new Color(0, 0, 0)));
			scrollPane.setBounds(211, 127, 253, 197);
			getContentPane().add(scrollPane);
			existingUsers.addListSelectionListener(new ListSelectionListener() { // start here... fix this...
			    @Override
			    public void valueChanged(ListSelectionEvent e) {
			      if (existingUsers.getSelectedValue() != null) {
			    	  final List<String> selectedValuesList = existingUsers.getSelectedValuesList();
			    	  userSelection = selectedValuesList.get(0); 
			      }
			    }
			  });
			
			
			JButton continueButton = new JButton("Accept user selection and start studying!");
			if (inSession) {
				continueButton.setText("Continue studying.");
			}
			continueButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (userSelection == null) {
						JOptionPane.showMessageDialog(null, "You have not chosen a list of characters to study!", "Oops!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					try {
						restoringState = true;
						initialize();
						dispose();
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					dispose();
				}
			});
			continueButton.setBounds(529, 497, 264, 23);
			getContentPane().add(continueButton);
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (inSession) {
						new HanziWelcomeScreen();
						dispose();
					} else {
						new HanziWelcomeScreen();
						dispose();
					}
					
				}
			});
			cancelButton.setBounds(402, 497, 89, 23);
			getContentPane().add(cancelButton);
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); // found these two lines on StackOverflow to make sure window is centered on appearing
			this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
			setVisible(true);
		}
	}
			
	public static void main(String[] args) throws FileNotFoundException {
		
		// The following code block prevents users from opening multiple instances of the application by using a file lock. 
		String userHome = System.getProperty("user.home");
		File file = new File(userHome, "hanziMultiInstanceBlocker.lock");
		try {
		    FileChannel fc = FileChannel.open(file.toPath(),
		            StandardOpenOption.CREATE,
		            StandardOpenOption.WRITE);
		    FileLock lock = fc.tryLock();
		    if (lock == null) {
		        JOptionPane.showMessageDialog (null, "Another instance of the application is already running!", "Oops!", JOptionPane.ERROR_MESSAGE);
		        return;
		    }
		} catch (IOException e) {
		    throw new Error(e);
		}
		// ---- //
		
		HanziDriller drillerProgram = new HanziDriller();
		drillerProgram.setSize(new Dimension(870,570));
		// drillerProgram.setVisible(true);
		HanziWelcomeScreen welcomeScreen = drillerProgram.new HanziWelcomeScreen();
		welcomeScreen.setModalityType(ModalityType.APPLICATION_MODAL);
		welcomeScreen.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		welcomeScreen.setVisible(true);
		
	}
}
