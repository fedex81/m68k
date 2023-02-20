package jaty68k;

import m68k.memory.AddressSpace;

import java.io.FileWriter;
import java.nio.ByteBuffer;

/**
 *
 * Java layer adapted from the jaty68k project.
 * https://github.com/alexwinston/Jaty68k
 *
 */
public class KatyMemorySpace implements AddressSpace
{
    private boolean debug = false;
    private ByteBuffer buffer;
    private int size;

    private int INPUT_RDF = 0x7C000;
    private int INPUT_ADDRESS = 0x78000;
    private int OUTPUT_TXE = 0x7D000;
    private int OUTPUT_ADDRESS = 0x7A000;

    private FileWriter aWriter;

    public KatyMemorySpace(int size)
    {
        try {
            aWriter = new FileWriter("a.log", false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.size = size + 1;
        buffer = ByteBuffer.allocateDirect(size + 1);
        writeByte(OUTPUT_TXE, 0x2);

//            aWriter.close();
    }

    private void debug(String s) {
        if (debug) {
            try {
                aWriter.write(s);
                aWriter.write('\n');
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void reset()
    {
    }

    public int getStartAddress()
    {
        return 0;
    }

    public int getEndAddress()
    {
        return size;
    }

    public int readByte(int addr)
    {
        try {
            int v = buffer.get(addr);
            int b = v & 0x00ff;
//            debug(String.format("Read byte %s at address %s", Integer.toHexString(b), Integer.toHexString(addr)));
            if (addr == INPUT_ADDRESS)
                writeByte(INPUT_RDF, 0x1);
            return b;
        } catch (Exception e) {
            System.out.println(String.format("Invalid read byte at address %s", Integer.toHexString(addr)));
            throw new RuntimeException(e);
        }
    }

    public int readWord(int addr)
    {
        try {
            int v = buffer.getShort(addr);
            int w = v & 0x0000ffff;
//            debug(String.format("Read word %s at address %s", Integer.toHexString(w), Integer.toHexString(addr)));

            return w;
        } catch (Exception e) {
            System.out.println(String.format("Invalid read word at address %s", Integer.toHexString(addr)));
            throw new RuntimeException(e);
        }
    }

    public int readLong(int addr)
    {
        try {
            int l = buffer.getInt(addr);
//		    debug(String.format("Read long %s at address %s", Integer.toHexString(l), Integer.toHexString(addr)));

            if (addr == INPUT_ADDRESS)
                writeByte(INPUT_RDF, 0x1);
            return l;
        } catch (Exception e) {
            System.out.println(String.format("Invalid read long at address %s", Integer.toHexString(addr)));
            throw new RuntimeException(e);
        }
    }

    public void writeByte(int addr, int value)
    {
//        debug(String.format("Write byte %s to address %s", Integer.toHexString(value), Integer.toHexString(addr)));
        byte b = (byte) (value & 0x00ff);
        buffer.put(addr, b);

        if (addr == OUTPUT_ADDRESS)
            System.out.print((char)b);
    }

    public void writeWord(int addr, int value)
    {
        buffer.putShort(addr, (short)(value & 0x0000ffff));
    }

    public void writeLong(int addr, int value)
    {
//        debug(String.format("Write long %s to address %s", Integer.toHexString(value), Integer.toHexString(addr)));
        buffer.putInt(addr, value);
    }

    public int internalReadByte(int addr)
    {
        return readByte(addr);
    }

    public int internalReadWord(int addr)
    {
        return readWord(addr);
    }

    public int internalReadLong(int addr)
    {
        return readLong(addr);
    }

    public void internalWriteByte(int addr, int value)
    {
        writeByte(addr, value);
    }

    public void internalWriteWord(int addr, int value)
    {
        writeWord(addr, value);
    }

    public void internalWriteLong(int addr, int value)
    {
        writeLong(addr, value);
    }

    public int size()
    {
        return size;
    }
}
