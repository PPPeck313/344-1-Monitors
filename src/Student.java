//Preston Peck
//Distributed Systems 344
//Prof. Fluture
//Project 1: Monitors
//April 15, 2017

import java.util.Random;
import java.util.Vector;

public class Student extends Thread {
	public static long time = System.currentTimeMillis();
	
	public int taken = 0;
	public int takenMax = 3;
	
	public int table = 0;
	
	public Teacher teach;
	public Vector<Integer> grades = new Vector<Integer>();
	
	public Student(int id, Teacher t) {
		setName("Student-" + id);
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
    
    public synchronized boolean enter() {
    	if (teach.administered < 4 && teach.current < teach.capacity) {//Students enter the classroom up to the classroom’s capacity.
    		table = teach.current / teach.numSeats;
    		teach.current++;
    		return true;
    	}
    	return false;
    }
    
    public synchronized void fileIn() {
    	teach.waiting++;
    	
    	synchronized (teach.inside) {
	    	teach.inside.notify();
	    	wait(teach.inside);
    	}
    }
    
    public synchronized void checkSeated() {
	    teach.waiting++;
	    
		if (teach.waiting == teach.current) {
    		synchronized (teach.teacher) {
    			teach.teacher.notify();
    		}
		}
    }
    
    public synchronized void takeASeat() {
    	teach.waiting--;
		msg("Taking a seat at table " + (table + 1));
		
    	if (teach.waiting == 0) {
    		synchronized (teach.teacher) {
    			teach.teacher.notify();
    		}
	    }
		
		synchronized (teach.tables.get(table)) {
			wait(teach.tables.get(table));//Once in the classroom, students take a seat at a table
		    msg("Received an exam");

		    checkSeated();
	    }
		
    	synchronized (teach.timer.examInProgress) {
    		wait(teach.timer.examInProgress);
    	}
    }
    
    public synchronized void getGrade() {
    	sleepRandom(5, "Checking work");//When the exam ends, each student takes a brief time to check their notes (simulated by sleep(random time) about 5 units).
		
		msg("Lining up for grade");
		Object notOb = new Object();
		teach.grading.add(notOb);
		teach.line.add(this);
		teach.waiting++;	
		
		if (teach.waiting == teach.current) {
    		synchronized (teach.teacher) {
    			teach.teacher.notify();
    		}
		}
			
		synchronized (notOb) {
			wait(notOb);
		}
		
		teach.current--;
    }
    
    public void leave() {
    	if (taken < takenMax) {
    		msg("Missed " + (takenMax - taken) + "exams");
    		
    		for (int i = taken; i <= takenMax; i++) {
    			grades.add(0);
    		}
    	}//If a student missed an exam, his grade will be 0.
    	
    	String report = "";
    	for (int i = 0; i < takenMax; i++) {
    		if (i == takenMax - 1) {
    			report += "Test " + i + ": " + grades.get(i);
    		}
    		
    		else {
    			report += "Test " + i + ": " + grades.get(i) + '\n';
    		}
    	}//At the end of the day, the exam grades for each student should be displayed. 
    	
    	teach.arrived--;
    	msg("Survived exam day! Final grades:" + '\n' + report);
    }
    
    
    public synchronized void run() {
    	sleepRandom(5, "Commuting");//Students come to school (simulated by sleep of random time)
    	teach.arrived++;
    	
    	while (taken < takenMax && teach.administered < 4) {//Each student should take three exams. takenMax = 3
    		synchronized (teach.outside) {
				msg("Waiting outside classroom");
				wait(teach.outside);//and wait in front of the classroom for the instructor to arrive.
    		}
    		
    		if (enter()) {
        		fileIn();
    			takeASeat();
		    	getGrade();
	    		
	    		taken++;
	    		msg("Taken " + taken + " exam(s)");
    		}
    	}//If a student took three exams it can terminate.
    	leave();
   }
}
