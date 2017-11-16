package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Fly is a Creature that fly slowly in the air.
*/
public class Fly extends Creature {
	public int face;
	private long cd = 2000;
	
    public Fly(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.2f;
    }


    public boolean isFlying() {
        return isAlive();
    }
    
    public void update(long elapsedTime) {
    	super.update(elapsedTime);
    	cd -= elapsedTime;
    	if(cd <= 0){
    		cooldown = 0;
    		cd = 2000;
    	}
    	else{
    		cooldown = 1;
    	}
    }

}
