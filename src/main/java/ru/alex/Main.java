package ru.alex;

import java.util.Random;
import java.util.concurrent.*;

public class Main {

    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(100);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(100);

    private static volatile String maxAString = "";
    private static volatile String maxBString = "";
    private static volatile String maxCString = "";

    private static volatile int maxACount = 0;
    private static volatile int maxBCount = 0;
    private static volatile int maxCCount = 0;

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        executorService.submit(() -> {
            for (int i = 0; i < 10_000; i++) {
                String text = generateText("abc", 100_000);
                try {
                    queueA.put(i + " строка: " + text);
                    queueB.put(i + " строка: " + text);
                    queueC.put(i + " строка: " + text);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            try {
                queueA.put(" ");
                queueB.put(" ");
                queueC.put(" ");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread aCounterThread = new Thread(() -> {
            while (true) {
                try {
                    String text = queueA.take();
                    if (text.isBlank()) break;

                    int count = countChar(text, 'a');
                    if (count > maxACount) {
                        maxACount = count;
                        maxAString = text;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        Thread bCounterThread = new Thread(() -> {
            while (true) {
                try {
                    String text = queueB.take();
                    if (text.isBlank()) break;

                    int count = countChar(text, 'b');
                    if (count > maxBCount) {
                        maxBCount = count;
                        maxBString = text;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        Thread cCounterThread = new Thread(() -> {
            while (true) {
                try {
                    String text = queueC.take();
                    if (text.isBlank()) break;

                    int count = countChar(text, 'c');
                    if (count > maxCCount) {
                        maxCCount = count;
                        maxCString = text;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        aCounterThread.start();
        bCounterThread.start();
        cCounterThread.start();

        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        aCounterThread.join();
        bCounterThread.join();
        cCounterThread.join();

        System.out.println("Max 'a' count: " + maxACount + " in string: " + maxAString.substring(0, 100) + "...");
        System.out.println("Max 'b' count: " + maxBCount + " in string: " + maxBString.substring(0, 100) + "...");
        System.out.println("Max 'c' count: " + maxCCount + " in string: " + maxCString.substring(0, 100) + "...");
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    private static int countChar(String text, char c) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}