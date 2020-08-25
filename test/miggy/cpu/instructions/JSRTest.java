package miggy.cpu.instructions;

import m68k.cpu.Size;
import miggy.BasicSetup;
import miggy.SystemModel;
import miggy.SystemModel.CpuFlag;

// $Revision: 21 $
public class JSRTest extends BasicSetup {
    public JSRTest(String test) {
        super(test);
    }

    public void testInstruction() {
        setInstruction(0x4e90);    //jsr (a0)
        SystemModel.CPU.setAddrRegister(0, codebase + 50);

        SystemModel.CPU.setCCR((byte) 0);
        int time = SystemModel.CPU.execute();

        assertEquals("Check PC", codebase + 50, SystemModel.CPU.getPC());
        assertEquals("Check Stack", codebase + 2, SystemModel.MEM.peek(SystemModel.CPU.getAddrRegister(7), Size.Long));
        assertFalse("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertFalse("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C", SystemModel.CPU.isSet(CpuFlag.C));
    }
}
