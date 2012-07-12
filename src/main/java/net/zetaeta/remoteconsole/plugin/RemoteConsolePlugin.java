package net.zetaeta.remoteconsole.plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RemoteConsolePlugin extends JavaPlugin {
    
    private SSLServerSocket socket;
    private SocketListenerThread socketListener;
    private Map<String, String> users = new HashMap<String, String>();
    private Map<Socket, NetworkListenerThread> sockets = new HashMap<Socket, NetworkListenerThread>();
    
    public static final int MAGIC_HANDSHAKE = 0xCAFEBABE;
    public static PrintStream out;
    
    @Override
    public void onEnable() {
        // Keystore
        System.setProperty("javax.net.ssl.keyStore", "sslKeystore.ks");
        System.setProperty("javax.net.ssl.keyStorePassword", "Heliocentric");
        // Debug stuff
//        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
//        System.setProperty("javax.net.debug", "ssl");
        
        try {
            out = new PrintStream(new File("/dev/stdout"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        openSocket();
        loadUsers();
        ConsoleHandler handler =  new ConsoleHandler() {
            {
                setFormatter(new ConsoleLogFormatter());
            }
            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().startsWith("BUKKIT:") || record.getMessage().contains("BUKKIT:")) {
                    return;
                }
                String format = getFormatter().format(record);
                dispatchMessage(format);
            }
        };
        Logger.getGlobal().addHandler(handler);
        Bukkit.getLogger().addHandler(handler);
    }
    
    @Override
    public void onDisable() {
        
    }
    
    public void openSocket() {
        FileConfiguration config = getConfig();
        int port = config.getInt("socket.port", 2222);
        try {
//            socket = new SSLServerSocket(port);
            socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        socketListener = new SocketListenerThread(this);
        socketListener.start();
    }
    
    public void loadUsers() {
        FileConfiguration config = getConfig();
        ConfigurationSection userSection = config.getConfigurationSection("users");
        if (userSection == null) {
            return;
        }
        for (String user : userSection.getKeys(false)) {
            users.put(user, userSection.getString(user));
        }
    }
    
    public SSLServerSocket getSocket() {
        return socket;
    }
    
    public void startListening(Socket s) {
        NetworkListenerThread nlt = new NetworkListenerThread(this, s);
        sockets.put(s, nlt);
        nlt.start();
    }
    
    public void dispatchMessage(String message) {
        for (Iterator<Map.Entry<Socket, NetworkListenerThread>> it = sockets.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Socket, NetworkListenerThread> entry = it.next();
            if (entry.getValue().isRunning()) {
                entry.getValue().dispatchMessage(message);
            }
            else {
                it.remove();
            }
        }
    }
    
    public boolean authenticate(String username, String password) {
        return users.containsKey(username) && password.equals(users.get(username));
    }
}
