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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides utility methods needed to read / write / delete / list file
 * containing binary data. 
 */
public class FileUtil
{
	static File directory = new File("data");
	
	public static void writeData(String name, byte data[]) throws IOException
	{
		if(!directory.exists()) {
			directory.mkdir();
			System.out.println("Created Data Directory");
		}
		
		File file = new File(directory, name);
		file.createNewFile();
		OutputStream ostream = new FileOutputStream(file);
		for(int idx = 0; idx < data.length; idx++) {
			ostream.write(data[idx]);
		}
		ostream.close();
	}

	public static byte[] readData(String name) throws ServerException, IOException
    {
		if(!directory.exists()) {
			directory.mkdir();
			System.out.println("Created Data Directory");
		}
		
		File file = new File(directory, name);
	    if(!file.exists()) {
	    	throw new ServerException("No File Found " + name);
	    }
	    
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    InputStream istream = new FileInputStream(file);
	    int ch = 0;
	    while((ch=istream.read()) != -1) {
	    	baos.write(ch);
	    }
	    istream.close();
	    return baos.toByteArray();
    }

	public static boolean deleteData(String name) throws ServerException
    {
		if(!directory.exists()) {
			directory.mkdir();
			System.out.println("Created Data Directory");
		}
		
		File file = new File(directory, name);
	    if(!file.exists()) {
	    	throw new ServerException("No File Found " + name);
	    }
	    
	    return file.delete();
    }
	
	public static List<String> directory() throws ServerException
    {
		List<String> result = new ArrayList<String>();
		if(directory.exists()) {
			File files[] = directory.listFiles();
			for(File file: files) {
				result.add(file.getName());
			}
		}
		return result;
    }
}
