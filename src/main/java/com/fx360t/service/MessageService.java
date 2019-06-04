package com.fx360t.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.fx360t.strategy.Message;

/**
 * Interface which provides simple messaging between several users.<br>
 * It extends {@link Remote} which allows it to be used as a remote service when running users on other JVMs.
 * @author Oleg
 * @param <T> - type of data to be used as a message
 */
public interface MessageService <T> extends Remote {
	/**Service name for this remote service*/
	String SERVICE_NAME ="MessageService";
	/**
	 * Register user names on this MessageService, so that they will be able to interact with each other
	 * @param userNames - an array of names to register
	 * @throws RemoteException
	 */
	void register(String... userNames) throws RemoteException;
	/**
	 * Send a message from sender to receiver.
	 * @param message - message to be sent
	 * @param senderName - sender of the message
	 * @param receiverName - receiver of the message
	 * @throws RemoteException
	 */
	void sendMessage(T message, String senderName, String receiverName) throws RemoteException;
	
	/**
	 * Unregister players on this Message service
	 * @param userNames - an array of user names to be unregistered
	 * @throws RemoteException
	 */
	void unregister(String... userNames) throws RemoteException;
	
	/**
	 * Wait for a new message for user.<br>
	 * Method blocks execution until new message is received for the provided recipient.<br>
	 * Returns a {@link Message} instance with information of sender, receiver and message body.
	 * @param recipientName - a user's name for whom message is requested
	 * @return {@link Message} instance with information of sender, receiver and message body or {@code null}
	 * if recipient is not registered on this message service
	 * @throws RemoteException
	 */
	Message<T> getNextMessage(String recipientName) throws RemoteException;
}
