package m68k.cpu.instructions;

import m68k.cpu.*;
import m68k.cpu.operand.Operand;
import m68k.cpu.operand.OperandTiming;

import static java.lang.Math.abs;
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

public class DIVS implements InstructionHandler
{
	protected final Cpu cpu;
	protected final boolean accurateDivTiming;

	public DIVS(Cpu cpu)
	{
		this.cpu = cpu;
		accurateDivTiming = cpu.getConfig().accurateDivTiming;
	}

	public void register(InstructionSet is)
	{
		int base = 0x81c0;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return divs(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode, Size.Word);
			}
		};

		for(int ea_mode = 0; ea_mode < 8; ea_mode++)
		{
			if(ea_mode == 1)
				continue;

			for(int ea_reg = 0; ea_reg < 8; ea_reg++)
			{
				if(ea_mode == 7 && ea_reg > 4)
					break;

				for(int r = 0; r < 8; r++)
				{
					is.addInstruction(base + (r << 9) + (ea_mode << 3) + ea_reg, i);
				}
			}
		}
	}

	protected final int divs(int opcode)
	{
		Operand op = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);

		int s = op.getWordSigned();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getDataRegisterLong(reg);
		int time;

		if(s == 0)
		{
			//divide by zero exception
			cpu.raiseException(5);
			time = 38;
		}
		else
		{
			int quot = d / s;

			if(quot > 32767 || quot < -32768)
			{
				//Overflow
				cpu.setFlags(Cpu.V_FLAG);
				cpu.clrFlags(Cpu.C_FLAG);
			}
			else
			{
				int remain = (d % s) & 0xffff;
				int result = (quot & 0x0000ffff) | (remain << 16);
				cpu.setDataRegisterLong(reg, result);

				if((quot & 0x8000) != 0)
				{
					cpu.setFlags(Cpu.N_FLAG);
					cpu.clrFlags(Cpu.Z_FLAG);
				}
				else
				{
					cpu.clrFlags(Cpu.N_FLAG);

					if(quot == 0)
						cpu.setFlags(Cpu.Z_FLAG);
					else
						cpu.clrFlags(Cpu.Z_FLAG);
				}

				cpu.clrFlags((Cpu.V_FLAG | Cpu.C_FLAG));
			}
			//worst case but less than 10% difference between best and worst cases
			time = accurateDivTiming ? getDivs68kCycles(d, s) : 158;
		}

		return time + OperandTiming.getOperandTiming(op, Size.Word);

	}

	/*
	 * Compute exact number of CPU cycles taken
	 * by DIVU and DIVS on a 68000 processor.
	 *
	 * Copyright (c) 2005 by Jorge Cwik, pasti@fxatari.com
	 */
	private static int getDivs68kCycles(long dividend, int divisor)
	{
		int mcycles;
		int aquot;
		int i;

		if( divisor == 0)
			return 0;

		mcycles = 6;

		if( dividend < 0)
			mcycles++;

		// Check for absolute overflow
		if( (abs( dividend) >> 16) >= abs( divisor))
		{
			return (mcycles + 2) * 2;
		}

		// Absolute quotient
		aquot = (int) (abs( dividend) / abs( divisor));

		mcycles += 55;

		if( divisor >= 0)
		{
			if( (short)dividend >= 0)
				mcycles--;
			else
				mcycles++;
		}

		// Count 15 msbits in absolute of quotient

		for( i = 0; i < 15; i++)
		{
			if( aquot >= 0)
				mcycles++;
			aquot <<= 1;
		}


		return mcycles * 2;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "divs", src, dst);
	}
}
