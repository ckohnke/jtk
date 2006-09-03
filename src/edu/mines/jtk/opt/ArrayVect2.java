/****************************************************************************
Copyright (c) 2004, Landmark Graphics and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.opt;

import edu.mines.jtk.util.Almost;
import java.util.logging.*;

/** Implement a Vect as a two dimensional array of doubles.
 The embedded data are exposed by a getData method.  For all practical
 purposes this member is public, except that this class must always
 point to the same array.  The implementation as an array
 is the point of this class, to avoid duplicate implementations
 elsewhere.  Multiple inheritance is prohibited and
 prevents the mixin pattern, but you can share the wrapped array
 as a private member of your own class,
 and easily delegate all implemented methods.
*/

public class ArrayVect2 implements Vect {
  @SuppressWarnings("unused")
private static final Logger LOG = Logger.getLogger("edu.mines.jtk.opt");
  private static final long serialVersionUID = 1L;

  /** wrapped data */
  private double[][] _data = null;

  /** variance */
  private double _variance = 1.;

  /** Wrap an array as a Vect.
      @param data This will be assigned to the public data.
      @param variance This variance will be used to divide data in
      multiplyInverseCovariance.
  */
  public ArrayVect2(double[][] data, double variance) {
    init(data, variance);
  }

  /** To be used with init() */
  protected ArrayVect2() {}

  /** Wrap an array as a Vect.
      @param data This will be assigned to the public data.
      @param variance This variance will be used to divide data in
      multiplyInverseCovariance.
  */
  protected void init(double[][] data, double variance) {
    this._data = data;
    _variance = variance;
  }

  /** Get the embedded data.
      @return Same array as passed to constructore.
   */
  public double[][] getData() {
    return _data;
  }

  /** Return the size of the embedded array
 * @return size of embedded array */
  public int getSize() {return _data.length*_data[0].length;}

  // Vect interface
  public void add(double scaleThis, double scaleOther, VectConst other) {
    ArrayVect2 o = (ArrayVect2) other;
    for (int i=0; i<_data.length && _data.length > 0; ++i) {
      for (int j=0; j<_data[0].length; ++j) {
        _data[i][j] = scaleThis*_data[i][j] + scaleOther*o._data[i][j];
      }
    }
  }

  public void project(double scaleThis, double scaleOther, VectConst other) {
    add(scaleThis, scaleOther, other);
  }

  // Vect interface
  public void dispose() {
    _data = null;
  }

  // Vect interface
  public void multiplyInverseCovariance() {
    double scale = Almost.FLOAT.divide (1., getSize()*_variance, 0.);
    VectUtil.scale(this, scale);
  }

  public double magnitude() {
    return Almost.FLOAT.divide (this.dot(this), getSize()*_variance, 0.);
  }

  // Vect interface
  public void constrain() {}

  // Vect interface
  public void postCondition() {}

  // VectConst interface
  @Override
public ArrayVect2 clone() {
    try {
      double[][] newData = new double[_data.length][];
      for (int i=0 ; i<newData.length; ++i) {
        newData[i] = _data[i].clone();
      }
      ArrayVect2 result = (ArrayVect2) super.clone();
      result.init(newData, _variance);
      return result;
    } catch (CloneNotSupportedException ex) {
      IllegalStateException e = new IllegalStateException(ex.getMessage());
      e.initCause(ex);
      throw e;
    }
  }

  // VectConst interface
  public double dot(VectConst other) {
    ArrayVect2 rhs = (ArrayVect2) other;
    double result = 0.;
    for (int i=0; i<_data.length; ++i) {
      for (int j=0; j<_data[0].length; ++j) {
        result += this._data[i][j] * rhs._data[i][j];
      }
    }
    return result;
  }

  /** Run tests
     @param args command line
     @throws Exception
   */
  public static void main(String[] args) throws Exception {
    double[][] a = new double[31][21];
    for (int i=0; i<a.length; ++i) {
      for (int j=0; j<a[i].length; ++j) {
        a[i][j] = i+2.4*j;
      }
    }
    Vect v = new ArrayVect2(a, 2.);
    VectUtil.test(v);

    // test inverse covariance
    for (int i=0; i<a.length; ++i) {
      for (int j=0; j<a[i].length; ++j) {
        a[i][j] = 1;
      }
    }
    v = new ArrayVect2(a, 3.);
    Vect w = v.clone();
    w.multiplyInverseCovariance();
    assert Almost.FLOAT.equal(1./3., v.dot(w));
    assert Almost.FLOAT.equal(1./3., v.magnitude());
  }
}