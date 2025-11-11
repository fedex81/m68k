package m68k.singlesteptests;

import m68k.clktest.AbstractJsonTest68k;
import m68k.clktest.json_schema.JsonTestUtil;
import m68k.clktest.json_schema.SingleInstructionRecord;
import m68k.cpu.Instruction;
import m68k.cpu.instructions.CHK;
import m68k.cpu.instructions.STOP;
import m68k.cpu.instructions.UNKNOWN;
import m68k.util.MC68000Helper;
import m68k.util.MC68000Helper.M68kState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static m68k.clktest.json_schema.JsonTestUtil.*;
import static m68k.cpu.Cpu.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2022
 */
public class SingleStepJsonTest68k extends AbstractJsonTest68k {

    public static String path_ss = "src/test/resources/singlestep_m68k_202511";

    static Stream<String> fileProviderSS() {
        File fpath = new File(path_ss);
        File[] files = fpath.listFiles();
        Predicate<File> validFile = f -> !f.isDirectory() && f.getName().endsWith(".json.zip");
        return Arrays.stream(files).filter(validFile).map(f -> f.getName()).sorted();
    }

    @MethodSource("fileProviderSS")
    @ParameterizedTest
    public void testJsonSS(String fileName) {
        StringBuilder err = testJsonInternal(path_ss, fileName);
        Assertions.assertEquals(0, err.length(), err.toString());
    }

    public String testOne(SingleInstructionRecord data){
        M68kState start = toStateObject(data.getInitial(), -4);
        M68kState expected = toStateObject(data.getFinal());
        fromStateObject(start, provider);
        final int startPc = provider.getPC();
//        System.out.println(MC68000Helper.dumpOp(provider.getM68k(), start.pc));
        boolean error = runSingle();
        if(error){
            return null;
        }
        M68kState actual = toStateObjectAdjustPc();
        StringBuilder memRes = checkMemory(data.getFinal().ram, memory);
        boolean isMatch = expected.equals(actual) && memRes.length() == 0;
        if(!isMatch){
            boolean ignore = handleSpecialCases(start, expected, actual);
            if(ignore){
                return null;
            }
            //retry
            memRes = checkMemory(data.getFinal().ram, memory);
            isMatch = expected.equals(actual) && memRes.length() == 0;
        }
        String res = null;
        if(!isMatch) {
                res = data.name  + "\n" + MC68000Helper.dumpOp(provider, startPc) + " -- ERROR\n" +
                        "Before: " + start +
                        "\nExpect: " + expected +
                        "\nActual: " + actual +
                        "\n"+memRes+"\n";
        }
        eraseMemory(data.getInitial().ram, memory);
        return res;
    }

    private M68kState toStateObjectAdjustPc(){
        provider.setPC(provider.getPC() + 4);
        return JsonTestUtil.toStateObject(provider);
    }

    public boolean handleSpecialCases(M68kState start, M68kState expected, M68kState actual){
        int opcode = start.opcode;
        Instruction inst = provider.getInstructionFor(opcode);
        Assertions.assertFalse(inst instanceof UNKNOWN);
        //ignore T0 trace bit (always 0) and T1
        actual.sr &= ~(TRACE_FLAG_T1|TRACE_FLAG_T0);
        expected.sr &= ~(TRACE_FLAG_T1|TRACE_FLAG_T0);

        //ignores the Z,N-flag for DIV*, different results on overflow
        boolean isDiv = (opcode & 0x80C0) == 0x80C0 || (opcode & 0x81C0) == 0x81C0;
        if(isDiv && (expected.sr & V_FLAG) > 0) {
            expected.sr &= ~(N_FLAG | Z_FLAG);
            actual.sr &= ~(N_FLAG | Z_FLAG);
        }
        //ignore N flag, lots of edge cases that are undefined
        if(isInstType.test(inst, CHK.class)){
            if((expected.sr & N_FLAG) != (actual.sr & N_FLAG)){
                actual.sr &= ~N_FLAG;
                actual.sr |= expected.sr & N_FLAG;
            }
        }
        //STOP needs to tweak the PC
        if(isInstType.test(inst, STOP.class)){
            if(expected.pc == actual.pc - 4){
                expected.pc = actual.pc;
            }
        }
        return false;
    }
}