package sledgehammer.conversion;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ConsoleTextArea extends JTextArea {

    private Image img;
    private int imgWidth;
    private int imgHeight;

    public ConsoleTextArea() {
        super();
        DefaultCaret caret = (DefaultCaret)this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        try{
            try {
                img = ImageIO.read(getClass().getResource("background.jpg"));
            } catch(Exception e) {
                System.err.println(e.toString());
            }
            if(img == null) {
                img = ImageIO.read(new File("mongoconversiontool" + File.separator + "background.png"));
            }
        } catch(IOException e) {
            System.err.println(e.toString());
        }
        if(img != null) {
            imgWidth = img.getWidth(null);
            imgHeight = img.getHeight(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage raster = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        g.drawImage(img,getWidth() - imgWidth,getHeight() - imgHeight,null);
    }

    public void println(Object... messages) {
        for(Object message : messages) {
            append(message + "\n");
        }
    }

    public void print(Object message) {
        append(message.toString());
    }

    public void clear() {
        setText("");
    }
}