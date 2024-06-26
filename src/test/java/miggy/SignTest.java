package miggy;

import m68k.cpu.CpuUtils;
import m68k.cpu.Size;
import m68k.util.TestCpuUtil;
import org.junit.jupiter.api.Test;

// $Revision: 21 $

public class SignTest extends BasicSetup {

    @Test public void testSignExtendByte() {
        SystemModel.MEM.poke(codebase, 0x80, Size.Byte);

        int val = SystemModel.MEM.peek(codebase, Size.Byte);

        TestCpuUtil.assertTrue("Byte not sign extended", val >= 0);

        val = (byte) SystemModel.MEM.peek(codebase, Size.Byte);

        TestCpuUtil.assertTrue("Byte sign extended", val < 0);
    }


    @Test public void testSignExtendWord() {
        SystemModel.MEM.poke(codebase, 0x8000, Size.Word);

        int val = SystemModel.MEM.peek(codebase, Size.Word);

        TestCpuUtil.assertTrue("Word not sign extended", val >= 0);

        val = (short) SystemModel.MEM.peek(codebase, Size.Word);

        TestCpuUtil.assertTrue("Word sign extended", val < 0);
    }

    @Test public void testSignExtendFetchByte() {
        //fetch will always read 2 bytes if Size.Byte
        SystemModel.MEM.poke(codebase, 0x0080, Size.Word);

        int val = SystemModel.CPU.fetch(Size.Byte);
        TestCpuUtil.assertTrue("Byte not sign extended", val >= 0);
        TestCpuUtil.assertEquals("Byte fetch", 0x0080, val);

        //reset PC
        SystemModel.CPU.setPC(codebase);
        val = (byte) SystemModel.CPU.fetch(Size.Byte);
        TestCpuUtil.assertTrue("Byte sign extended", val < 0);
    }

    @Test public void testSignExtendFetchWord() {
        SystemModel.MEM.poke(codebase, 0x8000, Size.Word);

        int val = SystemModel.CPU.fetch(Size.Word);
        TestCpuUtil.assertTrue("Word not sign extended", val >= 0);
        TestCpuUtil.assertEquals("Word fetch", 0x8000, val);

        //reset PC
        SystemModel.CPU.setPC(codebase);
        val = (short) SystemModel.CPU.fetch(Size.Word);
        TestCpuUtil.assertTrue("Word sign extended", val < 0);
    }

    @Test public void testSizeSignExtend() {
        int val = 0x0080;

        int result = CpuUtils.signExtendByte(val);
        TestCpuUtil.assertTrue("Byte sign extended", result < 0);

        val = 0x8000;
        result = CpuUtils.signExtendWord(val);
        TestCpuUtil.assertTrue("Word sign extended", result < 0);
    }
}
