package com.buddycloud.pubsub.tasks;

import com.buddycloud.pubsub.log.LogMe;
import com.buddycloud.pubsub.node.Nodes;

public class CacheCleaner implements Runnable {

	public void run() {
        
        try {
            while(true) {    
            	//Pause for 120 seconds
            	//Thread.sleep(120*1000);
            	Thread.sleep(600*1000);
            	Long start = System.currentTimeMillis();
                Nodes.getInstance().cleanCacheFromUnactiveNodes();
                LogMe.info("Cleaned nodescache in '" + Long.toString((System.currentTimeMillis() - start)) + "' milliseconds.");
            }
        } catch (InterruptedException e) {
        	LogMe.info("CacheCleaner interrupted!");
        }
	}
	
}