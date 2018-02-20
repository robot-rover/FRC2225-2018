package org.usfirst.frc.team2225.season2018.jetson;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public class ImageStream {
    ServerSocket server;
    Thread thread;
    Supplier<byte[]> imageCallback;
    public ImageStream(Supplier<byte[]> imageCallback) throws IOException {
        server = new ServerSocket(1280);
        thread = new Thread(this::runLoop);
        thread.setDaemon(true);
        this.imageCallback = imageCallback;
    }

    public void start() {
        thread.start();
    }

    private void runLoop() {
        while(true)
        try {
            Socket socket = server.accept();
            socket.getOutputStream().write(imageCallback.get());
            socket.close();
        } catch (IOException e) {
            System.err.println("Error while waiting for Socket Connection" + e.getMessage());
        }
    }
}
