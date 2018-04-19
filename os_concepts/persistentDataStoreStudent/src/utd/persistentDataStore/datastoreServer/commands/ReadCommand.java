package utd.persistentDataStore.datastoreServer.commands;

import java.io.IOException;

import utd.persistentDataStore.utils.StreamUtil;
import utd.persistentDataStore.utils.FileUtil;
import utd.persistentDataStore.utils.ServerException;

public class ReadCommand extends ServerCommand {
	public void run() throws IOException, ServerException
	{
		try {
			// Read command
			String name = StreamUtil.readLine(inputStream);
			System.out.println("Finished reading command");
			
			// Fetch data
			byte[] data = FileUtil.readData(name);
			System.out.println("Finished reading data");

			// Write response
			StreamUtil.writeLine("OK\n", outputStream);
			StreamUtil.writeLine(String.valueOf(data.length) + "\n", outputStream);
			StreamUtil.writeData(data, outputStream);
		}
		catch (Exception ex) {
			throw new ServerException("Exception while processing request. " + ex.getMessage());
		}
	}
}
