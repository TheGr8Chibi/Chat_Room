package main;

import java.net.*;
import java.util.Vector;
import java.io.*;
import java.util.ArrayList;

public class Server {

	// Thread for handling clients and accepting new connections
	static Thread t;
	static Thread connect;

	// Socket and data streams
	static Socket socket;
	static ServerSocket server;
	static DataInputStream input;
	static DataOutputStream output;

	// Connection port number
	final static int port = 5000;

	// Vector with clients
	static Vector<client> clients = new Vector<>();

	// List for thread ID's
	static ArrayList<Long> TS = new ArrayList<Long>();

	// List for usernames
	static ArrayList<String> userNames = new ArrayList<String>();

	public static void main(String[] args) throws IOException {

		// Initiate server
		server = new ServerSocket(port);

		// Starting thread for accepting clients
		connector c = new connector();
		Thread srv = new Thread(c);
		srv.start();
		System.out.println("Server started");
	}

	static public class connector implements Runnable {
		public void run() {
			while (true) {
				try {
					// Connects new clients with the server
					socket = server.accept();

					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());
					// Get username
					String userN = input.readUTF().trim();

					// Checks if there are any duplicates with existing usernames
					int duplicateId = 1;
					for (String un : userNames) {
						if (userN.equals(un)) {
							duplicateId++;
						}
					}

					// If duplicate exists, name will get an ID appended
					if (duplicateId != 1) {
						userN += "#" + duplicateId;
					}

					// Username added to username list
					userNames.add(userN);

					System.out.println();
					System.out.println("Client connected: " + userN);
					System.out.println();

					// Initiate new handler for client and starts thread for client
					client s = new client(socket, input, output, clients.size(), userN);
					clients.add(s);
					t = new Thread(s);
					t.start();
					output.writeUTF(
							"Connected to server at " + socket.getInetAddress() + " on port " + port + " as " + userN);
					// Add thread to arraylist
					TS.add(t.getId());

				} catch (IOException e) {
					// e.printStackTrace();
				}
			}
		}
	}

	static public class client implements Runnable {
		DataInputStream in;
		DataOutputStream out;
		Socket s;
		String un;
		int id;

		public client(Socket socket, DataInputStream inR, DataOutputStream outR, int idR, String userName) {
			in = inR;
			out = outR;
			s = socket;
			id = idR;
			un = userName;
		}

		@Override
		public void run() {

			while (true) {
				try {
					String inr = in.readUTF();
					// Send message to all clients
					if (!inr.isEmpty()) {
						inr = un + ": " + inr;
						for (client r : Server.clients) {
							if (r.id != id) {
								try {
									r.out.writeUTF(inr);
								} catch (IOException e) {
									e.printStackTrace();
									break;
								}
							}
						}
						System.out.println(inr);
					}
				} catch (IOException e) {
					// e.printStackTrace();
					removeClient(s, in, out, id, un);
					break;
				}
			}

		}
	}

	public static void removeClient(Socket s, DataInputStream in, DataOutputStream out, int id, String userName) {
		// Iterates through all threads
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			// Checks if thread ID is same as ID for thread to be stopped
			if (t.getId() == TS.get(id) && TS.size() != 0) {
				// Stops thread and removes from arraylist
				t.interrupt();
				TS.remove(id);
				// Close socket and data streams
				try {
					s.close();
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				// Remove client from clients vector
				clients.remove(id);

				// Remove username from list
				userNames.remove(userName);

				// Decreases the ID value for all clients above the removed one with 1
				for (client r : Server.clients) {
					if (r.id > id && r.id != 0) {
						r.id--;
					}
				}
				System.out.println();
				System.out.println("Client " + userName + " disconnected");
				System.out.println("Clients connected: " + clients.size());
				System.out.println();
				break;
			}
		}
	}

}
