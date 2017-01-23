//package org.unesco.jisis.dbserver;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.io.UnsupportedEncodingException;
//import java.net.InetAddress;
//import java.net.ServerSocket;
//import java.net.UnknownHostException;
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.StringTokenizer;
//
///**
// * Class for making a server console, can be attached to any InputStream
// * and OutputStream, for example System.in and System.out. Attaching to
// * a socket is also possible, but it's recommended to do some kind of
// * authenticating first so that not everybody can get access.
// * 
// * @author shadewind
// */
//public class ServerConsole
//{
//	private static final String prompt = "console> ";
//	
//	private final BufferedReader in;
//	private PrintWriter out;
//	private ConsoleDispatchThread thread;
//	private ServerSetup setup;
//	
//	/**
//	 * Constructs a new ServerConsole.
//	 * 
//	 * @param iStream  the InputStream to attach the console to
//	 * @param oStream  the OutputStream to attach the console to
//	 */
//	public ServerConsole(ServerSetup setup, InputStream iStream, OutputStream oStream)
//	  throws UnsupportedEncodingException
//	{
//		this.setup = setup;
//		
//		this.in = new BufferedReader(new InputStreamReader(iStream));
//		this.out = new PrintWriter(oStream);
//		this.thread = new ConsoleDispatchThread();
//	}
//	
//	/**
//	 * Activates this console.
//	 */
//	public void activate()
//	{
//		out.print(prompt);
//		out.flush();
//		thread.start();
//	}
//	
//	/**
//	 * Processes a command.
//	 * 
//	 * @param command  the command to process
//	 */
//	protected void process(String command)
//	{
//		Administrator admin = setup.getAdministrator();
//		AccountDataPool pool = setup.getAccountDataPool();
//		ServerSocket socket = setup.getServerSocket();
//		Date startDate = setup.getStartingTime();
//		
//		StringTokenizer tokenizer = new StringTokenizer(command, " ");
//		if(!tokenizer.hasMoreElements())
//		{
//			return;
//		}
//		String firstToken = tokenizer.nextToken();
//		
//		
//		try
//		{
//			if(firstToken.equals("status"))
//			{
//				InetAddress address = socket.getInetAddress();
//				try
//				{
//					out.println("Host: " + InetAddress.getLocalHost());
//				}
//				catch(UnknownHostException e)
//				{
//					out.println("Host: unable to determine");
//				}
//				out.println("Listening on port: " + socket.getLocalPort());
//				out.println("Start date: " + startDate.toString());
//				out.println("Users logged in: " + admin.getUserCount());
//				out.println("Number of switchboards: " + admin.getSwitchboardCount());
//			}
//			else if(firstToken.equals("kill"))
//			{
//				int user = 0;
//				if(tokenizer.countTokens() != 1)
//				{
//					out.println(" !! Wrong number of parameters");
//					return;
//				}
//				
//				try
//				{
//					user = Integer.parseInt(tokenizer.nextToken());
//				}
//				catch(NumberFormatException e)
//				{
//					out.println(" !! Invalid user");
//				}
//				
//				if(!admin.userIsConnected(user))
//				{
//					out.println(" !! User is not online");
//					return;
//				}
//				
//				admin.kill(user);
//				out.println(" * User " + user + " has been disconnected");
//			}
//			else if(firstToken.equals("isonline"))
//			{
//				int user = 0;
//				if(tokenizer.countTokens() != 1)
//				{
//					out.println(" !! Wrong number of parameters");
//					return;
//				}
//				try
//				{
//					user = Integer.parseInt(tokenizer.nextToken());
//				}
//				catch(NumberFormatException e)
//				{
//					out.println(" !! Invalid user");
//				}
//				
//				if(admin.userIsConnected(user))
//				{
//					out.println(" * User is online");
//				}
//				else
//				{
//					out.println(" * User is not online");
//				}
//			}
//			else if(firstToken.equals("onlinelist"))
//			{
//				int users[] = admin.getOnlineList();
//				for(int i =  0; i < users.length; i++)
//				{
//					out.print(users[i]);
//					if(i < (users.length - 1))
//					{
//						out.print(", ");
//					}
//					else
//					{
//						out.print("\n");
//					}
//				}
//			}
//			else if(firstToken.equals("killall"))
//			{
//				out.print(" * Killing all users... ");
//				admin.killAll();
//				out.println("done");
//			}
//			else if(firstToken.equals("shutdown"))
//			{
//				out.println(" * Stopping accepting of new connections");
//				try
//				{
//					socket.close();
//				}
//				catch(IOException e)
//				{
//					e.printStackTrace();
//					//well... there is'nt much to do
//				}
//				out.print(" * Killing all users... ");
//				admin.killAll();
//				out.println("done");
//				out.print(" * Flushing account data pool... ");
//				try
//				{
//					pool.flush();
//					out.println("done");
//				}
//				catch(SQLException e)
//				{
//					out.println("ERROR");
//					out.println(" * Details: " + e.getMessage());
//				}
//				out.print(" * Disposing account data pool resources... ");
//				pool.dispose();
//				out.println("done");
//				out.println(" * Server is shutdown. You may now exit.");
//			}
//			else if(firstToken.equals("help"))
//			{
//				out.println("  status            shows server status");
//				out.println("  kill [user]       disconnects specified user");
//				out.println("  isonline [user]   checks if the specified user is online");
//				out.println("  killall           disconnects all users");
//				out.println("  shutdown          shuts server down");
//			}
//		}
//		finally
//		{
//			out.print(prompt);
//			out.flush();
//		}
//	}
//	
//	/**
//	 * Dispatcher thread reading from input, sends to process method.
//	 */
//	class ConsoleDispatchThread extends Thread
//	{
//		public void run()
//		{
//			String read;
//			try
//			{
//				while(true)
//				{
//					read = in.readLine();
//					if(read == null)
//					{
//						break;
//					}
//					process(read);
//				}
//			}
//			catch(IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//}