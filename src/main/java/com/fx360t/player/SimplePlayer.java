package com.fx360t.player;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.fx360t.service.MessageService;
import com.fx360t.service.PlayerRegistrator;
import com.fx360t.strategy.GameStrategy;
import com.fx360t.strategy.Message;
import com.fx360t.strategy.StringMessageStrategy;

/**
 * Implementation of {@link Player} interface.<br>
 * SimplePlayer uses provided {@link GameStrategy} and {@link PlayerRegistrator}
 * to perform all its actions.<Br>
 * In the preparing phase player registers on the playground and triggers
 * {@link GameStrategy#ready} method  <br>
 * The main play phase (strategy turn loop) is started in {@code gameRunner} thread when playground calls
 * {@link #startToPlay} method where the first boolean parameter indicates if player is considered as initiator <br>
 * When all turns of initiator game is completed, i.e. when {@link #waitAllTurnsCompleted()} methods returns, 
 * playground will trigger {@link #gameOver()} method of this player.
 * @author Oleg
 */
public class SimplePlayer implements Runnable, Serializable, Player {

	private static final long serialVersionUID = 1L;
	
	
	
	private String name;
	/**
	 * Strategy of game used by player
	 */
	private transient GameStrategy<Message<String>> strategy;
	/**
	 * Playground where game process is managed and where this player registers
	 */
	private transient PlayerRegistrator playGround;
	
	/**
	 * Single thread executor service to provide a thread for running strategy turns
	 */
	private transient ExecutorService gameRunner = Executors.newSingleThreadExecutor();

	/**
	 * Single thread executor service to provide a thread in which final operations will be
	 * performed when the game is over.<br>
	 *  It is only used when dealing with RMI calls to shutdown program without generating {@link RemoteException}  
	 */
	private transient ExecutorService finalizer = Executors.newSingleThreadExecutor();
	private transient FinalizeTask finalizeTask = new FinalizeTask();
	
	/**
	 * Semaphore used in RMI mode in finalize task. <br>
	 * Serves as a signal to start finalizing.
	 */
	private transient Semaphore timeToFinalize = new Semaphore(1);
	
	/**
	 * Semaphore used for indicating the end of the game and returning from {@link #waitAllTurnsCompleted()} method 
	 */
	private transient Semaphore timeToGameOver = new Semaphore(1);
	/**
	 * Self reference to hold a player.<br>
	 * In case of RMI it holds a player stub which is passed as an argument to {@link PlayerRegistrator#registerPlayer} method.
	 * This stub then used by playground to perform RMI calls on this player.
	 */
	private Player selfRef;
	/**
	 * boolean flag indicates if this player uses remote playground and is itself a remote player
	 */
	private boolean remote;
	/**
	 * Message to start a game with
	 */
	private String startMessage;
	
	@Override
	public String getIdentity() {
		RuntimeMXBean mBean = ManagementFactory.getRuntimeMXBean();
		return name+"/"+mBean.getName();
	}

	@Override
	public String getName() throws RemoteException {
		return this.name;
	}
	/**
	 * Constructs a player for playing on local playground on the same JVM 
	 * @param name - the player's name
	 * @param strategy - the player's game strategy
	 * @param playGround - local playground to play on
	 */
	public SimplePlayer(String name, String startMessage, GameStrategy<Message<String>> strategy, PlayerRegistrator playGround) {
		this(name, startMessage,strategy, playGround, false);
	}
	
