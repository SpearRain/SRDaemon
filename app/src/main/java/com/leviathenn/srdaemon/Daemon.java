package com.leviathenn.srdaemon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Daemon extends Service {

    private static String TAG = "MyService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final int PORT = 5795; // Port number for the server to listen on

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                    Log.e(TAG, "Server started. Listening on port " + PORT);

                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        Log.e(TAG, "Client connected: " + clientSocket.getInetAddress().getHostAddress());

                        // Create a new thread to handle each client request
                        new ClientHandler(clientSocket).start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return START_STICKY;
    }
    static class ClientHandler extends Thread {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read client request
                String request = in.readLine();
                Log.e(TAG,"Received request from client: " + request);

                // Process request
                String response = processRequest(request);

                // Send response back to client
                out.println(response);

                // Close streams and socket
                in.close();
                out.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Process client request
        private String processRequest(String request) {
            // Parse request
            String[] parts = request.split("\\|");
            if (parts.length < 2 || !parts[0].equals("INSTALL_APP")) {
                return "Invalid request format";
            }

            // Extract parameters
            Map<String, String> params = new HashMap<>();
            for (int i = 1; i < parts.length; i++) {
                String[] keyValue = parts[i].split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }

            // Example: Extracting app_name and version
            String appName = params.get("app_name");
            String version = params.get("version");

            // Perform installation logic (replace with actual implementation)
            return "Installing " + appName + " version " + version;
        }
    };
}

