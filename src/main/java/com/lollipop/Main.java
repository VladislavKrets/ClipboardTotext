package com.lollipop;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import nu.pattern.OpenCV;
import org.opencv.core.Core;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class Main {

    static Tesseract tesseract;
    static ServerSocket serverSocket;

    static {
        OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.setProperty("TESSDATA_PREFIX", "/usr/share/tesseract-ocr/4.00/tessdata");
        tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/4.00/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("user_defined_dpi", "300");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        serverSocket = new ServerSocket(9999);
        Socket socket;
        String result;
        TransparentWindow transparentWindow;
        while (true) {
            socket = serverSocket.accept();
            result = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println("Result " + result);
            if (result.equals("rus")) tesseract.setLanguage("rus");
            else tesseract.setLanguage("eng");
            socket.close();
            transparentWindow = new TransparentWindow();
            transparentWindow.add(new DrawRect(transparentWindow));
            transparentWindow.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            transparentWindow.setVisible(true);
            System.out.println("visible");
        }
    }

    public static class TransparentWindow extends JFrame {

        public TransparentWindow() {
            setUndecorated(true);
            setBackground(new Color(1.0f, 1.0f, 1.0f, 0.2f));
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
        }
    }

    public static class DrawRect extends JPanel {
        int x, y, x2, y2;
        private Point prevPoint;
        private Point currentPoint;
        TransparentWindow transparentWindow;
        private boolean clear = false;

        DrawRect(TransparentWindow transparentWindow) {
            x = y = x2 = y2 = 0; //
            this.transparentWindow = transparentWindow;
            setOpaque(false);
            DrawRect.MyMouseListener listener = new DrawRect.MyMouseListener();
            addMouseListener(listener);
            setBackground(new Color(1.0f, 1.0f, 1.0f, 0f));
            addMouseMotionListener(listener);
            setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
        }

        public void setStartPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setEndPoint(int x, int y) {
            x2 = (x);
            y2 = (y);
        }

        public void drawPerfectRect(Graphics g, int x, int y, int x2, int y2) {
            int px = Math.min(x, x2);
            int py = Math.min(y, y2);
            int pw = Math.abs(x - x2);
            int ph = Math.abs(y - y2);
            g.drawRect(px, py, pw, ph);
        }

        class MyMouseListener extends MouseAdapter {

            public void mousePressed(MouseEvent e) {
                clear = false;
                prevPoint = MouseInfo.getPointerInfo().getLocation();
                setStartPoint(e.getX(), e.getY());
            }

            public void mouseDragged(MouseEvent e) {
                clear = false;
                setEndPoint(e.getX(), e.getY());
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                clear = true;
                setEndPoint(e.getX(), e.getY());
                transparentWindow.setBackground(new Color(1.0f, 1.0f, 1.0f, 0f));
                repaint();
                currentPoint = MouseInfo.getPointerInfo().getLocation();
                int minX = Math.min(prevPoint.x, currentPoint.x);
                int minY = Math.min(prevPoint.y, currentPoint.y);
                int maxX = Math.max(prevPoint.x, currentPoint.x);
                int maxY = Math.max(prevPoint.y, currentPoint.y);
                int width = maxX - minX;
                int height = maxY - minY;
                Rectangle screenRect = new Rectangle(minX, minY, width, height);
                BufferedImage capture = null;
                try {
                    capture = new Robot().createScreenCapture(screenRect);
                } catch (AWTException awtException) {
                    awtException.printStackTrace();
                }
                String line = null;
                try {
                    line = tesseract.doOCR(capture);
                } catch (TesseractException tesseractException) {
                    tesseractException.printStackTrace();
                }
                StringSelection selection = new StringSelection(line.trim());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                transparentWindow.setVisible(false);
                transparentWindow.dispatchEvent(new WindowEvent(transparentWindow, WindowEvent.WINDOW_CLOSING));
            }
        }

        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g.setColor(getBackground());
            Rectangle r = g.getClipBounds();
            g.fillRect(r.x, r.y, r.width, r.height);
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            g.clearRect(0, 0, dimension.width, dimension.height);
            if (!clear) {
                g.setColor(Color.DARK_GRAY);
                g2.setStroke(new BasicStroke(3));
                drawPerfectRect(g, x, y, x2, y2);
            }
        }
    }
}