package tai.arima.hanzidriller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class HanziCard implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String hanzi;
	String reading;
	String meaning;
	String traditional;
	int score;
	int totalViews;
	int lastSeen;
	public Date timeStamp; 
	ArrayList<String> multReads;
	static final String tones = "*āáǎàēéěèīíǐìōóǒòūúǔùǖǘǚǜ"; // this string is a container of all possible special characters used to help convert "real" pinyin to plain text characters
	
	HanziCard(String hanzi, String reading, String meaning) { // a constructor
		this.hanzi = hanzi;
		this.reading = reading;
		this.meaning = meaning;
		score = -1; // score is initialized to -1 to indicate the user has never seen the current hanzi card
		timeStamp = new Date(); // the date since last time a card was answered correctly will be kept track of, in order to only increase score if it has been >24 hours since last correct answer. This is to prevent people from cramming excessively to mastery
		totalViews = 0;
		multReads = new ArrayList<String>();
	}
	
	HanziCard() {
		score = -1;
		totalViews = 0;
		multReads = new ArrayList<String>();
		lastSeen = -1;
		timeStamp = new Date();
	}
	
	public void correctScore(boolean instantMastery, boolean reviewOn) {
		
		if (reviewOn) {
			Date now = new Date();
			long diffInMilliseconds = now.getTime() - timeStamp.getTime();
			if (diffInMilliseconds / 3600000D > 24 || score < 3) {
				score++;
			} 
		} 
		else if (score == -1 && instantMastery) {
			score = 6;
		} else if (score == -1 && !instantMastery) {
			score = 1;
		}
		else {
			score++;
		}
		this.timeStamp = new Date(); // each time a user gets a correct score, they get a new time stamp for the current date. Once an item goes into the reviewList, it should only increase in score if it has been at least 24 hours since their last correct score. This prevents users from mastering something in a single day.
		totalViews++;
	}
	
	public void incorrectScore() {
		score = 0;
		totalViews++;
	}
	
	public void undoScore(int lastScore) {
		score = lastScore;
	}
	
	public String convertPinyin() { // this function is used to convert "real" pinyin to plain text (i.e., no special characters (tāng -> tang1))
		int vowelPicker = -1;
		String fixedPinyin = "";
		
		for (int i = 0; i < reading.length(); i++) { // to iterate through the reading string character by character
			if (tones.indexOf(reading.charAt(i)) != -1) {
				 fixedPinyin = reading.substring(0, i);
				 vowelPicker = tones.indexOf(reading.charAt(i));
				 if (vowelPicker / 4.0 <= 1) {						// this string of if statements is based upon the positioning of vowels in the string tones
					 fixedPinyin += "a" + reading.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 2) {
					 fixedPinyin += "e" + reading.substring(i+1);
					 break;
				 }
				 else if (vowelPicker  / 4.0 <= 3) {
					 fixedPinyin += "i" + reading.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 4) {
					 fixedPinyin += "o" + reading.substring(i+1);
					 break;
				 }
				 else if (vowelPicker / 4.0 <= 5) {
					 fixedPinyin += "u" + reading.substring(i+1);
					 break;
				 }
				 else {
					 fixedPinyin += "v" + reading.substring(i+1);
					 break;
				 }
			}
		}
		if (vowelPicker == -1) { // in the case that no special characters were found in the reading, the original reading will be returned
			return reading;
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
	
}	
