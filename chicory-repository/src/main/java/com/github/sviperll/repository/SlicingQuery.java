package com.github.sviperll.repository;

public interface SlicingQuery<T> {
    public boolean isOrdered();

    public boolean isDescending();

    public boolean hasLimit();

    public int limit();

    public boolean hasConditions();

    public SlicingQueryCondition<T> condition();

    public SlicingQueryPostProcessing postProcessing();

    public static interface SlicingQueryCondition<T> {
        public boolean isLess();

        public boolean isGreater();

        public T value();
    }

    public interface SlicingQueryPostProcessing {
        public boolean needsToBeReveresed();
    }
}
