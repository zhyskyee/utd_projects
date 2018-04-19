/* NOTICE: All materials provided by this project, and materials derived 
 * from the project, are the property of the University of Texas. 
 * Project materials, or those derived from the materials, cannot be placed 
 * into publicly accessible locations on the web. Project materials cannot 
 * be shared with other project teams. Making project materials publicly 
 * accessible, or sharing with other project teams will result in the 
 * failure of the team responsible and any team that uses the shared materials. 
 * Sharing project materials or using shared materials will also result 
 * in the reporting of all team members for academic dishonesty. 
 */ 
 
package utd.persistentDataStore.simpleSocket.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import utd.persistentDataStore.datastoreClient.ConnectionException;
import utd.persistentDataStore.utils.StreamUtil;

public class ExampleClient
{
	private InetAddress address;
	private int port;

	public ExampleClient(InetAddress address, int port)
	{
		this.address = address;
		this.port = port;
	}

	/**
	 * Sends the given string to the server which will echo it back
	 */
	public String echo(String message) throws ConnectionException
	{
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Writing Message");
			StreamUtil.writeLine("echo\n", outputStream);
			StreamUtil.writeLine(message, outputStream);
			
			System.out.println("Reading Response");
			String result = StreamUtil.readLine(inputStream);
			System.out.println("Response " + result);
			
			return result;
		}
		catch (IOException ex) {
			throw new ConnectionException(ex.getMessage(), ex);
		}
	}
		
	/**
	 * Sends the given string to the server which will echo it back
	 */
	public String reverse(String message) throws ConnectionException
	{
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Writing Message");
			StreamUtil.writeLine("reverse\n", outputStream);
			StreamUtil.writeLine(message, outputStream);
			
			System.out.println("Reading Response");
			String result = StreamUtil.readLine(inputStream);
			System.out.println("Response " + result);
			
			return result;
		}
		catch (IOException ex) {
			throw new ConnectionException(ex.getMessage(), ex);
		}
	}

}
