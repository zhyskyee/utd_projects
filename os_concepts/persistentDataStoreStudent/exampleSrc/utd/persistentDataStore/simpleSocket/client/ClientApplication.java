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

import java.net.InetAddress;
import java.net.UnknownHostException;

import utd.persistentDataStore.datastoreClient.ConnectionException;

public class ClientApplication
{
	static public final int port = 10023;

	private void runApp() throws UnknownHostException, ConnectionException
	{
		byte byteAddr[] = { 127, 0, 0, 1 };
		InetAddress address = InetAddress.getByAddress(byteAddr);
		ExampleClient client = new ExampleClient(address, port);
		
		String message = "Now is the time for all good men";
		System.out.println("Calling Echo with " + message );
		String received = client.echo(message);
		System.out.println("Received " + received);
		
		System.out.println("\nCalling Reverse with " + message );
		received = client.reverse(message);
		System.out.println("Received " + received);
		System.out.println("Finished");
	}

	public static void main(String args[])
	{
		try {
			ClientApplication ca = new ClientApplication();
			ca.runApp();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
