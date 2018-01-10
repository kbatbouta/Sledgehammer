package sledgehammer.conversion;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ConsoleTextArea extends JTextArea {

    private final JScrollPane scroll;
    private Image img;
    private int imgWidth;
    private int imgHeight;

    public ConsoleTextArea(JScrollPane scroll) {
        super();
        this.scroll = scroll;
        DefaultCaret caret = (DefaultCaret)this.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        try{
            InputStream is = null;
            try {
                is = getClass().getResourceAsStream("background.png");
                img = ImageIO.read(is);
            } catch(Exception e) {
                System.out.println("Error loading image:");
                System.out.println(e.toString());
            }
            if(is != null) {
                is.close();
            }
            if(img == null) {
                img = ImageIO.read(new File("mongoconversiontool" + File.separator + "src" + File.separator
                        + "main" + File.separator + "resources" + File.separator + "background.png"));
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
        paintImmediately();
    }

    public void print(Object message) {
        append(message.toString());
        paintImmediately();
    }

    public void clear() {
        setText("");
        paintImmediately();
    }

    public void printStackTrace(Exception e) {
        println(e.getClass().getName() + ": " + e.toString());
        StackTraceElement[] stack = e.getStackTrace();
        for(StackTraceElement stackElement : stack) {
            println("\t" + stackElement.toString());
        }
        paintImmediately();
    }

    public void paintImmediately() {
        scrollDown();
    }

    public void scrollDown(){
        this.repaint(0);
        setCaretPosition(getText().length());
    }
}