package com.jayway.trumpet.server.domain;

public class Tuple<LEFT, RIGHT> {

    public final LEFT left;
    public final RIGHT right;

    public Tuple(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
    }

    public static <LEFT, RIGHT> Tuple<LEFT, RIGHT> create(LEFT left, RIGHT right){
        return new Tuple<>(left, right);
    }
}
