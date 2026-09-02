package com.toy;

public class Main {
    public static void main(String[] args) {
        LruCache<String, String> cache = new LruCache<>(2);

        cache.put("1", "one");
        cache.put("2", "two");
        System.out.println("Get 1: " + cache.get("1")); // returns "one"

        cache.put("3", "three"); // evicts "2"
        System.out.println("Get 2: " + cache.get("2")); // returns null

        cache.put("4", "four"); // evicts "1"
        System.out.println("Get 1: " + cache.get("1")); // returns null
        System.out.println("Get 3: " + cache.get("3")); // returns "three"
        System.out.println("Get 4: " + cache.get("4")); // returns "four"
    }
}
