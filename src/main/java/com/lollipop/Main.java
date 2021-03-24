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

}