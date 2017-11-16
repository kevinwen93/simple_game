package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {
	private float health;
    private static final float JUMP_SPEED = -.95f;

    private boolean onGround;
    public int cooldown;
    private long cd = 500;
   
    
    public void setH(int blood){
    	if(health + blood > 40){
    		health = 40;
    	}else{
    		if(blood < 0 && cooldown == 0){
    			health += blood;
    			cooldown = 1;
    			cd = 500;
    		}
    		if(blood > 0){
    			health += blood;
    		}
    	}
    	if(health <= 0){
    		setState(STATE_DYING);
    	}
    }
    
    public void setH(float blood){   	
    	if(health + blood > 40){
    		health = 40;
    	}else{
    		if(blood > 0){
    			health += blood;
    		}
    	}
    	if(health <= 0){
    		setState(STATE_DYING);
    	}
    }
    
    public void update(long elapsedTime) {
    	super.update(elapsedTime);
    	cd -= elapsedTime;
    	if(cd <= 0){
    		cooldown = 0;
    	}
    	else{
    		cooldown = 1;
    	}
    }
    
    
    
    public float getH(){
    	return health;
    }
    
    
    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        health = 20;
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }


    public float getMaxSpeed() {
        return 0.5f;
    }

    public void shoot(){
    	
    }
    
}
