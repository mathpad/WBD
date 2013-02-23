package uk.org.darnell.socket;
import java.io.*;
import java.net.*;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Server{
	public static Bitmap screen;
	static ServerSocket providerSocket;
	static Socket connection = null;
	static ObjectOutputStream out;
	static ObjectInputStream in;
	String message;
	private final String TAG = "Server.java";
	static byte nextPoint = 0;
	static byte[] points = new byte[100];
	static int lastX = 0;
	static int lastY = 0;
	static boolean toSend = false;

	public Server(){}
	public void run(Handler handler)
	{	
		try{
			//1. creating a server socket
			Log.d(TAG,"Creating socket");
			providerSocket = new ServerSocket(2004, 10);
			// providerSocket.setSoTimeout(3000);
			//2. Wait for connection
			System.out.println("Waiting for connection");
			handler.sendMessage(handler.obtainMessage(1,0,0,"Waiting for Connection"));
			connection = providerSocket.accept();
			System.out.println("Connection received from " + connection.getInetAddress().getHostName());
			handler.sendMessage(handler.obtainMessage(1,0,0,"Connection received from " + connection.getInetAddress().getHostName()));
			//3. get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			// sendMessage("Connection successful");
			//4. The two parts communicate via the input and output streams
			do{
				try{
					message = (String)in.readObject();
					System.out.println("client>" + message);
					handler.sendMessage(handler.obtainMessage(1,0,0,"Message from client>" + message));
					if (message.equals("screen"))
					{
						byte[] screendata = (byte[])in.readObject();
						screen = BitmapFactory.decodeByteArray(screendata, 0, screendata.length);
						handler.sendMessage(handler.obtainMessage(2,0,0,"screen data:" + screendata.length));
					}
					if (message.equals("bye"))
						sendMessage("bye");
				}
				catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
			}while(!message.equals("bye"));
		}
		catch(IOException ioException){
			handler.sendMessage(handler.obtainMessage(1,0,0,"Connection Failed"));
			// ioException.printStackTrace();
		}
		finally{
			//4: Closing connection
			
			try{
				providerSocket.close();
				if (connection != null)
				{
				  in.close();
				  out.close();
				}
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
			handler.sendMessage(handler.obtainMessage(1,0,0,"Connection Closed"));
		}
	}
	
	private static Handler handler2 = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			
		try{
			  providerSocket.close();
			if (connection != null)
			{
			  in.close();
			  out.close();
			}
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		}
	};
	
	public Handler getHandler() {return handler2;}
	
	void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("server>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	public static void sendMove(int X, int Y)
	{
		if (toSend) sendData();
		points[0] = (byte) (X % 256);
		points[1] = (byte) (X / 256);
		points[2] = (byte) (Y % 256);
		points[3] = (byte) (Y / 256);
		lastX = X;
		lastY = Y;
		nextPoint = 4;
	}

	public static void addPoint(int X, int Y)
	{
		points[nextPoint++] = (byte)(X-lastX);
		lastX = X;
		points[nextPoint++] = (byte)(Y-lastY);
		lastY = Y;
		toSend = true;
		if (nextPoint > 12) sendMove(X,Y); // send this and start a new one
	}
	
	public static void sendPoints(int X, int Y)
	{
		if (nextPoint >=4)
		{
		points[nextPoint++] = (byte)(X-lastX);
		lastX = X;
		points[nextPoint++] = (byte)(Y-lastY);
		lastY = Y;
		toSend = true;
		sendMove(X,Y); // pen up action - a bit hacky to set these coords - perhaps rethink	- should be overwritten		
		}
	}
		
	public static void sendData()	
	{	
		if (connection != null)
		{
			try{
				out.writeByte(7);
				out.writeByte(nextPoint);
				out.write(points,0,nextPoint);
				out.flush();
				Log.d("Server","sent points:" + nextPoint);
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		toSend = false;
	}
	
	
}
