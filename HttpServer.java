package name.duruofei;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class HttpServer 
{
	public static void main(String[] args) throws Exception
	{
		String dir =System.getProperty("user.dir");
		System.out.println(dir);
		File file = new File(dir+"\\config.pro");
		if(!file.exists())
		{
			System.out.println("找不到文件");
			System.exit(0);
		}
		Properties property =new Properties();
		property.load(new FileInputStream(file));
		String appDir = (String)property.get("dir");
		if(appDir==null)
		{
			System.out.println("属性设置错误");
			System.exit(0);
		}
		if(!(new File(appDir).isDirectory()))
		{
			System.out.println("属性设置错误");
			System.exit(0);
		}
		String pro = (String)property.get("port");
		if(pro==null)
		{
			System.out.println("属性设置错误");
			System.exit(0);
		}
		ServerSocket server = new ServerSocket(java.lang.Integer.valueOf(pro));
		while(true)
		{
			Socket socket =server.accept();
			new RequestDone(socket,appDir);
		}
	}
}
class RequestDone extends Thread
{
	private Socket socket;
	private String dir;
	private BufferedReader in;
	private OutputStream out;
	public RequestDone(Socket socket,String dir) {
		super();
		this.socket = socket;
		this.dir=dir;
		try
		{
			in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		}catch(Exception e)
		{
			Thread.currentThread().destroy();
		}
		this.start();
	}
	private String getHead(String queryresource)
	{
		String filename="";
		int index=queryresource.lastIndexOf("/");
		filename=queryresource.substring(index+1);
		String[] filetypes=filename.split("\\.");
		String filetype=filetypes[filetypes.length-1];
		if(filetype.equals("html"))
		{
			return "HTTP/1.0 200 OK\r\n"+"Content-Type:text/html\r\n" + "Server:myserver\r\n"+"connection:keep-alive\r\n";
		}
		else if(filetype.equals("jpg")||filetype.equals("gif")||filetype.equals("png"))
		{
			return "HTTP/1.0 200 OK\r\n"+"Content-Type:image/jpeg\r\n" + "Server:myserver\r\n"+"connection:keep-alive\r\n";
		}
		else if(filetype.equals("ico")||filetype.equals("ico")||filetype.equals("ico"))
		{
			return "HTTP/1.0 200 OK\r\n"+"Content-Type:image/ico\r\n" + "Server:myserver\r\n"+"connection:keep-alive\r\n";
		}
		else return null;
		
	}
	public void run()
	{
		while(true)
		{
		String queryThing=null;
		try
		{
			String firstLine = in.readLine();
			if(firstLine==null)return;
			//System.out.print(Thread.currentThread().getName());
			//System.out.println(firstLine);
			String ii=null;
			do
			{
				ii=in.readLine();
				if(ii==null)return;
				//System.out.println(ii);
			}while(!(ii.equals("")));
			
			queryThing = firstLine.split(" ")[1];
			InputStream filedata=new FileInputStream(dir+queryThing);
			ByteArrayOutputStream baos=new ByteArrayOutputStream();
			byte[] data = new byte[1024*3];
			int i=0;
			do
			{
				i=filedata.read(data);
				if(i!=-1)
				{
					baos.write(data, 0, i);
				}
			}while(i!=-1);
			filedata.close();
			String head=this.getHead(queryThing)+"content-length:"+baos.size()+"\r\n\r\n";
			out.write(head.getBytes());
			out.write(baos.toByteArray());
			out.flush();
		}catch(Exception e)
		{
			System.out.println(e);
		}finally
		{
			try
			{
				
			}catch(Exception e)
			{
				System.exit(0);
			}
		}
		//long end =System.currentTimeMillis();
		//System.out.println(queryThing+":"+(end-start));
	}
	}
}
