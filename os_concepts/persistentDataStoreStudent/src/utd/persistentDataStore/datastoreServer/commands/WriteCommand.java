package utd.persistentDataStore.datastoreServer.commands;

import java.io.IOException;

import utd.persistentDataStore.utils.StreamUtil;
import utd.persistentDataStore.utils.FileUtil;
import utd.persistentDataStore.utils.ServerException;

public class WriteCommand extends ServerCommand {
	public void run() throws IOException, ServerException
	{
		try {
			// Read command
			String name = StreamUtil.readLine(inputStream);
			String lengthString = StreamUtil.readLine(inputStream);
			int length = Integer.valueOf(lengthString);
			byte[] data = StreamUtil.readData(length, inputStream);
			System.out.println("Finished reading command");
			
			// Write data
			FileUtil.writeData(name, data);
			System.out.println("Finished writing data");

			// Write response
			this.sendOK();
		}
		catch (Exception ex) {
			throw new ServerException("Exception while processing request. " + ex.getMessage());
		}
	}
}
