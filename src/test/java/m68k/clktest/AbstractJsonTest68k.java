package m68k.clktest;

import com.google.gson.Gson;
import m68k.clktest.json_schema.JsonTestUtil;
import m68k.clktest.json_schema.SingleInstructionRecord;
import m68k.clktest.json_schema.SingleInstructionRecord.Final;
import m68k.clktest.json_schema.SingleInstructionRecord.Initial;
import m68k.cpu.Instruction;
import m68k.cpu.MC68000;
import m68k.cpu.instructions.*;
import m68k.util.FileUtil;
import m68k.util.MC68000Helper;
import m68k.util.MC68000Helper.M68kState;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static m68k.clktest.json_schema.JsonTestUtil.*;
import static m68k.cpu.Cpu.*;

/**
 * Federico Berti
 * <p>
 * Copyright 2022
 */
public abstract class AbstractJsonTest68k {

    public static final int MEM_SIZE_KB = 0x4000;

    protected final static Predicate<Instruction> instToSr = inst -> {
        String n = inst.getClass().getName();
        return n.contains(ORI_TO_SR.class.getSimpleName()) || n.contains(EORI_TO_SR.class.getSimpleName()) ||
                n.contains(ANDI_TO_SR.class.getSimpleName()) || n.contains(MOVE_TO_SR.class.getSimpleName());
    };

    protected final static Predicate<Instruction> isBranchOp = inst -> {
        String n = inst.getClass().getName();
        return n.contains(RTE.class.getSimpleName()) || n.contains(RTS.class.getSimpleName())
                || n.contains(RTR.class.getSimpleName()) || n.contains(JSR.class.getSimpleName())
                || n.contains(JMP.class.getSimpleName()) || n.contains(DBcc.class.getSimpleName())
                || n.contains(Bcc.class.getSimpleName());
    };

    protected final static BiPredicate<Instruction, Class<?>> isInstType = (inst, clazz) ->
            inst.getClass().getName().contains(clazz.getSimpleName());

    protected MC68000 provider;
    protected TestAddressSpace memory;


    @BeforeEach
    public void setup() {
        provider = new MC68000();
        memory = JsonTestUtil.createTestAddressSpace(MEM_SIZE_KB);
        provider.setAddressSpace(memory);
    }

    protected static Stream<String> fileProviderBase(String path) {
        File fpath = new File(path);
        File[] files = fpath.listFiles();
        Predicate<File> validFile = f -> !f.isDirectory() && f.getName().endsWith(".json.gz");
        return Arrays.stream(files).filter(validFile).map(f -> f.getName()).sorted();
    }

    protected StringBuilder testJsonInternal(String path, String fileName) {
        Gson a = new Gson();
        Path p = Paths.get(path, fileName);
        StringBuilder err = new StringBuilder();
        String s = new String(FileUtil.readBinaryFile(p, "json"));
        SingleInstructionRecord[] m = a.fromJson(s, SingleInstructionRecord[].class);
        for (SingleInstructionRecord id : m){
            if(id.getName() == null){
                continue;
            }
            String res = testOne(id);
            if(res!= null && res.length() > 0){
                err.append(res).append("\n");
//                break;
            }
        }

        return err;
    }

    public String testOne(SingleInstructionRecord data){
        M68kState start = toStateObject(data.getInitial());
        writeMemoryAtPc(start.pc, data.getInitial().prefetch);
        M68kState expected = toStateObject(data.getFinal());
        fromStateObject(start, provider);
//        System.out.println(MC68000Helper.dumpOp(provider.getM68k(), start.pc));
        boolean error = runSingle();
        if(error){
            return null;
        }
        M68kState actual = JsonTestUtil.toStateObject(provider);
        //        Assert.assertEquals("Exp: " + expected + "\nAct: " + actual, expected, actual);
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
        if(!isMatch) {
                return data.name  + "\n" + MC68000Helper.dumpOp(provider, start.pc) + " -- ERROR\n" +
                        "Before: " + start +
                        "\nExpect: " + expected +
                        "\nActual: " + actual + "\n"+memRes+"\n";
        }
        return null;
    }

    protected boolean runSingle(){
        Exception e = null;
        try {
            provider.execute();
        } catch (Exception e1){
            e = e1;
        }
        return handleExceptions(e);
    }

