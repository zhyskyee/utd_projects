package edu.utd.db.zhySql;

import java.io.RandomAccessFile;
import java.io.IOException;

/**
 *
 * @author Chris Irwin Davis
 * @version 1.0
 */
public class HexDump {
	/**
	 * <p>This method is used for debugging.
	 * @param ram is an instance of {@link RandomAccessFile}. 
	 * <p>This method will display the binary contents of the file to Stanard Out (stdout)
	 */
	static void displayBinaryHex(RandomAccessFile ram, int pageSize) {
		try {
			System.out.println("Dec\tHex\t 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F");
			ram.seek(0);
			long size = ram.length();
			int row = 1;
			System.out.print("0000\t0x0000\t");
			while(ram.getFilePointer() < size) {
				System.out.print(String.format("%02X ", ram.readByte()));
				// out.print(ram.readByte() + " ");
				/* Print the page header */
				if(row % pageSize == 0) {
					System.out.println();
					System.out.print("Dec\tHex\t 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F");
				}
				/* Print line header */
				if(row % 16 == 0) {
					System.out.println();
					System.out.print(String.format("%04d\t0x%04X\t", row, row));
				}
				row++;
			}
			System.out.println();	
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
}