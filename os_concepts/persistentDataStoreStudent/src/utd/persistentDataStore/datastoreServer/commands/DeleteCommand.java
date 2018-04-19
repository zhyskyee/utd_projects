package utd.persistentDataStore.datastoreServer.commands;

import java.io.IOException;

import utd.persistentDataStore.utils.StreamUtil;
import utd.persistentDataStore.utils.FileUtil;
import utd.persistentDataStore.utils.ServerException;

public class DeleteCommand extends ServerCommand {
	public void run() throws IOException, ServerException
	{
		try {
			// Read command
			String name = StreamUtil.readLine(inputStream);
			System.out.println("Finished reading command");
			
			// delete data
			FileUtil.deleteData(name);
			System.out.println("Finished deleting data");

			// Write response
			this.sendOK();
		}
		catch (Exception ex) {
			throw new ServerException("Exception while processing request. " + ex.getMessage());
		}
	}
}