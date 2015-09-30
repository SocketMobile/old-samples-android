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
import java.util.Locale;

public class Countries {

    public static ArrayList<String> getArray() {

        Locale[] locales = Locale.getAvailableLocales();
        ArrayList<String> countries = new ArrayList<>();

        for (Locale locale : locales) {

            String isoCode = locale.getCountry().trim();
            String name = locale.getDisplayCountry().trim();

            // Only insert countries with a valid ISO code
            if (isoCode != null && isoCode.isEmpty() &&
                    isoCode.length() == 2) {

                String country = isoCode + ":" + name;
                if (!countries.contains(country)) {
                    countries.add(country);
                }
            }
        }

        return countries;
    }

    public static String[] getList() {
        ArrayList<String> countries = getArray();
        return countries.toArray(new String[countries.size()]);
    }
}
