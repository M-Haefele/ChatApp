import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;



public class ServerMessager {

  private static List<PrintWriter> clients;
  public static ArrayList<String> activeUsers = new ArrayList<String>(10);
  public static int clientCount;
  private static final boolean RUNNING = true;

  public static void main(String[] args) {
    //executor service is a java API that handles threads as a single instance, a thread scheduler
    ExecutorService threadPool = Executors.newCachedThreadPool();

    clients = Collections.synchronizedList (new LinkedList<PrintWriter>());
    try (
      ServerSocket serverSocket = new ServerSocket(65530);
      ) {
      System.out.println("Waiting for client connections...");
      while(RUNNING) {
        try {
          Socket clientSocket = serverSocket.accept();
          ClientConnection client = new ClientConnection(clientSocket);
          threadPool.execute(client);
        } catch (IOException e) {
          System.err.println("IOException: Could not accept client connection");
          System.err.println(e.getMessage());
          }
        }
    } catch (IOException e) {
        System.err.println("IOException: Could not create ServerSocket on port " + 65530);
        System.out.println(e.getMessage());
    }

    threadPool.shutdown();
  }

  //create an internal class handler for client connections
  public static class ClientConnection implements Runnable {
      private Socket clientSocket;
      private String userName;
      private PrintWriter out;
  

      public ClientConnection(Socket clientSocket) {
          this.clientSocket = clientSocket;
      } 


      public void run() {
          try (
            BufferedReader in = new BufferedReader(
                                 new InputStreamReader(
                                  clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          ) 
        {
          this.out = out;
          ServerMessager.clients.add(out);
          String message = "Enter a username for the chatroom in the format 'username = yourUserName'";
          privateMessage(message, this.out);
          boolean notAuthorized = true;
          while(notAuthorized){
              String userAuth = in.readLine();
              String[] tokens = userAuth.split(" ");
              if(tokens.length > 2){
                this.userName = tokens[2];
                populateUserList(this.userName);
                notAuthorized = false;
              }
          }
          System.out.printf("Server: %s has joined the Server.\n" , this.userName);
          broadcast(String.format("Server: Welcome %s !\n", this.userName, this.userName), out);
          String line = "";
        //System.out.printf("%s: %s\n", this.userName, line);
        while((line = in.readLine()) != null) {
          //IF ELSE 
          if(line.equals("AllUsers")){
            privateMessage(displayUsers(), this.out);
          }
          else{
            System.out.printf("%s: %s\n", this.userName, line);
            broadcast(String.format("%s: %s\n", this.userName, line), out);
          }
        }

        } catch (IOException e) {
          System.err.println(e.getMessage());
        } finally {
          System.out.printf("%s has left the chat.\n", this.userName);
          broadcast(String.format("Server: %s has left the chat.\n", this.userName), this.out);
           // remove client PrintWriter from broadcast list
          ServerMessager.clients.remove(this.out);
        try {
          this.clientSocket.close();
        } catch (IOException e) {
          System.err.println("Cannot close client connection");
        }
      }
    } // end of run method

    public void broadcast(String message, PrintWriter thisUser) {
        for(PrintWriter client : ServerMessager.clients) {
            if(client != thisUser || ServerMessager.clients.size() == 1){
                client.println(message);
            }
        }
    } // end of broadcast method

    public void privateMessage(String message, PrintWriter thisUser){
        for(PrintWriter client : ServerMessager.clients){
            if(client == thisUser){
              client.println(message);
            }
        }
    }

   public void populateUserList(String user){
      ServerMessager.activeUsers.add(user);
   }

   public static String displayUsers(){
    String list = "";
      for(int i = 0; i < ServerMessager.activeUsers.size(); i++){
        list += activeUsers.get(i) + "\n";
      }
      return list;
   }





  }
}