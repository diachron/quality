/**
 * 
 */
package de.unibonn.iai.eis.diachron.io.utilities;

import java.util.Scanner;

/**
 * @author Carlos
 *
 */
public class Menus {

	private static Scanner scanner;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method displays the main menu
	 * @return
	 */
	public static int menuMain(){
		int value = 0;
		scanner = new Scanner(System.in);

		System.out
				.println("############################################################################################");
		System.out
				.println("############################################################################################");
		System.out
				.println("##################       ENTERPRISE INFORMATION SYSTEM - LAB SS14      #####################");
		System.out
				.println("############################################################################################");
		System.out
				.println("##################                    DEVELOPED BY:                    #####################");
		System.out
				.println("############################################################################################");
		System.out
				.println("##################            Carlos Montoya      - 2599128            #####################");
		System.out
				.println("############################################################################################");
		System.out
				.println("############################################################################################");
		System.out
				.println("##################     Please Choose one of the next values            #####################");
		System.out
				.println("##################           for the given OPTIONS:                    #####################");
		System.out
				.println("############################################################################################");
		System.out
				.println("##################        1. Evaluate a Data Set                       #####################");
		System.out
				.println("##################        2. Check Metrics for the Given Data sets     #####################");
		System.out
				.println("##################        3. Exit                                      #####################");
		System.out
				.println("############################################################################################");

		value = scanner.nextInt();
		System.out.println("\n\n\n");
		return value;
	}
	
	
	/**
	 * This method displays the main menu
	 * @return
	 */
	public static String menuUrl(){
		String value = "";
		scanner = new Scanner(System.in);

		System.out
				.println("############################################################################################");
		System.out
				.println("#########     Please write the url of the SPARQL endpoint to be evaluated      #############");
		System.out
				.println("############################################################################################");
		value = scanner.next();
		System.out.println("\n\n\n");
		return value;
	}
	
	/**
	 * This method displays the main menu
	 * @return
	 */
	public static String menuMail(){
		String value = "";
		scanner = new Scanner(System.in);

		System.out
				.println("############################################################################################");
		System.out
				.println("#########     Please write the mail to send when it finish the process to be evaluated  ####");
		System.out
				.println("############################################################################################");
		value = scanner.next();
		System.out.println("\n\n\n");
		return value;
	}
	
	
	/**
	 * Message of good bye
	 */
	public static void exitMsg() {
		System.out
				.println("############################################################################################");
		System.out
				.println("##################          Thank you for use our software             #####################");
		System.out
				.println("############################################################################################");

	}
	
}
