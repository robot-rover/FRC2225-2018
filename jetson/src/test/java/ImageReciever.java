import boofcv.gui.image.ImagePanel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

public class ImageReciever extends JFrame {
    static final String hostname = "10.42.0.186";
    static final int port = 1280;
    static boofcv.gui.image.ImagePanel panel;
    public static void main(String[] args) {
        new ImageReciever();
    }

    public ImageReciever() {
        super("Camera Stream");
        createBufferStrategy(1);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        panel = new ImagePanel();
        panel.setName("Camera Stream");
        panel.setVisible(true);
        getContentPane().add(panel);
        Thread renderer = new Thread(ImageReciever::renderLoop);
        renderer.setDaemon(true);
        renderer.start();
        setSize(640, 480);
        setVisible(true);
    }

    static void renderLoop() {
        while(true) {
            try {
                Socket connection = new Socket(hostname, port);
                InputStream in = connection.getInputStream();
                BufferedImage im = ImageIO.read(in);
                panel.setImageRepaint(im);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
