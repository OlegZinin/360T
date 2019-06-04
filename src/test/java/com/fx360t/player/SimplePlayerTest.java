package com.fx360t.player;

import java.rmi.RemoteException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fx360t.service.PlayerRegistrator;
import com.fx360t.strategy.GameStrategy;
import com.fx360t.strategy.Message;

@RunWith(MockitoJUnitRunner.class)
public class SimplePlayerTest {
	@Mock
	private PlayerRegistrator playGround;
	@Mock
	private GameStrategy<Message<String>> strategy;
	private boolean prepared;
	private boolean finished;
	private boolean playing;
	private boolean started;
	private boolean registered;
	@Before
	public void init(){
		Mockito.doAnswer((inv)->prepared=true).
				when(strategy).ready(Mockito.any());
		Mockito.doAnswer((inv)->started = true)
				.when(strategy).start(Mockito.any(), Mockito.any());
		Mockito.doAnswer((inv)->playing = true)
				.when(strategy).play(Mockito.any());
		Mockito.doAnswer((inv)->finished = true)
			.when(strategy).finish(Mockito.any());
		
		try {
			Mockito.doAnswer((inv)->registered = true)
			.when(playGround).registerPlayer(Mockito.any());
		} catch (RemoteException e) {
		}
	}
	@Test
	public void prepareTest(){
		Player player1 = new SimplePlayer("John", "startM", strategy, playGround);
		try {
			player1.prepare();
		} catch (RemoteException e) {
			Assert.fail();
		}
		Assert.assertTrue(prepared);
		Assert.assertTrue(registered);
	}

	@Test
	public void startToPlayTest(){
		Player player1 = new SimplePlayer("John", "startM", strategy, playGround);
		Player player2 = new SimplePlayer("Bob", "startM", strategy, playGround);
		try {
			player1.prepare();
		} catch (RemoteException e) {
			Assert.fail();
		}
		try {
			player1.startToPlay(true, player2);
		} catch (RemoteException e) {
			Assert.fail();
		}
		Assert.assertTrue(started);	
	}

	@Test
	public void playTest() throws InterruptedException{
		Player player1 = new SimplePlayer("John", "startM", strategy, playGround);
		Player player2 = new SimplePlayer("Bob", "startM", strategy, playGround);
		try {
			player1.prepare();
		} catch (RemoteException e) {
			Assert.fail();
		}
		try {
			player1.startToPlay(true, player2);
		} catch (RemoteException e) {
			Assert.fail();
		}
		Thread.sleep(1000);
		Assert.assertTrue(playing);	
	}

	@Test
	public void finishTest() throws InterruptedException{
		Player player1 = new SimplePlayer("John", "startM", strategy, playGround);
		Player player2 = new SimplePlayer("Bob", "startM", strategy, playGround);
		try {
			player1.prepare();
		} catch (RemoteException e) {
			Assert.fail();
		}
		try {
			player1.startToPlay(true, player2);
		} catch (RemoteException e) {
			Assert.fail();
		}
		Thread.sleep(1000);
		
		try {
			player1.gameOver();
		} catch (RemoteException e) {
			Assert.fail();
		}
		Assert.assertTrue(finished);
	}


}
