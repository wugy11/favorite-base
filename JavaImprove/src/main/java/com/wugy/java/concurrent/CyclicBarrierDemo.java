package com.wugy.java.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * wugy on 2017/10/19 16:03
 */
public class CyclicBarrierDemo {

    public static void main(String[] args) {
        new HorseRace(7, 1000);
    }
}

class HorseRace {

    private static final int FINISH_LINE = 75;
    private List<Horse> horses = new ArrayList<>();
    private ExecutorService service = Executors.newCachedThreadPool();

    public HorseRace(int nHorses, final int pause) {
        CyclicBarrier barrier = new CyclicBarrier(nHorses, () -> {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < FINISH_LINE; i++)
                s.append("=");
            System.out.println(s);

            horses.forEach(horse -> System.out.println(horse.tracks()));

            for (Horse horse : horses) {
                if (horse.getStrides() > FINISH_LINE) {
                    System.out.println(horse + " win!");
                    service.shutdownNow();
                    return;
                }
            }

            try {
                TimeUnit.MILLISECONDS.sleep(pause);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        for (int i = 0; i < nHorses; i++) {
            Horse horse = new Horse(barrier);
            horses.add(horse);
            service.execute(horse);
        }
    }

}

class Horse implements Runnable {

    private static int counter = 0;
    private final int id = counter++;
    private static Random rand = new Random(47);
    private int strides = 0;
    private final CyclicBarrier barrier;

    public Horse(CyclicBarrier barrier) {
        this.barrier = barrier;
    }

    public synchronized int getStrides() {
        return strides;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    strides += rand.nextInt(3);
                }
                barrier.await();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Horse{" + "id=" + id + '}';
    }

    public String tracks() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < getStrides(); i++) s.append("*");
        s.append(id);
        return s.toString();
    }
}