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

import utd.persistentDataStore.utils.StreamUtil;

public class ReverseHandler extends Handler
{
	public void run() throws IOException
	{
		// Read message
		String inMessage = StreamUtil.readLine(inputStream);
		System.out.println("inMessage: " + inMessage);

		// Write response
		String outMessage = reverse(inMessage) + "\n";
		StreamUtil.writeLine(outMessage, outputStream);
		System.out.println("Finished writing message");
	}

	private String reverse(String data)
	{
		byte dataBuff[] = data.getBytes();
		int buffSize = dataBuff.length;
		byte reverseBuff[] = new byte[buffSize];
		for (int idx = 0; idx < buffSize; idx++) {
			reverseBuff[idx] = dataBuff[(buffSize - idx) - 1];
		}
		return new String(reverseBuff);
	}

}
