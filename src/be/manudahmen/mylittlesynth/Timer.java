/*
 * This file is part of Plants-Growth-2
 *     Plants-Growth-2 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Plants-Growth-2 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Plants-Growth-2.  If not, see <http://www.gnu.org/licenses/>.
 */


package be.manudahmen.mylittlesynth;

/**
 * @author Manuel Dahmen <manueldahmen.smoke@gmail.com>
 */
public class Timer {
    private long timeElapsed = 0;
    private long timeElapsedSystem = 0;


    public Timer() {
    }

    public void init() {
        timeElapsedSystem = System.nanoTime();
        timeElapsed = 0;
    }

    public double getTimeElapsed() {
        long timeInter = System.nanoTime();

        timeElapsed = (timeInter - timeElapsedSystem);

        return timeElapsed / 1E9;
    }


    public void setTimeSeconds(double v) {
        this.timeElapsedSystem = (long) (System.nanoTime() - v * 1E9);
    }
}