    //TODO address error, do not fail the test but it should be handled
    private boolean handleExceptions(Exception e){
        if(memory.addressError){
            memory.addressError = false;
            return true;
        }
        //STOP can generate an exception
        if(e != null){
            Instruction inst = provider.getInstructionFor(provider.getOpcode());
            if(!isInstType.test(inst, STOP.class)) {
                e.printStackTrace();
                return true;
            }
        }
        return false;
    }

    public boolean handleSpecialCases(M68kState start, M68kState expected, M68kState actual){
        int opcode = start.opcode;
        Instruction inst = provider.getInstructionFor(opcode);
        //ignore T0 trace bit (always 0) and T1
        actual.sr &= ~(TRACE_FLAG_T1|TRACE_FLAG_T0);
        expected.sr &= ~(TRACE_FLAG_T1|TRACE_FLAG_T0);

        //TODO DIVU divByZero behaves differently
        //ignores the N-flag for DIV*, different results on overflow
        boolean isDiv = (opcode & 0x80C0) == 0x80C0;
        if(isDiv && (expected.sr & V_FLAG) > 0) {
            expected.sr &= 0xFFF7;
            actual.sr &= 0xFFF7;
        }
        return false;
    }

    //TODO check who is right?
    //switch supervisor state, ignore A7
    protected void handleA7_OnUserStateSwitch(M68kState start, M68kState expected, M68kState actual){
        int prevSv = start.sr & SUPERVISOR_FLAG;
        int sv = actual.sr & SUPERVISOR_FLAG;
        if(prevSv != sv){
            expected.ar[7] = actual.ar[7];
        }
    }

    public M68kState toStateObject(Initial is){
        return toStateObject(is, 0);
    }

    public M68kState toStateObject(Initial is, int pcDelta){
        M68kState state = new M68kState();
        state.ar = toArIntArray.apply(is);
        state.dr = toDrIntArray.apply(is);
        state.pc = is.pc.intValue() + pcDelta;
        state.sr = is.sr.intValue();
        state.usp = is.usp.intValue();
        state.ssp = is.ssp.intValue();
        state.ar[7] = (state.sr & SUPERVISOR_FLAG) > 0 ? state.ssp : state.usp;
        JsonTestUtil.writeMemory(is.ram, memory);
        state.opcode = memory.readWord(state.pc);
        return state;
    }

    public M68kState toStateObject(Final is){
        M68kState state = new M68kState();
        state.ar = toArIntArrayF.apply(is);
        state.dr = toDrIntArrayF.apply(is);
        state.pc = is.pc.intValue();
        state.sr = is.sr.intValue();
        state.usp = is.usp.intValue();
        state.ssp = is.ssp.intValue();
        state.ar[7] = (state.sr & SUPERVISOR_FLAG) > 0 ? state.ssp : state.usp;
        return state;
    }

    public void writeMemoryAtPc(int pc, List<Long> prefetch){
        for (int i = 0; i < prefetch.size(); i++) {
            memory.writeWord(pc + (i << 1), prefetch.get(i).intValue());
        }
    }
    public static final Function<Initial, int[]> toArIntArray = is ->
            new int[]{is.a0.intValue() , is.a1.intValue(), is.a2.intValue(), is.a3.intValue(), is.a4.intValue(),
                    is.a5.intValue(),is.a6.intValue(), Integer.MAX_VALUE};
    public static final Function<Initial, int[]> toDrIntArray = is -> new int[]{is.d0.intValue(),
            is.d1.intValue(), is.d2.intValue(), is.d3.intValue(), is.d4.intValue(),
            is.d5.intValue(), is.d6.intValue(), is.d7.intValue()};
    public static final Function<Final, int[]> toArIntArrayF = is ->
            new int[]{is.a0.intValue() , is.a1.intValue(), is.a2.intValue(), is.a3.intValue(), is.a4.intValue(),
                    is.a5.intValue(),is.a6.intValue(), Integer.MAX_VALUE};
    public static final Function<Final, int[]> toDrIntArrayF = is -> new int[]{is.d0.intValue(),
            is.d1.intValue(), is.d2.intValue(), is.d3.intValue(), is.d4.intValue(),
            is.d5.intValue(), is.d6.intValue(), is.d7.intValue()};
}