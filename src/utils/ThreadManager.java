package utils;

import backend.interpreter.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ThreadManager {

    @FunctionalInterface
    public interface Function2<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    static public <T, S> List<S> runOnMaxNumberOfThreads(T[] executeOn, Function2<T, S, Integer, S> toRun) {
        final int numberOfThreads = Math.min(Runtime.getRuntime().availableProcessors(), executeOn.length);

        int valuesPerThread = executeOn.length / numberOfThreads;

        ArrayList<S> results = new ArrayList<S>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            results.add(null);
        }

        Thread[] threads = new Thread[numberOfThreads];
        for (int j = 0; j < numberOfThreads; j++) {
            final int finalJ = j;
            threads[j] = new Thread(() -> {
                int from = finalJ * valuesPerThread;
                int to;
                if (finalJ == numberOfThreads - 1) {
                    to = executeOn.length;
                } else {
                    to = from + valuesPerThread;
                }

                S previousValue = null;

                for (int i = from; i < to; i++) {
                    previousValue = toRun.apply(executeOn[i], previousValue, i);
                }
                results.set(finalJ, previousValue);
            });
            threads[j].start();
        }

        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }
        }

        return results;
    }
}
