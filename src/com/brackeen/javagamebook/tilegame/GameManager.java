package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
 */
public class GameManager extends GameCore {

	public static void main(String[] args) {
		new GameManager().run();
	}

	// uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
	private static final AudioFormat PLAYBACK_FORMAT =
		new AudioFormat(44100, 16, 1, true, false);

	private static final int DRUM_TRACK = 1;

	public static final float GRAVITY = 0.002f;

	private Point pointCache = new Point();
	private TileMap map;
	private MidiPlayer midiPlayer;
	private SoundManager soundManager;
	private ResourceManager resourceManager;
	private Sound prizeSound;
	private Sound boopSound;
	private InputManager inputManager;
	private TileMapRenderer renderer;

	private GameAction moveLeft;
	private GameAction moveRight;
	private GameAction jump;
	private GameAction exit;
	private GameAction shoot;
	int face = 1;
	int flag = 0;
	long finishTime = 0;
	int auto = 1;
	int wait = 0;
	long startTime = 0;

	public void init() {
		super.init();

		// set up input manager
		initInput();

		// start resource manager
		resourceManager = new ResourceManager(
				screen.getFullScreenWindow().getGraphicsConfiguration());

		// load resources
		renderer = new TileMapRenderer();
		renderer.setBackground(
				resourceManager.loadImage("background.png"));

		// load first map
		map = resourceManager.loadNextMap();
		
		
		
		// load sounds
		soundManager = new SoundManager(PLAYBACK_FORMAT);
		prizeSound = soundManager.getSound("sounds/prize.wav");
		boopSound = soundManager.getSound("sounds/boop2.wav");

		// start music
		midiPlayer = new MidiPlayer();
		Sequence sequence =
			midiPlayer.getSequence("sounds/music.midi");
		midiPlayer.play(sequence, true);
		toggleDrumPlayback();
	}


	/**
        Closes any resurces used by the GameManager.
	 */
	public void stop() {
		super.stop();
		midiPlayer.close();
		soundManager.close();
	}


	private void initInput() {
		moveLeft = new GameAction("moveLeft");
		moveRight = new GameAction("moveRight");
		jump = new GameAction("jump",
				GameAction.DETECT_INITAL_PRESS_ONLY);
		exit = new GameAction("exit",
				GameAction.DETECT_INITAL_PRESS_ONLY);
		shoot = new GameAction("shoot", GameAction.DETECT_INITAL_PRESS_ONLY);//initially the shooting are just press and release to shoot

		inputManager = new InputManager(
				screen.getFullScreenWindow());
		inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

		inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
		inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
		inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
		inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
		inputManager.mapToKey(shoot, KeyEvent.VK_S);//map the shoot with key shoot
	}


	private void checkInput(long elapsedTime) {
		if (exit.isPressed()) {
			stop();
		}

		Player player = (Player)map.getPlayer();
		if (player.isAlive()) {
			float velocityX = 0;
			if (moveLeft.isPressed()) {
				velocityX-=player.getMaxSpeed();
				face = -1;
				player.setH(0.01f);
			}
			if (moveRight.isPressed()) {
				velocityX+=player.getMaxSpeed();
				face = 1;
				player.setH(0.01f);
			}
			if (jump.isPressed()) {
				player.jump(false);
			}

			if (shoot.getState() == 0){//release
				flag = 0;
			}

			if (shoot.isPressed()) {
			
				if(flag == 0){
					startTime = System.currentTimeMillis();
					flag = 1;
				}
				if(System.currentTimeMillis() - finishTime > 1000){
					wait = 0;
				}
				if (shoot.getState() == 0){//release
					flag = 0;
				}
				if(wait == 0){
					if(System.currentTimeMillis() - startTime > 800 && flag == 1){
						auto = 10;
						
					}else {
						auto  = 1;
					
					}						
				for(int lc = 0; lc < auto; lc++){

					//System.out.println("shoot pressed");
					//when shoot is pressed
					Image[] images = new Image[2];

					images[0] = resourceManager.loadImage("bullet21.png");
					images[1] = resourceManager.loadImage("bullet22.png");
					Animation[] bulletAnim = new Animation[4];
					for (int i=0; i<4; i++) {
						if(face == 1)
							bulletAnim[i] = ResourceManager.createBulletAnim(images[0]);
						else
							bulletAnim[i] = ResourceManager.createBulletAnim(images[1]);
					}
					Bullet bullet = new Bullet(bulletAnim[0], bulletAnim[1],bulletAnim[2], bulletAnim[3]);
					bullet.setX(player.getX()+lc*20);
					bullet.setY(player.getY()+20);
					bullet.setVelocityY(0);
					bullet.face = face;
					map.addSprite(bullet);
				}
				wait = 1;
				finishTime = System.currentTimeMillis();

				}
			}
			player.setVelocityX(velocityX);
		}
	}
		



