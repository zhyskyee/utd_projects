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
 
package utd.persistentDataStore.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import utd.persistentDataStore.datastoreClient.ClientException;

/**
 * This class provides services used to read & write data to 
 * streams in various formats needed to implement the 
 * project services.
 */
public class StreamUtil
{
	/**
	 * Reads and returns the string without newline. 
	 * ByteArrayOutputStream is a very handy method of collecting 
	 * bytes when the length is unknown.
	 */
	public static String readLine(InputStream inputStream) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		for(int idx = 0; idx < 1000; idx++) {
			int ch = inputStream.read();
			if(ch == '\n' || ch == -1) {
				String result = baos.toString();
				return result;				
			}
			else {
				baos.write(ch);
			}
		}
		System.err.println("************************");
		throw new IOException ("No NewLine seen after 1000 characters");
	}
	
	/**
	 * Reads and returns the next N bytes of data from the stream
	 * in a byte array.
	 */
	public static byte[] readData(int length, InputStream inputStream) throws IOException
    {
	    byte bytes[] = new byte[length];
	    for(int idx = 0; idx < length; idx++) {
	    	int ch = inputStream.read();
	    	bytes[idx] = (byte)ch;
	    }
	    return bytes;
    }

	public static void writeData(byte data[], OutputStream outputStream) throws IOException
	{
		outputStream.write(data);
		outputStream.flush();
	}

	public static void writeLine(String line, OutputStream outputStream) throws IOException
	{
		if(!line.endsWith("\n")) {
			line = line + "\n";
		}
		outputStream.write(line.getBytes());
		outputStream.flush();
	}

	/**
	 * Close the socket by closing its input stream;
	 */
	public static void closeSocket(InputStream inputStream) 
	{
		if(inputStream != null) {
			try {
	            inputStream.close();
            }
            catch (IOException ex) {
            	System.out.println(ex);
            	ex.printStackTrace();
            }
		}
	}
	
	public static void sendError(String errMsg, OutputStream outputStream) 
    {
		if(!errMsg.endsWith("\n")) {
			errMsg = errMsg + "\n";
		}
		
		try {
	        outputStream.write(errMsg.getBytes());
	        outputStream.flush();
        }
        catch (IOException ex) {
        	System.out.println(ex.getMessage());
        	ex.printStackTrace();
        }
    }

}
