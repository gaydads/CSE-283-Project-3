/**
 * 
 */
package gaydadsProject3;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author David
 *
 */
public class ChatServerThread extends Thread{

	// Integer ID for the client (Not Used)
	//protected int clientNumber;
	private Socket clientSocket;
	private DataOutputStream dos;
	private DataInputStream dis;


	public ChatServerThread(Socket clientSocket ) {
		// Call the super class (Thread) constructor.
		super("ChatServerThread_");
		// Save a reference to the Socket connection 
		// to the client.
		this.clientSocket = clientSocket;

		// Save the ID for the client
		//this.clientNumber = clientNumber;

	}

	public void run() {
		//Unneeded Method, Left in for Debugging.
		//displayClientInfo();
		
		createClientStreams();
		handleClient();
		closeClientConnection();
	}

	/* Displays IP address and port number information 
	 * associated with a particular client.
	 */
	protected void displayClientInfo()
	{	
		// Display IP address and port number client is using.
		System.out.println( "Client "
				+ " IP address: " + clientSocket.getInetAddress()
				+ "\nClient Port number: " + clientSocket.getPort() );

	} // end displayClientInfo

	/**
	 * This method creates input and output streams for client
	 */
	protected void createClientStreams() {

		try {
			dis = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			dos = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void handleClient() {

		int clientRequestType = 0;

		try {
			clientRequestType = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (clientRequestType == 1) {
			addClientToDataBase();

		}
		else if (clientRequestType == 2) {
			removeClientFromDatabase();
		}
		else if (clientRequestType == 3) {

			handleInfoRequest();
		}
		else {

		}


		//For Debugging Purposes
		//System.out.println("Handled Request Type:" + clientRequestType);
		//System.out.println("Handled Client" + clientNumber);
	}

	protected void handleInfoRequest() {

		//Send number of peers available
		try {
			//System.out.println(ChatServer.clientMap.size());
			dos.writeInt(ChatServer.clientMap.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Entry<String, InetSocketAddress> e : ChatServer.clientMap.entrySet()) {
			
			try {
				//System.out.println(e.getKey());
				dos.writeUTF(e.getKey());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			byte [] ip = e.getValue().getAddress().getAddress();
			
			try {
				dos.write(ip);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			try {
				dos.writeInt(e.getValue().getPort());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			//System.out.println(e.getKey() + "= IP: " + e.getValue().getAddress() + " Port: " + e.getValue().getPort());
		}

	}

	protected void addClientToDataBase() {
		String peerScreenName = "";
		byte [] ip = new byte[4];
		int peerPort = 0;

		try {
			peerScreenName = dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dis.read(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InetAddress peerIp = null;
		try {
			peerIp = InetAddress.getByAddress(ip);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			peerPort = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InetSocketAddress peerISA = new InetSocketAddress(peerIp, peerPort);

		ChatServer.clientMap.put(peerScreenName, peerISA);
		
		System.out.println("Peer Added to DB. Current Network: " + ChatServer.clientMap.toString());
		
	}
	
	protected void removeClientFromDatabase() {
		String peerScreenName = "";

		try {
			peerScreenName = dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ChatServer.clientMap.remove(peerScreenName);
		
		System.out.println("Peer Removed! Current Network: " + ChatServer.clientMap.toString());
		
	}

	/**
	 * This method closes the client's connection by closing the client socket
	 * datainput and output streams
	 */
	protected void closeClientConnection() {
		try {
			dis.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			dos.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





}