	public void draw(Graphics2D g) {
		renderer.draw(g, map,
				screen.getWidth(), screen.getHeight());
	}


	/**
        Gets the current map.
	 */
	public TileMap getMap() {
		return map;
	}


	/**
        Turns on/off drum playback in the midi music (track 1).
	 */
	public void toggleDrumPlayback() {
		Sequencer sequencer = midiPlayer.getSequencer();
		if (sequencer != null) {
			sequencer.setTrackMute(DRUM_TRACK,
					!sequencer.getTrackMute(DRUM_TRACK));
		}
	}


	/**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
	 */
	public Point getTileCollision(Sprite sprite,
			float newX, float newY)
	{
		float fromX = Math.min(sprite.getX(), newX);
		float fromY = Math.min(sprite.getY(), newY);
		float toX = Math.max(sprite.getX(), newX);
		float toY = Math.max(sprite.getY(), newY);

		// get the tile locations
		int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
		int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
		int toTileX = TileMapRenderer.pixelsToTiles(
				toX + sprite.getWidth() - 1);
		int toTileY = TileMapRenderer.pixelsToTiles(
				toY + sprite.getHeight() - 1);

		// check each tile for a collision
		for (int x=fromTileX; x<=toTileX; x++) {
			for (int y=fromTileY; y<=toTileY; y++) {
				if (x < 0 || x >= map.getWidth() ||
						map.getTile(x, y) != null)
				{
					// collision found, return the tile
					if(sprite instanceof Bullet){
						Bullet temp = (Bullet) sprite;
						temp.setState(Creature.STATE_DEAD);
						return null;
					}
					if(sprite instanceof EvilBullet){
						EvilBullet temp = (EvilBullet) sprite;
						temp.setState(Creature.STATE_DEAD);
						return null;
					}
					pointCache.setLocation(x, y);
					return pointCache;
				}
			}
		}

		// no collision found
		return null;
	}


