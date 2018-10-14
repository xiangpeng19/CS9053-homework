package edu.nyu.cs9053.homework5.memory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: blangel
 * Date: 9/30/15
 * Time: 8:47 AM
 *
 * Fix the inner class leak.  When invoked before changing, this will either run out of memory (JVM will end with an
 * java.lang.OutOfMemoryError) or it'll print a statement saying too much memory was used.
 */
public class Faucet {

	//	modification: 
	//	1) change the inner classes Drain and Water to static inner class
	//	2) move the initial flow length variable out of the Faucet constructor and pass it into Water constructor
	//  3) create a new Random instance in Water
    private static class Drain {

        private void drain(Water water) {
            water.consume();
        }

    }

    private static class Water {

        private final AtomicInteger remaining;
        
        private final Random random;

        private Water(int flowLength) {
        	this.remaining = new AtomicInteger(flowLength);
        	this.random = new Random();
        }

        private int consume() {
            int current = remaining.get();
            int consumed = ((int) ((1d / (double) (random.nextInt(4) + 1)) * current)) + 1;
            int remainder = Math.max(0, current - consumed);
            remaining.set(remainder);
            return consumed;
        }

        private boolean dry() {
            return remaining.get() == 0;
        }

    }

    private static final long THIRTY_SECONDS_MS = 30L * 1000L;

    private static final long BYTE_TO_MB = 1024L * 1024L;

    private final static int MAX_FLOW = 1000;

    /**
     * Students, do not modify this main method
     * @param args to the program
     */
    public static void main(String[] args) {
        long runtime = System.currentTimeMillis() + THIRTY_SECONDS_MS;
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        FancyOutput.print("Starting with ^yellow^%s^r^ MB used memory%n", (usedMemory / BYTE_TO_MB));
        Collection<Water> waters = new LinkedList<>();
        while (System.currentTimeMillis() < runtime) {
            Faucet faucet = new Faucet(new Random());
            Water water = faucet.turnOn();
            waters.add(water);
            FancyOutput.print("^water^");
            while (!water.dry()) {
                faucet.drain(water);
            }
        }
        Runtime.getRuntime().gc(); // students, do not do this in practice (doing simply for ease of demonstration on leak in homework)
        long total = Runtime.getRuntime().totalMemory();
        long endMemory = total - Runtime.getRuntime().freeMemory();
        double percentage = ((double) endMemory / (double) total) * 100d;
        FancyOutput.print("%nProcessed ^cyan^%d^r^ water%n", waters.size());
        FancyOutput.print("Ending with ^yellow^%d^r^ MB used memory%n", (endMemory / BYTE_TO_MB));
        if (percentage > 50d) {
            FancyOutput.print("^red^You've been leaking memory!^r^%n");
        } else {
            FancyOutput.print("^green^Nothing leaked, nice!^r^%n");
        }
    }

    private final Random random;

    private final Drain drain;

    private final Object[] flow;
    

    private final int initial;

    public Faucet(Random random) {
        this.random = random;
        this.drain = new Drain();
        initial = random.nextInt(MAX_FLOW) + 1;
        this.flow = new Object[initial];
        for (int i = 0; i < initial; i++) {
            this.flow[i] = new Object();
        }
    }

    public void drain(Water water) {
        this.drain.drain(water);
    }

    public Water turnOn() {
        return new Water(initial);
    }

}