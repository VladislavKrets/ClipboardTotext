package com.lollipop;

import javax.swing.*;
import java.awt.*;

public class TransparentWindow extends JFrame {

    public TransparentWindow() {
        setUndecorated(true);
        setBackground(new Color(1.0f, 1.0f, 1.0f, 0.2f));
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
    }
}
