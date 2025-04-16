package m68k.cpu.instructions;

import m68k.cpu.Cpu;
import m68k.cpu.CpuConfig;
import m68k.cpu.MC68000;
import m68k.memory.AddressSpace;
import m68k.memory.MemorySpace;
import m68k.util.TestCpuUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * ${FILE}
 * <p>
 * Federico Berti
 * <p>
 * Copyright 2019
 */
public class TasTest {

    AddressSpace bus;
    Cpu cpu;
    static CpuConfig tasBroken = new CpuConfig(true, false, false);

    @BeforeEach public void setUp() {
        setUp(CpuConfig.DEFAULT_CONFIG);
    }

    private void setUp(CpuConfig cpuConfig) {
        //create 1kb of memory for the cpu
        bus = new MemorySpace(1);

        cpu = new MC68000(cpuConfig);
        cpu.setAddressSpace(bus);
        cpu.reset();
        cpu.setAddrRegisterLong(7, 0x200);
    }


    // 0100 1010 1100 0000
    @Test
    public void testTasRegOk() {
        bus.writeWord(4, 0x4AC0);    //TAS D0
        cpu.setPC(4);
        cpu.setDataRegisterLong(0, 0);

        cpu.execute();
        int res = cpu.getDataRegisterLong(0);
        TestCpuUtil.assertEquals(1, (res & 0xFF) >> 7);
    }

    @Test public void testTasRegBroken() {
        setUp(tasBroken);
        bus.writeWord(4, 0x4AC0);    //TAS D0
        cpu.setPC(4);
        cpu.setDataRegisterLong(0, 0);

        cpu.execute();
        int res = cpu.getDataRegisterLong(0);
        TestCpuUtil.assertEquals(1, (res & 0xFF) >> 7);
    }

    @Test public void testTasMemOk() {
        int val = 0x20;
        int memAddr = 100;
        bus.writeWord(4, 0x4AD0);    //TAS (A0)
        bus.writeByte(memAddr, val);
        cpu.setPC(4);
        cpu.setAddrRegisterLong(0, memAddr);

        cpu.execute();
        int res = bus.readByte(memAddr);
        TestCpuUtil.assertEquals(1, (res & 0xFF) >> 7);
        TestCpuUtil.assertEquals(val | 0x80, res);
    }

    @Test public void testTasMemBroken() {
        setUp(tasBroken);
        bus.writeWord(4, 0x4AD0);    //TAS (A0)
        bus.writeByte(100, 0x20);
        cpu.setPC(4);
        cpu.setAddrRegisterLong(0, 100);

        cpu.execute();
        int res = bus.readByte(100);
        TestCpuUtil.assertEquals(0, (res & 0xFF) >> 7);
        TestCpuUtil.assertEquals(0x20, res);
    }
}
