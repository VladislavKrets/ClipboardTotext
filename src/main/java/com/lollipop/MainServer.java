package com.lollipop;

import net.sourceforge.tess4j.Tesseract;
import nu.pattern.OpenCV;
import org.opencv.core.Core;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class MainServer {

    static Tesseract tesseract;

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
        ServerSocket serverSocket = new ServerSocket(9999);
        Socket socket;
        String result;
        TransparentWindow transparentWindow;
        while (true) {
            socket = serverSocket.accept();
            result = new BufferedReader(new InputStreamReader(socket.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            if (result.equals("rus")) tesseract.setLanguage("rus");
            else tesseract.setLanguage("eng");
            socket.close();
            transparentWindow = new TransparentWindow();
            transparentWindow.add(new DrawRect(transparentWindow));
            transparentWindow.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            transparentWindow.setVisible(true);
        }
    }

}