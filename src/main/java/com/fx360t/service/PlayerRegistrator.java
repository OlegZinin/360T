package com.fx360t.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.fx360t.player.Player;

/**
 * PlayerRegistrator interface provides methods for registering and unregistering {@link Player}.<br>
 * It extends {@link Remote} interface to be used as a remote service when running {@link Player} instances 
 * on different JVM 
 * @author Oleg
 */
public interface PlayerRegistrator extends Remote{
	/**Service name to be used when exporting service*/
	String SERVICE_NAME="PlayGround";
	/**Registers a new {@link Player} 
	 * @param player - Player to be registered 
	 */
	void registerPlayer(Player player) throws RemoteException;
	/**Unregisters player
	 * @param player - player to be unregistered
	 */
	void unregisterPlayer(Player player) throws RemoteException;
}
