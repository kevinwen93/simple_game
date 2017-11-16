package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Grub extends Creature {
	
	int cd = 2000;
    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.05f;
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
