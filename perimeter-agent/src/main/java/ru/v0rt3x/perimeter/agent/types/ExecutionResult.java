package ru.v0rt3x.perimeter.agent.types;

public class ExecutionResult<T> {
    public boolean isDone() {
        return false;
    }

    public boolean isCancelled() {
        return false;
    }

    public T get() {
        return null;
    }
}
