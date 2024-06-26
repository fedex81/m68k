package m68k.cpu.instructions;

import m68k.cpu.Cpu;
import m68k.cpu.MC68000;
import m68k.memory.AddressSpace;
import m68k.memory.MemorySpace;
import m68k.util.TestCpuUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008-2010, Tony Headford
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//  following conditions are met:
//
//    o  Redistributions of source code must retain the above copyright notice, this list of conditions and the
//       following disclaimer.
//    o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
//       following disclaimer in the documentation and/or other materials provided with the distribution.
//    o  Neither the name of the M68k Project nor the names of its contributors may be used to endorse or promote
//       products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
*/
public class ADDTest {
    AddressSpace bus;
    Cpu cpu;

    @BeforeEach
    public void setUp() {
        bus = new MemorySpace(1);    //create 1kb of memory for the cpu
        cpu = new MC68000();
        cpu.setAddressSpace(bus);
        cpu.reset();
        cpu.setAddrRegisterLong(7, 0x200);
    }

    @Test
    public void testADD() {
        cpu.setDataRegisterByte(0, 0x40);
        cpu.setDataRegisterByte(1, 0x80);
        TestCpuUtil.writeCodeAndSetPc(cpu, bus, 4, 0xd001); // add.b d1,d0

        int ticks = cpu.execute();
        assertEquals(6, cpu.getPC());
        assertEquals(0xc0, cpu.getDataRegisterByte(0));
        assertEquals(0x80, cpu.getDataRegisterByte(1));
        assertEquals(4, ticks);
        assertFalse(cpu.isFlagSet(Cpu.C_FLAG));
        assertFalse(cpu.isFlagSet(Cpu.V_FLAG));
        assertFalse(cpu.isFlagSet(Cpu.Z_FLAG));
        assertTrue(cpu.isFlagSet(Cpu.N_FLAG));
        assertFalse(cpu.isFlagSet(Cpu.X_FLAG));


        cpu.setDataRegisterWord(0, 0x8000);
        cpu.setDataRegisterWord(1, 0x8500);
        TestCpuUtil.writeCodeAndSetPc(cpu, bus, 4, 0xd041); // add.w d1,d0

        ticks = cpu.execute();
        assertEquals(6, cpu.getPC());
        TestCpuUtil.assertEquals("d0", 0x0500, cpu.getDataRegisterWord(0));
        TestCpuUtil.assertEquals("d1", 0x8500, cpu.getDataRegisterWord(1));
        assertEquals(4, ticks);
        TestCpuUtil.assertTrue("c-flag", cpu.isFlagSet(Cpu.C_FLAG));
        TestCpuUtil.assertTrue("v-flag", cpu.isFlagSet(Cpu.V_FLAG));
        TestCpuUtil.assertFalse("z-flag", cpu.isFlagSet(Cpu.Z_FLAG));
        TestCpuUtil.assertFalse("n-flag", cpu.isFlagSet(Cpu.N_FLAG));
        TestCpuUtil.assertTrue("x-flag", cpu.isFlagSet(Cpu.X_FLAG));

        cpu.setDataRegisterLong(0, 0xfffffffc);
        cpu.setDataRegisterLong(1, 0x04);
        TestCpuUtil.writeCodeAndSetPc(cpu, bus, 4, 0xd081); // add.l d1,d0

        ticks = cpu.execute();
        assertEquals(6, cpu.getPC());
        TestCpuUtil.assertEquals("d0", 0, cpu.getDataRegisterLong(0));
        TestCpuUtil.assertEquals("d1", 0x04, cpu.getDataRegisterWord(1));
        assertEquals(8, ticks);
        TestCpuUtil.assertTrue("c-flag", cpu.isFlagSet(Cpu.C_FLAG));
        TestCpuUtil.assertFalse("v-flag", cpu.isFlagSet(Cpu.V_FLAG));
        TestCpuUtil.assertTrue("z-flag", cpu.isFlagSet(Cpu.Z_FLAG));
        TestCpuUtil.assertFalse("n-flag", cpu.isFlagSet(Cpu.N_FLAG));
        TestCpuUtil.assertTrue("x-flag", cpu.isFlagSet(Cpu.X_FLAG));
    }
}
