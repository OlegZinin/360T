package com.fx360t.strategy;

import com.fx360t.player.Player;
/**
 * Interface represents several steps of game process for the players.<br>
 * Typical game process for one player would be as follows: <br>
 *  1. {@link #ready(Player)} <br>
 *  2. {@link #start(Object)} <br>
 *  3. {@link #play(Player)} until {@link #stopCondition()} is {@code true}<br>
 *  4. {@link #finish(Player)}
 * @author Oleg
 * @param <T> - the type of data used in players interactions
 */
public interface GameStrategy<T> {
	/**
	 * Returns {@code true} when the game process should be stopped
	 * @return {@code true} when the game process should be stopped
	 */
	boolean stopCondition();
	/**
	 * Defines a step when {@link Player} is prepared and ready for playing
	 * @param player - a player who is ready for the game
	 * @return {@code true} if preparation was successful 
	 */
	boolean ready(Player player);
	/**
	 * Defines a starting point of the game process
	 * @param data - some useful data which the game process depends on. 
	 * @param player - player who started the game process
	 * @return {@code true} if start was successful
	 */
	boolean start(Player player, T data);
	
	/**
	 * Defines one turn for the player
	 * @param player - the player who makes this turn
	 * @return {@code true} if play turn was successfull
	 */
	boolean play(Player player);
	
	/**
	 * Defines a finishing point of the game process for the player
	 * @param player
	 * @return {@code true} if finish was successful
	 */
	boolean finish(Player player);
}
