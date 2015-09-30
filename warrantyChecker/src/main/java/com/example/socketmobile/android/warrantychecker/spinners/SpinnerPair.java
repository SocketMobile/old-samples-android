/*
 * Copyright 2015 Socket Mobile, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.socketmobile.android.warrantychecker.spinners;

import java.util.ArrayList;

public class SpinnerPair implements Comparable<SpinnerPair> {

    public String text;

    public String value;

    public SpinnerPair(String value, String text) {
        if (null == value) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.value = value;
        this.text = text;
    }

    public static ArrayList<SpinnerPair> getArrayList(String[] list) {
        ArrayList<SpinnerPair> pairs = new ArrayList<>();
        for (String item : list) {
            String[] kvp = item.split(":");
            SpinnerPair pair = new SpinnerPair(kvp[0], kvp[1]);
            pairs.add(pair);
        }
        return pairs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || ((Object) this).getClass() != o.getClass()) {
            return false;
        }

        SpinnerPair other = (SpinnerPair) o;

        if (!text.equals(other.text)) {
            return false;
        }
        if (!value.equals(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = text.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int compareTo(SpinnerPair another) {
        return text.compareTo(another.text);
    }
}
