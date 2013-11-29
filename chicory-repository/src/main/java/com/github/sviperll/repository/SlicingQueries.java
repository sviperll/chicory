/*
 * Copyright (C) 2013 Victor Nazarov <asviraspossible@gmail.com>
 */

package com.github.sviperll.repository;

import com.github.sviperll.repository.SlicingQuery.SlicingQueryCondition;
import com.github.sviperll.repository.SlicingQuery.SlicingQueryPostProcessing;

public class SlicingQueries {
    private static final SlicingQueryPostProcessing NOT_NEEDS_TO_BE_REVERESED = new NotNeededToBeReversedRepositorySlicingResult();
    private static final SlicingQueryPostProcessing NEEDS_TO_BE_REVERESED = new NeedsToBeReversedRepositorySlicingResult();

    @SuppressWarnings("rawtypes")
    private static final UnlimitedQuery UNLIMITED_REPOSITORY_SLICING = new UnlimitedQuery();

    public static <T> SlicingQuery<T> firstN(final int limit) {
        return new FirstNQuery<T>(limit);
    }

    public static <T> SlicingQuery<T> lastN(final int limit) {
        return new LastNQuery<T>(limit);
    }

    @SuppressWarnings("unchecked")
    public static <T> SlicingQuery<T> unlimited() {
        return UNLIMITED_REPOSITORY_SLICING;
    }

    public static <T> SlicingQuery<T> firstNAfter(final int limit, final T value) {
        return new FirstNAfterSomeQuery<T>(limit, value);
    }

    public static <T> SlicingQuery<T> lastNBefore(final int limit, final T value) {
        return new LastNBeforeSomeQuery<T>(limit, value);
    }

    private SlicingQueries() {
    }

    private static class FirstNQuery<T> implements SlicingQuery<T> {
        private final int limit;

        public FirstNQuery(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return false;
        }

        @Override
        public SlicingQueryCondition<T> condition() {
            throw new UnsupportedOperationException("Has no conditions.");
        }

        @Override
        public SlicingQueryPostProcessing postProcessing() {
            return NOT_NEEDS_TO_BE_REVERESED;
        }
    }

    private static class LastNQuery<T> implements SlicingQuery<T> {
        private final int limit;
        public LastNQuery(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return true;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return false;
        }

        @Override
        public SlicingQueryCondition<T> condition() {
            throw new UnsupportedOperationException("Has no condition");
        }

        @Override
        public SlicingQueryPostProcessing postProcessing() {
            return NEEDS_TO_BE_REVERESED;
        }
    }

    private static class UnlimitedQuery<T> implements SlicingQuery<T> {
        public UnlimitedQuery() {
        }

        @Override
        public boolean isOrdered() {
            return false;
        }

        @Override
        public boolean isDescending() {
            throw new UnsupportedOperationException("Is not ordered.");
        }

        @Override
        public boolean hasLimit() {
            return false;
        }

        @Override
        public int limit() {
            throw new UnsupportedOperationException("Has no limit.");
        }

        @Override
        public boolean hasConditions() {
            return false;
        }

        @Override
        public SlicingQueryCondition<T> condition() {
            throw new UnsupportedOperationException("Has no conditions.");
        }

        @Override
        public SlicingQueryPostProcessing postProcessing() {
            return NOT_NEEDS_TO_BE_REVERESED;
        }
    }

    private static class NotNeededToBeReversedRepositorySlicingResult implements SlicingQueryPostProcessing {
        @Override
        public boolean needsToBeReveresed() {
            return false;
        }
    }

    private static class FirstNAfterSomeQuery<T> implements SlicingQuery<T> {
        private final int limit;
        private final T value;

        public FirstNAfterSomeQuery(int limit, T value) {
            this.limit = limit;
            this.value = value;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return true;
        }

        @Override
        public SlicingQueryCondition<T> condition() {
            return new SlicingQueryCondition<T>() {
                @Override
                public boolean isLess() {
                    return false;
                }

                @Override
                public boolean isGreater() {
                    return true;
                }

                @Override
                public T value() {
                    return value;
                }
            };
        }

        @Override
        public SlicingQueryPostProcessing postProcessing() {
            return NOT_NEEDS_TO_BE_REVERESED;
        }
    }

    private static class NeedsToBeReversedRepositorySlicingResult implements SlicingQueryPostProcessing {
        @Override
        public boolean needsToBeReveresed() {
            return true;
        }
    }

    private static class LastNBeforeSomeQuery<T> implements SlicingQuery<T> {
        private final int limit;
        private final T value;

        public LastNBeforeSomeQuery(int limit, T value) {
            this.limit = limit;
            this.value = value;
        }

        @Override
        public boolean isOrdered() {
            return true;
        }

        @Override
        public boolean isDescending() {
            return true;
        }

        @Override
        public boolean hasLimit() {
            return true;
        }

        @Override
        public int limit() {
            return limit;
        }

        @Override
        public boolean hasConditions() {
            return true;
        }

        @Override
        public SlicingQueryCondition<T> condition() {
            return new SlicingQueryCondition<T>() {
                @Override
                public boolean isLess() {
                    return true;
                }

                @Override
                public boolean isGreater() {
                    return false;
                }

                @Override
                public T value() {
                    return value;
                }
            };
        }

        @Override
        public SlicingQueryPostProcessing postProcessing() {
            return NEEDS_TO_BE_REVERESED;
        }
    }
}
