package edu.agh.lroza.concurrent;

import java.util.concurrent.atomic.AtomicLong;

import edu.agh.lroza.common.Id;

class LongId implements Id {
    private static final AtomicLong generator = new AtomicLong();
    private final Long id;

    public static Id get() {
        return new LongId(generator.getAndIncrement());
    }

    LongId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LongId) {
            return ((LongId) obj).id.equals(id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "LongId(" + id + ")";
    }
}
