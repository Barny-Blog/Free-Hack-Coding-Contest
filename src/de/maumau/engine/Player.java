package de.maumau.engine;

import java.net.SocketTimeoutException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import de.maumau.networking.Network;

/**
 * @author Barny
 */

public class Player {

	private String name;
	private boolean isBot;
	private int[] hand;
	private Dealer dealer;
	private Scanner eingabe;
	private boolean skipPlayer = false;
	private boolean drawTwoCards = false;
	private boolean knaveIsOnTop = false;
	private boolean isNetworkPlayer;
	private boolean couldDrawCards = true;
	private boolean cardsSent = false;
	private int networkId,validTurns;
	private Network network;
	
	public Player(String name,boolean isBot) {
		this.name = name;
		this.isBot = isBot;	
		this.isNetworkPlayer = false;
		this.validTurns = 0;
	}
	
	public Player(String name,int networkId,Network network) {
		this.name = name;
		this.isBot = false;	
		this.isNetworkPlayer = true;
		this.networkId = networkId;
		this.network = network;
		this.validTurns = 0;
	}
	
	public String getName() {
		return this.name;
	}
	
	protected void showHandCards() {
		if(!isNetworkPlayer) {
			System.out.println("Karten auf der Hand:");
		}else {
			network.sendToPlayer("Karten auf der Hand:",networkId);
		}
		for(int i=0;i<hand.length;i++) {
			if(!isNetworkPlayer) {
				System.out.println((i)+".)" +dealer.getNameOfCard(hand[i]));
			}else {
				network.sendToPlayer((i)+".)" +dealer.getNameOfCard(hand[i]),networkId);
			}
		}
	}
	
	protected void turn(boolean contest) {
		if(contest) {
			networkBotBattleTurn();
		}else {
			if(!isNetworkPlayer) {
				if(!isBot) {
					playersTurn();
				}else {
					botsTurn();
				}
			}else {
				networkPlayersTurn();
			}
		}
	}
	
	public void setDealer(Dealer dealer) {
		this.dealer = dealer;
		int[] temp = dealer.getHandForPlayer();
		hand = new int[temp.length];
		for(int i=0;i<temp.length;i++) {
			hand[i] = temp[i];
		}
	}
	
	protected int getNoOfCardsLeft() {
		return hand.length;
	}
	
	protected void skipNextRound() {
		skipPlayer = true;
	}
	
	protected boolean willbeSkipped() {
		return skipPlayer;
	}
	
	protected void drawTwoCards() {
		drawTwoCards = true;
	}
	
