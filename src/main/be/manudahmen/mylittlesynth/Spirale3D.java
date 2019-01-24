package be.manudahmen.mylittlesynth;

import be.manudahmen.empty3.Point3D;
import be.manudahmen.empty3.core.nurbs.ParametricCurve;

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
      this.INCR = (amplitude + pas) / 100000.0D;
   }

   public Point3D getPoint3D(double t) {
      return Point3D.X.mult(Math.cos(6.283185307179586D * (this.frequency * t + this.originDecayPC))).plus(Point3D.Y.mult(Math.sin(6.283185307179586D * (this.frequency * t + this.originDecayPC)))).plus(Point3D.Z.mult(this.pas));
   }

   public double getAmplitude(double t) {
      return this.getPoint3D(t).get2D().norme();
   }

   public Point3D calculerPoint3D(double v) {
      return this.getPoint3D(v);
   }

   public Point3D calculerVitesse3D(double v) {
      return this.getPoint3D(v).moins(this.getPoint3D(v - this.INCR)).mult(this.INCR);
   }
}
