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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

/**
 * @author David
 *
 */
public class ChatPeer implements ChatPeerInterfaceListener {


	public static String serverIpAddress = "127.0.0.1";
	static final int SERVER_PORT = 32100;
	//These are used to set up the connection between client and server later
	Socket clientSocket = null;
	DataOutputStream dos = null;
	DataInputStream dis = null;
	InetAddress peerServerIp = null;
	InetSocketAddress peerISA;
	ServerSocket peerSocket = null;
	String serverIpStrg;
	String peerScreenName;
	//For ServerSocketLoop
	boolean isListening;
	ChatPeerInterface face;
	ArrayList<ChatThread> clientConnections;
	ConcurrentHashMap<String, InetSocketAddress> peerMap;

	public ChatPeer() throws IOException {

		isListening = true;
		clientConnections = new ArrayList<ChatThread>();
		peerMap = new ConcurrentHashMap<String, InetSocketAddress>();

		//Set Up Peer
		// Ask the user for a screen name.
		peerScreenName = JOptionPane.showInputDialog("Enter Screen Name: ");
		createPeerServerSocket();
		createPeerISA();
		getServerIP();
		addPeerToServerDatabase();
		createChatPeerInterfaceGUI();
		
		//Loop to listen for Connecting Peers
		listenForPeers();

		//Close Peer
		closeSocketAndStreams();
		peerSocket.close();

	}


	/**
	 * This method uses the peer's Socket and ServerSocket to listen for other peers to chat with
	 * until the peer exits the network.
	 * @throws IOException
	 */
	protected void listenForPeers() {
		//Semi-Infinite Loop. Once ChatGUI is closed, isListening == false
		try {
			//Needed to make sure loop functions and checks isListening variable every 10ms
			peerSocket.setSoTimeout(10);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			while (isListening) {

				try {
					// Receive connections from other clients
					Socket s = peerSocket.accept();
					// Make a thread to take to the client
					clientConnections.add( new ChatThread(peerScreenName, s )); 
				}
				catch (SocketTimeoutException e) {
					// Loop back up to check the isListening
				} 
			}
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//End ServerSocket Functions and Remove self from Server DB
		closeThreads();
		removePeerFromServerDatabase();
		closePeerServerSocket();

	}

	/**
	 * This method is used to close the peer listening ServerSocket
	 */
	private void closePeerServerSocket() {
		try {
			peerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * This method creates the peer ServerSocket to listen to other peers
	 */
	protected void createPeerServerSocket() {

		try {
			peerSocket = new ServerSocket(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method creates the PeerISA to share with the server
	 */
	protected void createPeerISA() {
		peerISA = new InetSocketAddress(peerSocket.getInetAddress(), peerSocket.getLocalPort());
	}

	/**
	 * This method could be used to enter another ServerIp, but I assume the server is always on the local machine
	 */
	protected void getServerIP() {

		serverIpStrg = serverIpAddress;
		System.out.println("\nServer IP is " + serverIpStrg);

		try {
			peerServerIp = InetAddress.getByName(serverIpStrg);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to connect to the server
	 */
	protected void connectToServer() {

		try {
			clientSocket = new Socket(peerServerIp, SERVER_PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method creates the input and output streams needed.
	 */
	protected void createOutputAndInputStream() {
		try {
			dos = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dis = new DataInputStream(clientSocket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method sends a message (1) to the server prompting that it wants to add itself to the Database
	 * (This is only called when the peer first connects)
	 */
	protected void addPeerToServerDatabase() {
		connectToServer();
		createOutputAndInputStream();
		byte [] ip = peerISA.getAddress().getAddress();

		try {
			dos.writeInt(1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			dos.writeUTF(peerScreenName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dos.write(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			dos.writeInt(peerISA.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeSocketAndStreams();
	}

	/**
	 * This method contacts the server and prompts it to remove the peer from the database
	 * (This is only called when the peer exits the ChatPeerInterface)
	 */
	protected void removePeerFromServerDatabase() {
		connectToServer();
		createOutputAndInputStream();
		try {
			dos.writeInt(2);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			dos.writeUTF(peerScreenName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		closeSocketAndStreams();
	}

	/**
	 * This creates a new ChatPeerInterfaceGUI attached to the ChatPeer
	 */
	protected void createChatPeerInterfaceGUI() {
		
		face = new ChatPeerInterface(this, peerScreenName );
	
	}

	/**
	 * This method updates the interface after the Server supplies network updates
	 */
	protected void updateInterfaceGUI() {

		int i = 0;
		face.clearList();

		for (Entry<String, InetSocketAddress> e : peerMap.entrySet()) {
			face.addFriendToList(e.getKey(), i);
			i++;
		}

	}

	/**
	 * This method prompts the server for information.
	 */
	protected void getInfoFromServer() {
		connectToServer();
		createOutputAndInputStream();

		//Clear the current ConcurrentHashMap if it contains elements
		if (!peerMap.isEmpty()) {
			peerMap.clear();
		}

		//Send Server 3, requesting a list of peers and their info
		try {
			dos.writeInt(3);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int numberOfPeersAvailable = 0;

		//Get the number of peers to be shared from the Server
		try {
			numberOfPeersAvailable = dis.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getPeersFromServer(numberOfPeersAvailable);
		
		closeSocketAndStreams();
	}
	
	protected void getPeersFromServer(int numberOfPeersAvailable) {
		
		//For the number of peers, create loop to receive the information
				//System.out.println(numberOfPeersAvailable);
				for (int i = 0; i < numberOfPeersAvailable; i++) {
					String peerScreenName = "";
					byte [] ip = new byte[4];
					int peerPort = 0;

					try {
						peerScreenName = dis.readUTF().toString();

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

					//Create an ISA to store with the peer's screen name
					peerISA = new InetSocketAddress(peerIp, peerPort);

					//Store new list of peers in ConcurrentHashMap
					peerMap.put(peerScreenName, peerISA);
				}
	}


	/**
	 * This method closes the client socket and streams
	 */
	protected void closeSocketAndStreams() {

		try {
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			dos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method creates a new chat thread based on the peer double clicked to chat with
	 */
	@Override
	public void contactFriend(String friendName, int friendIndex) {
		try {
			clientConnections.add( new ChatThread(peerScreenName, new Socket(peerMap.get(friendName).getAddress(), peerMap.get(friendName).getPort())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method closes all the open threads when the peer exits the ChatPeerInterface
	 */
	protected void closeThreads() {
		for (int i = 0; i < clientConnections.size(); i ++) {
			clientConnections.get(i).finish();
		}
	}

	/**
	 * This method stops the peer ServerSocket from listening and finishes the current threads
	 */
	@Override
	public void quit() {
		isListening = false;
		System.out.println("Unregister with the server, close connections and clean up " +
				"while the GUI disposes itself." );

	}

	/**
	 * This method contacts the server and updates the list of peers on the PeerInterfaceGUI
	 */
	@Override
	public void updateFriendList() {
		getInfoFromServer();
		updateInterfaceGUI();

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new ChatPeer();

	}



}
