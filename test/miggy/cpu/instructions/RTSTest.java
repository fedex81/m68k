package miggy.cpu.instructions;

import m68k.cpu.Size;
import miggy.BasicSetup;
import miggy.SystemModel;
import miggy.SystemModel.CpuFlag;

// $Revision: 21 $
public class RTSTest extends BasicSetup {
    public RTSTest(String test) {
        super(test);
    }

    public void testReturn() {
        setInstruction(0x4e75);    //rts

        SystemModel.CPU.setCCR((byte) 0);
        SystemModel.CPU.push(codebase + 100, Size.Long);

        int time = SystemModel.CPU.execute();

        assertEquals("Check PC restored", codebase + 100, SystemModel.CPU.getPC());
        assertFalse("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertFalse("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C", SystemModel.CPU.isSet(CpuFlag.C));
    }
}