	/**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
	 */
	public boolean isCollision(Sprite s1, Sprite s2) {
		// if the Sprites are the same, return false
		if (s1 == s2) {
			return false;
		}

		if (s1 instanceof Bullet && s2 instanceof Bullet){
			return false;
		}


		// if one of the Sprites is a dead Creature, return false
		if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
			return false;
		}
		if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
			return false;
		}

		// get the pixel location of the Sprites
		int s1x = Math.round(s1.getX());
		int s1y = Math.round(s1.getY());
		int s2x = Math.round(s2.getX());
		int s2y = Math.round(s2.getY());

		// check if the two sprites' boundaries intersect
		return (s1x < s2x + s2.getWidth() &&
				s2x < s1x + s1.getWidth() &&
				s1y < s2y + s2.getHeight() &&
				s2y < s1y + s1.getHeight());
	}


	/**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
	 */
	public Sprite getSpriteCollision(Sprite sprite) {

		// run through the list of Sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite otherSprite = (Sprite)i.next();
			if (isCollision(sprite, otherSprite)) {
				// collision found, return the Sprite
				return otherSprite;
			}
		}

		// no collision found
		return null;
	}


	/**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
	 */
	public void update(long elapsedTime) {
		Creature player = (Creature)map.getPlayer();


		// player is dead! start map over
		if (player.getState() == Creature.STATE_DEAD) {
			map = resourceManager.reloadMap();
			return;
		}

		// get keyboard/mouse input
		checkInput(elapsedTime);

		// update player
		updateCreature(player, elapsedTime);
		player.update(elapsedTime);
		ArrayList<Sprite> badSprites = new ArrayList<Sprite>();
		// update other sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite sprite = (Sprite)i.next();
			
			if (sprite instanceof Creature) {
				Creature creature = (Creature)sprite;
				if (creature.getState() == Creature.STATE_DEAD) {
					i.remove();
					Player temp = (Player) player;
					if(!(creature instanceof Bullet) && !(creature instanceof EvilBullet)){
						temp.setH(10);
					}
				}
				else {
					updateCreature(creature, elapsedTime, player);
					if(creature instanceof Fly || creature instanceof Grub){
						badSprites.add(sprite);
					}
				}				
			}
			// normal update
			sprite.update(elapsedTime);
		}
		int evilface = 1;
		
		//System.out.println(badSprites.size());
		
		for(Sprite temp: badSprites){
			if (Math.abs(temp.getX() - player.getX()) < screen.getWidth() && temp.cooldown == 0)
			{
				if(temp.getVelocityX() > 0){
					evilface = 1;
				}else{
					evilface = 0;
				}
				Image[] images = new Image[2];

				images[0] = resourceManager.loadImage("evil_bullet2.png");
				images[1] = resourceManager.loadImage("evil_bullet1.png");
				Animation[] bulletAnim = new Animation[4];
				for (int i1=0; i1<4; i1++) {
					if(evilface == 1)
						bulletAnim[i1] = ResourceManager.createBulletAnim(images[0]);
					else
						bulletAnim[i1] = ResourceManager.createBulletAnim(images[1]);
				}
				EvilBullet evilbullet = new EvilBullet(bulletAnim[0], bulletAnim[1],bulletAnim[2], bulletAnim[3]);
				evilbullet.setX(temp.getX()+20);
				//System.out.println("temp x"+Float.toString(temp.getX()));
				evilbullet.setY(temp.getY());
				evilbullet.setVelocityY(0);
				if(temp.getVelocityX()>0){
					evilbullet.setVelocityX(0.3f);
				}else{
					evilbullet.setVelocityX(-0.3f);
				}
				
				evilbullet.face = face;
				map.addSprite(evilbullet);
				temp.cooldown = 1;
			}
		}
	}


	/**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
	 */
	private void updateCreature(Creature creature,
			long elapsedTime)
	{

		// apply gravity
		if (!creature.isFlying()) {
			creature.setVelocityY(creature.getVelocityY() +
					GRAVITY * elapsedTime);
		}

		// change x
		float dx = creature.getVelocityX();
		float oldX = creature.getX();
		float newX = oldX + dx * elapsedTime;
		Point tile =
			getTileCollision(creature, newX, creature.getY());
		if (tile == null) {
			creature.setX(newX);
		}
		else {
			// line up with the tile boundary
			if (dx > 0) {
				creature.setX(
						TileMapRenderer.tilesToPixels(tile.x) -
						creature.getWidth());
			}
			else if (dx < 0) {
				creature.setX(
						TileMapRenderer.tilesToPixels(tile.x + 1));
			}
			creature.collideHorizontal();
		}
		if (creature instanceof Player) {
			checkPlayerCollision((Player)creature, false, 0);
		}

		if (creature instanceof Bullet) {
			checkBulletCollision((Bullet)creature, true);
		}

		// change y
		float dy = creature.getVelocityY();
		float oldY = creature.getY();
		float newY = oldY + dy * elapsedTime;
		tile = getTileCollision(creature, creature.getX(), newY);
		if (tile == null) {
			creature.setY(newY);
		}
		else {
			// line up with the tile boundary
			if (dy > 0) {
				creature.setY(
						TileMapRenderer.tilesToPixels(tile.y) -
						creature.getHeight());
			}
			else if (dy < 0) {
				creature.setY(
						TileMapRenderer.tilesToPixels(tile.y + 1));
			}
			creature.collideVertical();
		}
		if (creature instanceof Player) {
			boolean canKill = (oldY < creature.getY() - 10);
			checkPlayerCollision((Player)creature, canKill, oldY);
		}

	}
	
	private void updateCreature(Creature creature,
			long elapsedTime, Creature temp)
	{

		// apply gravity
		if (!creature.isFlying()) {
			creature.setVelocityY(creature.getVelocityY() +
					GRAVITY * elapsedTime);
		}

		Player player = (Player) temp;
		float dx = creature.getVelocityX();
		if(creature instanceof Fly || creature instanceof Grub){
			if(creature.getX() < player.getX()){
				dx = Math.abs(creature.getVelocityX());
				creature.setVelocityX(Math.abs(creature.getVelocityX()));
			}
			else{
				dx = -Math.abs(creature.getVelocityX());
				creature.setVelocityX(-Math.abs(creature.getVelocityX()));
			}
		}
		
		// change x
		
		float oldX = creature.getX();
		float newX = oldX + dx * elapsedTime;
		Point tile =
			getTileCollision(creature, newX, creature.getY());
		if (tile == null) {
			creature.setX(newX);
		}
		else {
			// line up with the tile boundary
			if (dx > 0) {
				creature.setX(
						TileMapRenderer.tilesToPixels(tile.x) - creature.getWidth());
			}
			else if (dx < 0) {
				creature.setX(
						TileMapRenderer.tilesToPixels(tile.x + 1));
			}
			/*if((creature instanceof Fly && creature instanceof Grub)){
				creature.collideHorizontal();
			}*/
		}
		if (creature instanceof Player) {
			checkPlayerCollision((Player)creature, false, 0);
		}

		if (creature instanceof Bullet) {
			checkBulletCollision((Bullet)creature, true);
		}

		// change y
		float dy = creature.getVelocityY();
		float oldY = creature.getY();
		float newY = oldY + dy * elapsedTime;
		tile = getTileCollision(creature, creature.getX(), newY);
		if (tile == null) {
			creature.setY(newY);
		}
		else {
			// line up with the tile boundary
			if (dy > 0) {
				creature.setY(
						TileMapRenderer.tilesToPixels(tile.y) -
						creature.getHeight());
			}
			else if (dy < 0) {
				creature.setY(
						TileMapRenderer.tilesToPixels(tile.y + 1));
			}
			creature.collideVertical();
		}
		if (creature instanceof Player) {
			boolean canKill = (oldY < creature.getY() - 10);
			checkPlayerCollision((Player)creature, canKill, oldY);
		}

	}
	


	/**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
	 */
	public void checkPlayerCollision(Player player,
			boolean canKill, float oldY)
	{
		if (!player.isAlive()) {
			return;
		}

		// check for player collision with other sprites
		Sprite collisionSprite = getSpriteCollision(player);
		if(collisionSprite instanceof EvilBullet) {
			player.setH(-5);
			System.out.println("health" + player.getH());
			EvilBullet temp = (EvilBullet) collisionSprite;
			temp.setState(Creature.STATE_DYING);
			return;
		}
		if (collisionSprite instanceof PowerUp) {
			acquirePowerUp((PowerUp)collisionSprite, player);
		}
		else if (collisionSprite instanceof Creature) {
			Creature badguy = (Creature)collisionSprite;
			if (canKill){
					// kill the badguy and make player bounce
					soundManager.play(boopSound);
					badguy.setState(Creature.STATE_DYING);
					player.setY(badguy.getY() - player.getHeight());
					player.jump(true);
					//System.out.println("chong zi y"+Float.toString(oldY));
					//System.out.println("my y "+ Float.toString(player.getY()));
					//System.out.println("my health" + Integer.toString((int)player.getH()));
			}
			else {
				// player dies!
				player.setH(-5);
				
				//System.out.println("my health" + Integer.toString((int)player.getH())+"    "+Long.toString(System.currentTimeMillis()));
			}
		}
		if(collisionSprite instanceof EvilBullet) {
			player.setH(-5);
			EvilBullet temp = (EvilBullet) collisionSprite;
			temp.setState(Creature.STATE_DYING);
		}
	}

	public void checkBulletCollision(Bullet bullet,
			boolean canKill)
	{
		if (!bullet.isAlive()) {
			return;
		}

		// check for player collision with other sprites
		Sprite collisionSprite = getSpriteCollision(bullet);
		if (collisionSprite instanceof Creature) {
			Creature badguy = (Creature)collisionSprite;
			if (canKill && !(badguy instanceof Player)) {
				// kill the badguy and make player bounce
				soundManager.play(boopSound);
				badguy.setState(Creature.STATE_DYING);
				bullet.setState(Creature.STATE_DYING);
			}
		}
	}

	public Sprite getSpriteCollision(Bullet bullet) {

		// run through the list of Sprites
		Iterator i = map.getSprites();
		while (i.hasNext()) {
			Sprite otherSprite = (Sprite)i.next();
			if (isCollision(bullet, otherSprite)) {
				// collision found, return the Sprite
				return otherSprite;
			}
		}

		// no collision found
		return null;
	}



	/**
        Gives the player the speicifed power up and removes it
        from the map.
	 */
	public void acquirePowerUp(PowerUp powerUp, Player player) {
		// remove it from the map
		map.removeSprite(powerUp);

		if (powerUp instanceof PowerUp.Star) {
			// do something here, like give the player points
			soundManager.play(prizeSound);
		}
		else if (powerUp instanceof PowerUp.Music) {
			// change the music
			soundManager.play(prizeSound);
			toggleDrumPlayback();
		}
		else if (powerUp instanceof PowerUp.Goal) {
			// advance to next map
			soundManager.play(prizeSound,
					new EchoFilter(2000, .7f), false);
			map = resourceManager.loadNextMap();
		}
		else if (powerUp instanceof PowerUp.Mushroom) {
			player.setH(5);
		}
	}

}
