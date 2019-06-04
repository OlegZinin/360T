package com.fx360t.service;

import org.junit.Assert;
import org.junit.Test;

import com.fx360t.strategy.Message;

public class SimpleMessageServiceTest extends SimpleMessageService {
	@Test
	public void registerUserTest(){
		Assert.assertTrue(getMessages().isEmpty());
		String user ="User";
		this.register(user);
		Assert.assertTrue(getMessages().containsKey(user));
		this.register(user);
		Assert.assertTrue(getMessages().size()==1);
	}
	@Test
	public void unregisterTest(){
		Assert.assertTrue(getMessages().isEmpty());
		String user ="User";
		this.register(user);
		this.unregister("User2");
		Assert.assertTrue(getMessages().containsKey(user));
		this.unregister(user);
		Assert.assertTrue(getMessages().isEmpty());
	}
	@Test
	public void sendMessageTest(){
		this.register("user1","user2");
		this.sendMessage("message","user1", "user2");
		Assert.assertTrue(getMessages().get("user1")!=null && 
					getMessages().get("user1").isEmpty());
		Assert.assertTrue(getMessages().get("user2")!=null
				 		&& getMessages().get("user2").size()==1);
		Assert.assertTrue(getMessages().get("user2").peek()!=null && 
				getMessages().get("user2").poll().getMessageBody().equals("message"));
		
		this.sendMessage("message","user1", "user1");
		Assert.assertTrue(getMessages().get("user1")!=null && 
				getMessages().get("user1").isEmpty());
		this.sendMessage("message","user1", "user3");
		Assert.assertTrue(getMessages().get("user3")==null);
		
		this.sendMessage("message","user3", "user1");
		Assert.assertTrue(getMessages().get("user1").isEmpty());
	}
	@Test
	public void getMessageTest(){
		this.register("user1","user2");
		this.sendMessage("message","user1", "user2");
		Message<String> mess = this.getNextMessage("user2");
		Assert.assertTrue(mess!=null && mess.getMessageBody().equals("message")
					&& mess.getReceiverName().equals("user2")
					&& mess.getSenderName().equals("user1"));
		mess = this.getNextMessage("user3");
		Assert.assertTrue(mess==null);
	}
}
