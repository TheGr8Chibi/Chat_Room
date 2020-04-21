package main;

import java.net.*;
import java.util.Vector;
import java.io.*;

public class Server {

	static Thread t;
	static Thread connect;

	// Socket and data streams
	static Socket socket;
	static ServerSocket server;
	static DataInputStream input;
	static DataOutputStream output;

	final static int port = 5006;
	static Vector<read> ar = new Vector<>();
	static int clientCount = 1;

	public static void main(String[] args) throws IOException {

		server = new ServerSocket(port);

		connector c = new connector();
		Thread srv = new Thread(c);
		srv.start();
	}

	static public class connector implements Runnable {
		public void run() {
			while (true) {
				try {
					socket = server.accept();
					System.out.println("Client connected");

					input = new DataInputStream(socket.getInputStream());
					output = new DataOutputStream(socket.getOutputStream());

					read s = new read(socket, input, output, clientCount);
					ar.add(s);
					t = new Thread(s);
					t.start();
					clientCount++;
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		}
	}

	static public class read implements Runnable {
		DataInputStream in;
		DataOutputStream out;
		Socket s;
		int id;

		public read(Socket socket, DataInputStream inR, DataOutputStream outR, int id) {
			in = inR;
			out = outR;
			s = socket;
			this.id = id;
		}

		@Override
		public void run() {

			while (true) {
				try {
					String inr = in.readUTF();
					// Send message to all clients
					for (read r : Server.ar) {
						if (r.id != id) {
							try {
								r.out.writeUTF(inr);
							} catch (IOException e) {
								//e.printStackTrace();
							}
						}
					}
					System.out.println(inr);
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}

		}
	}

}
