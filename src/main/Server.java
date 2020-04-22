package main;

import java.net.*;
import java.util.Vector;
import java.io.*;
import java.util.ArrayList; 

public class Server {

	//Thread for handling clients and accepting new connections
	static Thread t;
	static Thread connect;

	// Socket and data streams
	static Socket socket;
	static ServerSocket server;
	static DataInputStream input;
	static DataOutputStream output;

	//Connection port number
	final static int port = 5000;
	
	//Vector with clients
	static Vector<client> clients = new Vector<>();
	
	//List for thread ID's
	static ArrayList<Long> TS = new ArrayList<Long>();
	
	public static void main(String[] args) throws IOException {

		//Initiate server
		server = new ServerSocket(port);

		//Starting thread for accepting clients
		connector c = new connector();
		Thread srv = new Thread(c);
		srv.start();
		System.out.println("Server started");
		System.out.println();
	}

	static public class connector implements Runnable {
		public void run() {
			while (true) {
				try {
					//Connects new clients with the server
					socket = server.accept();
					System.out.println("Client " + clients.size() + " connected");
					System.out.println();
					
					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());

					//Initiate new handler for client and starts thread for client
					client s = new client(socket, input, output, clients.size());
					clients.add(s);
					t = new Thread(s);
					t.start();
					//Add thread to arraylist
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
		int id;

		public client(Socket socket, DataInputStream inR, DataOutputStream outR, int idR) {
			in = inR;
			out = outR;
			s = socket;
			id = idR;
		}

		@Override
		public void run() {

			while (true) {
				try {
					String inr = in.readUTF();
					// Send message to all clients
					inr = "Client " + id + ": " + inr;
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
				} catch (IOException e) {
					// e.printStackTrace();
					removeClient(s, in, out, id);
					break;
				}
			}

		}
	}

	public static void removeClient(Socket s, DataInputStream in, DataOutputStream out, int id) {
		//Iterates through all threads
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			//Checks if thread ID is same as ID for thread to be stopped
			if (t.getId() == TS.get(id) && TS.size() != 0) {
				//Stops thread and removes from arraylist
				t.interrupt();
				TS.remove(id);
				//Close socket and data streams
				try {
					s.close();
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				//Remove client from clients vector
				clients.remove(id);

				//Decreases the ID value for all clients above the removed one with 1
				for (client r : Server.clients) {
					if (r.id > id && r.id != 0) {
						r.id--;
					}
				}
				System.out.println();
				System.out.println("Client " + id + " disconnected");
				System.out.println("Clients connected: " + clients.size());
				System.out.println();
				break;
			}
		}
	}

}
