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

import be.manudahmen.empty3.Point3D;
import be.manudahmen.empty3.core.nurbs.CourbeParametriquePolynomialeBezier;

public class Enveloppe {
    private final double minDuration;
    private CourbeParametriquePolynomialeBezier form;
    private boolean release = false;
    private boolean end = false;
    Point3D[] points;
    private double time;
    private Timer timer;


    public Enveloppe(double minDuration) {
        this.minDuration = minDuration;
        form = new
                CourbeParametriquePolynomialeBezier(
                points = new Point3D[]{
                        new Point3D(0.0, 0.0, 0.0),
                        new Point3D(0.0, 0.0, 0.0),
                        new Point3D(0.0, 1.0, minDuration / 10),
                        new Point3D(0.0, 1.0, minDuration / 2),
                        new Point3D(0.0, 1.0, minDuration * 3 / 4.0),
                        new Point3D(0.0, 0.2, minDuration * 8 / 10.0),
                        new Point3D(0.0, 0.0, minDuration),
                        new Point3D(0.0, 0.0, minDuration + 1)
                });

    }

    public double getVolume(double duration) {
        if (end) {
            return 0.0;
        }
        if (!isRelease() && duration < minDuration * 3 / 4.0) {
            return form.calculerPoint3D(duration).getY();
        } else if (!isRelease() && duration >= minDuration * 3 / 4.) {
            return form.calculerPoint3D(minDuration * 3 / 4.).getY();
        } else if (isRelease()) {
            return form.calculerPoint3D(duration - timer.getTotalTimeElapsed()).getY();
        }
        return 0.0;
    }

    public void setRelease() {
        this.release = true;
        time = 0.5;
    }

    public void fireEndEvent() {
        this.end = true;
    }

    public boolean isRelease() {
        return release;
    }

    public boolean isEnd() {
        return end;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public double getForm(double v) {
        return form.calculerPoint3D(v).getY();
    }

    public double getBrutVolume(double noteDuration) {
        return form.calculerPoint3D(noteDuration).getY();
    }
}
