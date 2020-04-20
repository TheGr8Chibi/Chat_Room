package main;

import java.net.*;
import java.io.*;

public class Server {

	static Thread read;
	static Thread connect;

	// Socket and data streams
	static Socket socket;
	// static ServerSocket server;
	static DataInputStream input;
	static DataOutputStream output;

	final static int port = 5006;

	public static void main(String[] args) throws IOException {

		// connect = new Thread(new connect());
		// connect.start();

		ServerSocket server = new ServerSocket(port);
		
		while (true) {
			try {
				socket = server.accept();
				System.out.println("Client connected");

				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());

				read = new Thread(new read(socket, input, output));
				read.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void broadcast() {
		try {
			output.writeUTF("hello");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public class read implements Runnable {
		DataInputStream in;
		DataOutputStream out;
		Socket s;

		public read(Socket socket, DataInputStream inR, DataOutputStream outR) {
			in = inR;
			out = outR;
			s = socket;
		}

		@Override
		public void run() {
			while (true) {
				try {
					broadcast();
					String inr = in.readUTF();
					out.writeUTF(inr);
					System.out.println(inr);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
}
