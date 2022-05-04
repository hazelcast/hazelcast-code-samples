/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.samples.sql.hazdb;

public class TestData {

    // Name, Played, Won, Drawn, Lost, Goals For, Goals Against, Goal Difference, Points.
    // Goal Difference and Points are specified but could be derived.
    // Played should equal Won plus Drawn plus Lost, so one of these four could be derived.
    static final Object[][] BUNDESLIGA = new Object[][] {
            { "Borussia Mönchengladbach", 11, 8, 1, 2, 24, 11, 13, 25 },
            { "RB Leipzig", 11, 6, 3, 2, 29, 12, 17, 21 },
            { "Bayern Munich", 11, 6, 3, 2, 29, 16, 13, 21 },
            { "SC Freiburg", 11, 6, 3, 2, 20, 12, 8, 21 },
            { "1899 Hoffenheim", 11, 6, 2, 3, 16, 14, 2, 20 },
            { "Borussia Dortmund", 11, 5, 4, 2, 23, 15, 8, 19 },
            { "Schalke 04", 11, 5, 4, 2, 20, 14, 6, 19 },
            { "Bayer Leverkusen", 11, 5, 3, 3, 17, 15, 2, 18 },
            { "Eintracht Frankfurt", 11, 5, 2, 4, 21, 16, 4, 17 },
            { "VfL Wolfsburg", 11, 4, 5, 2, 11, 10, 1, 17 },
            { "Union Berlin", 11, 4, 1, 6, 13, 17, -4, 13 },
            { "Hertha BSC", 11, 3, 2, 6, 17, 21, -4, 11 },
            { "Fortuna Düsseldorf", 11, 3, 2, 6, 15, 19, -4, 11 },
            { "Werder Bremen", 11, 2, 5, 4, 18, 24, -6, 11 },
            { "FC Augsburg", 11, 2, 4, 5, 13, 24, -11, 10 },
            { "Mainz 05", 11, 3, 0, 8, 12, 30, -18, 9 },
            { "1. FC Köln", 11, 2, 1, 8, 10, 23, -13, 7 },
            { "SC Paderborn", 11, 1, 1, 9, 11, 26, -15, 4 }, };

    // Many:1, stadium may have old name and new sponsor name
    static final String[][] STADIA = new String[][] {
            { "Allianz Arena", "Bayern Munich" },
            { "BayArena", "Bayer Leverkusen" },
            { "Deutsche Bank Park", "Eintracht Frankfurt" },
            { "Waldstadion", "Eintracht Frankfurt" },
            { "RheinEnergieStadion", "1. FC Köln" }, };
}
