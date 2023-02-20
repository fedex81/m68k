package jaty68k;

import m68k.memory.AddressSpace;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;

/**
 *
 * Java layer adapted from the jaty68k project.
 * https://github.com/alexwinston/Jaty68k
 *
 */
public class Console extends JFrame {
    private final JatyMonitor.JatyCpu cpu;
    FileWriter aWriter;

    JTextArea aTextArea = new JTextArea();

    PrintStream aPrintStream = new PrintStream(new FilteredStream(new ByteArrayOutputStream()));
    boolean enterKey;
    char[] charBuffer = new char[3];

    public Console(final JatyMonitor.JatyCpu cpu, final AddressSpace memory) {
        this.cpu = cpu;
        try {
            aWriter = new FileWriter("c.log", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setSize(600, 400);
        add("Center", new JScrollPane(aTextArea));
        setVisible(true);

        System.setOut(aPrintStream); // catches System.out messages
//        System.setErr(aPrintStream); // catches error messages

        ((AbstractDocument) aTextArea.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string,
                                     AttributeSet attr) throws BadLocationException {
                fb.insertString(offset, string, attr);
            }
        });
        aTextArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
//                System.out.print(Integer.toHexString(c));
                memory.writeByte(0x7c000, 0x0);
                memory.writeByte(0x78000, c);
                cpu.raiseSafeInterrupt(2);

                e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
//                if(e.getKeyCode() == KeyEvent.VK_ENTER){
//                    e.consume();
//                }
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(new KeyEventDispatcher() {
                    public boolean dispatchKeyEvent(KeyEvent ke) {
                        if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextArea) {
                            if (ke.getID() == KeyEvent.KEY_PRESSED) {
                                int key = ke.getKeyCode();
                                if (key == KeyEvent.VK_ENTER) {
                                    enterKey = true;
                                    memory.writeByte(0x7c000, 0x0);
                                    memory.writeByte(0x78000, 0xd);
                                    cpu.raiseSafeInterrupt(2);

                                    ke.consume();
                                } else if (key == KeyEvent.VK_BACK_SPACE) {
                                    enterKey = true;
                                    memory.writeByte(0x7c000, 0x0);
                                    memory.writeByte(0x78000, 0x8);
                                    cpu.raiseSafeInterrupt(2);

                                    ke.consume();
                                }
                            }
                            if (ke.getID() == KeyEvent.KEY_TYPED && enterKey) ke.consume();
                            if (ke.getID() == KeyEvent.KEY_RELEASED) enterKey = false;
                        }
                        return false;
                    }
                });
    }

    class FilteredStream extends FilterOutputStream {
        public FilteredStream(OutputStream aStream) {
            super(aStream);
        }

        public void write(byte b[]) throws IOException {
            String aString = new String(b);
            aTextArea.append(aString);

            int len = aTextArea.getDocument().getLength();
            aTextArea.setCaretPosition(len);
        }

        public void write(byte b[], int off, int len) throws IOException {
            // http://www.tldp.org/HOWTO/Keyboard-and-Console-HOWTO-5.html
            char currentChar = (char)b[0];
            charBuffer[0] = charBuffer[1];
            charBuffer[1] = charBuffer[2];
            charBuffer[2] = currentChar;
            if (charBuffer[0] == 0x8 && charBuffer[1] == 0x20 && charBuffer[2] == 0x8 ||
                    charBuffer[1] == 0x8 && charBuffer[2] == 0x20) {
                return;
            }
            if (currentChar == '\b') {
                try {
                    Document document = aTextArea.getDocument();
                    document.remove(document.getLength() - 1, 1);
                    return;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            String aString = new String(b, off, len);
            aTextArea.append(aString);

            int length = aTextArea.getDocument().getLength();
            aTextArea.setCaretPosition(length);
//            FileWriter aWriter = new FileWriter("c.log", true);
//            aWriter.write(aString);
        }
    }
}