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
   private double timeFactMax = 10.0D;

   public Enveloppe(double minDurationSec) {
      this.minDurationSec = minDurationSec;
      this.form = new CourbeParametriquePolynomialeBezier(this.points = new Point3D[]{new Point3D(0.0D, 0.0D, 0.0D), new Point3D(0.0D, 0.0D, 0.0D), new Point3D(0.0D, 1.0D, minDurationSec), new Point3D(0.0D, 1.0D, minDurationSec * this.timeFactMax)});
   }

   public double getVolume(double duration) {
      double volume = 1.0D;
      if (!this.isRelease()) {
         volume = this.form.calculerPoint3D(duration).getY();
      } else {
         volume = this.form.calculerPoint3D((double)this.timer.getTotalTimeElapsed()).getY();
      }

      return Math.abs(volume) > 1.0D ? Math.signum(volume) : volume;
   }

   public void setRelease() {
      this.release = true;
   }

   public void fireEndEvent() {
      this.end = true;
   }

   public boolean isRelease() {
      return this.release;
   }

   public boolean isEnd() {
      return this.end;
   }

   public void setTimer(Timer timer) {
      this.timer = timer;
   }

   public double getForm(double v) {
      return this.form.calculerPoint3D(v).getY();
   }

   public double getBrutVolume(double noteDurationSec) {
      return this.getVolume(noteDurationSec);
   }

   public void setDuration(double inc) {
      for(int i = 0; i < this.points.length; ++i) {
         this.points[i].setZ(this.points[i].getZ() + inc);
      }

   }
}
