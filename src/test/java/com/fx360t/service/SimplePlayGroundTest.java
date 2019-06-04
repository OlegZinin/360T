package com.fx360t.service;

import java.rmi.RemoteException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.fx360t.player.Player;

@RunWith(MockitoJUnitRunner.class)
public class SimplePlayGroundTest {
	@Mock
	private Player firstPlayer;
	private boolean firstInGame;
	@Mock
	private Player secondPlayer;
	private boolean secondInGame;
	@Mock
	private Player oneMorePlayer;

	@Before
	public void init() {
		try {
			Mockito.when(firstPlayer.getIdentity()).thenReturn("firstPlayer");
			Mockito.when(firstPlayer.getName()).thenReturn("firstPlayer");
			Mockito.when(secondPlayer.getIdentity()).thenReturn("secondPlayer");
			Mockito.when(secondPlayer.getName()).thenReturn("secondPlayer");
			Mockito.when(oneMorePlayer.getIdentity()).thenReturn("oneMorePlayer");
			Mockito.when(oneMorePlayer.getName()).thenReturn("oneMorePlayer");
			Mockito.doAnswer((inv) -> {
				firstInGame = true;
				return null;
			}).when(firstPlayer).startToPlay(Mockito.anyBoolean(), Mockito.any());
			Mockito.doAnswer((inv) -> {
				secondInGame = true;
				return null;
			}).when(secondPlayer).startToPlay(Mockito.anyBoolean(), Mockito.any());

			Mockito.doAnswer((inv) -> {
				firstInGame = false;
				return null;
			}).when(firstPlayer).gameOver();
			Mockito.doAnswer((inv) -> {
				secondInGame = false;
				return null;
			}).when(secondPlayer).gameOver();

		} catch (RemoteException e) {
			Assert.fail();
		}
	}

	@Test
	public void registerPlayerTest() {
		SimplePlayGround playGround = new SimplePlayGround();
		try {
			playGround.registerPlayer(null);
			Assert.assertTrue(playGround.playersCount() == 0);
			playGround.registerPlayer(firstPlayer);
			Assert.assertTrue(playGround.playersCount() == 1);
			playGround.registerPlayer(firstPlayer);
			Assert.assertTrue(playGround.playersCount() == 1);
			playGround.registerPlayer(secondPlayer);
			Assert.assertTrue(playGround.playersCount() == 2);
			playGround.registerPlayer(oneMorePlayer);
			Assert.assertTrue(playGround.playersCount() == 2);

		} catch (RemoteException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void startPlayingTest() {
		SimplePlayGround playGround = new SimplePlayGround();
		try {
			Assert.assertFalse(playGround.startPlaying());

			playGround.registerPlayer(firstPlayer);
			playGround.registerPlayer(secondPlayer);
			Assert.assertTrue(playGround.startPlaying());

			Assert.assertFalse(playGround.startPlaying());
			Assert.assertTrue(firstInGame && secondInGame);
			
		} catch (RemoteException e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void waitFinishTest() {
		SimplePlayGround playGround = new SimplePlayGround();
		try {
			Assert.assertFalse(playGround.waitUntilGameIsFinished());

			playGround.registerPlayer(firstPlayer);
			playGround.registerPlayer(secondPlayer);
			Assert.assertFalse(playGround.waitUntilGameIsFinished());

			playGround.startPlaying();

			Assert.assertTrue(playGround.waitUntilGameIsFinished());

		} catch (RemoteException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	@Test
	public void finishPlayingTest() {
		SimplePlayGround playGround = new SimplePlayGround();
		try {
			Assert.assertFalse(playGround.finishPlaying());

			playGround.registerPlayer(firstPlayer);
			playGround.registerPlayer(secondPlayer);
			Assert.assertFalse(playGround.finishPlaying());
			playGround.startPlaying();
			Assert.assertTrue(playGround.finishPlaying());
			Assert.assertFalse(firstInGame || secondInGame);
			

		} catch (RemoteException e) {
			Assert.fail(e.getMessage());
		}
	}
	@Test
	public void unregisterPlayerTest(){
		SimplePlayGround playGround = new SimplePlayGround();
		try {
			playGround.unregisterPlayer(null);
			Assert.assertTrue(playGround.playersCount() == 0);
			playGround.registerPlayer(firstPlayer);
			playGround.unregisterPlayer(secondPlayer);
			Assert.assertTrue(playGround.playersCount() == 1);

			playGround.registerPlayer(secondPlayer);
			playGround.unregisterPlayer(oneMorePlayer);
			Assert.assertTrue(playGround.playersCount() == 2);
			
			playGround.unregisterPlayer(secondPlayer);
			Assert.assertTrue(playGround.playersCount() == 1);
			playGround.unregisterPlayer(secondPlayer);
			Assert.assertTrue(playGround.playersCount() == 1);
			playGround.unregisterPlayer(firstPlayer);
			Assert.assertTrue(playGround.playersCount() == 0);
			
		} catch (RemoteException e) {
			Assert.fail(e.getMessage());
		}
	}
}
