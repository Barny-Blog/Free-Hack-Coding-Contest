package de.maumau.engine;

public class BotEngine {
	
	private int[] hand;
	private int topCard;
	private int wishedCard;
	
	public BotEngine(int[] hand,int topCard, int wishedCard) {
		this.hand = new int[hand.length];
		for(int i=0;i<hand.length;i++) {
			this.hand[i] = hand[i];
		}
		this.topCard = topCard;
		this.wishedCard = wishedCard;
	}
	
	public int calcNextCard() {
		int[] possibleCards = getPossibleCards();
		
		if(possibleCards.length == -1) {
			return -1;	
		}
		
		// Haben wir eine 7 oder 8? --> Dann legen
		for(int i=0;i<possibleCards.length;i++) {
			if(isEight(possibleCards[i]) || isSeven(possibleCards[i])) {
				for(int k=0;k<hand.length;k++) {
					if(hand[k] == possibleCards[i]) {
						hand = removeCardFromHand(k);
						return k;
					}
				}
			}
		}
		
		for(int i=0;i<possibleCards.length;i++) {
			if(!isKnave(possibleCards[i])) {
				for(int k=0;k<hand.length;k++) {
					if(hand[k] == possibleCards[i]) {
						hand = removeCardFromHand(k);
						return k;
					}
				}
			}
		}
		
		for(int i=0;i<possibleCards.length;i++) {
			if(isKnave(possibleCards[i])) {
				for(int k=0;k<hand.length;k++) {
					if(hand[k] == possibleCards[i]) {
						hand = removeCardFromHand(k);
						return k;
					}
				}
			}
		}

		
		return -1;
	}

	public int calcWishedColor() {
		int[] colors = new int[4];
		
		// Karten nach Farben sortieren
		for(int i=0;i<hand.length;i++) {
			colors[getColorOfCard(hand[i])] = hand[i];
		}
		
		// Von welcher Farbe sind die meisten vorhanden?
		int maxCards = colors[0];
		int colorIndex = 0;
		for(int i=1;i<colors.length;i++) {
			if(colors[i] > maxCards) {
				maxCards = colors[i];
				colorIndex = i;
			}
		}
		
		return colorIndex;
	}
	
	private int[] getPossibleCards() {
		int[] temp = new int[0];
		if(wishedCard == -1) {
			for(int i=0;i<hand.length;i++) {
				if(isKnave(hand[i]) || isSameColor(hand[i]) || isSameImage(hand[i])) {
					temp = incArray(temp,hand[i]);
				}
			}
		}else {
			for(int i=0;i<hand.length;i++) {
				if(!isKnave(hand[i]) && isSameColor(hand[i],wishedCard)) {
					temp = incArray(temp,hand[i]);
				}
			}
		}
		return temp;
	}
	
	private boolean isKnave(int card) {
    	return (card / 4) == 4;
    }
    
    private boolean isSeven(int card) {
    	return (card / 4) == 0;
    }
    
    private boolean isEight(int card) {
    	return (card / 4) == 1;
    }
    
    private boolean isSameColor(int card) {
        int pottCardColumnNo = topCard % 4;
        int cardColumnNo = card % 4;
        return pottCardColumnNo == cardColumnNo;
    }
    
    private boolean isSameColor(int card,int wishedColor) {
        int pottCardColumnNo = wishedColor;
        int cardColumnNo = card % 4;
        return pottCardColumnNo == cardColumnNo;
    }
    
    private int getColorOfCard(int card) {
    	return card % 4;
    }
    
    private boolean isSameImage(int card) {
        int pottCardRowStart = topCard / 4;
        int cardRowStart = card / 4;
        return pottCardRowStart == cardRowStart;
    }

    private int[] incArray(int[] arr,int card) {
    	int[] temp = new int[arr.length+1];
    	
    	for(int i=0;i<arr.length;i++) {
    		temp[i] = arr[i];
    	}
    	
    	temp[temp.length-1] = card;
    	return temp;
    }
    
    private int[] removeCardFromHand(int index) {
    	int[] temp = new int[hand.length-1];
    	if(hand.length >= 2) {
	    	for(int i=0;i<temp.length;i++) {
	    		if(i < index) {
	    			temp[i] = hand[i];
	    		}else {
	    			if(i > index) {
	    				temp[i-1] = hand[i];
	    			}
	    		}
	    	}
    	}else {
    		if(temp.length == 1) {
    			temp[0] = hand[0];
    		}
    	}
    	return temp;
    }
    
}
