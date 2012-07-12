package net.zetaeta.remoteconsole.plugin;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketListenerThread extends Thread {
    
    private ServerSocket socket;
    private RemoteConsolePlugin plugin;
    private boolean running = true;
    
    public SocketListenerThread(RemoteConsolePlugin plugin) {
        this.plugin = plugin;
        socket = plugin.getSocket();
    }
    
    @Override
    public void run() {
        Socket s;
        try {
            System.out.println("Starting socket loop");
            while (running && (s = socket.accept()) != null && running) {
//                System.out.println("Socket loop!");
                System.out.println("Accepted socket at " + s.getInetAddress());
                plugin.startListening(s);
                if (interrupted()) {
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Finished socket loop!");
    }
    
    public void shutdown() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interrupt();
    }
}
