package m68k.cpu;

import static m68k.cpu.Cpu.*;

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
public final class CpuUtils
{

	private static final StringBuilder disasmBuffer = new StringBuilder();
	public static int signExtendByte(int value)
	{
		return (byte)value;
	}

	public static int signExtendWord(int value)
	{
		return (short)value;
	}

	public static boolean testCC(int cc, int reg_sr)
	{
		int ccr = reg_sr & 0x001f;

		switch(cc)
		{
			case 0:		// T
			{
				return true;
			}
			case 1:		// F
			{
				return false;
			}
			case 2:		//HI:
			{
				return ((ccr & (C_FLAG | Z_FLAG)) == 0);
			}
			case 3:		//LS:
			{
				return ((ccr & (C_FLAG | Z_FLAG)) != 0);
			}
			case 4:		//CC:
			{
				return ((ccr & C_FLAG) == 0);
			}
			case 5:		//CS:
			{
				return ((ccr & C_FLAG) != 0);
			}
			case 6:		//NE:
			{
				return ((ccr & Z_FLAG) == 0);
			}
			case 7:		//EQ:
			{
				return ((ccr & Z_FLAG) != 0);
			}
			case 8:		//VC:
			{
				return ((ccr & V_FLAG) == 0);
			}
			case 9:		//VS:
			{
				return ((ccr & V_FLAG) != 0);
			}
			case 10:	//PL:
			{
				return ((ccr & N_FLAG) == 0);
			}
			case 11:	//MI:
			{
				return ((ccr & N_FLAG) != 0);
			}
			case 12:	//GE:
			{
				int v = ccr & (N_FLAG | V_FLAG);
				return (v == 0 || v == (N_FLAG | V_FLAG));
			}
			case 13:	//LT:
			{
				int v = ccr & (N_FLAG | V_FLAG);
				return (v == N_FLAG || v == V_FLAG);
			}
			case 14:	//GT:
			{
				int v = ccr & (N_FLAG | V_FLAG | Z_FLAG);
				return (v == 0 || v == (N_FLAG | V_FLAG));
			}
			case 15:	//LE:
			{
				int v = ccr & (N_FLAG | V_FLAG | Z_FLAG);
				return ((v & Z_FLAG) != 0 || (v == N_FLAG) || (v == V_FLAG));
			}
		}
		throw new IllegalArgumentException("Invalid Condition Code value!");
	}

	public static DisassembledOperand disassembleEA(Cpu cpu, int address, int mode, int reg, Size sz, boolean is_src)
	{
		int bytes_read = 0;
		int mem = 0;
		disasmBuffer.setLength(0);

		switch(mode)
		{
			case 0:
			{
				disasmBuffer.append("d").append(reg);
				break;
			}
			case 1:
			{
				disasmBuffer.append("a").append(reg);
				break;
			}
			case 2:
			{
				disasmBuffer.append("(a").append(reg).append(")");
				break;
			}
			case 3:
			{
				disasmBuffer.append("(a").append(reg).append(")+");
				break;
			}
			case 4:
			{
				disasmBuffer.append("-(a").append(reg).append(")");
				break;
			}
			case 5:
			{
				mem = cpu.readMemoryWordSigned(address);
				disasmBuffer.append(String.format("$%04x",(short)mem)).append("(a").append(reg).append(")");
				bytes_read = 2;
				break;
			}
			case 6:
			{
				mem = cpu.readMemoryWord(address);
				int dis = signExtendByte(mem);
				disasmBuffer.append(String.format("$%02x",(byte)dis)).append("(a").append(reg).append(",");
				disasmBuffer.append(((mem & 0x8000) != 0 ? "a" : "d")).append((mem >> 12) & 0x07).append(((mem & 0x0800) != 0 ? ".l" : ".w")).append(")");
				bytes_read = 2;
				break;
			}
			case 7:
			{
				switch(reg)
				{
					case 0:
					{
						mem = cpu.readMemoryWord(address);
						disasmBuffer.append(String.format("$%04x", mem));
						bytes_read = 2;
						break;
					}
					case 1:
					{
						mem = cpu.readMemoryLong(address);
						disasmBuffer.append(String.format("$%08x", mem));
						bytes_read = 4;
						break;
					}
					case 2:
					{
						mem = cpu.readMemoryWordSigned(address);
						disasmBuffer.append(String.format("$%04x(pc)",(short)mem));
						bytes_read = 2;
						break;
					}
					case 3:
					{
						mem = cpu.readMemoryWord(address);
						int dis = signExtendByte(mem);
						disasmBuffer.append(String.format("$%02x(pc,", (byte)dis));
						disasmBuffer.append(((mem & 0x8000) != 0 ? "a" : "d")).append((mem >> 12) & 0x07).append(((mem & 0x0800) != 0 ? ".l" : ".w")).append(")");
						bytes_read = 2;
						break;
					}
					case 4:
					{
						if(is_src)
						{
							if(sz == Size.Long)
							{
								mem = cpu.readMemoryLong(address);
								bytes_read = 4;
								disasmBuffer.append(String.format("#$%08x", mem));
							}
							else
							{
								mem = cpu.readMemoryWord(address);
								bytes_read = 2;
								disasmBuffer.append(String.format("#$%04x", mem));

								if(sz == Size.Byte)
								{
									mem &= 0x00ff;
								}
							}
						}
						else
						{
							if(sz == Size.Byte)
							{
								disasmBuffer.append("ccr");
							}
							else
							{
								disasmBuffer.append("sr");
							}
						}
						break;
					}
					default:
					{
						throw new IllegalArgumentException("Invalid reg specified for mode 7: " + reg);
					}
				}
				break;
			}
			default:
			{
				throw new IllegalArgumentException("Invalid mode specified: " + mode);
			}
		}
		return new DisassembledOperand(disasmBuffer.toString(), bytes_read, mem);
	}
}
