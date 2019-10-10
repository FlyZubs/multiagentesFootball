package team.legacy.NoSwarm;
//
//	File:			Brain.java
//	Author:		Krzysztof Langner
//	Date:			1997/04/28
//
//    Modified by:	Paul Marlow

import java.lang.Math;


class Brain extends Thread implements SensorInput
{
	//---------------------------------------------------------------------------
	// This constructor:
	// - stores connection to noswarm
	// - starts thread for this object
	public Brain(SendCommand noswarm, String team, 
							char side, int number, String playMode)
	{
		m_timeOver = false;
		m_noswarm = noswarm;
		m_memory = new Memory();
		m_team = team;
		m_side = side;
//		m_number = number;
//		m_playMode = playMode;
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
	public void run()
	{
		ObjectInfo object;
		ObjectInfo player;

		// first put it somewhere on my side
		m_noswarm.move( -Math.random()*52.5 , Math.random()*34.0 );

		while( !m_timeOver )
		{
			object = m_memory.getObject("ball");
			player = m_memory.getObject("player " + m_team); 

			if( object == null )
			{
				// If you don't know where is ball then find it
				m_noswarm.turn(40);
				m_memory.waitForNewInfo();
			}
			else if( object.m_distance >= 10.0 )
			{
				// If ball is too far then
				// turn to ball or 
				// if we have correct direction then go to ball
				if( object.m_direction != 0 ){
					m_noswarm.turn(object.m_direction);
				}else{
					m_noswarm.dash(10*object.m_distance);
				}
			}
			else if(object.m_distance < 10.0 && object.m_distance > 0.9){
				//if we see another player closer
				if( (player != null) && (player.m_distance < 2*object.m_distance) ){
					m_noswarm.dash(-10);
				}else{
					if( object.m_direction != 0 ){
						m_noswarm.turn(object.m_direction);
					}else{
						m_noswarm.dash(10*object.m_distance);
				}

				}
			}
			else{
				if( m_side == 'l' )
					object = m_memory.getObject("goal r");
				else
					object = m_memory.getObject("goal l");

				if( object == null )
				{
					m_noswarm.turn(40);
					m_memory.waitForNewInfo();
				}
				else{
					m_noswarm.kick(100, object.m_direction);
				}
			}
		
			// sleep one step to ensure that we will not send
			// two commands in one cycle.
			try{
				Thread.sleep(2*SoccerParams.simulator_step);
			}catch(Exception e){}
		}

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
	}


	//---------------------------------------------------------------------------
	// This function receives hear information from player
	public void hear(int time, int direction, String message)
	{
	}

	//---------------------------------------------------------------------------
	// This function receives hear information from referee
	public void hear(int time, String message)
	{						 /*
		StringTokenizer tokenizer = new StringTokenizer(message,"() ", true);
		String token;

		// First is referee token and time token
		tokenizer.nextToken();
		tokenizer.nextToken();
		tokenizer.nextToken();
		token = tokenizer.nextToken();

		if(token.compareTo("time_over") == 0)
			m_timeOver = true;
			*/
	}


//===========================================================================
// Private members
	private SendCommand	m_noswarm;			// robot which is controled by this brain
	private Memory			m_memory;				// place where all information is stored
	private char				m_side;
	private String				m_team;
	volatile private boolean		m_timeOver;
}
