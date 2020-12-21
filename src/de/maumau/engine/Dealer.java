package de.maumau.engine;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import de.maumau.networking.*;

/**
 * @author Barny
 */
public class Dealer {

	private final int CARDS_FOR_PLAYER = 7;

	private int[] cards;
	private String[] nameOfCards;
	private Deck openCards;
	private Deck pott;
	private int[] handedOutCards;
	private Player[] player;
	private int wishedColor;
	private Network network;
	private boolean isNetworkgame = false;
	private boolean contest = false;
	private List<Integer> offlinePlayers;
	private int timesMixed = 0;

	public Dealer(int noOfPlayers, int port,boolean contest) {
		String logfilename = "";
		network = new Network(noOfPlayers,port,contest);
		this.contest = contest;
		isNetworkgame = true;
		Player[] temp = network.getPlayers();
		player = new Player[temp.length];
		for(int i=0;i<temp.length;i++) {
			player[i] = temp[i];
			logfilename += player[i].getName() + " vs ";
		}
		offlinePlayers = new ArrayList<Integer>();
		
		initializeDealer();
	}

	public Dealer(Player player1,Player player2) {
		player = new Player[2];
		player[0] = player1;
		player[1] = player2;
		offlinePlayers = new ArrayList<Integer>();
		initializeDealer();
	}

	public Dealer(Player player1,Player player2,Player player3) {
		player = new Player[3];
		player[0] = player1;
		player[1] = player2;
		player[2] = player3;
		offlinePlayers = new ArrayList<Integer>();
		initializeDealer();
	}

