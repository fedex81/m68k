package m68k.cpu.instructions;

import junit.framework.TestCase;
import m68k.cpu.Cpu;
import m68k.cpu.MC68000;
import m68k.memory.AddressSpace;
import m68k.memory.MemorySpace;

import static org.junit.Assert.assertNotEquals;

/**
 * Federico Berti
 * <p>
 * Copyright 2021
 */
public class MOVETest extends TestCase {

    AddressSpace bus;
    Cpu cpu;
    int stack = 0x231;

    public void setUp() {
        bus = new MemorySpace(1);    //create 1kb of memory for the cpu
        cpu = new MC68000();
        cpu.setAddressSpace(bus);
        cpu.reset();
        cpu.setAddrRegisterLong(7, stack);
    }

    //TODO enable
    public void ignoreTestMoveByteA7() {
        bus.writeWord(0, 0x1F0F); //move.b	a7,-(a7)
        cpu.setAddrRegisterLong(0, stack);
        cpu.execute();
        //a7 is decremented by 2 instead of 1
        assertEquals("Check for a7", stack & 0xFF, cpu.readMemoryByte(stack - 2));
        assertNotEquals("Check for a7", stack & 0xFF, cpu.readMemoryByte(stack - 1));
    }

    public void testMoveByte() {
        bus.writeWord(0, 0x1108); //move.b	a0,-(a0)
        cpu.setAddrRegisterLong(0, stack);
        cpu.execute();
        assertEquals("Check for a0", stack & 0xFF, cpu.readMemoryByte(stack - 1));
    }

    public void testMoveWord() {
        bus.writeWord(0, 0x3108); //move.w	a0,-(a0)
        cpu.setAddrRegisterLong(0, stack);
        cpu.execute();
        assertEquals("Check for a0", stack, cpu.readMemoryWord(stack - 2));
    }

    public void testMoveLong() {
        bus.writeWord(0, 0x22c9); //move.l	a1,(a1)+
        cpu.setAddrRegisterLong(1, stack);
        cpu.execute();
        assertEquals("Check for a1", stack, cpu.readMemoryLong(stack));
    }

}
