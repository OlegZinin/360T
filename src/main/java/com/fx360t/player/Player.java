package com.fx360t.player;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.fx360t.service.PlayGround;
import com.fx360t.strategy.GameStrategy;
/**
 * Player interface represents a player which can be registered on {@link PlayGround} and 
 * uses {@link GameStrategy} to play.<br>
 * It extends {@link Remote} interface to be available as a remote service when playground runs on other JVM.<br>
 * Its methods are used by a playground for example to inform a player that the game is started or
 * ended.Playground decides which of players should start the game.<br>
 * Game process for the player consists of following steps:<br>
 * 1. {@link #prepare()} <br>
 * 2. {@link #startToPlay}<br>
 * 3. {@link #gameOver()}<br>
 * @author Oleg
 */
public interface Player extends Remote {
	/**
	 * Id of this player. May be a name or something else that uniquely identifies this player.<br>
	 * Must not be null
	 * @return id of this player
	 * @throws RemoteException
	 */
	String getIdentity() throws RemoteException;
	/**
	 * Name of this player
	 * @return name of the player
	 */
	String getName() throws RemoteException;
	/**
	 * Defines initial preparation for playing
	 * @throws RemoteException
	 */
	void prepare() throws RemoteException;
	
	/**
	 * Wait for all player turns are completed.<br>
	 * Method blocks program execution until all turns are completed
	 * @throws RemoteException
	 */
	void waitAllTurnsCompleted() throws RemoteException;;
	/**
	 * Start to play with another player.<br>
	 * Method is called by a playground.
	 * @param initiator - {@code true} if this player is considered as initiator by playground
	 * @param other - other player to play with
	 * @throws RemoteException
	 */
	void startToPlay(boolean initiator, Player other)throws RemoteException;
	
	/**
	 * Defines a final step when the game is over.<br>
	 * Invoked by a playground
	 * @throws RemoteException
	 */
	void gameOver() throws RemoteException;

}