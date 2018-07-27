package be.manudahmen.mylittlesynth;

import be.manudahmen.empty3.Courbe;
import be.manudahmen.empty3.Point3D;
import be.manudahmen.empty3.Representable;
import be.manudahmen.empty3.core.nurbs.ParametricCurve;

/**
 * Created by win on 27/07/18.
 */
public class Spirale3D extends ParametricCurve {
    private final double amplitude;
    private final double pas;
    private final double originDecayPC;
    private final double frequency;
    private double INCR;

    public Spirale3D(double frequency, double amplitude, double pas, double originDecayPC) {
        this.amplitude = amplitude;
        this.pas = pas;
        this.originDecayPC = originDecayPC;
        this.frequency = frequency;
        INCR = (amplitude + pas) / 100000.0;
    }

    public Point3D getPoint3D(double t) {
        return Point3D.X.mult(Math.cos(2 * Math.PI * (frequency * t + originDecayPC)))
                .plus(Point3D.Y.mult(Math.sin(2 * Math.PI * (frequency * t + originDecayPC))))
                .plus(Point3D.Z.mult(pas));
    }

    public double getAmplitude(double t) {
        return getPoint3D(t).get2D().norme();
    }

    @Override
    public Point3D calculerPoint3D(double v) {
        return getPoint3D(v);
    }

    @Override
    public Point3D calculerVitesse3D(double v) {
        return getPoint3D(v).moins(getPoint3D(v - INCR)).mult(INCR);
    }
}
