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
 *  Last modified 25.09.19 06:44 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

class BKTree<T> implements Iterable<T> {

    private final class _node<N> {

        private final T item;
        private Map<Integer, _node<N>> children = null;

        _node(T item) {
            this.item = item;
        }

        T item() {
            return item;
        }

        void addChild(int key, T item) {

            if(children == null) children = new Hashtable<>();

            children.put(key, new _node<>(item));
        }

        _node<N> get(int key) {
            return children.get(key);
        }

        List<Integer> keys() {
            return children == null ? new ArrayList<>() : new ArrayList<>(children.keySet());
        }

        boolean containsKey(int key) {
            return children != null && children.containsKey(key);
        }
    }

    private _node<T> _root = null;
    private int size = 0;

    BKTree() {}

    public final int size() {
        return size;
    }

    final T getRootItem() {
        return _root.item();
    }

    void add(T item) {

        if(_root == null) {
            _root = new _node<>(item);
            ++size;
            return;
        }

        _node<T> curNode = _root;

        final String it = item.toString().toLowerCase();

        int dist = levenshteinDistance(curNode.item().toString().toLowerCase(), it);

        while(curNode.containsKey(dist)) {

            if(dist == 0) return;

            curNode = curNode.get(dist);

            dist = levenshteinDistance(curNode.item().toString().toLowerCase(), it);
        }

        curNode.addChild(dist, item);
        ++size;
    }

    final List<T> search(String word, int d) {
        ArrayList<T> rtn = new ArrayList<>(size);
        recursiveSearch(_root, rtn, word.toLowerCase(), d);
        rtn.trimToSize();
        return rtn;
    }

    private void recursiveSearch(_node<T> node, List<T> rtn, String word, int d) {

        final int curDist = levenshteinDistance(node.item().toString().toLowerCase(), word);
        final int minDist = curDist - d;
        final int maxDist = curDist + d;

        if(curDist <= d) rtn.add(node.item());

        for(int key: node.keys()) {
            if(key >= minDist && key <= maxDist) {
                recursiveSearch(node.get(key), rtn, word, d);
            }
        }
    }

    private static int levenshteinDistance(String first, String second) {

        if(first.length() == 0) return second.length();
        if(second.length() == 0) return first.length();

        final int lenFirst = first.length();
        final int lenSecond = second.length();

        final int[][] d = new int[lenFirst + 1][lenSecond + 1];

        for(int i = 0; i <= lenFirst; ++i) d[i][0] = i;

        for(int i = 0; i <= lenSecond; ++i) d[0][i] = i;

        for(int i = 1; i <= lenFirst; ++i) {

            for(int j = 1; j <= lenSecond; ++j) {

                int match = (first.charAt(i - 1) == second.charAt(j - 1)) ? 0 : 1;

                d[i][j] = min(min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + match);
            }
        }

        return d[lenFirst][lenSecond];
    }

    ArrayList<T> asList() {
        final ArrayList<T> l = new ArrayList<>(size);
        for(T t: this) l.add(t);
        return l;
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {

        final class _iterator implements Iterator<T> {

            final Iterator<T> iter;
            final ArrayList<T> items = new ArrayList<>(size);

            private _iterator() {
                items.add(_root.item());
                nextNode(_root.children);
                items.trimToSize();
                iter = items.iterator();
            }

            private void nextNode(Map<Integer, _node<T>> children) {

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
}
