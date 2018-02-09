//Preston Peck
//Distributed Systems 344
//Prof. Fluture
//Project 1: Monitors
//April 15, 2017

import java.util.Vector;

public class main {
	public static Vector<Student> students = new Vector<Student>();
	public static Vector<Teacher> teachers = new Vector<Teacher>();
	
	public static void main(String args[]) {
		try {
			int numStudents = Integer.valueOf(args[0]);
			int numTeachers = 1;
			int numTimers = 1;
			int capacity = Integer.valueOf(args[1]);
			int numSeats = Integer.valueOf(args[2]);
			
			for (int i = 0; i < numTeachers; i++) {
				teachers.add(new Teacher(i + 1, capacity, numSeats));
				teachers.get(i).start();
			}
			
			for (int i = 0; i < numStudents; i++) {
				students.add(new Student(i + 1, teachers.get(0)));
				students.get(i).start();
			}
		}
		
		catch (Exception e) {
			System.out.println(e);
			System.out.println("3 arguments required: numStudents, capacity, numSeats");
		}
	}
}