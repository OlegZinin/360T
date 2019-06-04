package com.fx360t.service;

import java.rmi.RemoteException;

/**
 * A PlayGround interface which provides methods for implementing a game process<br>
 * Typical game process would be represented as follows:<br>
 * 1. {@link #waitAllPlayersRegistered()} <br> 
 * 2. {@link #startPlaying()}<br>
 * 3. {@link #waitUntilGameIsFinished()} <br>
 * 4. {@link #finishPlaying()} <br>
 * @author Oleg
 */
public interface PlayGround {
	/**
	 * Get number of currently registered players 
	 * @return the number of registered players
	 */
	int playersCount();
	/**
	 * Starts a new game process
	 * @return {@code true} if game process started
	 */
	boolean startPlaying();  
    /**
     * Blocks execution until some stop-condition happens indicating that the game is over
     * @return {@code true} if waiting successfully completed 
     */
    boolean waitUntilGameIsFinished();
    /**
     * Performs some operations that should happen after the game is over.<br>
     * For example, notifying all registered players about that.
     * @return {@code true} if successfully finish playing
     */
    boolean finishPlaying();
    /**
     * Blocks execution until all necessary players are registered
     */
	void waitAllPlayersRegistered();
}