	public void startNewGame() {
		do{
			for(int i=0;i<player.length;i++) {
				if(contest) {
					offlinePlayers = network.getOfflineList();
				}
				boolean skipPlayerWhileOffline = false;
				if(!offlinePlayers.contains(i))  {
					System.out.println("[#] Zug von Spieler "+player[i].getName().toString());
					if(!isNetworkgame) {
						System.out.println("\n[#] Spieler "+player[i].getName()+" ist dran!");
					} else {
						if(!contest) {
							if(!network.broadcastMessage("\n[#] Spieler "+player[i].getName().toString()+" ist dran!")) {
								skipPlayerWhileOffline = true;
								offlinePlayers = network.getOfflineList();
							}
						}else {
							JSONObject turn = new JSONObject();
							turn.put("status","okay");
							turn.put("turnOf",player[i].getName().toString());
							turn.put("skipped",player[i].willbeSkipped());
							turn.put("cardsLeft",player[i].getNoOfCardsLeft());
							turn.put("topCard",showTopCard());
							
							if(!network.broadcastMessage(turn.toJSONString())) {
								skipPlayerWhileOffline = true;
								offlinePlayers = network.getOfflineList();
							}
						}
					}
					if(!skipPlayerWhileOffline && !offlinePlayers.contains(i)) {
						player[i].turn(contest);
						if(!player[i].couldDrawCards()) {
							checkForWinnerInCaseOfNoCardsLeft();
							Thread.currentThread().interrupt();
							break;
						}
						if(player[i].getNoOfCardsLeft() == 0) {
							if(!isNetworkgame) {
								System.out.println("Spieler "+player[i].getName()+" hat gewonnen!");
							}else {
								if(!contest) {
									network.broadcastMessage("Spieler "+player[i].getName()+" hat gewonnen!");
								}else {
									JSONObject won = new JSONObject();

									won.put("status","okay");
									won.put("won",true);
									won.put("nameOfWinner",player[i].getName().toString());

									System.out.println("[#] Spieler "+player[i].getName().toString()+" hat gewonnen!");
									if(!network.broadcastMessage(won.toJSONString())) {
										offlinePlayers = network.getOfflineList();
									}
								}
							}
							break;
						}else {
							if(contest) {
								offlinePlayers = network.getOfflineList();
							}
							if(checkForUnexpectedWin()) {
								Thread.currentThread().interrupt();
								break;
							}
						}
					}else {
						if(checkForUnexpectedWin()) {
							Thread.currentThread().interrupt();
							break;
						}
					}
				}else {
					if(checkForUnexpectedWin()) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}while(!playerHasWon() && !Thread.currentThread().isInterrupted());

		if(Thread.currentThread().isInterrupted()) {
			network.interruptConnections();
		}
	}

	private void checkForWinnerInCaseOfNoCardsLeft() {
		JSONObject won = new JSONObject();
		int max = 0,index = 0;
		won.put("status","okay");
		won.put("won",true);

		for(int i=0;i<player.length;i++) {
			if(!offlinePlayers.contains(i)) {
				max = player[i].getValidTurns();
				index = i;
			}
		}

		for(int k=index+1;k<player.length;k++) {
			if(!offlinePlayers.contains(k)) {
				if(player[k].getValidTurns() > max) {
					max = player[k].getValidTurns();
					index = k;
				}
			}
		}
		won.put("nameOfWinner",player[index].getName().toString());
		System.out.println("[!] Es gibt keine Karten mehr zu ziehen!");
		System.out.println("[!] Mind. 1 Spieler spielt nicht korrekt! Spiel wird abgebrochen...");
		System.out.println("[#] Gewinner mit den meisten gültigen Zügen: "+player[index].getName().toString());
		network.broadcastMessage(won.toJSONString());
	}

	private boolean checkForUnexpectedWin() {
		if(offlinePlayers.size() == player.length-1) {
			JSONObject won = new JSONObject();

			won.put("status","okay");
			won.put("won",true);
			for(int k=0;k<player.length;k++) {
				if(!offlinePlayers.contains(k)) {
					won.put("nameOfWinner",player[k].getName().toString());
					System.out.println("[#] Spieler "+player[k].getName().toString()+" hat gewonnen!");
					break;
				}
			}
			network.broadcastMessage(won.toJSONString());
			return true;
		}
		return false;
	}

	protected boolean newTurn(int card,Player player) {
		if(!isInOpenCards(card) && isInHandedOutCards(card)) {
			if(isSameColor(card) || isSameImage(card)) {
				if(wishedColor != -1) {
					if(isWishedColor(card) && !isKnave(card)) {
						wishedColor = -1;
						checkSpecialCards(card, player);
						pott.push(card);
						deleteUsedCard(card);
						return true;
					}else {
						return false;
					}
				}

				checkSpecialCards(card, player);

				pott.push(card);
				deleteUsedCard(card);
				return true;
			}else {
				if(isKnave(card)) {
					wishedColor = -1;
					pott.push(card);
					deleteUsedCard(card);
					return true;
				}else {
					if(wishedColor != -1) {
						if(isWishedColor(card) && !isKnave(card)) {
							wishedColor = -1;
							pott.push(card);
							deleteUsedCard(card);
							checkSpecialCards(card, player);
							return true;
						}else {
							return false;
						}
					}else {
						return false;
					}
				}
			}
		}else {
			System.out.println("[#] Ziehe Karte...");
			return false;
		}
	}

	private void checkSpecialCards(int card, Player player) {
		if(isKnave(card)) {
			wishedColor = -1;
		}

		if(isEight(card)) {
			skipNextPlayer(player);
		}

		if(isSeven(card)) {
			nextPlayerDrawTwoCards(player);
		}
	}

	protected int[] getHandForPlayer() {
		int[] hand = new int[handedOutCards.length/player.length];
		for(int i=0;i<hand.length;i++) {
			hand[i] = openCards.peek();
			openCards.pop();    		
			for(int k=0;k<handedOutCards.length;k++) {
				if(handedOutCards[k] == -1) {
					handedOutCards[k] = hand[i];
					break;
				}
			}
		}
		return hand;
	}

	public int showTopCard() {
		return pott.peek();
	}

	protected int checkStatus() {
		int topCard = pott.peek();

		if(isKnave(topCard)) {
			return 1;
		}

		if(isSeven(topCard)) {
			return 2;
		}

		if(isEight(topCard)) {
			return 3;
		}

		return 0;
	}

	protected int[] drawTwoCards() {
		int[] twoCards = new int[2];

		if(openCards.size() > 1) {
			timesMixed = 0;
			for(int i=0;i<twoCards.length;i++) {
				twoCards[i] = openCards.peek();
				openCards.pop();
			}
			handedOutCards = incArray(handedOutCards,twoCards[0]);
			handedOutCards = incArray(handedOutCards,twoCards[1]);
			return twoCards;
		}else {
			if(timesMixed < 3) {
				mixAfterOpencardsEmpty();
				timesMixed++;
				return drawTwoCards();
			}else {
				int[] error = {-1,-1};
				return error;
			}
		}
	}

	protected int drawCard() {
		int temp;
		if(!openCards.isEmpty()) {
			timesMixed = 0;
			temp = openCards.peek();
			openCards.pop();
			handedOutCards = incArray(handedOutCards,temp);
			return temp;
		}else {
			if(timesMixed < 3) {
				mixAfterOpencardsEmpty();
				timesMixed++;
				return drawCard();
			}else {
				return -1;
			}
		}
	}

	protected void setWishedColor(int wishedColor) {
		this.wishedColor = wishedColor;
	}

	protected int getWishedColor() {
		return wishedColor;
	}

	protected String getStringOfWishedColor() {
		switch(wishedColor) {
			case 0: {
				return "Pik";
			}
			case 1: {
				return "Karo";
			}
			case 2: {
				return "Herz";
			}
			case 3: {
				return "Kreuz";
			}
		}
		return null;   	
	}

	protected String getNameOfCard(int card) {
		return nameOfCards[card];
	}

	private void nextPlayerDrawTwoCards(Player player) {
		for(int i=0;i<this.player.length;i++) {
			if(this.player[i].getName().equals(player.getName())) {
				if(i <= this.player.length-2) {
					this.player[i+1].drawTwoCards();
				}else {
					this.player[0].drawTwoCards();
				}
			}
		}
	}

	private void skipNextPlayer(Player player) {
		for(int i=0;i<this.player.length;i++) {
			if(this.player[i].getName().equals(player.getName())) {
				if(i <= this.player.length-2) {
					this.player[i+1].skipNextRound();
				}else {
					this.player[0].skipNextRound();
				}
			}
		}
	}

	private boolean playerHasWon() {
		for(int i=0;i<player.length;i++) {
			if(player[i].getNoOfCardsLeft() == 0) {
				return true;
			}
		}
		return false;
	}

	private void initializeDealer() {
		cards = new int[32];
		if(!isNetworkgame) {
			nameOfCards = initNameOfCardsForNetworkPlay();
		}else {
			nameOfCards = initNameOfCardsForNetworkPlay();
		}
		initializeCards();
		mixCardsToOpenCards();
		handedOutCards = new int[player.length * CARDS_FOR_PLAYER];
		for(int i=0;i<handedOutCards.length;i++) {
			handedOutCards[i] = -1;
		}

		for(int i=0;i<player.length;i++) {
			player[i].setDealer(this);
		}
		wishedColor = -1;
	}

	private void mixAfterOpencardsEmpty() {
		System.out.println("[!] Der Dealer mischt die Karten neu");
		int tempTopCard = pott.peek();
		pott.pop();
		int[] tempPott = new int[pott.size()];
		int i = 0;
		if(!pott.isEmpty()) {
			do {
				tempPott[i] = pott.peek();
				pott.pop();
				i++;
			}while(!pott.isEmpty());

			pott.push(tempTopCard);

			do{
				int random = getRandomNo(0,tempPott.length);
				if(tempPott[random] != -1) {
					openCards.push(tempPott[random]);
					tempPott[random] = -1;
				}
			}while(!allisused(tempPott));
		}else {
			pott.push(tempTopCard);
		}
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

	private void deleteUsedCard(int card) {
		for(int i=0;i<handedOutCards.length;i++) {
			if(handedOutCards[i] == card) {
				handedOutCards[i] = -1;
				break;
			}
		}
	}

	private boolean isSameColor(int card) {
		int pottCardColumnNo = pott.peek() % 4;
		int cardColumnNo = card % 4;
		return pottCardColumnNo == cardColumnNo;
	}

	private boolean isSameImage(int card) {
		int pottCardRowStart = pott.peek() / 4;
		int cardRowStart = card / 4;
		return pottCardRowStart == cardRowStart;
	}

	private void initializeCards() {
		for(int i=0;i<32;i++) {
			cards[i] = i;
		}
	}

	private void mixCardsToOpenCards() {
		openCards = new Deck();    
		do{
			int random = getRandomNo(0,32);
			if(cards[random] != -1) {
				openCards.push(cards[random]);
				cards[random] = -1;
			}
		}while(!allisused());
		pott = new Deck();
		pott.push(openCards.peek());
		openCards.pop();
		initializeCards();
	}

	private boolean allisused() {
		for(int i=0;i<cards.length;i++) {
			if(cards[i] != -1) {
				return false;
			}
		}
		return true;
	}

	private boolean allisused(int[] temp) {
		for(int i=0;i<temp.length;i++) {
			if(temp[i] != -1) {
				return false;
			}
		}
		return true;
	}

	private boolean isInOpenCards(int card) {
		return openCards.isInside(card);
	}

	private boolean isInHandedOutCards(int card) {
		for(int i=0;i<handedOutCards.length;i++) {
			if(handedOutCards[i] == card) {
				return true;
			}
		}
		return false;
	}

	private int getRandomNo(int min, int max) {
		return((int)((Math.random()) * max + min));
	}

	private int[] incArray(int[] arr,int card) {
		int[] temp = new int[arr.length+1];

		for(int i=0;i<arr.length;i++) {
			temp[i] = arr[i];
		}

		temp[temp.length-1] = card;
		return temp;
	}

	private boolean isWishedColor(int card) {
		int cardColumnNo = card % 4;
		return getWishedColor() == cardColumnNo;
	}

	private String[] initNameOfCards() {
		String[] arr = new String[cards.length];

		arr[0] = Character.toString((char)0x2660)+" 7";
		arr[1] = Character.toString((char)0x2662)+" 7";
		arr[2] = Character.toString((char)0x2661)+" 7";
		arr[3] = Character.toString((char)0x2663)+" 7";
		arr[4] = Character.toString((char)0x2660)+" 8";
		arr[5] = Character.toString((char)0x2662)+" 8";
		arr[6] = Character.toString((char)0x2661)+" 8";
		arr[7] = Character.toString((char)0x2663)+" 8";
		arr[8] = Character.toString((char)0x2660)+" 9";
		arr[9] = Character.toString((char)0x2662)+" 9";
		arr[10] = Character.toString((char)0x2661)+" 9";
		arr[11] = Character.toString((char)0x2663)+" 9";
		arr[12] = Character.toString((char)0x2660)+" 10";
		arr[13] = Character.toString((char)0x2662)+" 10";
		arr[14] = Character.toString((char)0x2661)+" 10";
		arr[15] = Character.toString((char)0x2663)+" 10";
		arr[16] = Character.toString((char)0x2660)+" Bube";
		arr[17] = Character.toString((char)0x2662)+" Bube";
		arr[18] = Character.toString((char)0x2661)+" Bube";
		arr[19] = Character.toString((char)0x2663)+" Bube";
		arr[20] = Character.toString((char)0x2660)+" Dame";
		arr[21] = Character.toString((char)0x2662)+" Dame";
		arr[22] = Character.toString((char)0x2661)+" Dame";
		arr[23] = Character.toString((char)0x2663)+" Dame";
		arr[24] = Character.toString((char)0x2660)+" König";
		arr[25] = Character.toString((char)0x2662)+" König";
		arr[26] = Character.toString((char)0x2661)+" König";
		arr[27] = Character.toString((char)0x2663)+" König";
		arr[28] = Character.toString((char)0x2660)+" Ass";
		arr[29] = Character.toString((char)0x2662)+" Ass";
		arr[30] = Character.toString((char)0x2661)+" Ass";
		arr[31] = Character.toString((char)0x2663)+" Ass";

		return arr;
	}

	private String[] initNameOfCardsForNetworkPlay() {
		String[] arr = new String[cards.length];

		arr[0] = "Pik 7";
		arr[1] = "Karo 7";
		arr[2] = "Herz 7";
		arr[3] = "Kreuz 7";
		arr[4] = "Pik 8";
		arr[5] = "Karo 8";
		arr[6] = "Herz 8";
		arr[7] = "Kreuz 8";
		arr[8] = "Pik 9";
		arr[9] = "Karo 9";
		arr[10] = "Herz 9";
		arr[11] = "Kreuz 9";
		arr[12] = "Pik 10";
		arr[13] = "Karo 10";
		arr[14] = "Herz 10";
		arr[15] = "Kreuz 10";
		arr[16] = "Pik Bube";
		arr[17] = "Karo Bube";
		arr[18] = "Herz Bube";
		arr[19] = "Kreuz Bube";
		arr[20] = "Pik Dame";
		arr[21] = "Karo Dame";
		arr[22] = "Herz Dame";
		arr[23] = "Kreuz Dame";
		arr[24] = "Pik König";
		arr[25] = "Karo König";
		arr[26] = "Herz König";
		arr[27] = "Kreuz König";
		arr[28] = "Pik Ass";
		arr[29] = "Karo Ass";
		arr[30] = "Herz Ass";
		arr[31] = "Kreuz Ass";

		return arr;
	}

	public synchronized void quitGame() {
		System.out.println("[#] Server wird beendet...");
		network.interruptConnections();
		network = null;
		Thread.currentThread().interrupt();
	}
	
	

}
