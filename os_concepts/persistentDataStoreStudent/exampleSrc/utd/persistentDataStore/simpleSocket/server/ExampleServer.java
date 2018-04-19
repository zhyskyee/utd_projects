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
 
package utd.persistentDataStore.simpleSocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import utd.persistentDataStore.utils.ServerException;
import utd.persistentDataStore.utils.StreamUtil;

public class ExampleServer
{
	static public final int port = 10023;

	public void startup() throws IOException
	{
		System.out.println("Starting Service at port " + port);

		InputStream inputStream = null;
		OutputStream outputStream = null;

		ServerSocket serverSocket = new ServerSocket(port);
		
		while (true) {
			try {
				System.out.println("Waiting for request");
				Socket clientSocket = serverSocket.accept();

				System.out.println("Request received");
				inputStream = clientSocket.getInputStream();
				outputStream = clientSocket.getOutputStream();
				Handler handler = parseRequest(inputStream);
				
				System.out.println("Processing Request: " + handler);
				handler.setInputStream(inputStream);
				handler.setOutputStream(outputStream);
				handler.run();
				
				StreamUtil.closeSocket(inputStream);
			}
			catch (ServerException ex) {
				System.out.println("Problem processing request. " + ex.getMessage());
				StreamUtil.sendError(ex.getMessage(), outputStream);
				StreamUtil.closeSocket(inputStream);
			}
			catch (IOException ex) {
				System.out.println("Exception while processing request. " + ex.getMessage());
				ex.printStackTrace();
				StreamUtil.closeSocket(inputStream);
			}
		}
	}

	private Handler parseRequest(InputStream inputStream) throws IOException, ServerException
	{
		String commandString = StreamUtil.readLine(inputStream);

		if ("echo".equalsIgnoreCase(commandString)) {
			Handler handler = new EchoHandler();
			return handler;
		}
		else if ("reverse".equalsIgnoreCase(commandString)) {
			Handler handler = new ReverseHandler();
			return handler;
		}
		else {
			throw new ServerException("Unknown Request: " + commandString);
		}
	}

	public static void main(String args[])
	{
		try {
			ExampleServer simpleServer = new ExampleServer();
			simpleServer.startup();
		}
		catch (IOException ex) {
			System.out.println(ex);
			ex.printStackTrace();
		}
	}
}
