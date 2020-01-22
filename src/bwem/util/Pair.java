package bwem.util;

import java.util.Objects;

public class Pair<K, V> {
    private K first;
    private V second;

    public Pair(final K first, final V second) {
        this.first = first;
        this.second = second;
    }

    //BWMIRROR SUPPORT
    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    //BWEM SUPPORT
    public K getLeft() {
        return first;
    }

    public V getRight() {
        return second;
    }

    public void setLeft(final K left) {
        first = left;
    }

    public void setRight(final V right) {
        second = right;
    }

    //MAP.ENTRY SUPPORT
    public K getKey() {
        return first;
    }

    public V getValue() {
        return second;
    }

    @Override
    public String toString() {
        return "{" + first + ", " + second + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return Objects.equals(first, pair.first) &&
                Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
