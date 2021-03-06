/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package chat.system.server;

import chat.system.objects.ChatMessage;
import chat.system.objects.ChatPerson;
import chat.system.objects.ServerMessage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Chat server. Manages all the clients and proliferates the messages to them.
 *
 * @author jon
 */
public class Server extends Observable implements Observer {
    
    // Listens for new connections and returns a Socket object for them.
    ServerSocket ss;
    // PEOPLEZ!!!
    ArrayList<ChatPerson> people;

    /**
     * Starts the server
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) {
        System.out.println("main");
        for (int i = 0; i < args.length; i++) {
            System.out.println(args[i]);
        }
        new Server();
    }

    /**
     * A chat server
     */
    public Server() {
        try {
            // The port to run on.
            ss = new ServerSocket(3191);
        } catch (IOException ex) {
            System.err.println("Could not start server. Could not open port");
        }
        people = new ArrayList<>();
        System.out.println("Listening for clients");
        listenForClients();
    }

    @Override
    public void update(Observable o, Object arg) {
        //System.out.println("update");
        if (o instanceof ClientConnection) {
            // Echo to all clients
            if (arg instanceof ChatMessage) {
                this.setChanged();
                this.notifyObservers(arg);
            } else if (arg instanceof ServerMessage) {
                parseRequest((ServerMessage) arg);
            } else if (arg instanceof ChatPerson) {
                people.add((ChatPerson) arg);
                this.setChanged();
                this.notifyObservers(arg);
            }
        }
    }

    private void listenForClients() {
        while (true) {
            try {
                // Waits for a client to connect, then give a Socket for them
                Socket s = ss.accept();
                System.out.println("New Client" + s.getInetAddress());
                ClientConnection cc = new ClientConnection(s);
                cc.addObserver(this);
                this.addObserver(cc);
                people.add(cc.getName());
                Thread t = new Thread(cc);
                t.start();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void parseRequest(ServerMessage serverMessage) {
        //System.out.println("parse request");
        if (serverMessage.getServerCode() == 1) {
            // Send it to all clients
            this.setChanged();
            this.notifyObservers(serverMessage);
        } else if (serverMessage.getServerCode() == 2) {
            for (int i = 0; i < people.size(); i++) {
                if (people.get(i).getName() != null) {
                    this.setChanged();
                    this.notifyObservers(people.get(i));
                    System.out.println(people.get(i).getName());
                }
            }
        }
    }
}
