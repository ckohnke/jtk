/****************************************************************************
Copyright (c) 2004, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package edu.mines.jtk.mosaic;

/**
 * Translates and scales (maps) user coordinates to/from device coordinates.
 * The mapping is specified by two rectangles, one in user coordinates and 
 * the other in device coordinates.
 * <p>
 * Device coordinates are ints, and the device coordinate rectangle 
 * typically corresponds to device bounds. User coordinates are doubles.
 * <p>
 * In conversion from user to device coordinates, the latter are clipped 
 * to lie in the range [-32768,32767], which is the range of a 16-bit
 * short integer. Although device coordinates are represented by ints, 
 * they are often limited by an underlying graphics systems to the 16-bit 
 * range of shorts.
 * <p>
 * Conversion from/to user coordinates to/from device coordinates behaves 
 * robustly in the cases where the mapping is degenerate. For example, if
 * the device coordinate rectangle has zero width, then conversion from 
 * any device x-coordinate to user x-coordinate yields the average of the 
 * user x-coordinate bounds. Likewise, if the user coordinate rectangle 
 * has zero width, then conversion from any user x-coordinate to device 
 * x-coordinate yields the average of the device x-coordinate bounds.
 *
 * @author Dave Hale, Colorado School of Mines
 * @version 2004.12.23
 */
public class Transcaler {

  /**
   * Constructs a transcaler with identity coordinate mapping.
   * In this mapping, device coordinates equal user coordinates,
   * rounded to the nearest integer.
   */
  public Transcaler() {
    this(0.0,1.0,0.0,1.0,0,1,0,1);
  }

  /**
   * Constructs a transcaler with specified coordinate mapping.
   * @param x1u the user x-coordinate corresponding to x1d.
   * @param y1u the user y-coordinate corresponding to y1d.
   * @param x2u the user x-coordinate corresponding to x2d.
   * @param y2u the user y-coordinate corresponding to y2d.
   * @param x1d the device x-coordinate corresponding to x1u.
   * @param y1d the device y-coordinate corresponding to y1u.
   * @param x2d the device x-coordinate corresponding to x2u.
   * @param y2d the device y-coordinate corresponding to y2u.
   */
  public Transcaler(
    double x1u, double y1u, double x2u, double y2u,
    int    x1d, int    y1d, int    x2d, int    y2d)
  {
    setMapping(x1u,y1u,x2u,y2u,x1d,y1d,x2d,y2d);
  }

  /**
   * Sets the coordinate mapping for this transcaler.
   * @param x1u the user x-coordinate corresponding to x1d.
   * @param y1u the user y-coordinate corresponding to y1d.
   * @param x2u the user x-coordinate corresponding to x2d.
   * @param y2u the user y-coordinate corresponding to y2d.
   * @param x1d the device x-coordinate corresponding to x1u.
   * @param y1d the device y-coordinate corresponding to y1u.
   * @param x2d the device x-coordinate corresponding to x2u.
   * @param y2d the device y-coordinate corresponding to y2u.
   */
  public void setMapping(
    double x1u, double y1u, double x2u, double y2u,
    int    x1d, int    y1d, int    x2d, int    y2d)
  {
    _x1u = x1u;  _x2u = x2u;  _y1u = y1u;  _y2u = y2u;
    _x1d = x1d;  _x2d = x2d;  _y1d = y1d;  _y2d = y2d;
    computeShiftAndScale();
  }

  /**
   * Sets the user-coordinate part of the mapping for this transcaler.
   * @param x1u the user x-coordinate corresponding to the current x1d.
   * @param y1u the user y-coordinate corresponding to the current y1d.
   * @param x2u the user x-coordinate corresponding to the current x2d.
   * @param y2u the user y-coordinate corresponding to the current y2d.
   */
  public void setMapping(double x1u, double y1u, double x2u, double y2u) {
    _x1u = x1u;  _x2u = x2u;  _y1u = y1u;  _y2u = y2u;
    computeShiftAndScale();
  }

  /**
   * Sets the device-coordinate part of the mapping for this transcaler.
   * @param x1d the device x-coordinate corresponding to the current x1u.
   * @param y1d the device y-coordinate corresponding to the current y1u.
   * @param x2d the device x-coordinate corresponding to the current x2u.
   * @param y2d the device y-coordinate corresponding to the current y2u.
   */
  public void setMapping(int x1d, int y1d, int x2d, int y2d) {
    _x1d = x1d;  _x2d = x2d;  _y1d = y1d;  _y2d = y2d;
    computeShiftAndScale();
  }

  /**
   * Converts the specified user x-coordinate to device x-coordinate.
   * @param xu the user x-coordinate.
   * @return the device x-coordinate.
   */
  public int x(double xu) {
    double xd = _xushift+_xuscale*xu;
    if (xd<DMIN) {
      xd = DMIN;
    } else if (xd>DMAX) {
      xd = DMAX;
    }
    return (int)xd;
  }

  /**
   * Converts the specified user y-coordinate to device y-coordinate.
   * @param yu the user y-coordinate.
   * @return the device y-coordinate.
   */
  public int y(double yu) {
    double yd = _yushift+_yuscale*yu;
    if (yd<DMIN) {
      yd = DMIN;
    } else if (yd>DMAX) {
      yd = DMAX;
    }
    return (int)yd;
  }

  /**
   * Converts the specified device x-coordinate to user x-coordinate.
   * @param xd the device x-coordinate.
   * @return the user x-coordinate.
   */
  public double x(int xd) {
    return _xdshift+_xdscale*xd;
  }

  /**
   * Converts the specified device y-coordinate to user y-coordinate.
   * @param yd the device y-coordinate.
   * @return the user y-coordinate.
   */
  public double y(int yd) {
    return _ydshift+_ydscale*yd;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private static final double DMIN = -32768.0; // device min coordinate
  private static final double DMAX =  32767.0; // device max coordinate

  private double _x1u,_y1u,_x2u,_y2u;
  private int    _x1d,_y1d,_x2d,_y2d;
  private double _xushift,_xuscale,_yushift,_yuscale;
  private double _xdshift,_xdscale,_ydshift,_ydscale;

  private void computeShiftAndScale() {
    if (_x1u!=_x2u) {
      _xuscale = (_x2d-_x1d)/(_x2u-_x1u);
      _xushift = _x1d-_x1u*_xuscale+0.5;
    } else {
      _xushift = 0.5*(_x1d+_x2d)+0.5;
      _xuscale = 0.0;
    }
    if (_x1d!=_x2d) {
      _xdscale = (_x2u-_x1u)/(_x2d-_x1d);
      _xdshift = _x1u-_x1d*_xdscale;
    } else {
      _xdshift = 0.5*(_x1u+_x2u);
      _xdscale = 0.0;
    }
    if (_y1u!=_y2u) {
      _yuscale = (_y2d-_y1d)/(_y2u-_y1u);
      _yushift = _y1d-_y1u*_yuscale+0.5;
    } else {
      _yushift = 0.5*(_y1d+_y2d)+0.5;
      _yuscale = 0.0;
    }
    if (_y1d!=_y2d) {
      _ydscale = (_y2u-_y1u)/(_y2d-_y1d);
      _ydshift = _y1u-_y1d*_ydscale;
    } else {
      _ydshift = 0.5*(_y1u+_y2u);
      _ydscale = 0.0;
    }
  }
}