package de.maumau.networking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import de.maumau.engine.Player;

/**
 * 
 * @author Barny
 */

public class Network {
	private int noOfPlayers;
	private int port;
	private ServerSocket server;
	private Socket[] conn;
	private BufferedReader[] in;
	private BufferedWriter[] out;
	private String[] playerNames;
	private boolean contest;
	private List<Integer> offlinePlayers;
	
	public Network(int noOfPlayers, int port,boolean contest) {
		this.noOfPlayers = noOfPlayers;
		this.port = port;
		this.contest = contest;
		this.offlinePlayers = new ArrayList<Integer>();
		while(true) {
			if(waitForPlayers()) {
				break;
			}
		}
	}
	
	public boolean broadcastMessage(String msg) {
		boolean addedConnectionToOfflineList = false;
		for(int i=0;i<conn.length;i++) {
			if(!offlinePlayers.contains(i)) {
				try {
					out[i].write(msg);
					out[i].newLine();
					out[i].flush();
				}catch(IOException e) {
					if(!retryConnectionWithLastMessage(msg,i)) {
						addToOffinePlayers(i);
						addedConnectionToOfflineList = true;
						System.out.println("[!] Spieler "+i+" ist offline und wird ab jetzt übersprungen!");
					}
				}
			}
		}
		return !addedConnectionToOfflineList;
	}
	
	public List<Integer> getOfflineList() {
		return offlinePlayers;
	}
	
	public void broadcastMessage(String msg, int senderId) {
		for(int i=0;i<conn.length;i++) {
			if(i != senderId) {
				try {
					out[i].write(msg);
					out[i].newLine();
					out[i].flush();
				}catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	} 
	
	public boolean sendToPlayer(String msg,int receiverID) {
		if(receiverID < conn.length && receiverID > -1) {
			try {
				out[receiverID].write(msg);
				out[receiverID].newLine();
				out[receiverID].flush();
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	public boolean retryConnectionWithLastMessage(String msg,int receiverID) {
		boolean isOffline = true;
		int retryCounter = 0;
		if(receiverID < conn.length && receiverID > -1) {
			boolean msgSent = false;
			while(isOffline && retryCounter < 3) {
				try {
					out[receiverID].write(msg);
					out[receiverID].newLine();
					out[receiverID].flush();
					msgSent = true;
				} catch (IOException e) {
					retryCounter++;
					msgSent = false;
				}
				if(msgSent) {
					isOffline = false;
				}
			}
			return !isOffline;
		}
		return false;
	}
	
	public Player[] getPlayers() {
		Player[] player = new Player[noOfPlayers];
		
		for(int i=0;i<conn.length;i++) {
			player[i] = new Player(playerNames[i],i,this);
		}
		
		return player;
	}
	
	public int readLineFromPlayer(int networkId) {
		int temp = -1;
		try {
			temp = Integer.valueOf(in[networkId].readLine());
		} catch(Exception e) {
			return -1;
		}
		return temp;
	}
	
	public String readLineFromBot(int networkId) {
		String temp = "";
		try {
			temp = in[networkId].readLine();
		} catch(Exception e) {
			return "{\"status\" : \"skip\",\"selectedCard\" : 1,\"wishedColor\" : -1,\"skipped\" : False}";
		}
		return temp;
	}
	
	public void interruptConnections() {
		if(conn == null || in == null || out == null) {
			System.out.println("[!] Server konnte nicht neugestartet werden! Beende...");
			System.exit(0);
		}
		for(int i=0;i<conn.length;i++) {
			try {
				in[i].close();
				out[i].close();
				conn[i].close();
			}catch(IOException e) {
				System.out.println("[!] Spieler "+i+" bereits getrennt!");
			}
		}
		try {
			server.close();
		} catch (IOException e) {
			System.out.println("[!] Server konnte nicht beendet werden!");
		}
	}
	
	private void addToOffinePlayers(int senderId) {
		boolean alreadyInList = false;
		
		for(int i=0;i<offlinePlayers.size();i++) {
			if(offlinePlayers.get(i) == senderId) {
				alreadyInList = true;
				break;
			}
		}
		
		if(!alreadyInList) {
			offlinePlayers.add(senderId);
		}
	}
	
	private boolean waitForPlayers() {
		boolean waitForPlayers = true;
		try {
			server = new ServerSocket(port);
			conn = new Socket[noOfPlayers];
			in = new BufferedReader[noOfPlayers];
			out = new BufferedWriter[noOfPlayers];
			playerNames = new String[noOfPlayers];
			System.out.println("[#] Server hat alles initialisiert!");
			System.out.println("[#] Warte auf Verbindungen...");
			int i = 0;
			do {
				boolean playerConnected = true;
				conn[i] = server.accept();
				System.out.println("[#] Ein Spieler hat sich verbunden!");
				in[i] = new BufferedReader(new InputStreamReader(conn[i].getInputStream()));
				out[i] = new BufferedWriter(new OutputStreamWriter(conn[i].getOutputStream()));
				
				if(!contest) {
					out[i].write("Bitte gib deinen Namen ein:");
					out[i].newLine();
					out[i].flush();
					playerNames[i] = in[i].readLine();
					out[i].write("Das Spiel startet in Kürze!");
					out[i].newLine();
					out[i].flush();
				}else {
					JSONObject toSend = new JSONObject();
					toSend.put("status","okay");
					toSend.put("sendName", true);
					out[i].write(toSend.toJSONString());
					out[i].newLine();
					out[i].flush();
					String JSON_ResponseLine;
					JSONParser parser = new JSONParser();
					try {
						JSON_ResponseLine = in[i].readLine();
						Object obj = parser.parse(JSON_ResponseLine);
						JSONObject JSON_Response = (JSONObject) obj;
						
						String status = (String) JSON_Response.get("status");
						if(status.equals("okay")) {
							playerNames[i] = (String) JSON_Response.get("name");
						}
						
						JSONObject nameResponse = new JSONObject();
						
						nameResponse.put("status","okay");
						nameResponse.put("name",playerNames[i]);
						nameResponse.put("setName",true);
						
						out[i].write(nameResponse.toJSONString());
						out[i].newLine();
						out[i].flush();
						
					}catch(ParseException e) {
						playerNames[i] = "Bot "+i;
						
						JSONObject nameResponse = new JSONObject();
						
						nameResponse.put("status","error");
						nameResponse.put("name","Bot "+i);
						nameResponse.put("setName",false);
						
						out[i].write(nameResponse.toJSONString());
						out[i].newLine();
						out[i].flush();
					}catch(NullPointerException e1) {
						System.out.println("[!] Spieler hat während Authentifizierung die Verbindung geschlossen!");
						playerConnected = false;
					}catch(IOException e1) {
						System.out.println("[!] Spieler hat während Authentifizierung die Verbindung geschlossen!");
						playerConnected = false;
					}
					
				}
				if(playerConnected) {
					i++;
					waitForPlayers = i < noOfPlayers;
				}
			}while(waitForPlayers);
			
		}catch(IOException e) {
			System.out.println("[!] Unerwarteter Fehler! Läuft bereits eine Instanz des Servers?");
			System.out.println("[#] Server wird neu gestartet");
			this.offlinePlayers = new ArrayList<Integer>();
			interruptConnections();
			return false;
		}
		
		if(contest) {
			JSONObject gameStart = new JSONObject();
			gameStart.put("status","okay");
			gameStart.put("gameStart",true);
			broadcastMessage(gameStart.toJSONString());	
		}else {
			broadcastMessage("Willkommen! Das spielt beginnt JETZT!");	
		}
		return true;
	}
	
}
