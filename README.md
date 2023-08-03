Fork of the [m68k](https://github.com/tonyheadford/m68k) java emulator, with changes to facilitate its integration within the 
[helios](https://github.com/fedex81/helios) project. 

Summary of the changes
- gradle build system
	- adds the following dependencies: tinylog, gson
- additional test suite from [here](https://github.com/TomHarte/ProcessorTests)
- alternate instruction timing
- a few refactoring passes, mostly cosmetic/minor stuff
