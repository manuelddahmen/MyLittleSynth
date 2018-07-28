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
    private long timePause = 0;
    private long definitiveTime = -1;

    public Timer() {
    }

    public void init() {
        timeElapsedSystem = System.nanoTime();
        timeElapsed = 0;
        definitiveTime = -1;
    }

    public void pause() {
        timePause = System.nanoTime();
    }

    public void resume() {
        timePause = 0;

    }


    public long getTotalTimeElapsed() {
        long timeInter = System.nanoTime();

        if (timePause > 0) {
            timeElapsed = timeInter - timePause + timeElapsedSystem;

        } else if (definitiveTime > 0) {
            return definitiveTime;
        } else {
            timeElapsed = (timeInter - timeElapsedSystem);

        }


        return timeElapsed;
    }

    public void stop() {
        definitiveTime = (long) (getTotalTimeElapsed() * 1E9);
    }

    public long getDefinitiveTime() {
        return definitiveTime;
    }

    public long getInitTime() {
        return timeElapsedSystem;
    }
}
