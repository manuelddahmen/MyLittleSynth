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
    private double minDurationSec;
    private CourbeParametriquePolynomialeBezier form;
    private boolean release = false;
    private boolean end = false;
    Point3D[] points;
    private double time;
    private Timer timer;
    private double timeFactMax = 10;

    public Enveloppe(double minDurationSec) {
        this.minDurationSec = minDurationSec;
        form = new
                CourbeParametriquePolynomialeBezier(
                points = new Point3D[]{
                        new Point3D(0.0, 0.0, 0.0),
                        new Point3D(0.0, 0.0, 0.0),
                        new Point3D(0.0, 1.0, minDurationSec),
                        new Point3D(0.0, 1.0, minDurationSec * timeFactMax),
                });

    }

    public double getVolume(double duration) {
        double volume = 1.0;

        if (!isRelease()) {
            volume = form.calculerPoint3D(duration).getY();
        } else {
            volume = form.calculerPoint3D(timer.getTotalTimeElapsed()).getY();
        }
        if (Math.abs(volume) > 1)
            return Math.signum(volume);
        else
            return volume;
    }

    public void setRelease() {
        this.release = true;
        //time = 0.5;
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

    public double getBrutVolume(double noteDurationSec) {
        return getVolume(noteDurationSec);
    }

    public void setDuration(double inc) {
        for (int i = 0; i < points.length; i++) {
            points[i].setZ(points[i].getZ() + inc);
        }
    }
}
