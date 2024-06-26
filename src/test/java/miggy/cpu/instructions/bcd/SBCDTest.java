package miggy.cpu.instructions.bcd;

import m68k.cpu.Size;
import miggy.BasicSetup;
import miggy.SystemModel;
import miggy.SystemModel.CpuFlag;
import org.junit.jupiter.api.Test;

import static m68k.util.TestCpuUtil.*;

// $Revision: 21 $
public class SBCDTest extends BasicSetup {

    @Test public void testReg() {
        setInstructionAtPC(0x8101);    //sbcd d1,d0
        SystemModel.CPU.setDataRegister(0, 0x0099);
        SystemModel.CPU.setDataRegister(1, 0x0001);
        SystemModel.CPU.setCCR((byte) 0);

        int time = SystemModel.CPU.execute();

        assertEquals("Check result", 0x0098, SystemModel.CPU.getDataRegister(0));
        assertFalse("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertTrue("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C", SystemModel.CPU.isSet(CpuFlag.C));
    }

    @Test public void testMem() {
        setInstructionAtPC(0x8109);    //sbcd -(a1),-(a0)
        SystemModel.CPU.setAddrRegister(0, codebase + 100);
        SystemModel.CPU.setAddrRegister(1, codebase + 108);
        SystemModel.MEM.poke(codebase + 98, 0x0100, Size.Word);
        SystemModel.MEM.poke(codebase + 106, 0x0001, Size.Word);
        SystemModel.CPU.setCCR((byte) 0);

        int time = SystemModel.CPU.execute();

        assertEquals("Check result", 0x0199, SystemModel.MEM.peek(codebase + 98, Size.Word));
        assertTrue("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertTrue("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertTrue("Check C", SystemModel.CPU.isSet(CpuFlag.C));

        SystemModel.CPU.setPC(codebase);
        time = SystemModel.CPU.execute();
        assertEquals("Check result 2", 0x0099, SystemModel.MEM.peek(codebase + 98, Size.Word));
        assertFalse("Check X 2", SystemModel.CPU.isSet(CpuFlag.X));
        assertFalse("Check N 2", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z 2", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V 2", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C 2", SystemModel.CPU.isSet(CpuFlag.C));
    }

    @Test public void testMem2() {
        setInstructionAtPC(0x8108);    //sbcd -(a0),-(a0)
        SystemModel.CPU.setAddrRegister(0, codebase + 100);
        SystemModel.MEM.poke(codebase + 98, 0x3916, Size.Word);
        SystemModel.CPU.setCCR((byte) 0);

        int time = SystemModel.CPU.execute();

        //39-16 = 23
        assertEquals("Check result", 0x2316, SystemModel.MEM.peek(codebase + 98, Size.Word));
        assertFalse("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertFalse("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C", SystemModel.CPU.isSet(CpuFlag.C));
    }
}
