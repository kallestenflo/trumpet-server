package com.jayway.fixture;

public class ThrowableExpecter<T extends Throwable> {

    private final Class<T> expected;

    public ThrowableExpecter(Class<T> expected) {
        this.expected = expected;
    }

    @FunctionalInterface
    public interface Actor {
        void act() throws Throwable;
    }

    public static <T extends Throwable> ThrowableExpecter<T> expect(Class<T> clazz){
        return new ThrowableExpecter<T>(clazz);
    }

    public T when(Actor actor) {
        try {
            actor.act();
        } catch (Throwable throwable) {
            if(!throwable.getClass().isAssignableFrom(expected)){
                throw new AssertionError("Expected  " + expected.getClass().getName() + " got " + throwable.getClass().getName());
            }
            return expected.cast(throwable);
        }
        throw new AssertionError("Expected exception " + expected.getClass().getName());
    }

}
