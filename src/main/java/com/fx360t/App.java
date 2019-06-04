package com.fx360t;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import com.fx360t.player.Player;
import com.fx360t.player.SimplePlayer;
import com.fx360t.service.MessageService;
import com.fx360t.service.PlayerRegistrator;
import com.fx360t.service.SimpleMessageService;
import com.fx360t.service.SimplePlayGround;
import com.fx360t.strategy.StringMessageStrategy;
/**
 * Main class which starts playground, message service and two players if not in remote mode.<br>
 * Pass argument {@code -remote} to start in remote mode. In this case playground will be waiting until all 
 * remote players registered and then the game will be started.
 * @author Oleg
 */
public class App {

	public static void main(String[] args) throws RemoteException  {
		
		boolean isRemote = (args.length>0 && "-remote".equalsIgnoreCase(args[0]));
		
		MessageService<String> messageService = new SimpleMessageService();
		SimplePlayGround playGround = new SimplePlayGround();
		if (isRemote) {
			int registryPort = (args.length>1 && args[1].matches("\\d+") ? Integer.parseInt(args[1]):1099);
			try {
				initializeRMI(messageService, playGround,registryPort);
			} catch (RemoteException | AlreadyBoundException | UnknownHostException e) {
				System.out.println("Unable to initialize RMI: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			Player player1 = new SimplePlayer("John","Hi there",
								new StringMessageStrategy(messageService), playGround);
			Player player2 = new SimplePlayer("Bob","Hi there", new StringMessageStrategy(messageService), playGround);
			try {
				player1.prepare();
				player2.prepare();		
			} catch (RemoteException e) {
				//never thrown in this mode
			}
			
		}
		
		playGround.waitAllPlayersRegistered();
		
		playGround.startPlaying();
		
		playGround.waitUntilGameIsFinished();
		
		playGround.finishPlaying();
		
		System.exit(0);
	}

	private static void initializeRMI(MessageService<String> messageService, PlayerRegistrator playGround , int registryPort)
			throws RemoteException, AlreadyBoundException, UnknownHostException {
		System.out.println("Initializing RMI on " + InetAddress.getLocalHost()+":"+registryPort);
		Registry registry = LocateRegistry.createRegistry(registryPort);
		MessageService<String> msStub = (MessageService<String>) UnicastRemoteObject.exportObject(messageService,0);
		registry.bind("MessageService", msStub);
		System.out.println("MessageService is ready..");
		PlayerRegistrator rem = playGround;
		PlayerRegistrator pgStub = (PlayerRegistrator) UnicastRemoteObject.exportObject(rem, 0);
		registry.bind("PlayGround", pgStub);
		System.out.println("PlayGround is ready..");
		System.out.println("Waiting for players to connect...");
	}

}
