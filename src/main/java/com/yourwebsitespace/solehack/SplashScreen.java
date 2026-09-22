package com.yourwebsitespace.solehack;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

// A custom "welcome" splash window shown while the mod loads, before
// Minecraft's own screens are ready. Runs on Swing (independent of
// Minecraft's OpenGL rendering), so it can appear this early.
public class SplashScreen {

    private static JWindow window;
    private static long shownAtMillis;

    // Minimum time the splash stays visible, regardless of how fast mod
    // loading actually finishes - otherwise it can flash and vanish almost
    // instantly, since Forge preInit is usually very quick.
    private static final long MIN_DISPLAY_MILLIS = 2500L;

    // Loaded from the mod's bundled resources (classpath), same pattern as
    // your other textures - to customize the image, replace the PNG file at
    // src/main/resources/assets/solehack/textures/gui/splash.png
    private static final String IMAGE_RESOURCE_PATH = "/assets/solehack/textures/gui/splash.png";

    public static void show(String welcomeText) {
        SwingUtilities.invokeLater(() -> {
            try {
                buildWindow(welcomeText);
                shownAtMillis = System.currentTimeMillis();
            } catch (Exception ex) {
                System.err.println("SoleHack: failed to show splash screen: " + ex.getMessage());
            }
        });
    }

    private static void buildWindow(String welcomeText) throws IOException {
        BufferedImage image;
        try (InputStream stream = SplashScreen.class.getResourceAsStream(IMAGE_RESOURCE_PATH)) {
            if (stream == null) {
                throw new IOException("Splash image not found at " + IMAGE_RESOURCE_PATH);
            }
            image = ImageIO.read(stream);
        }

        ImageIcon icon = new ImageIcon(image);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        JLabel imageLabel = new JLabel(icon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);

        JLabel textLabel = new JLabel(welcomeText, SwingConstants.CENTER);
        textLabel.setForeground(Color.WHITE);
        textLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        textLabel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        panel.add(textLabel, BorderLayout.NORTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(true);
        progressBar.setString("Loading...");
        panel.add(progressBar, BorderLayout.SOUTH);

        window = new JWindow();
        window.setContentPane(panel);
        window.pack();
        window.setLocationRelativeTo(null); // center on screen
        window.setAlwaysOnTop(true);
        window.setVisible(true);
    }

    // Call once mod setup has finished. Waits out any remaining time toward
    // MIN_DISPLAY_MILLIS on a background thread, so it never blocks Forge's
    // own loading, then closes the window on the Swing thread.
    public static void close() {
        new Thread(() -> {
            long elapsed = System.currentTimeMillis() - shownAtMillis;
            long remaining = MIN_DISPLAY_MILLIS - elapsed;
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException ignored) {
                }
            }
            SwingUtilities.invokeLater(() -> {
                if (window != null) {
                    window.dispose();
                    window = null;
                }
            });
        }, "SoleHack-SplashCloser").start();
    }
}