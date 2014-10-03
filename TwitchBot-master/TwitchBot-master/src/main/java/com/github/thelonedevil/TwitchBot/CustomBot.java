package com.github.thelonedevil.TwitchBot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class CustomBot extends PircBotX {

	public CustomBot(Configuration<? extends PircBotX> configuration) {
		super(configuration);
		// TODO Auto-generated constructor stub
	}
	
	public void stop(){
		this.stopBotReconnect();
		this.shutdown();
	}

}
