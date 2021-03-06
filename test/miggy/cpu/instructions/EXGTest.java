package miggy.cpu.instructions;

import miggy.BasicSetup;
import miggy.SystemModel;
import miggy.SystemModel.CpuFlag;

// $Revision: 21 $
public class EXGTest extends BasicSetup {
    public EXGTest(String test) {
        super(test);
    }

    public void testInstruction() {
        setInstruction(0xc141);    // exg d0,d1

        SystemModel.CPU.setDataRegister(0, 0x98765432);
        SystemModel.CPU.setDataRegister(1, 0x12345678);
        SystemModel.CPU.setCCR((byte) 0);
        int time = SystemModel.CPU.execute();

        assertEquals("Check d0", 0x12345678, SystemModel.CPU.getDataRegister(0));
        assertEquals("Check d1", 0x98765432, SystemModel.CPU.getDataRegister(1));
        assertFalse("Check X", SystemModel.CPU.isSet(CpuFlag.X));
        assertFalse("Check N", SystemModel.CPU.isSet(CpuFlag.N));
        assertFalse("Check Z", SystemModel.CPU.isSet(CpuFlag.Z));
        assertFalse("Check V", SystemModel.CPU.isSet(CpuFlag.V));
        assertFalse("Check C", SystemModel.CPU.isSet(CpuFlag.C));
    }
}
