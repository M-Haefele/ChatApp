import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientMessager{
  private static boolean isConnected = true;

  public static void main(String[] args) {
    if(args.length != 2) {
      	System.err.println("Usage java ClientMessager HostName/IPAddress portNum");
      	System.exit(0);
    }

    String hostName = args[0];
    int portNumber = Integer.parseInt(args[1]);


    ExecutorService threadPool = Executors.newFixedThreadPool(2);

    try {
      	Socket socket = new Socket(hostName, portNumber);
      	BufferedReader serverIn = new BufferedReader(
                            		new InputStreamReader(
                              				socket.getInputStream()));
     	PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
      	Scanner userInput = new Scanner(System.in);

      	System.out.printf("Connected to Server %s:%d\n", hostName, portNumber);
      	System.out.println("This is Matt's CSCI 4301 Chat Application");
      	System.out.println("-----------------------------------------------------");
      	System.out.println("|   Exit the chat client by pressing 'q' at any time |");
      	System.out.println("|   Type 'AllUsers' to see a list of active users    |");
      	System.out.println("------------------------------------------------------");
  	
      	//serverOut.println(userName);
      

    // thread for accepting user input and sending to server
    threadPool.execute(new Runnable() {
     	public void run() {
        	String line;
        	while(userInput.hasNextLine()) {
          	line = userInput.nextLine();
          	if(line.toLowerCase().equals("q")) {
            	isConnected = false;
            	try {
              		serverOut.close();
              		serverIn.close();
              		userInput.close();
            	}catch (IOException e) {
              		System.err.println("IOException on closing I/O resources");
              		System.err.println(e.getMessage());
             	}
              	return;
            	}
            //System.out.printf("Sending \"%s\" to server\n", line);
            serverOut.println(line); 
          } 
        }
     });

      // thread for accepting server input and displaying it to screen
      threadPool.execute(new Runnable() {
        public void run() {
          String line = "";
          try {
            while(isConnected && ((line = serverIn.readLine()) != null)) {
              System.out.printf("%s\n", line); 
            }
          } catch (IOException e) {
            System.out.println("You have ended connection to the Server");
          }
        }
      });

      threadPool.shutdown();
    } catch (IOException e) {
      System.err.println("IOException: Couldn't establish connect to server");
      System.err.printf("HostName: %s\nPort: %d\n", hostName, portNumber);
      System.err.println(e.getMessage());
    }


  }//end main



}//end ClientMessager