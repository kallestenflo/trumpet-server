package com.jayway.trumpet.server.infrastructure.trumpeteer;

import java.util.function.BiConsumer;

class Pair<K, V> {

    public final K key;


    public final V value;


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<>(k, v);
    }

    void consume(BiConsumer<K, V> consumer) {
        consumer.accept(key, value);
    }
}
