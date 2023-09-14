import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;

public class Server{

	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	ObservableList<String> userList = FXCollections.observableArrayList();
	ListView<String> listView = new ListView<>(userList);

	Server(Consumer<Serializable> call){

		callback = call;
		server = new TheServer();
		server.start();
	}


	public class TheServer extends Thread{

		public void run() {

			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");


				while(true) {

					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					c.start();
					userList.add("Client " + count);

					Platform.runLater(() -> {
						listView.setItems(userList);
					});

					count++;

				}
			}//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}
		}//end of while
	}


	class ClientThread extends Thread {
		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;

		ClientThread(Socket s, int count) {
			this.connection = s;
			this.count = count;
		}

		public void updateClients(String message) {
			for (int i = 0; i < clients.size(); i++) {
				ClientThread t = clients.get(i);
				try {
					t.out.writeObject(message);
				} catch (Exception e) {}
			}
		}
		public void userMessage(Integer client, String message){
			ClientThread t = clients.get(client-1);
			try {
				t.out.writeObject(message);
			}
			catch(Exception e) {}
		}


		public void run() {
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			} catch (Exception e) {
				System.out.println("Streams not open");
			}
			updateClients("Online users: " + userList.toString());

			while (true) {
				try {
					String data = in.readObject().toString();
					if(!data.contains("@")) {
						callback.accept("client: " + count + " sent: " + data);
						updateClients("client #" + count + " said: " + data);
					}
					else{
						String message = data;
						callback.accept("client: " + count + " sent: " + data); //server

						String messageText = message.replaceAll("@client\\d+\\s*", "");
						Pattern pattern = Pattern.compile("@client(\\d+)");
						Matcher matcher = pattern.matcher(message);
						while (matcher.find()) {
							int clientId = Integer.parseInt(matcher.group(1));
							userMessage(clientId, "Message from client "+count+": "+messageText);
						}
					}
				}
				catch (Exception e) {
					callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
					userList.remove("Client " + count);
					updateClients("Client " + count + ": is offline\n" + "Online users: " + userList.toString());
					clients.remove(this);
					userList.remove("Client " + count);
					break;
				}
			}
		}
	}
}

