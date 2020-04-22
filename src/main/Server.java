package main;

import java.net.*;
import java.util.Vector;
import java.io.*;
import java.util.ArrayList; 

public class Server {

	static Thread t;
	static Thread connect;

	// Socket and data streams
	static Socket socket;
	static ServerSocket server;
	static DataInputStream input;
	static DataOutputStream output;

	final static int port = 5006;
	
	//Vector with clients
	static Vector<client> clients = new Vector<>();
	
	//List for thread ID's
	static ArrayList<Long> TS = new ArrayList<Long>();
	
	public static void main(String[] args) throws IOException {

		server = new ServerSocket(port);

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
					socket = server.accept();
					System.out.println("Client " + clients.size() + " connected");
					System.out.println();
					
					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());

					client s = new client(socket, input, output, clients.size());
					clients.add(s);
					t = new Thread(s);
					t.start();
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
		for (Thread t : Thread.getAllStackTraces().keySet()) {
			if (t.getId() == TS.get(id) && TS.size() != 0) {
				t.interrupt();
				TS.remove(id);
				try {
					s.close();
					in.close();
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				clients.remove(id);

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
