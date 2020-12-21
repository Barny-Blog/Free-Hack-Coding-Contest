package de.maumau.main;

import java.awt.EventQueue;

import de.maumau.engine.Dealer;
import de.maumau.engine.Player;
import de.maumau.view.FrmMain;

public class Main {
	
	static int[] playernames;
	static int noOfPlayers = 0;
	static Player[] players;
	static int port = 1338;
	
	
	public static void main(String[] args) {
		if(args.length == 0) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						FrmMain frame = new FrmMain();
					    frame.setLocationRelativeTo(null);
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}else {
			if(parseArgsForContest(args)) {
				System.out.println("[#] Starte Modus \"Free-Hack Contest\" (Port "+port+")");
				Dealer dealer = new Dealer(2,port,true);
				dealer.startNewGame();
			}else {
				if(parseArgsForNetworkGame(args)) {
					System.out.println("[#] Starte Modus \"Netzwerkspiel\" (Port "+port+")");
					Dealer dealer = new Dealer(noOfPlayers,port,false);
					dealer.startNewGame();
				}else {
					if(parseArgsForOfflineGame(args)) {
						if(players.length == 2) {
							System.out.println("[#] Starte Modus \"Offlinespiel\"");
							Dealer dealer = new Dealer(players[0],players[1]);
							dealer.startNewGame();	
						}else {
							if(players.length == 3) {
								System.out.println("[#] Starte Modus \"Offlinespiel\"");
								Dealer dealer = new Dealer(players[0],players[1],players[2]);
								dealer.startNewGame();	
							}
						}		
					}else {
						printHelp();
						System.exit(0);
					}
				}
			}
		}
	}
	
	private static boolean parseArgsForContest(String[] arguments) {
		boolean isContest = false;
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().equals("-FREEHACKCONTEST")) {
				isContest = true;
			}
		}
		port = 0;
		if(!isContest) {
			return false;
		}else {
			for(int i=0;i<arguments.length;i++) {
				if(arguments[i].toUpperCase().contains("-PORT=")) {
					String[] temp = arguments[i].split("=");
					try {
						port = Integer.valueOf(temp[1]);
					}catch(NumberFormatException e) {
						port = 1338;
					}
					return true;
				}
			}
			if(port == 0) {
				port = 1338;
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean parseArgsForNetworkGame(String[] arguments) {
		String[] temp = new String[2];
		boolean networkGame = false;
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().equals("-ISNETWORKGAME")) {
				networkGame = true;
			}
		}
		
		if(!networkGame) {
			return false;
		}
		noOfPlayers = 0;
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().contains("-PLAYERS=")) {
				temp = arguments[i].split("=");
				if(temp.length > 1) {
					try {
						noOfPlayers = Integer.valueOf(temp[1]);
					}catch(NumberFormatException e) {
						System.out.println("[!] \""+temp[1]+"\" ist keine Zahl! Bitte gültige Zahl eingeben!");
					}
				}else {
					System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
					return false;
				}
			}
		}
		if(noOfPlayers <=1) {
			System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
			return false;
		}
		port = 0;
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().contains("-PORT=")) {
				temp = arguments[i].split("=");
				if(temp.length > 1) {
					try {
						port = Integer.valueOf(temp[1]);
					}catch(NumberFormatException e) {
						port = 1338;
					}
				}else {
					System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
					return false;
				}
			}
		}
		
		if(port == 0) {
			port = 1338;
		}
		
		return noOfPlayers > 0;
	}
	
	private static boolean parseArgsForOfflineGame(String[] arguments) {
		String[] temp = new String[2];
		boolean botGame = false;
		int noOfPlayers = 0;
		
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().contains("-ISNETWORKGAME") || arguments[i].toUpperCase().contains("-FREEHACKCONTEST") ) {
				return false;
			}
		}
		
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().contains("-PLAYER=")) {
				noOfPlayers++;
			}
		}
		
		if(noOfPlayers <= 1) {
			System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
			return false;
		}
		
		players = new Player[noOfPlayers];
		
		for(int i=0;i<arguments.length;i++) {
			if(arguments[i].toUpperCase().equals("-ISBOTGAME")) {
				botGame = true;
			}
		}
		int k = 0;
		if(botGame) {
			for(int i=0;i<arguments.length;i++) {
				if(arguments[i].toUpperCase().contains("-PLAYER=")) {
					temp = arguments[i].split("=");
					if(temp.length > 1) {
						if(k == 0) {
							players[k] = new Player(temp[1],false); 
						}else {
							players[k] = new Player(temp[1],true);
						}
					}else {
						System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
						return false;
					}
					k++;
				}
			}
		}else {
			for(int i=0;i<arguments.length;i++) {
				if(arguments[i].toUpperCase().contains("-PLAYER=")) {
					temp = arguments[i].split("=");
					if(temp.length > 1) {
						players[k] = new Player(temp[1],false); 
						k++;	
					}else {
						System.out.println("[!] Nicht genügend oder fehlerhafte Parameter!");
						return false;
					}
				}
			}
			
			for(int i=0;i<players.length;i++) {
				if(players[i] == null) {
					return false;
				}
			}
			
		}
		
		return true;
	}
	
	private static void printHelp() {
		System.out.println("###########################################################################################################");
		System.out.println("#                            .:: MauMau Vers. 1.0.1 coded by Barny ::.                                    #");
		System.out.println("#   Usage: java -jar MauMau.jar {-players=X} {-isNetworkGame} {-player=} {-isBotGame}                     #");
		System.out.println("#   -players=X = No. of Players who will play a networkgame                                               #");
		System.out.println("#   -isNetworkGame = If is set the game will be a networkGame                                             #");
		System.out.println("#   -isBotGame = If is set, only player 1 is human                                                        #");
		System.out.println("#   -player = Name of player                                                                              #");
		System.out.println("#   -port=X = Port for networkgame (Default is 1338)                                                      #");
		System.out.println("#   -FreeHackContest = Start contest for 2 players                                                        #");
		System.out.println("#                                                                                                         #");
		System.out.println("#   Example networkgame on port 1338: java -jar MauMau.jar -players=2 -isNetworkGame -port=1338           #");
		System.out.println("#   Example offline game with bot: java -jar MauMau.jar -isBotGame -player=Playername1 -player=Playername2#");
		System.out.println("###########################################################################################################");
	}

}
