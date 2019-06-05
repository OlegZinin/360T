package com.fx360t.strategy;

import java.rmi.RemoteException;

import com.fx360t.player.Player;
import com.fx360t.service.MessageService;

/**
 * A {@link GameStrategy} implementation for the case of game data to be
 * {@link Message} of String.<br>
 * It uses three steps when playing a turn ({@link #play}):<br>
 * 1. Wait for a message for a {@link Player} <br>
 * 2. Prepare a reply message<br>
 * 3. Send reply to other {@link Player}<br>
 * When a {@link Player} calls {@link #start} method then this strategy
 * considered to be an "initiator" strategy and its stop-condition will depend
 * on the number of sent and received messages.<br>
 * This number is limited by {@code stopValue} value, which equals to ten.
 * 
 * @author Oleg
 */
public class StringMessageStrategy implements GameStrategy<Message<String>> {
	/**
	 * {@link MessageService} used in this strategy for sending and receiving
	 * string messages
	 */
	private MessageService<String> messageService;
	/**
	 * Counter for sent messages
	 */
	private int counterSent;
	/**
	 * Counter for received messages
	 */
	private int counterReceived;
	/**
	 * Limit of sent and received messages, used to determine a stop-condition
	 * if this strategy is "initiator"-strategy
	 */
	private int stopValue = 10;
	private boolean initiatorStrategy;
	/**
	 * Stop-condition flag
	 */
	private boolean stopCondition = false;
	
	/**
	 * ready to play flag
	 */
	private boolean isReady;
	/**
	 * playing flag
	 */
	private boolean isPlaying;
	
	public StringMessageStrategy(MessageService<String> messageService) {
		if(messageService == null)
			throw new NullPointerException("Message service can not be null");
		this.messageService = messageService;
	}

	public boolean stopCondition() {
		// if this is an initiatorStrategy then check the number
		// of sent and received messages
		if (initiatorStrategy)
			return counterSent == stopValue && counterReceived == stopValue;
		return stopCondition;
	}

	public boolean play(Player player) {
		if(!isReady){
			System.out.println("Can't play. Not ready");
			return false;
		}
		isPlaying= true;
		/*
		 * Three-step turn: 
		 * 1. Wait for a message 
		 * 2. Prepare reply 
		 * 3. Send prepared reply
		 */
		Message<String> message = waitForMessage(player);
		Message<String> reply = prepareReply(message);
		sendReply(reply);
		return true;
	}

	@Override
	public boolean start(Player player, Message<String> data) {
		if(data==null){
			System.out.println("Can't start with null data");
			return false;
		}
		if(!isReady){
			System.out.println("Strategy not ready. Can't start playing");
			return false;
		}
		if(isPlaying){
			System.out.println("Can't start playing as playing is already started");
			return false;
		}
		initiatorStrategy = true;
		try {
			messageService.sendMessage(data.getMessageBody(), 
					(player.getIdentity()),
					data.getReceiverName());
			counterSent++;
		} catch (RemoteException e) {
			handleRemoteException(e);
			return false;
		}
		return true;
	}

	@Override
	public boolean ready(Player player) {
		if(isReady){
			System.out.println("Already ready to play");
			return false;
		}
		if(isPlaying){
			System.out.println("Can't get ready. Already playing");
			return false;
		}
		counterReceived = 0;
		counterSent = 0;
		stopCondition = false;
		isPlaying=false;
		try {
			messageService.register(player.getIdentity());
			isReady= true;
			return true;
		} catch (RemoteException e) {
			handleRemoteException(e);
			return false;
		}
	}

	@Override
	public boolean finish(Player player) {
		if(!isReady || !isPlaying){
			System.out.println("Can't finish as playing not started yet");
			return false;
		}
		try {
			messageService.unregister(player.getIdentity());
			stopCondition = true;
			isPlaying=false;
			isReady= false;
			return true;
		} catch (RemoteException e) {
			handleRemoteException(e);
			return false;
		}
	}
	/**
	 * First step of one play turn - wait for a message. <br>
	 * It takes a {@link Player} as a parameter and waits until a message is received via Message Service. 
	 */
	
	private Message<String> waitForMessage(Player player){
		try {
			Message<String> mes = messageService.getNextMessage(player.getIdentity());
			counterReceived++;
			System.out.println(mes + ", totalSent = " + counterSent + ", totalReceived = " + counterReceived) ;
			return mes;
		} catch (RemoteException e) {
			handleRemoteException(e);
			return null;
		}
	}
	/**
	 * Second step in one play turn - prepare a reply message.<br>
	 * It takes incoming message as a parameter, reverts its sender and receiver and
	 * adds the value of {@code conterSent} to its body
	 */
	private Message<String> prepareReply(Message<String> message){
		if(message==null)
			return null;
		return new Message<String>
			    (message.getMessageBody() + counterSent,
			    		message.getReceiverName(),
			    		message.getSenderName());
	}
	/**
	 * Third step in one play turn - send prepared reply.
	 * It takes a prepared message and sends it to the receiver via messageService		
	 */
	private void sendReply(Message<String> reply){
		if(reply == null)
			return;
		//don't send message if this is initiator strategy and counterSent equals stopValue
		if (initiatorStrategy && counterSent == stopValue)
			return;
		try {
			messageService.sendMessage(reply.getMessageBody(),
					reply.getSenderName(), 
					reply.getReceiverName());
			counterSent++;
		} catch (RemoteException e) {
			handleRemoteException(e);
		}
	}
	/**
	 * Simple RemoteException handler<br>
	 * Just prints out stacktrace.
	 * @param e
	 */
	private static void handleRemoteException(RemoteException e){
		System.out.println("Fail to perform an operation due to Exception :" + e.getMessage());
		e.printStackTrace();
	}
	
}