	private void networkBotBattleTurn() {
		boolean playerIsOffline = false;
		
		JSONObject toSend = new JSONObject();
		
		knaveIsOnTop = dealer.checkStatus() == 1;
		toSend.put("status","okay");
		toSend.put("topcard",dealer.showTopCard());
		if(drawTwoCards) {
			int[] temp = dealer.drawTwoCards();
			if(temp[0] != -1 && temp[1] != -1) {
				hand = incArray(hand,temp[0]);
				hand = incArray(hand,temp[1]);
				toSend.put("drawTwoCards",true);
				JSONArray drawedCards = new JSONArray();
				for(int i=0;i<temp.length;i++) {
					drawedCards.add(temp[i]);
				}
				toSend.put("drawedCards",drawedCards);
				drawTwoCards = false;
			}else {
				couldDrawCards = false;
			}
		}else {
			toSend.put("drawTwoCards",false);
		}
			
		if(!skipPlayer) {
			toSend.put("skipped",false);
			if(knaveIsOnTop && dealer.getStringOfWishedColor() != null) {
				toSend.put("wishedColor",dealer.getWishedColor());
			}else {
				toSend.put("wishedColor",-1);
			}
			
			toSend.put("cardsLeft", hand.length);
			
			if(!cardsSent) {
				JSONArray handCards = new JSONArray();
				for(int i=0;i<hand.length;i++) {
					handCards.add(hand[i]);
				}
				
				toSend.put("hand",handCards);
				cardsSent = true;
			}
			
			if(!network.sendToPlayer(toSend.toJSONString(),networkId)) {
				if(!network.retryConnectionWithLastMessage(toSend.toJSONString(),networkId)) {
					JSONObject skip = new JSONObject();
					skip.put("status","okay");
					network.sendToPlayer(skip.toJSONString(),networkId);
					int card = dealer.drawCard();
					hand = incArray(hand, card);
					playerIsOffline = true;
					System.out.println("[!] Verbindung zu Spieler "+networkId+" verloren! Spieler setzt diese Runde aus!");
				}
			}
			if(!playerIsOffline) {
				int selectedCard;
				String JSON_ResponseLine;
				try {
					JSON_ResponseLine = network.readLineFromBot(networkId);
					
					JSONParser parser = new JSONParser();
					try {
						Object obj = parser.parse(JSON_ResponseLine);
						JSONObject JSON_Response = (JSONObject) obj;
						
						String status = (String) JSON_Response.get("status");
						
						if(!status.equals("skip")) {
							long temp;
							temp = (Long) JSON_Response.get("selectedCard");
							try {
								selectedCard = (int) temp;
								if(selectedCard < 0 || selectedCard > 31) {
									throw new NumberFormatException();
								}
							}catch(Exception e) {
								System.out.println("[!] Fehlerhafte Karte von "+getName());
								selectedCard = 1;
							}
							
							int selectedCard2 = selectedCard;
							boolean cardFound = false;
							for(int i=0;i<hand.length;i++) {
								if(hand[i] == selectedCard) {
									selectedCard = i;
									cardFound = true;
									break;
								}
							}
							
							if(cardFound) {								
								if(dealer.newTurn(hand[selectedCard],this)) {
									hand = deleteCardFromHand(hand,hand[selectedCard]);
									validTurns++;
									System.out.println("[#] Spieler "+getName().toString()+" legt die Karte "+dealer.getNameOfCard(selectedCard2));
								}else {
									JSONObject error = new JSONObject();
									error.put("status","error");
									int card = dealer.drawCard();
									if(card != -1) {
										hand = incArray(hand, card);
										error.put("drawedCards",card);
									}else {
										couldDrawCards = false;
										error.put("drawedCards",-1);
									}
									network.sendToPlayer(error.toJSONString(),networkId);
									System.out.println("[!] Spieler "+getName().toString()+" erhält eine Strafkarte!");
								}
								
								if(dealer.checkStatus() == 1 && !knaveIsOnTop) {
									temp = (Long) JSON_Response.get("wishedColor");
									int selectedColor = (int) temp;
									if(selectedColor > -1 && selectedColor < 4) {
										dealer.setWishedColor(selectedColor);
									}else {
										dealer.setWishedColor(0);
									}
									System.out.println("[#] Spieler "+getName().toString()+" wünscht sich die Farbe "+dealer.getStringOfWishedColor());
								}
							}else {
								JSONObject error = new JSONObject();
								error.put("status","error");
								int card = dealer.drawCard();
								if(card != -1) {
									hand = incArray(hand, card);
									error.put("drawedCards",card);
								}else {
									couldDrawCards = false;
									error.put("drawedCards",-1);
								}
								network.sendToPlayer(error.toJSONString(),networkId);
								System.out.println("[!] Spieler "+getName().toString()+" erhält eine Strafkarte!");
							}
						}else {
							JSONObject skip = new JSONObject();
							skip.put("status","okay");
							int card = dealer.drawCard();
							if(card != -1) {
								hand = incArray(hand, card);
								skip.put("drawedCards",card);
							}else {
								couldDrawCards = false;
								skip.put("drawedCards",-1);
							}
							network.sendToPlayer(skip.toJSONString(),networkId);
							System.out.println("[#] Spieler "+getName().toString()+" setzt aus!");
						}
					}catch(ParseException e) {
						JSONObject error = new JSONObject();
						error.put("status","error");
						int card = dealer.drawCard();
						if(card != -1) {
							hand = incArray(hand, card);
							error.put("drawedCards",card);
						}else {
							couldDrawCards = false;
							error.put("drawedCards",-1);
						}
						network.sendToPlayer(error.toJSONString(),networkId);
						System.out.println("[!] Spieler "+getName().toString()+" erhält eine Strafkarte!");
					}
					
				}catch(Exception e) {
					JSONObject error = new JSONObject();
					error.put("status","error");
					int card = dealer.drawCard();
					if(card != -1) {
						hand = incArray(hand, card);
						error.put("drawedCards",card);
					}else {
						couldDrawCards = false;
						error.put("drawedCards",-1);
					}
					network.sendToPlayer(error.toJSONString(),networkId);
					System.out.println("[!] Spieler "+getName().toString()+" erhält eine Strafkarte!");
				}
			}
		}else {
			skipPlayer = false;
			toSend.put("skipped",true);
			System.out.println("[#] Spieler "+getName().toString()+" setzt aus!");
		}
	}

