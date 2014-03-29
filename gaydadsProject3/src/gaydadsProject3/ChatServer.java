/**
 * 
 */
package gaydadsProject3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David
 *
 */
public class ChatServer extends Thread {

	
	ServerSocket serverSocket = null;
	//So all threads can access and update the server HashMap
	//(Maybe could be done better, but couldn't find neat alternative.)
	public static ConcurrentHashMap<String, InetSocketAddress> clientMap;
	DataOutputStream dos;
	DataInputStream dis;
	Socket clientSocket = null;
	// Port number for server to listen on
	final int SERVER_PORT = 32100;
	/*Flag for controlling listening loop (Could be set to false to cause the server to stop listening for clients*/
	private boolean isListening = true;
	// Counter for tracking number of clients

	/**
	 * Sets up ServerSocket to listen for requests
	 */
	public ChatServer() {

		clientMap = new ConcurrentHashMap<String, InetSocketAddress>();
		try {
			serverSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Display socket information.
		displayContactInfo();

		// Enter infinite loop to listen for clients
		try {
			listenForClients();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	/**
	 * Displays contact info for the client connection... Taken from Lab 2 instructions
	 */
	protected void displayContactInfo() {

		try { 
			// Display contact information. 
			System.out.println( 
					"Number Server standing by to accept Clients:" 

				 + "\nIP : " + InetAddress.getLocalHost() 
				 + "\nPort: " + serverSocket.getLocalPort() 
				 + "\n\n" ); 

		} catch (UnknownHostException e) { 
			// NS lookup for host IP failed? 
			// This should only happen if the host machine does 
			// not have an IP address. 
			e.printStackTrace(); 
		}
	}

	/**
	 * Infinite Loop to listen for requests
	 * @throws IOException
	 */
	protected void listenForClients() throws IOException
	{
		while (isListening) {

			// Create new thread to hand each client.
			// Pass the Socket object returned by the accept 
			// method to the thread.

			new ChatServerThread(serverSocket.accept()).start();

		}

	} // end listenForClients

	/**
	 * This method could be used to close the server.
	 */
	public void closeServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new ChatServer();
	}

}
