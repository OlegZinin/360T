package com.fx360t.strategy;

import java.io.Serializable;

import com.fx360t.service.MessageService;
/**
 * A wrapper class for messages used in {@link MessageService}.<br>
 * Besides message body it contains information about sender and receiver 
 * @author Oleg
 * @param <T> - data type for message bodies
 */
public class Message<T> implements Serializable{
	private static final long serialVersionUID = 1L;
	private T messageBody;
	private String senderName;
	private String receiverName;
	public Message(T messageBody, String senderName, String receiverName) {
		super();
		this.messageBody = messageBody;
		this.senderName = senderName;
		this.receiverName = receiverName;
	}
	public T getMessageBody() {
		return messageBody;
	}
	public String getSenderName() {
		return senderName;
	}
	public String getReceiverName() {
		return receiverName;
	}
	@Override
	public String toString() {
			return senderName +" -> " +receiverName +" : " + messageBody;
	}
	
}
