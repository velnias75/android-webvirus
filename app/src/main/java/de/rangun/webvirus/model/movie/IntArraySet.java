/*
 * Copyright 2019 by Heiko Schäfer <heiko@rangun.de>
 *
 *  This file is part of android-webvirus.
 *
 *  android-webvirus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  android-webvirus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with android-webvirus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Last modified 06.11.19 01:21 by heiko
 */

/*
  An implementation of a Set using primitive int data type.
  It uses {@link androidx.collection.SparseArrayCompat as a backing map,
 * the same as HashSet uses HashMap internally.
 *
 * Note that this container keeps its mappings in an array data structure,
 * using a binary search to find keys.  The implementation is not intended to be appropriate for
 * data structures that may contain large numbers of items.  It is generally slower than a
 * traditional HashSet, since lookups require a binary search and adds and removes require
 * inserting and deleting entries in the array.  For containers holding up to hundreds of items,
 * the performance difference is not significant, less than 50%.
 *
 * To help with performance, the container includes an optimization when removing
 * keys: instead of compacting its array immediately, it leaves the removed entry marked
 * as deleted.  The entry can then be re-used for the same key, or compacted later in
 * a single garbage collection step of all removed entries.  This garbage collection will
 * need to be performed at any time the array needs to be grown or the the map size or
 * entry values are retrieved.
 *
 * Source: https://engineering.fb.com/android/primitive-sets-for-android/
 */

package de.rangun.webvirus.model.movie;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

final class IntArraySet implements Cloneable {

    private SparseArrayCompat<IntArraySet> backingMap;

    /**
     * Constructs a new empty instance of {@code IntArraySet}.
     */
    public IntArraySet() {
        this(new SparseArrayCompat<>());
    }

    /**
     * Internal CTor to initialize the internal backing map.
     */
    private IntArraySet(SparseArrayCompat<IntArraySet> backingMap) {
        this.backingMap = backingMap;
    }

    /**
     * Adds the specified item to this {@code IntArraySet}, if not already present.
     *
     * @param item
     *            the object to add.
     */
    public void add(int item) {
        backingMap.put(item, this);
    }

    /**
     * Get all the items in this {@code IntArraySet}.
     *
     * @return  {@code int[]} array of all the items in the set.
     *          A new array will be allocated for each request, modifying it won't affect the set.
     */
    public int[] getAllItems() {
        int size = backingMap.size();
        int[] ret = new int[size];

        for (int i = 0; i < size; i++) {
            ret[i] = backingMap.keyAt(i);
        }
        return ret;
    }

    @NonNull
    @Override
    public IntArraySet clone() {
        IntArraySet clone = null;
        //noinspection SpellCheckingInspection
        try {
            clone = (IntArraySet) super.clone();
            clone.backingMap = backingMap.clone();
        } catch (CloneNotSupportedException cnse) {
            /* ignore */
        }
        //noinspection ConstantConditions
        return clone;
    }

    @NonNull
    @Override
    public String toString() { return backingMap.toString(); }
}
