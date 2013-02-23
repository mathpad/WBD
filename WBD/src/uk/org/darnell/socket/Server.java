package uk.org.darnell.socket;
import java.io.*;
import java.net.*;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Server{
	static ServerSocket providerSocket;
	static Socket connection = null;
	static ObjectOutputStream out;
	static ObjectInputStream in;
	private final String TAG = "WBD Server";

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
					readMessage(handler);
			}while(true);
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
		
	private void readMessage(Handler handler)
	{
		try {
		switch (in.readByte()) {
		case 7:
			int X,Y;
			int numBytes = in.readByte() - 4;
			X = in.readByte() + in.readByte() * 256;
			Y = in.readByte() + in.readByte() * 256;
			
			handler.sendMessage(handler.obtainMessage(4,X,Y));
			
			while (numBytes > 0)
			{
				X += in.readByte();
				Y += in.readByte();
				handler.sendMessage(handler.obtainMessage(5,X,Y));
				numBytes -= 2;
			}
		}
		}
		catch(IOException ioException){
			handler.sendMessage(handler.obtainMessage(1,0,0,"Connection Failed"));
			// ioException.printStackTrace();
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
	
}
