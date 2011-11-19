package edu.agh.lroza.concept;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

class ServerImplJ implements Server {
    Set<Integer> set = new HashSet<Integer>();
    private Random random = new Random(System.currentTimeMillis());

    ServerImplJ() {
        for (int i = 1; i <= 100; i++)
            set.add(i);
    }

    public int remove() {
        set.remove(random.nextInt(set.size()));
        return set.size();
    }

    public int iterate() {
        int sum = 0;
        Iterator<Integer> iterator = set.iterator();
        while (iterator.hasNext()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sum += iterator.next();
        }
        return sum;
    }
}