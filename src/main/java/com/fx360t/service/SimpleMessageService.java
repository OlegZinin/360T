package com.fx360t.service;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.fx360t.strategy.Message;

/**
 * Simple implementation of {@link MessageService} with String messages.<br>
 * Internally maintains a {@link Map} that holds a queue of messages for every
 * registered user
 * 
 * @author Oleg
 */
public class SimpleMessageService implements MessageService<String> {
	private Map<String, BlockingQueue<Message<String>>> userMessages = new ConcurrentHashMap<>();

	@Override
	public void register(String... users) {
		
		if (users != null)
			for (String user : users) {
				if (users != null)
					this.userMessages.putIfAbsent(user, new LinkedBlockingQueue<>());
			}
	}

	@Override
	public void sendMessage(String message, String senderName, String receiverName) {
		if (senderName == null || !userMessages.containsKey(senderName)) {
			System.out.println("User " + senderName + " is not registered");
			return;
		}
		if (receiverName == null || !userMessages.containsKey(receiverName)) {
			System.out.println("User " + receiverName + " is not registered");
			return;
		}
		if (senderName.equals(receiverName)) {
			System.out.println("Can't send message to yourself");
			return;
		}
		Message<String> mess = new Message<>(message, senderName, receiverName);
		try {
			userMessages.get(receiverName).put(mess);
		} catch (InterruptedException e) {
			System.out.println("Interrupted while sending a message to "+receiverName +": " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void unregister(String... users) {
		if (users != null)
			for (String user : users) {
				if (user != null)
					this.userMessages.remove(user);
			}
	}

	@Override
	public Message<String> getNextMessage(String recipient) {
		try {
			if(recipient ==null || !userMessages.containsKey(recipient)){
				System.out.println("User " +recipient+ " is not registered");
				return null;
			}
			Message<String> message = userMessages.get(recipient).take();
			
			return message;
		} catch (InterruptedException e) {
			System.out.println("Interrupted while getting a message for "+ recipient+ ": " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