	public int getValidTurns() {
		return validTurns;
	}
	
	public boolean couldDrawCards() {
		return couldDrawCards;
	}
	
	private void networkPlayersTurn() {
		if(dealer.checkStatus() == 1) {
			knaveIsOnTop = true;
		}else {
			knaveIsOnTop = false;
		}
		
		if(drawTwoCards) {
			int[] temp = dealer.drawTwoCards();
			hand = incArray(hand,temp[0]);
			hand = incArray(hand,temp[1]);
			drawTwoCards = false;
		}
		
		if(!skipPlayer) {
			if(knaveIsOnTop && dealer.getStringOfWishedColor() != null) {
				network.sendToPlayer("[#] Oberste Karte ist: "+dealer.getNameOfCard(dealer.showTopCard()),networkId);
				network.sendToPlayer("[!] Gewünschte Farbe ist: "+dealer.getStringOfWishedColor(),networkId);
			}else {
				network.sendToPlayer("[#] Oberste Karte ist: "+dealer.getNameOfCard(dealer.showTopCard()),networkId);
			}
			
			network.broadcastMessage("[#] Ich habe noch "+hand.length+" Karten übrig",networkId);
			
			showHandCards();
			int selectedCard;
			try {
				selectedCard = network.readLineFromPlayer(networkId);
				if(selectedCard < 0 || selectedCard > 31) {
					throw new NumberFormatException();
				}
				if(dealer.newTurn(hand[selectedCard],this)) {
					hand = deleteCardFromHand(hand,hand[selectedCard]);
				}else {
					network.sendToPlayer("[#] Dieser Zug ist nicht erlaubt! Ziehe eine Karte...",networkId);
					network.broadcastMessage("[#] Spieler "+this.getName()+" hat das Spiel nicht verstanden! Er zieht zur Strafe eine Karte!",networkId);
					int card = dealer.drawCard();
					hand = incArray(hand, card);
				}
				
				if(dealer.checkStatus() == 1 && !knaveIsOnTop) {
					network.sendToPlayer("[!] Welche Farbe möchtest du dir wünschen?",networkId);
					network.sendToPlayer("0.) Pik",networkId);
					network.sendToPlayer("1.) Karo",networkId);
					network.sendToPlayer("2.) Herz",networkId);
					network.sendToPlayer("3.) Kreuz",networkId);
					int selectedColor = network.readLineFromPlayer(networkId);
					if(selectedColor > -1 && selectedColor < 4) {
						dealer.setWishedColor(selectedColor);
					}else {
						System.out.println("[!] Fehlerhafte Eingabe! Pik wird gewählt!");
						dealer.setWishedColor(0);
					}
				}
				
			}catch(Exception e) {
				network.sendToPlayer("[#] Ziehe eine Karte...",networkId);
				int card = dealer.drawCard();
				hand = incArray(hand, card);
			}
		}else {
			skipPlayer = false;
		}
	}
	
