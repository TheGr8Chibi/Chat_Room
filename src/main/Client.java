package main;

import java.net.*;
import java.util.Scanner;
import java.io.*;

public class Client {

	// Data threads
	static Thread sendMessage;
	static Thread readMessage;

	// User input
	static Scanner scanner = new Scanner(System.in);

	// Socket and data streams
	static Socket socket;
	static DataInputStream input;
	static DataOutputStream output;

	public static void main(String[] args) {

		// Port number for socket connection
		final int port = 5006;

		try {
			// Gets localhost IP
			InetAddress IP = InetAddress.getByName("localhost");

			// Setting up connection to server
			socket = new Socket(IP, port);
			System.out.println("Connected to server at " + IP + " on port " + port);

			// Initiate data streams
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Start thread for receiving messages
		readMessage = new Thread(new readMessage());
		readMessage.start();

		// Start thread for sending messages
		sendMessage = new Thread(new sendMessage());
		sendMessage.start();
	}

	static public class sendMessage implements Runnable {
		public void run() {
			while (true) {
				// Read user input
				String userIn = scanner.nextLine();
				try {
					// Send user input
					output.writeUTF(userIn);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	static public class readMessage implements Runnable {
		public void run() {
			while (true) {
				try {
					// Read and print incoming data
					System.out.println("READ");
					String incoming = input.readUTF();
					System.out.println(incoming);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
