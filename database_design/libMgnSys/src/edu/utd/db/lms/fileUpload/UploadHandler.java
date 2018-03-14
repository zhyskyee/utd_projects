package edu.utd.db.lms.fileUpload;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
//import org.apache.commons.io.IOUtils;

import edu.utd.db.lms.dbAction.JDBCUtil;
/**
 * Servlet implementation class UploadHandler
 */
public class UploadHandler extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadHandler() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String path2save = this.getServletContext().getRealPath("WEB-INF/filePath");
		File file = new File(path2save);
		JDBCUtil jdbcUtil = new JDBCUtil();
		if ( !file.exists() && !file.isDirectory() )
		{
			System.out.println("The file or its dir doesnt exist, Create it");
			file.mkdirs();
		}
		System.out.println("=====================");
		//Hints
		String message = new String("Success!");
		String msgPage2Fwd = new String("/loadMsg.jsp");
		try 
		{
			//Buildup a factory
			DiskFileItemFactory factory = new DiskFileItemFactory();
			System.out.println("The original size is set to " + factory.getSizeThreshold());
			factory.setSizeThreshold(100*1024);
			factory.setRepository(file);
			//buildup a fileuploader
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setProgressListener(new ProgressListener() {
				public String loadMsgStatic = new String("Loading percentage: ");
				public String loadMsgVaried = new String("00%");
				public Boolean bUpdated = false;
				public int outputUpdateWordNum = 0;
				@Override
				public void update(long arg0, long arg1, int arg2) {
					// TODO Auto-generated method stub
					for (int i = 0; bUpdated == true && i <= outputUpdateWordNum; i++) {
						System.out.print("\b");
					}
					loadMsgVaried = Long.toString(arg1 == 0 ? 0 : 100 * arg0 / arg1) + "%";
					System.out.println(loadMsgStatic + loadMsgVaried);
					outputUpdateWordNum = loadMsgStatic.length() + loadMsgVaried.length();
					bUpdated = true;
					System.out.println("The num of chars are printed: " + outputUpdateWordNum);
				}
			});
			upload.setHeaderEncoding("UTF-8");
			
			if (!ServletFileUpload.isMultipartContent(request)) 
			{
				//do it in traditional way
				System.out.println("isNot MultipartContent request!");
				return;
			}
			
			upload.setFileSizeMax(1024 * 1024 * 5);
			upload.setSizeMax(upload.getFileSizeMax() * 2);
			
			List<FileItem> list = upload.parseRequest(request);
			for (FileItem fileItem : list) 
			{
				if (fileItem.isFormField()) 
				{
					String name = fileItem.getFieldName();
					String value = fileItem.getString("UTF-8");
					System.out.println(name + " => " + value);
				} 
				else 
				{	
					String filename = fileItem.getName();
					String name = fileItem.getFieldName();
					System.out.println("The filename in request is :" + filename + "  name:" + name);
					
					if (filename == null || filename.trim().equals("")) {
						continue;
					}
					
					filename = filename.substring(filename.lastIndexOf("\\") + 1);
					String fileExtName = filename.substring(filename.lastIndexOf(".") + 1);
					
					if (!fileExtName.equalsIgnoreCase("csv"))
					{
						System.out.println("This file type is not acceptable!");
						throw new Exception("This file is not CSV format!");
					}
					
					InputStream inputStr = fileItem.getInputStream();
					String saveFileName = UUID.randomUUID().toString() + "_" + filename;
					String saveFilePath = path2save + "/" + saveFileName;
					
					FileOutputStream fops = new FileOutputStream(saveFilePath);
					byte[] buf = new byte[1024];
					int buflen = 0;
					while ((buflen = inputStr.read(buf)) > 0) {
						fops.write(buf, 0, buflen);
					}
					
					inputStr.close();
					fops.close();
					
					if (name.equals("BookFile"))
					{
						 System.out.println("BookPath:" + saveFilePath + " Record reads =" + jdbcUtil.initBook(saveFilePath));
					} else if (name.equals("BorrowerFile")) {
					     System.out.println("BorrowerPath:" + saveFilePath + " Record reads =" + jdbcUtil.initBorrower(saveFilePath));
					} else {
						System.out.println("Uploaded file is not acceptable!!!");
					}
				   
				}	
			}
		} 
		catch (FileUploadBase.FileSizeLimitExceededException e) 
		{
		      request.setAttribute("message", e.getMessage());
		      request.getRequestDispatcher(msgPage2Fwd).forward(request, response);
		      e.printStackTrace();
		      return;
		}
		catch (FileUploadBase.SizeLimitExceededException e)
		{
		      request.setAttribute("message", e.getMessage());
		      request.getRequestDispatcher(msgPage2Fwd).forward(request, response);
		      e.printStackTrace();
		      return;
		}
		catch (Exception e) 
		{
		      message= "Upload Failed！";
		      if(!e.getMessage().equals(""))
		      {
		    	  	message += e.getMessage();
		      }
		      else
		      {
		    	  	System.out.println("unknow error!");
		      }
		      e.printStackTrace();
		}
		
		request.setAttribute("message", message);
		request.getRequestDispatcher(msgPage2Fwd).forward(request, response);
		System.out.println("The end of uploading...");
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Current Time: " + (new Date()) + "\n\r");
		doGet(request, response);
	}

}
