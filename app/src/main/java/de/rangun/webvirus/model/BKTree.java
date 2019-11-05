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
 *  Last modified 01.10.19 00:48 by heiko
 */

package de.rangun.webvirus.model;

import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.min;

abstract class BKTree<T> implements Iterable<T> {

    private final class _node<N> {

        private T item;

        @Nullable
        private Map<Integer, _node<N>> children = null;

        _node(T item) { this.item = item; }

        T item() {
            return item;
        }

        void addChild(int key, T item) {

            if(children == null) children = new ArrayMap<>();

            children.put(key, new _node<>(item));
        }

        void replace(T newItem) { item = newItem; }

        @Nullable
        _node<N> get(int key) {
            return Objects.requireNonNull(children).get(key);
        }

        @NonNull
        List<Integer> keys() {
            return children == null ? new ArrayList<>() : new ArrayList<>(children.keySet());
        }

        boolean containsKey(int key) {
            return children != null && children.containsKey(key);
        }
    }

    @Nullable
    private _node<T> _root = null;
    private int size = 0;

    BKTree() {}

    public final int size() {
        return size;
    }

    final T getRootItem() {
        return Objects.requireNonNull(_root).item();
    }

    void add(@NonNull T item) {

        if(_root == null) {
            _root = new _node<>(item);
            ++size;
            return;
        }

        _node<T> curNode = _root;

        final char[] it = lowerCaseItem(item);

        int dist =
                damerauLevenshteinDistance(lowerCaseItem(curNode.item()), it);

        while(curNode.containsKey(dist)) {

            if(dist == 0) return;

            curNode = curNode.get(dist);

            dist = damerauLevenshteinDistance(lowerCaseItem(Objects.requireNonNull(curNode).item()),
                    it);
        }

        curNode.addChild(dist, item);

        ++size;
    }

    public void replaceItem(T o, T n) { if(_root != null) visitNextNode(_root, o, n); }

    private void visitNextNode(_node<T> node, T o, T n) {

        if(node.item() == o) {
            node.replace(n);
            return;
        }

        if(node.children != null) for(_node<T> c: node.children.values()) visitNextNode(c, o, n);
    }

    @NonNull
    final List<T> search(@NonNull String word, int d) {
        ArrayList<T> rtn = new ArrayList<>(size);
        recursiveSearch(Objects.requireNonNull(_root), rtn, lowerCaseWord(word), d);
        rtn.trimToSize();
        return rtn;
    }

    private void recursiveSearch(@NonNull _node<T> node, @NonNull List<T> rtn,
                                 @NonNull char[] word, int d) {

        final int curDist = damerauLevenshteinDistance(lowerCaseItem(node.item()), word);
        final int minDist = curDist - d;
        final int maxDist = curDist + d;

        if(curDist <= d) rtn.add(node.item());

        for(int key: node.keys()) {
            if(key >= minDist && key <= maxDist) {
                recursiveSearch(Objects.requireNonNull(node.get(key)), rtn, word, d);
            }
        }
    }

    private static int damerauLevenshteinDistance(@NonNull char[] source,
                                                  @NonNull char[] target) {

        final int sourceLength = source.length;
        final int targetLength = target.length;

        if(sourceLength == 0) return targetLength;
        if(targetLength == 0) return sourceLength;

        final int[][] dist = new int[sourceLength + 1][targetLength + 1];

        for(int i = 0; i <= sourceLength; ++i) dist[i][0] = i;
        for(int j = 0; j <= targetLength; ++j) dist[0][j] = j;

        for(int i = 1; i <= sourceLength; ++i) {

            final char sca = source[i - 1];

            for(int j = 1; j <= targetLength; ++j) {

                final char tca = target[j - 1];
                final int cost = sca == tca ? 0 : 1;

                dist[i][j] =
                        min(min(dist[i - 1][j] + 1, dist[i][j - 1] + 1), dist[i - 1][j - 1] + cost);

                if(j > 1 && i > 1 && sca == target[j - 2] && source[i - 2] == tca) {
                    dist[i][j] = min(dist[i][j], dist[i - 2][j - 2] + cost);
                }
            }
        }

        return dist[sourceLength][targetLength];
    }

    @NonNull
    ArrayList<T> asList() {
        final ArrayList<T> l = new ArrayList<>(size);
        for(T t: this) l.add(t);
        return l;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {

        final class _iterator implements Iterator<T> {

            @NonNull
            final Iterator<T> iter;
            final ArrayList<T> items = new ArrayList<>(size);

            private _iterator() {
                items.add(Objects.requireNonNull(_root).item());
                nextNode(_root.children);
                items.trimToSize();
                iter = items.iterator();
            }

            private void nextNode(@Nullable Map<Integer, _node<T>> children) {

                if(children != null) {
                    for(_node<T> n: children.values()) {
                        items.add(n.item());
                        nextNode(n.children);
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return iter.next();
            }
        }

        return new _iterator();
    }

    protected abstract char[] lowerCaseItem(T item);

    protected abstract char[] lowerCaseWord(String word);
}
