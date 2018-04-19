package utd.persistentDataStore.datastoreServer.commands;

import java.io.IOException;
import java.util.List;

import utd.persistentDataStore.utils.StreamUtil;
import utd.persistentDataStore.utils.FileUtil;
import utd.persistentDataStore.utils.ServerException;

public class DirectoryCommand extends ServerCommand {
	public void run() throws IOException, ServerException
	{
		try {
			// Fetch directory
			List<String> directory = FileUtil.directory();
			System.out.println("Finished reading directory");

			// Write response
			StreamUtil.writeLine("OK\n", outputStream);
			StreamUtil.writeLine(String.valueOf(directory.size()) + "\n", outputStream);
			for (String dir : directory) {
				StreamUtil.writeLine(dir + "\n", outputStream);
			}
		}
		catch (Exception ex) {
			throw new ServerException("Exception while processing request. " + ex.getMessage());
		}
	}
}