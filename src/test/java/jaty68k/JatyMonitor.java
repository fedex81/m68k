package jaty68k;

import m68k.cpu.Cpu;
import m68k.cpu.MC68000;
import m68k.memory.AddressSpace;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Run ucLinux as built by the katy68k project.
 * https://www.bigmessowires.com/68-katy/
 *
 * Java layer adapted from the jaty68k project.
 * https://github.com/alexwinston/Jaty68k
 *
 * How to:
 * - launch JatyMonitor:main, 1st arg should be the location of the uclinux binary.
 * - type "j"
 * - type "003000"
 *
 */
public class JatyMonitor extends m68k.Monitor
{
	private boolean hasBreakpoints = false;

	private static final String ROM_LOC = "./src/test/resources/linux-pcb.bin";

	static class JatyCpu extends MC68000 {
		int safeInterrupt;
		int safeInterruptTimer;

		public void raiseSafeInterrupt(int priority) {
			safeInterrupt = priority;
		}

		public void executeSafeInterrupt() {
			if (safeInterrupt > this.getInterruptLevel() || safeInterruptTimer++ == 100) {
				this.raiseInterrupt(this.safeInterrupt);
				this.safeInterrupt = 0;
				this.safeInterruptTimer = 0;
			}
		}
	}

	public JatyMonitor(Cpu cpu, AddressSpace memory)
	{
		super(cpu, memory);
	}

	public static void main(String[] args)
	{
		int mem_size = 0xfffff;	// 512kb of memory default

		System.out.println("m68k Monitor v0.1 - Copyright 2008-2010 Tony Headford");

		Path p = Paths.get(ROM_LOC);
		String romLoc = p.toAbsolutePath().toString();
		if (args.length != 0) {
			romLoc = args[0];
		}
		System.out.println("Attempting to load uCLinux kernel from: " + romLoc);

		AddressSpace memory = new KatyMemorySpace(mem_size);

		Cpu cpu = new JatyCpu();
		cpu.setAddressSpace(memory);
		cpu.reset();	//init cpu

		new jaty68k.Console((JatyCpu) cpu, memory);

		JatyMonitor monitor = new JatyMonitor(cpu,memory);
		monitor.writer = new PrintWriter(System.out);
		monitor.handleLoad(new String[] {"load", "0", romLoc});

		monitor.run();
		monitor.handleGo(new String[0]);
	}

	@Override
	public void run()
	{
		writer = new PrintWriter(System.out);
		reader = new BufferedReader(new InputStreamReader(System.in));
		running = true;
	}

	@Override
	protected void handleGo(String[] tokens)
	{
		int timer = 0;
		int count = 0;
		boolean going = true;

		assert cpu instanceof JatyCpu;

		while(running && going)
		{
			try
			{
				if (timer++ == 10000) {
					timer = 0;
					cpu.raiseInterrupt(5);
				}
				((JatyCpu)cpu).executeSafeInterrupt();

				int time = cpu.execute();
				count += time;
				int addr = cpu.getPC();
				if(hasBreakpoints && breakpoints.contains(addr))
				{
					//time to stop
					writer.println("BREAKPOINT");
					going = false;
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				going = false;
			}

		}
		writer.printf("[Consumed %d ticks]\n", count);
	}

	@Override
	protected void handleBreakPoints(String[] tokens)
	{
		if(tokens.length > 1)
		{
			// add or remove toggle
			try
			{
				int addr = parseInt(tokens[1]);
				if(breakpoints.contains(addr)) {
					breakpoints.remove(addr);
					if (breakpoints.size() == 0)
						hasBreakpoints = false;
				} else {
					hasBreakpoints = true;
					breakpoints.add(addr);
				}
			}
			catch(NumberFormatException e)
			{
				return;
			}
		}

		//list breakpoints
		writer.println("Breakpoints:");
		for(int bp : breakpoints)
		{
			writer.println(String.format("$%x", bp));
		}
	}

	protected void handleLoad(String[] tokens)
	{
		super.handleLoad(tokens);
		writer.flush();
	}
}
