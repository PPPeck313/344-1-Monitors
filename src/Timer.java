//Preston Peck
//Distributed Systems 344
//Prof. Fluture
//Project 1: Monitors
//April 15, 2017

import java.util.Random;

public class Timer extends Thread {
	public static long time = System.currentTimeMillis();
	
	public Teacher teach;
	
	public Object examInProgress = new Object();
	
	public Timer(int id, Teacher t) {
		setName("Timer-" + id);
		teach = t;
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time) + "] "+ getName() + ": " + m);
	}
	
    public void sleep (int seconds, String verb) {
    	try {
    		int time = seconds * 1000;//convert to milliseconds
    		msg(verb + " for " + time + " ms");
    		Thread.sleep(time);
    	}
    	
    	catch (InterruptedException e) {
    		msg(verb + " interrupted!");
    	}
    }
	
    public void sleepRandom (int seconds, String verb) {
    	try {
    		Random rand = new Random();
    		int time = rand.nextInt((seconds * 1000) + 1);//convert to milliseconds
    		msg(verb + " for " + time + " ms");
    		Thread.sleep(time);
    	}
    	
    	catch (InterruptedException e) {
    		msg(verb + " interrupted!");
    	}
    }
    
	public void wait(Object ob) {
		try {
			ob.wait();
		}
		
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
    
    public synchronized void run() {	
    	while (teach.administered < teach.administeredMax) {
    		synchronized (teach.exam) {
    			wait(teach.exam);
    		}
    		
    		sleep(5, "Ticking");//Each exam takes 1 hour.
    		
    		teach.administered++;
    		
    		synchronized (teach.exam) {
    			teach.exam.notify();
    		}
    	}
    }
}
