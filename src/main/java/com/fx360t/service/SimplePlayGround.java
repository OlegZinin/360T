package com.fx360t.service;

import java.rmi.RemoteException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.fx360t.player.Player;
/**
 * Simple implementation of {@link PlayGround} interface. <br>
 * This implementation could be used as a PlayGround for only two players.<br>
 * Also it can be exported as a remote service to be used by players on other JVMs.
 * For this purpose it should be exported as a {@link PlayerRegistrator}. <br>
 * The first registered player is considered as initiator and will be triggered to start playing by invoking
 * {@link Player#startToPlay} method when the second player is also ready to play. 
 * 
 * @author Oleg
 */
public class SimplePlayGround implements PlayGround, PlayerRegistrator {
	
	private Player firstPlayer;
	private Player secondPlayer;
	/**
	 * Number of registered players
	 */
	private int registered = 0;
	/**
	 * boolean flag indicating if the game is running
	 */
	private volatile boolean gameIsRunning;
	/**
	 * Latch used for waiting all 2 players are registered
	 */
	private transient CountDownLatch allRegistered = new CountDownLatch(2);
	/**
	 * Lock to be used in register and unregister methods
	 */
	private transient Lock registeringLock = new ReentrantLock();
	@Override
	public boolean startPlaying() {
		try {
			if(playersCount()<2){
				System.out.println("Can't start playing as not all players registered yet");
				return false;
			}
			if(gameIsRunning){
				System.out.println("Can't start playing as the game is already started");
				return false;
			}
			System.out.println("Start to play");
			firstPlayer.startToPlay(true, secondPlayer);
			secondPlayer.startToPlay(false, firstPlayer);
			gameIsRunning = true;
		} catch (RemoteException e) {
			System.out.println("Unable to start playing due to Exception: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public void registerPlayer(Player player) throws RemoteException {
		registeringLock.lock();
		try {
			if (player == null) {
				System.out.println("Can't register NULL player.");
				return;
			}
			if (gameIsRunning) {
				System.out.println("Can't register new player as the game has already started.");
				return;
			}
			if (firstPlayer != null && secondPlayer != null) {
				System.out.println("Failed to register new player. Already have two registered players");
				return;
			}
			if (firstPlayer == null) {
				firstPlayer = player;
				registered++;
				System.out.println(firstPlayer.getName() + " registered in game. Wait for one more player");
				allRegistered.countDown();
			} else if (secondPlayer == null) {
				if (player.getIdentity().equals(firstPlayer.getIdentity())) {
					System.out.println("Failed to register new player. This player is already registered");
					return;
				}
				secondPlayer = player;
				System.out.println(secondPlayer.getName() + " registered in game. ");
				registered++;
				allRegistered.countDown();
			}
		} finally {
			registeringLock.unlock();
		}
	}

	@Override
	public int playersCount() {
		return registered;
	}

	@Override
	public boolean waitUntilGameIsFinished() {
		try {
			if(!gameIsRunning){
				System.out.println("Can't start waiting for finish as the game is not started yet");
				return false;
			}

			firstPlayer.waitAllTurnsCompleted();
			System.out.println(firstPlayer.getName() + " ready to finish");
			return true;
		} catch (RemoteException e) {
			System.out.println("Failed to wait finishing the game due to exception :" + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean finishPlaying() {
		if(!gameIsRunning){
			System.out.println("Can't finish playing as the game is not started yet");
			return false;
		}
			
		gameIsRunning = false;
		try {
			firstPlayer.gameOver();
			System.out.println("FirstPlayer ends his game");
			secondPlayer.gameOver();
			System.out.println("SecondPlayer ends his game");
			return true;
		} catch (RemoteException e) {
			System.out.println("Abnormal finishing the game due to exception : "+ e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void waitAllPlayersRegistered() {
		if (playersCount() < 2) {
			try {
				allRegistered.await();
				System.out.println("All players registered");
			} catch (InterruptedException e) {
				System.out.println("Interrupted while waiting all registered players :" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void unregisterPlayer(Player player) throws RemoteException {
		registeringLock.lock();
		try {
			if (player == null) {
				System.out.println("Can't unregister NULL player.");
				return;
			}
			if (registered == 0) {
				System.out.println("Can't unregister player as no players were registered yet");
				return;
			}
			if (gameIsRunning) {
				System.out.println("Can't unregister player as the game has already started.");
				return;
			}

			if (firstPlayer != null && firstPlayer.getIdentity().equals(player.getIdentity())) {
				firstPlayer = null;
				registered--;
				return;
			}
			if (secondPlayer != null && secondPlayer.getIdentity().equals(player.getIdentity())) {
				secondPlayer = null;
				registered--;
			}
		} finally {
			registeringLock.unlock();
		}
	}
}
