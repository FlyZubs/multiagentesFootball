package team.legacy.A1;
//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

//    Modified by:      Edgar Acosta
//    Date:             March 4, 2008

import java.lang.Math;
import java.util.regex.*;

class Brain extends Thread implements SensorInput
{
    //---------------------------------------------------------------------------
    // This constructor:
    // - stores connection to agent
    // - starts thread for this object
    public Brain(SendCommand agent, 
		 String team, 
		 char side, 
		 int number, 
		 String playMode)
    {
	m_timeOver = false;
	m_agent = agent;
	m_memory = new Memory();
	m_team = team;
	m_side = side;
	// m_number = number;
	m_playMode = playMode;
	m_waiting=true;
	start();
    }


    //---------------------------------------------------------------------------
    // This is main brain function used to make decision
    // In each cycle we decide which command to issue based on
    // current situation. the rules are:
    //
    //	1. If you don't know where is ball then turn right and wait for new info
    //
    //	2. If ball is too far to kick it then
    //		2.1. If we are directed towards the ball then go to the ball
    //		2.2. else turn to the ball
    //
    //	3. If we dont know where is opponent goal then turn wait 
    //				and wait for new info
    //
    //	4. Kick ball
    //
    //	To ensure that we don't send commands to often after each cycle
    //	we waits one simulator steps. (This of course should be done better)

    // ***************  Improvements ******************
    // Allways know where the goal is.
    // Move to a place on my side on a kick_off
    // ************************************************

    public void run()
    {
	ObjectInfo object;
	ObjectInfo ball; 

	while( !m_timeOver )
	    {
		//wait for a sense_body message to ensure that
		// we don't send two commands in one simulation cycle
		// also wait until we have information to deal with
		while(m_waiting || m_memory.isEmpty()){
		    //System.out.println("Waiting...");
		}
		// before a kick off move somewhere on my side
		if(Pattern.matches("^before_kick_off.*",m_playMode))
		    moveToMySide();
		ball = m_memory.getObject("ball");
		object = m_memory.getObject("player " + m_team);
		if( ball == null ){
		    // If you don't know where the ball is, then find it
		    m_agent.turn(44);
		    m_memory.clearInfo(); //it was waitForNewInfo(), 
		    //but I don't think it needs to explicitly wait --Edgar
		}
		else if( ball.m_distance > 5.0 && (object != null && object.m_distance <ball.m_distance)){
		    m_agent.turn(ball.m_direction);
		    m_memory.clearInfo();
		}
		else if( ball.m_distance > 0.7){
		    // If ball is too far then
		    // turn to ball or 
		    // if we have correct direction then go to ball
		    if( ball.m_direction < -2.5 || ball.m_direction > 2.5 ){
			m_agent.turn(ball.m_direction);
			m_memory.clearInfo();
		    }
		    else{ //otherwise run toward the ball
			float power=dash_factor*ball.m_distance;
			dash_factor*=0.9; //this decreases the dash factor, it is restored when we get new visual info
			if(power > 100)
			    power=100;
			m_agent.dash(power);
		    }
		}
		else {
		    // We know where the ball is and we can kick it
		    // so look for goal
		    if( m_side == 'l' )
			object = m_memory.getObject("goal r");
		    else
			object = m_memory.getObject("goal l");
		    
		    if( object == null )
			{
			    m_agent.turn(44);
			    m_memory.clearInfo(); //it was waitForNewInfo(), but I don't think it needs to explicitly wait --Edgar
			}
		    else
			m_agent.kick(100, object.m_direction);
		}
		
		// sleep one step to ensure that we will not send
		// two commands in one cycle.
		 		try{
		 		    Thread.sleep(2*SoccerParams.simulator_step);
		 		}catch(Exception e){}
		m_waiting=true;
	    }
	m_agent.bye();
    }


    //===========================================================================
    // Here are suporting functions for implement logic


    //===========================================================================
    // Implementation of SensorInput Interface

    //---------------------------------------------------------------------------
    // This function sends see information
    public void see(VisualInfo info)
    {
	m_memory.store(info);
	dash_factor=20;
    }


    //---------------------------------------------------------------------------
    // This function receives hear information from player
    public void hear(int time, int direction, String message)
    {
    }

    //---------------------------------------------------------------------------
    // This function receives hear information from referee
    public void hear(int time, String message)
    {						 
	if(message.compareTo("time_over") == 0)
	    m_timeOver = true;
	//the next should take into account all play modes, by now is good
	if(Pattern.matches("^before_kick_off.*",message))
	    m_playMode=message;

    }

    //--------------------------------------------------------------------------
    // This function receives a sense_body message
    // It doesn't do anything other than signaling the time to send a command
    public void sense(String message)
    {
	m_waiting=false;
    }

    public void moveToMySide()
    {
	    m_agent.move( -Math.random()*52.5 , 34 - Math.random()*68.0 );
	    m_playMode="!"+m_playMode;
    }

    //===========================================================================
    // Private members
    private SendCommand	                m_agent;			// robot which is controled by this brain
    private Memory			m_memory;				// place where all information is stored
    private char			m_side;
    volatile private boolean		m_timeOver,m_waiting;
    private String                      m_team;
    volatile private String                      m_playMode;
    volatile private float                       dash_factor=20; //
}
