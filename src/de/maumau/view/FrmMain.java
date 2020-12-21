package de.maumau.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import de.maumau.engine.Dealer;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.awt.Font;

public class FrmMain extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel gbStatus, gpOptions;
	private JLabel lblDescStatus, lblStatus,lblNoOfPlayer, lblPort;
	private JRadioButton rdbtnFreehackContest, rdbtnNetworkgame;
	private JButton btnClose, btnStart;
	private Thread game;
	private JTextField edPort;
	private JSpinner spNoOfPlayer;
	private int noOfPlayer, port;

	/**
	 * Create the frame.
	 */
	public FrmMain() {
		setResizable(false);
		setTitle("Free-Hack Coding-Contest 2017 ");
		setUpForm();
		setUpActionListeners();
	}
	
	private void performStart() {
		noOfPlayer = (int)spNoOfPlayer.getValue();
		port = 0;
		if(edPort.getText().length() > 0 && isNumber(edPort.getText())) {
			if(Integer.valueOf(edPort.getText()) > 1) {
				port = Integer.valueOf(edPort.getText());
			}
		}
		
		if(port != 0) {
			btnStart.setText("Unterbrechen");
			lblStatus.setForeground(Color.GREEN);
			if(rdbtnFreehackContest.isSelected()) {
				lblStatus.setText("Free-Hack Contest aktiv (Port: "+port+","+noOfPlayer+" Spieler)");
				game = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Dealer dealer = new Dealer(noOfPlayer,port,true);
						dealer.startNewGame();
					}
				});
			}else {
				lblStatus.setText("Netzwerkspiel aktiv (Port: "+port+","+noOfPlayer+" Spieler)");
				game = new Thread(new Runnable() {
					
					@Override
					public void run() {
						Dealer dealer = new Dealer(noOfPlayer,port,false);
						dealer.startNewGame();
					}
				});
			}
			game.start();
			
			rdbtnFreehackContest.setEnabled(false);
			rdbtnNetworkgame.setEnabled(false);
			spNoOfPlayer.setEnabled(false);
			edPort.setEnabled(false);
		}
	}
	
	private void breakAction() {
			btnStart.setText("Starten");
			lblStatus.setForeground(Color.red);
			lblStatus.setText("Nicht aktiv");
			
			rdbtnFreehackContest.setEnabled(true);
			rdbtnNetworkgame.setEnabled(true);
			spNoOfPlayer.setEnabled(true);
			edPort.setEnabled(true);
			System.exit(0);
	}
	
	private void setUpActionListeners() {
		btnClose.addActionListener(this);
		btnStart.addActionListener(this);
		rdbtnFreehackContest.addActionListener(this);
		rdbtnNetworkgame.addActionListener(this);
	}
	
	private void setUpForm() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 457, 233);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		gbStatus = new JPanel();
		gbStatus.setBorder(new TitledBorder(null, "Status", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		gbStatus.setBounds(12, 12, 418, 59);
		contentPane.add(gbStatus);
		gbStatus.setLayout(null);
		
		lblDescStatus = new JLabel("Status:");
		lblDescStatus.setBounds(12, 24, 57, 15);
		gbStatus.add(lblDescStatus);
		
		lblStatus = new JLabel("Nicht aktiv");
		lblStatus.setBounds(70, 24, 330, 15);
		lblStatus.setForeground(Color.red);
		gbStatus.add(lblStatus);
		
		gpOptions = new JPanel();
		gpOptions.setBorder(new TitledBorder(null, "Optionen", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		gpOptions.setBounds(12, 75, 418, 81);
		contentPane.add(gpOptions);
		gpOptions.setLayout(null);
		
		rdbtnFreehackContest = new JRadioButton("Free-Hack Contest");
		rdbtnFreehackContest.setBounds(20, 22, 161, 23);
		rdbtnFreehackContest.setSelected(true);
		rdbtnFreehackContest.setMnemonic(KeyEvent.VK_F);
		gpOptions.add(rdbtnFreehackContest);
		
		rdbtnNetworkgame = new JRadioButton("Netzwerkspiel");
		rdbtnNetworkgame.setBounds(239, 22, 112, 23);
		rdbtnNetworkgame.setMnemonic(KeyEvent.VK_N);
		gpOptions.add(rdbtnNetworkgame);
		
		edPort = new JTextField();
		edPort.setText("1338");
		edPort.setBounds(92, 50, 56, 20);
		edPort.setHorizontalAlignment(JTextField.CENTER);
		gpOptions.add(edPort);
		edPort.setColumns(10);
		
		lblPort = new JLabel("Port:");
		lblPort.setBounds(60, 52, 33, 14);
		gpOptions.add(lblPort);
		
		lblNoOfPlayer = new JLabel("Anzahl Spieler:");
		lblNoOfPlayer.setBounds(198, 52, 88, 16);
		gpOptions.add(lblNoOfPlayer);
		
		spNoOfPlayer = new JSpinner();
		spNoOfPlayer.setModel(new SpinnerNumberModel(2, 2, 4, 1));
		spNoOfPlayer.setBounds(289, 50, 43, 20);
		gpOptions.add(spNoOfPlayer);
		
		btnClose = new JButton("Schließen");
		btnClose.setBounds(256, 160, 135, 25);
		btnClose.setMnemonic(KeyEvent.VK_C);
		contentPane.add(btnClose);
		
		btnStart = new JButton("Starten");
		btnStart.setBounds(55, 160, 135, 25);
		btnStart.setMnemonic(KeyEvent.VK_S);
		contentPane.add(btnStart);
		
		JLabel lblProgrammingByBarny = new JLabel("Programming by Barny for Free-Hack");
		lblProgrammingByBarny.setFont(new Font("Courier New", Font.PLAIN, 10));
		lblProgrammingByBarny.setBounds(247, 188, 204, 16);
		contentPane.add(lblProgrammingByBarny);
	}
	
	private boolean isNumber(String text) {
		try {
			@SuppressWarnings("unused")
			int number = Integer.valueOf(text);
		}catch(NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if(source == btnClose) {
			System.exit(0);
		}
		
		if(source == rdbtnFreehackContest) {
			rdbtnNetworkgame.setSelected(!rdbtnNetworkgame.isSelected());
		}
		
		if(source == rdbtnNetworkgame) {
			rdbtnFreehackContest.setSelected(!rdbtnFreehackContest.isSelected());
		}
		
		if(source == btnStart) {
			if(btnStart.getText().equals("Starten")) {
				performStart();
			}else {
				breakAction();
			}
		}
	}
}
