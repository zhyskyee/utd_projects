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
 
package utd.persistentDataStore.datastoreClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import utd.persistentDataStore.utils.StreamUtil;

public class DatastoreClientImpl implements DatastoreClient
{
	private InetAddress address;
	private int port;

	public DatastoreClientImpl(InetAddress address, int port)
	{
		this.address = address;
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see utd.persistentDataStore.datastoreClient.DatastoreClient#write(java.lang.String, byte[])
	 */
	@Override
    public void write(String name, byte data[]) throws ClientException, ConnectionException
	{
		// Replace with implementation
		// throw new RuntimeException("Executing Write Operation");
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Executing Write Operation");
			StreamUtil.writeLine("write\n", outputStream);
			StreamUtil.writeLine(name + "\n", outputStream);
			StreamUtil.writeLine(String.valueOf(data.length) + "\n", outputStream);
			StreamUtil.writeData(data, outputStream);
			
			System.out.println("Reading Response");
			String responseCode = StreamUtil.readLine(inputStream);
			if ("OK".equalsIgnoreCase(responseCode)) {
				System.out.println("Response " + responseCode);
			} else {
				System.out.println("Response Error " + responseCode);
				throw new ClientException(responseCode);
			}
		}
		catch (Exception ex) {
			throw new ClientException(ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see utd.persistentDataStore.datastoreClient.DatastoreClient#read(java.lang.String)
	 */
	@Override
    public byte[] read(String name) throws ClientException, ConnectionException
	{
		// Replace with implementation
		// throw new RuntimeException("Executing Read Operation");
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Executing Read Operation");
			StreamUtil.writeLine("read\n", outputStream);
			StreamUtil.writeLine(name + "\n", outputStream);
			
			System.out.println("Reading Response");
			String responseCode = StreamUtil.readLine(inputStream);
			if ("OK".equalsIgnoreCase(responseCode)) {
				System.out.println("Response " + responseCode);
				String lengthString = StreamUtil.readLine(inputStream);
				int length = Integer.valueOf(lengthString);
				byte[] data = StreamUtil.readData(length, inputStream);
				
				return data;
			} else {
				System.out.println("Response Error " + responseCode);
				throw new ClientException(responseCode);
			}
		}
		catch (Exception ex) {
			throw new ClientException(ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see utd.persistentDataStore.datastoreClient.DatastoreClient#delete(java.lang.String)
	 */
	@Override
    public void delete(String name) throws ClientException, ConnectionException
	{
		// Replace with implementation
		// throw new RuntimeException("Executing Delete Operation");
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Executing Delete Operation");
			StreamUtil.writeLine("delete\n", outputStream);
			StreamUtil.writeLine(name + "\n", outputStream);
			
			System.out.println("Reading Response");
			String responseCode = StreamUtil.readLine(inputStream);
			if ("OK".equalsIgnoreCase(responseCode)) {
				System.out.println("Response " + responseCode);
			} else {
				System.out.println("Response Error " + responseCode);
				throw new ClientException(responseCode);
			}
		}
		catch (Exception ex) {
			throw new ClientException(ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see utd.persistentDataStore.datastoreClient.DatastoreClient#directory()
	 */
	@Override
    public List<String> directory() throws ClientException, ConnectionException
	{
		// Replace with implementation
		// throw new RuntimeException("Executing Directory Operation");
		try {
			System.out.println("Opening Socket");
			Socket socket = new Socket();
			SocketAddress saddr = new InetSocketAddress(address, port);
			socket.connect(saddr);
			InputStream inputStream = socket.getInputStream();
			OutputStream outputStream = socket.getOutputStream();
			
			System.out.println("Executing Directory Operation");
			StreamUtil.writeLine("directory\n", outputStream);
			
			System.out.println("Reading Response");
			String responseCode = StreamUtil.readLine(inputStream);
			if ("OK".equalsIgnoreCase(responseCode)) {
				System.out.println("Response " + responseCode);
				String lengthString = StreamUtil.readLine(inputStream);
				int length = Integer.valueOf(lengthString);
				List<String> directory = new ArrayList<String>();
				for (int i = 0; i < length; i++) {
					String dir = StreamUtil.readLine(inputStream);
					directory.add(dir);
				}
				return directory;
			} else {
				System.out.println("Response Error " + responseCode);
				throw new ClientException(responseCode);
			}
		}
		catch (Exception ex) {
			throw new ClientException(ex.getMessage(), ex);
		}
	}

}
