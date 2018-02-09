//Preston Peck
//Distributed Systems 344
//Prof. Fluture
//Project 1: Monitors
//April 15, 2017

import java.util.Random;
import java.util.Vector;

public class Teacher extends Thread {
	public static long time = System.currentTimeMillis();
	
	public int administered = 0;
	public int administeredMax = 4;
	
	public volatile int arrived = 0;
	public volatile int waiting = 0;
	public volatile int current = 0;
	public volatile int capacity = 0;
	
	public volatile int numSeats = 0;
	public volatile int numTables = 0;
	
	public Timer timer = new Timer(1, this);
	
	public Object outside = new Object();
	public Object inside = new Object();
	public Object teacher = new Object();
	public Object exam = new Object();//Timer has visibility
	
	public volatile Vector<Object> tables = new Vector<Object>(numTables);
	public volatile Vector<Student> line = new Vector<Student>();
	public volatile Vector<Object> grading = new Vector<Object>();
	
	public Teacher(int id, int cap, int seats) {
		setName("Teacher-" + id);
		capacity = cap;
		numSeats = seats;
		numTables = capacity / numSeats;//Each table has numSeats seats.
		timer.start();

		for (int i = 0; i < numTables; i++) {
			tables.add(new Object());//Students that sit at the same table should wait on the same object.
		}
	}
	
	public void msg(String m) {
		System.out.println("[" + (System.currentTimeMillis() - time) + "] "+ getName() + ": " + m);
	}

	public void wait(Object ob) {
		try {
			ob.wait();
		}
		
		catch (InterruptedException e) {
			e.printStackTrace();
		}
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
    
    public void gradeTest() {
		Random rand = new Random();
		
		if (!line.isEmpty()) {
    		sleepRandom(2, "Grading " + line.get(0).getName() + "'s exam");
    		//It will take the instructor a very brief (random) time (up to 2 units) to check an exam. FCFS.
    		
    		int grade = rand.nextInt(100 + 1);
    		msg("Gave " + line.get(0).getName() + " a " + grade);
    		//After each exam is checked, the instructor will assign a grade and notify the student.
    			
    		line.get(0).grades.add(grade);
    		line.remove(0);
    			
    		Object notOb = grading.get(0);
    		grading.remove(notOb);
    			
    		synchronized (notOb) {
    			notOb.notify();
    		}
		}
    }
    
    public void run() {
    	sleepRandom(5, "Commuting");
    	
    	while (administered < administeredMax) {//Four exams are administrated throughout the day. administeredMax = 4
	    	waiting = 0;
	    		
	    	synchronized (outside) {
		    	outside.notifyAll();
	    	}
	    		
	    	msg("Classroom opened. Waiting for students to be seated.");
	    		
	    	try {
	    		synchronized (teacher) {
	    			teacher.wait(1000);
	    		}
			} 
	    		
	    	catch (InterruptedException e) {
				e.printStackTrace();
			}
	    		
	    	synchronized (inside) {
	    		inside.notify();
	    	}
	    		
	    	if (current != 0) {
	    		synchronized (teacher) {
	    			wait(teacher);
	    		}
	    	}
	    		
	    	msg("Handing out exams.");
	    		
	    	waiting = 0;
	    		
		    for (int i = 0; i < numTables; i++) {
		    	synchronized (tables.get(i)) {
		    		tables.get(i).notifyAll();
		    	}
	    	}//When it’s time to start the exam, the instructor hands out the exams (by signaling the students)
	    		
	    	if (current != 0) {
	    		synchronized (teacher) {
	    			wait(teacher);
	    		}
	    	}
	    		
	    	msg("Setting timer. Test has begun.");
	    		
	    	synchronized (exam) {
	    		exam.notify();
	    		wait(exam);
	    	}
	    		
	    	synchronized (timer.examInProgress) {
	    		timer.examInProgress.notifyAll();
	    	}
	    		
	    	waiting = 0;
	    		
	    	if (current != 0) {
	    		synchronized (teacher) {
	    			wait(teacher);
	    		}
	    	}
	    		
	    	while (current > 0) {
	    		gradeTest();
	    	}
	    		
	    	msg("Administered " + administered + " exam(s)");
	    	
	    	if (administered != 4) {	    	
	    		sleep(5, "Prepping for next exam");//The exams are scheduled every 2 hours
	    	}
	    }//The instructor will terminate after all four exams are administered.
    	
    	while (arrived != 0) {
	    	synchronized (outside) {	
	    		outside.notifyAll();
			}
	    	
	    	try {
	    		synchronized (teacher) {
	    			teacher.wait(1000);
	    		}
			} 
	    	
	    	catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	
    	msg("Survived exam day!");
    }
}
