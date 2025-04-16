package m68k.cpu;

import m68k.cpu.instructions.*;
import m68k.util.LogHelper;
import org.slf4j.Logger;

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
public class MC68000 extends CpuCore implements InstructionSet
{
	private final static Logger LOG = LogHelper.getLogger(MC68000.class.getSimpleName());
	protected final Instruction[] i_table;
	protected final Instruction unknown;
	protected int loaded_ops;

	protected final CpuConfig cpuConfig;

	public MC68000() {
		this(CpuConfig.DEFAULT_CONFIG);
	}

	public MC68000(CpuConfig c){
		cpuConfig = c;
		i_table = new Instruction[NUM_OPCODES];
		unknown = new UNKNOWN(this);
		for(int i = 0; i < NUM_OPCODES; i++)
			i_table[i] = unknown;
		loaded_ops = 0;
		loadInstructionSet();
		LOG.info("Using CpuConfig: {}", cpuConfig);
	}

	@Override
	public int execute() {
		if(cpuConfig.enablePrefetch){
			return executePrefetch();
		} else {
			return executeNoPrefetch();
		}
	}

	private int executePrefetch() {
		//save the PC address
		currentInstructionAddress = reg_pc;
		//fetch
		opcode = ir;
		reg_pc += 2;
		instruction = i_table[opcode];
		int res = instruction.execute(opcode);
		prefetch();
		return res;
	}

	private int executeNoPrefetch() {
		//save the PC address
		currentInstructionAddress = reg_pc;
		//fetch
		opcode = fetchPCWord();
		instruction = i_table[opcode];
		return instruction.execute(opcode);
	}

	@Override
	public int getPrefetchWord(){
		return cpuConfig.enablePrefetch ? ir : readMemoryWord(reg_pc);
	}

	protected void loadInstructionSet()
	{
		new ABCD(this).register(this);
		new ADD(this).register(this);
		new ADDA(this).register(this);
		new ADDI(this).register(this);
		new ADDQ(this).register(this);
		new ADDX(this).register(this);
		new AND(this).register(this);
		new ANDI(this).register(this);
		new ANDI_TO_SR(this).register(this);
		new ANDI_TO_CCR(this).register(this);
		new ASL(this).register(this);
		new ASR(this).register(this);
		new Bcc(this).register(this);
		new BCHG(this).register(this);
		new BCLR(this).register(this);
		new BSET(this).register(this);
		new BTST(this).register(this);
		new CHK(this).register(this);
		new CLR(this).register(this);
		new CMP(this).register(this);
		new CMPA(this).register(this);
		new CMPI(this).register(this);
		new CMPM(this).register(this);
		new DBcc(this).register(this);
		new DIVS(this).register(this);
		new DIVU(this).register(this);
		new EOR(this).register(this);
		new EORI(this).register(this);
		new EORI_TO_CCR(this).register(this);
		new EORI_TO_SR(this).register(this);
		new EXG(this).register(this);
		new EXT(this).register(this);
		new ILLEGAL(this).register(this);
		new JMP(this).register(this);
		new JSR(this).register(this);
		new LEA(this).register(this);
		new LINK(this).register(this);
		new LSL(this).register(this);
		new LSR(this).register(this);
		new MOVE(this).register(this);
		new MOVE_TO_CCR(this).register(this);
		new MOVE_TO_SR(this).register(this);
		new MOVE_FROM_SR(this).register(this);
		new MOVE_USP(this).register(this);
		new MOVEA(this).register(this);
		new MOVEM(this).register(this);
		new MOVEP(this).register(this);
		new MOVEQ(this).register(this);
		new MULS(this).register(this);
		new MULU(this).register(this);
		new NBCD(this).register(this);
		new NEG(this).register(this);
		new NEGX(this).register(this);
		new NOP(this).register(this);
		new NOT(this).register(this);
		new OR(this).register(this);
		new ORI(this).register(this);
		new ORI_TO_SR(this).register(this);
		new ORI_TO_CCR(this).register(this);
		new PEA(this).register(this);
		new RESET(this).register(this);
		new ROL(this).register(this);
		new ROR(this).register(this);
		new ROXL(this).register(this);
		new ROXR(this).register(this);
		new RTE(this).register(this);
		new RTR(this).register(this);
		new RTS(this).register(this);
		new SBCD(this).register(this);
		new Scc(this).register(this);
		new STOP(this).register(this);
		new SUB(this).register(this);
		new SUBA(this).register(this);
		new SUBI(this).register(this);
		new SUBQ(this).register(this);
		new SUBX(this).register(this);
		new SWAP(this).register(this);
		new TAS(this).register(this);
		new TRAP(this).register(this);
		new TRAPV(this).register(this);
		new TST(this).register(this);
		new UNLK(this).register(this);
		assert loaded_ops == 46371;
	}

	public void addInstruction(int opcode, Instruction i)
	{
		//build the instruction table
		Instruction current = i_table[opcode];

		if(current == unknown)
		{
			i_table[opcode] = i;
			loaded_ops++;
		}
		else
		{
			throw new IllegalArgumentException("Attempted to overwrite existing instruction [" + current.getClass().getName() + "] at 0x" + String.format("%04x", opcode) + " with [" + i.getClass().getName() + "]");
		}
	}

	public Instruction getInstructionFor(int opcode)
	{
		return i_table[opcode];
	}

	public Instruction getInstructionAt(int address)
	{
		// read opcode at address
		int opcode = readMemoryWord(address);
		return getInstructionFor(opcode);
	}

	@Override
	public CpuConfig getConfig() {
		return cpuConfig;
	}
}
