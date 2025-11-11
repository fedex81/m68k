package m68k.clktest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Federico Berti
 * <p>
 * Copyright 2022
 */
public class ClkJsonTest68k extends AbstractJsonTest68k{

    public static String pathClk = "src/test/resources/test_m68k_json_202210";

    private final static String[] knownIssues = {"ASL.b", "ASR", "DIVU", "LINK"};

    static {
        Arrays.sort(knownIssues);
    }
    public static void main(String[] args) throws InterruptedException {
        ClkJsonTest68k t = new ClkJsonTest68k();
        int cnt = 0;
        do {
            System.out.println("Run: " + ++cnt);
            fileProvider().forEach(tc -> {
                t.setup();
                StringBuilder sb = t.testJsonInternal(pathClk, tc);
//                System.out.println(sb);
            });
            Thread.sleep(500);
        } while (true);
    }

    static Stream<String> fileProvider() {
        return fileProviderBase(pathClk);
    }

    @MethodSource("fileProvider")
    @ParameterizedTest
    public void testJsonClk(String fileName) {
        boolean ignore = Arrays.stream(knownIssues).anyMatch(n -> fileName.contains(n));
        if(ignore){
            System.out.println("Ignoring " + fileName);
            return;
        }
        StringBuilder err = testJsonInternal(pathClk, fileName);
        Assertions.assertEquals(0, err.length(), err.toString());
    }
}