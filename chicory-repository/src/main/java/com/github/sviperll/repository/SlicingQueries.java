/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
        return new FirstNQuery<>(limit);
    }

    public static <T> SlicingQuery<T> lastN(final int limit) {
        return new LastNQuery<>(limit);
    }

    @SuppressWarnings("unchecked")
    public static <T> SlicingQuery<T> unlimited() {
        return UNLIMITED_REPOSITORY_SLICING;
    }

    public static <T> SlicingQuery<T> firstNAfter(final int limit, final T value) {
        return new FirstNAfterSomeQuery<>(limit, value);
    }

    public static <T> SlicingQuery<T> lastNBefore(final int limit, final T value) {
        return new LastNBeforeSomeQuery<>(limit, value);
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
            return true;
        }

        @Override
        public boolean isDescending() {
            return false;
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
