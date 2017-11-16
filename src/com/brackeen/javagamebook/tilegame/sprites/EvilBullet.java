package com.brackeen.javagamebook.tilegame.sprites;
import com.brackeen.javagamebook.graphics.Animation;
/**
A Bullet is a Creature that fly quickly in the air.
*/
public class EvilBullet extends Creature {
	public int face;
	public EvilBullet(Animation left, Animation right,
			Animation deadLeft, Animation deadRight)
	{
		super(left, right, deadLeft, deadRight);
	}

	public float getMaxSpeed() {
		return 0.3f; //bullet speed really fast
	}

	public boolean isFlying() {
		return isAlive();
	}
	
	public void wakeUp() {
        if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(face*getMaxSpeed()); //fly to the right
        }
        //System.out.println(getX());
    }
}
