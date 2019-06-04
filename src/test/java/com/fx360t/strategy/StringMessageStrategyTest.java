package com.fx360t.strategy;

import java.rmi.RemoteException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fx360t.player.Player;
import com.fx360t.service.MessageService;

@RunWith(MockitoJUnitRunner.class)
public class StringMessageStrategyTest {
	@Mock
	private Player firstPlayer;
	@Mock
	private Player secondPlayer;

	@Mock
	private MessageService<String> messageService;

	private boolean firstRegistered;
	private boolean secondRegistered;
	private String sentMessage_1;
	private String sentMessage_2;

	@Before
	public void init() {
		try {
			Mockito.when(firstPlayer.getIdentity()).thenReturn("firstPlayer");
			Mockito.when(firstPlayer.getName()).thenReturn("firstPlayer");
			Mockito.when(secondPlayer.getIdentity()).thenReturn("secondPlayer");
			Mockito.when(secondPlayer.getName()).thenReturn("secondPlayer");
			Mockito.doAnswer((inv) -> firstRegistered = true).
					when(messageService).register("firstPlayer");

			Mockito.doAnswer((inv) -> firstRegistered = false).
					when(messageService).unregister("firstPlayer");
			Mockito.doAnswer((inv) -> secondRegistered = true).
					when(messageService).register("secondPlayer");
			Mockito.doAnswer((inv)->sentMessage_1 = inv.getArgumentAt(0, String.class)).
					when(messageService).sendMessage(Mockito.anyString(), 
									Mockito.eq("firstPlayer"), 
									Mockito.eq("secondPlayer"));
			Mockito.doAnswer((inv)->sentMessage_2 = inv.getArgumentAt(0, String.class)).
					when(messageService).sendMessage(Mockito.anyString(), 
									Mockito.eq("secondPlayer"), 
									Mockito.eq("firstPlayer"));
			Mockito.doAnswer((inv)->new Message<>(sentMessage_1,"firstPlayer","secondPlayer")).
					when(messageService).getNextMessage("secondPlayer");
			
			Mockito.doAnswer((inv)->new Message<>(sentMessage_1,"secondPlayer","firstPlayer")).
					when(messageService).getNextMessage("firstPlayer");
			
		} catch (RemoteException e) {
			Assert.fail();
		}
	}

	@Test
	public void readyTest() {
		try{
			new StringMessageStrategy(null);
			Assert.fail();
		}catch(NullPointerException npe){
		}
		
		GameStrategy<Message<String>> strategy1 = new StringMessageStrategy(messageService);
		Assert.assertTrue(strategy1.ready(firstPlayer));
		Assert.assertFalse(strategy1.ready(firstPlayer));
		Assert.assertTrue(firstRegistered);

		GameStrategy<Message<String>> strategy2 = new StringMessageStrategy(messageService);
		Assert.assertTrue(strategy2.ready(secondPlayer));
		Assert.assertFalse(strategy2.ready(secondPlayer));
		Assert.assertTrue(secondRegistered);
		Assert.assertFalse(strategy1.stopCondition());
		Assert.assertFalse(strategy2.stopCondition());

	}

	@Test
	public void startTest() {
		try {
			GameStrategy<Message<String>> strategy1 = new StringMessageStrategy(messageService);
			GameStrategy<Message<String>> strategy2 = new StringMessageStrategy(messageService);
			Message<String> mess = new Message<String>("Hello", firstPlayer.getIdentity(), secondPlayer.getIdentity());

			Assert.assertFalse(strategy1.start(firstPlayer, null));
			
			Assert.assertFalse(strategy1.start(firstPlayer, mess));

			strategy1.ready(firstPlayer);

			strategy2.ready(secondPlayer);

			Assert.assertTrue(strategy1.start(firstPlayer, mess));

		} catch (RemoteException e) {
			Assert.fail();
		}
	}

	@Test
	public void playTest() {
		try {
			GameStrategy<Message<String>> strategy1 = new StringMessageStrategy(messageService);
			GameStrategy<Message<String>> strategy2 = new StringMessageStrategy(messageService);
			Message<String> mess = new Message<String>("Hello", firstPlayer.getIdentity(), secondPlayer.getIdentity());

			Assert.assertFalse(strategy1.play(firstPlayer));
			strategy1.ready(firstPlayer);

			strategy2.ready(secondPlayer);

			strategy1.start(firstPlayer, mess);
			Assert.assertTrue("Hello".equals(sentMessage_1));
			
			Assert.assertTrue(strategy1.play(firstPlayer));
			
			Assert.assertTrue(strategy2.play(secondPlayer));
			Assert.assertTrue(sentMessage_2!=null && sentMessage_2.startsWith(sentMessage_1));
			for(int i =0;i<9;i++){
				strategy1.play(firstPlayer);
			}
			Assert.assertTrue(strategy1.stopCondition());
		} catch (RemoteException e) {
			Assert.fail();
		}
	}
	@Test
	public void finishTest(){
		GameStrategy<Message<String>> strategy1 = new StringMessageStrategy(messageService);
		Assert.assertFalse(strategy1.finish(firstPlayer));
		strategy1.start(firstPlayer, new Message<String>("Test","firstPlayer","secondPlayer"));
		Assert.assertFalse(strategy1.finish(firstPlayer));
		strategy1.ready(firstPlayer);
		Assert.assertFalse(strategy1.finish(firstPlayer));
		strategy1.play(firstPlayer);
		Assert.assertTrue(strategy1.finish(firstPlayer));
		Assert.assertFalse(firstRegistered);
		Assert.assertTrue(strategy1.stopCondition());
		
	}
}
