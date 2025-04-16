package m68k.cpu;

import java.util.StringJoiner;

/**
 * Federico Berti
 * <p>
 * Copyright 2025
 */
public class CpuConfig {
    public static final CpuConfig DEFAULT_CONFIG = new CpuConfig(false, false, false);
    public final boolean emulateBrokenTasWrite;
    public final boolean accurateDivTiming;

    //TODO not working
    public final boolean enablePrefetch;

    public CpuConfig(boolean brokenTas, boolean divTiming, boolean enablePrefetch){
        emulateBrokenTasWrite = brokenTas;
        accurateDivTiming = divTiming;
        this.enablePrefetch = enablePrefetch;
    }

    /**
     * true -> hardware where write-back to *memory* doesn't work
     */
    public boolean emulateBrokenTasWrite(){
       return emulateBrokenTasWrite;
    }

    public boolean accurateDivTiming() {
        return accurateDivTiming;
    }

    public boolean enablePrefetch() {
        return enablePrefetch;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CpuConfig.class.getSimpleName() + "[", "]")
                .add("emulateBrokenTasWrite=" + emulateBrokenTasWrite)
                .add("accurateDivTiming=" + accurateDivTiming)
                .add("enablePrefetch=" + enablePrefetch)
                .toString();
    }
}