	/**
	 * Constructs a player for playing on local or remote playground depending on passed parameter.
	 * @param name - the player's name
	 * @param strategy - the player's game strategy
	 * @param playGround - playground to play on
	 * @param remote - flag indicating if player and playground runs on different JVMs.
	 */
	public SimplePlayer(String name, String startMessage,GameStrategy<Message<String>> strategy, PlayerRegistrator playGround,
			boolean remote) {
		this.name = name;
		this.strategy = strategy;
		this.playGround = playGround;
		this.remote = remote;
		this.startMessage = startMessage;
	}
	/**
	 * Implements operations of ending this player process. <br>
	 * Used to finish application in RMI mode 
	 * 
	 */
	private class FinalizeTask implements Runnable{
		public void run(){
			try {
				// wait until it is possible to finalize process
				timeToFinalize.acquire();
				UnicastRemoteObject.unexportObject(SimplePlayer.this, true);
				System.exit(0);
			} catch (InterruptedException | NoSuchObjectException e) {
				;
			}
		}
	}
	public void prepare() throws RemoteException {
		if (remote) {
			try {
				//acquire lock which will be then waited on from FinalizeTask
				timeToFinalize.acquire();
			} catch (InterruptedException e) {
				throw new RemoteException(e.getMessage(), e);
			}
			//exporting this player to be available from playground
			Player stub = (Player) UnicastRemoteObject.exportObject(this, 0);
			selfRef = stub;
			//executing finalize task
			finalizer.execute( finalizeTask);
		} else
			selfRef = this;
		
		this.strategy.ready(selfRef);
		this.playGround.registerPlayer(selfRef);
		
	}

	/*
	 * Main strategy turn loop 
	 */
	@Override
	public void run() {
		//Playing until stop condition is met
		while (!strategy.stopCondition()) {
			strategy.play(selfRef);
		}
		//release the lock, signal to waitAllTurnsCompleted
		timeToGameOver.release();
	}

	@Override
	public void waitAllTurnsCompleted() {
		try {
			// waiting when semaphore will be released
			timeToGameOver.acquire();
		} catch (InterruptedException e) {
			System.out.println("Interrupted while wiating all turns completed");
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public void startToPlay(boolean initiator, Player other) throws RemoteException {
		try {
			//lock semaphore, now it will be waited in waitAllTurnsCompleted
			timeToGameOver.acquire();
		} catch (InterruptedException e) {
			throw new RemoteException(e.getMessage(), e);
		}
		//execute main strategy loop
		gameRunner.execute(this);
		if (initiator) {
			System.out.println(name + " starts messaging with " + other.getName());
			//start the process, send a message to other player
			Message<String> message = new Message<>(startMessage, name, other.getIdentity());
			strategy.start(selfRef, message);
		}
		
	}

	@Override
	public void gameOver() {
		strategy.finish(selfRef);
		//shutdown main loop
		gameRunner.shutdown();
		System.out.println(name+" finished the game");
		if (remote) {
			// signal to finalize task
			timeToFinalize.release();
		}
	}

	/**
	 * Main method used to run Player in its own JVM.<br>
	 * Parameters define user name, starting message  and optionally RMI registry address (i.e. 'localhost:1099'). 
	 * @param args
	 */
	public static void main(String[] args) {
		String rmiAddress = (args.length > 2 ? args[2] : "localhost:1099");
		PlayerRegistrator playGround = null;
		try {
			System.out.println("Trying to connect to PlayGround at " + rmiAddress);
			playGround = (PlayerRegistrator) Naming
					.lookup("rmi://" + rmiAddress + "/" + PlayerRegistrator.SERVICE_NAME);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println(
					"Can't connect to a playground due to exception: " + e.getMessage() + "\n. Program will exit now");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Found PlayGround at " + rmiAddress);
		MessageService<String> messageService = null;
		try {
			System.out.println("Trying to connect to MessageService at " + rmiAddress);
			messageService = (MessageService<String>) Naming
					.lookup("rmi://" + rmiAddress + "/" + MessageService.SERVICE_NAME);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.out.println("Can't connect to a MessageService due to exception: " + e.getMessage()
					+ "\n. Program will exit now");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Found MessageService at" + rmiAddress);
		String name = args.length > 0 ? args[0] : "Player_NoName";
		String message = args.length>1? args[1] : "Hi there";
		Player player1 = new SimplePlayer(name, message,
				new StringMessageStrategy(messageService), playGround, true);
		try {
			player1.prepare();
		} catch (RemoteException e) {
			System.out.println(
					"Can't prepare player for game due to exception: " + e.getMessage() + "\n. Program will exit now");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println(name + " is ready to play...");
	}

	
}