	private void playersTurn() {		
		if(dealer.checkStatus() == 1) {
			knaveIsOnTop = true;
		}else {
			knaveIsOnTop = false;
		}
		
		if(drawTwoCards) {
			int[] temp = dealer.drawTwoCards();
			hand = incArray(hand,temp[0]);
			hand = incArray(hand,temp[1]);
			drawTwoCards = false;
		}
		
		if(!skipPlayer) {
			if(knaveIsOnTop && dealer.getStringOfWishedColor() != null) {
				System.out.println("[#] Oberste Karte ist: "+dealer.getNameOfCard(dealer.showTopCard()));
				System.out.println("[!] Gewünschte Farbe ist: "+dealer.getStringOfWishedColor());
			}else {
				System.out.println("[#] Oberste Karte ist: "+dealer.getNameOfCard(dealer.showTopCard()));
			}
			
			showHandCards();
			int selectedCard;
			try {
				eingabe = new Scanner(System.in);
				selectedCard = eingabe.nextInt();
				if(dealer.newTurn(hand[selectedCard],this)) {
					hand = deleteCardFromHand(hand,hand[selectedCard]);
				}else {
					System.out.println("[#] Dieser Zug ist nicht erlaubt! Ziehe eine Karte...");
					int card = dealer.drawCard();
					hand = incArray(hand, card);
				}
				
				if(dealer.checkStatus() == 1 && !knaveIsOnTop) {
					System.out.println("[!] Welche Farbe möchtest du dir wünschen?");
					System.out.println("0.) Pik");
					System.out.println("1.) Karo");
					System.out.println("2.) Herz");
					System.out.println("3.) Kreuz");
					eingabe = new Scanner(System.in);
					try {
						int selectedColor = eingabe.nextInt();
						if(selectedColor > -1 && selectedColor < 4) {
							dealer.setWishedColor(selectedColor);
						}else {
							System.out.println("[!] Fehlerhafte Eingabe! Pik wird gewählt!");
							dealer.setWishedColor(0);
						}
					}catch(Exception e) {
						System.out.println("[!] Fehlerhafte Eingabe! Pik wird gewählt!");
						dealer.setWishedColor(0);
					}
				}
				
			}catch(Exception e) {
				System.out.println("[#] Ziehe eine Karte...");
				int card = dealer.drawCard();
				hand = incArray(hand, card);
			}
		}else {
			skipPlayer = false;
		}
	}
	
	private void botsTurn() {
		if(dealer.checkStatus() == 1) {
			knaveIsOnTop = true;
		}else {
			knaveIsOnTop = false;
		}
		
		if(drawTwoCards) {
			int[] temp = dealer.drawTwoCards();
			hand = incArray(hand,temp[0]);
			hand = incArray(hand,temp[1]);
			drawTwoCards = false;
		}
		
		if(!skipPlayer) {
			if(knaveIsOnTop && dealer.getStringOfWishedColor() != null) {
				System.out.println("[!] Gewünschte Farbe ist: "+dealer.getStringOfWishedColor());
			}
			System.out.println("[#] Ich habe noch "+hand.length+" Karten übrig");
			BotEngine bot;
			if(knaveIsOnTop && dealer.getStringOfWishedColor() != null) {
				bot = new BotEngine(hand,dealer.showTopCard(),dealer.getWishedColor());
			}else {
				bot = new BotEngine(hand,dealer.showTopCard(),-1);
			}

			int selectedCard = bot.calcNextCard();
			
			if(selectedCard == -1) {
				int card = dealer.drawCard();
				hand = incArray(hand, card);
				System.out.println("[#] Ich habe eine Karte gezogen!");
			}else {	
				if(dealer.newTurn(hand[selectedCard],this)) {
					hand = deleteCardFromHand(hand,hand[selectedCard]);
				}else {
					int card = dealer.drawCard();
					hand = incArray(hand, card);
				}
				
				if(dealer.checkStatus() == 1 && !knaveIsOnTop) {
					int selectedColor = bot.calcWishedColor();
					if(selectedColor > -1 && selectedColor < 4) {
						dealer.setWishedColor(selectedColor);
					}else {
						dealer.setWishedColor(0);
					}
				}
				System.out.println("[#] Ich habe "+dealer.getNameOfCard(dealer.showTopCard())+" gelegt!");
			}
		}else {
			skipPlayer = false;
		}
	}
	
	private int[] incArray(int[] arr,int card) {
	    int[] temp = new int[arr.length+1];
	    	
	    for(int i=0;i<arr.length;i++) {
	    	temp[i] = arr[i];
	    }
	    	
	    temp[temp.length-1] = card;
	    return temp;
	}
	 
	private int[] deleteCardFromHand(int[] arr, int card) {
		int[] temp = new int[arr.length-1];
		int k = 0;
		for(int i = 0;i<arr.length;i++) {
			if(arr[i] != card) {
				temp[k] = arr[i];
				k++;
			}
		}
		return temp;
	}
	
}
