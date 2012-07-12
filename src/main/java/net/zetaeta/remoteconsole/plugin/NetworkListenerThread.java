package net.zetaeta.remoteconsole.plugin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class NetworkListenerThread extends Thread {
    
    private Socket socket;
    private RemoteConsolePlugin plugin;
    private boolean running = true;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean authenticated;
    
    public NetworkListenerThread(RemoteConsolePlugin plugin, Socket socket) {
        this.plugin = plugin;
        this.socket = socket;
        try {
//            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed getting input stream from socket!", e);
        }
    }
    
    @Override
    public void run() {
        try {
            System.out.println("Starting commmunications!");
//            if (socket.isClosed()) {
//                System.out.println("Socket closed!");
//            }
//            if (!socket.isConnected()) {
//                System.out.println("Socket not connected!");
//            }
//            if (socket.isInputShutdown()) {
//                System.out.println("Socket input shutdown!");
//            }
//            if (socket.isOutputShutdown()) {
//                System.out.println("Socket output shutdown!");
//            }
            DataOutputStream dataOs = new DataOutputStream(socket.getOutputStream());
            System.out.println("Sending handshake: " + Integer.toHexString(RemoteConsolePlugin.MAGIC_HANDSHAKE));
            dataOs.writeInt(RemoteConsolePlugin.MAGIC_HANDSHAKE);
            if (socket.isClosed()) {
                return;
            }
            DataInputStream dataIs = new DataInputStream(socket.getInputStream());
            String username = dataIs.readUTF();
            String password = dataIs.readUTF();
            if (!plugin.authenticate(username, password)) {
                System.out.println("Failed authentication!");
                dataOs.writeInt(0);
                socket.close();
                return;
            }
            dataOs.writeInt(1);
            System.out.println("Authenticated!");
            authenticated = true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e1) {
            e1.printStackTrace();
            return;
        }
        String s;
        try {
            System.out.println("Starting listening loop!");
            while (running && (s = reader.readLine()) != null) {
//                System.out.println("Recieved message: " + s);
                if (s.startsWith("/")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.substring(1));
                }
                else {
                    Bukkit.broadcastMessage(s);
                }
                if (interrupted()) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished listening loop!");
    }
    
    public void shutdown() {
        running = false;
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void dispatchMessage(String message) {
//        RemoteConsolePlugin.out.println("BUKKIT: Dispatching message " + message);
        if (authenticated) {
            writer.println(message);
            writer.flush();
        }
    }
}
