package de.hgu.gsehen.evapotranspiration;

import static java.lang.Math.PI;

import java.util.Calendar;

import org.junit.platform.commons.annotation.Testable;

public class EnvCalculator {
  private static final double ALPHA = 0.23;
  private static final double LAI = 0.7;
  private static final double RS = 70;
  private static final double RA = 208;
  private static final double GSC = 0.082;

  /**
   * Main method to calculate the daily reference evapotranspiration as published by Allen et al.
   * All parameters are daily values. method changes object DayData et0
   * 
   * @author MO
   * @param dayData An Object of the DayData class
   * @param geoData An Object of the GeoData class
   */
  @Testable
  public static void calculateEt0(DayData dayData, GeoData geoData) {
    Integer yday = tellDoy(dayData);

    double psi = convertDegToRad(geoData);
    dayData.setEt0(5);
  }


  /**
   * Method to convert the geo position degree into rad.
   * 
   * @author MO
   * @param geoData An object of the GeoData class
   * @return double rad
   */
  private static double convertDegToRad(GeoData geoData) {
    double psi = geoData.getGeoWid() * PI / 180;
    return (psi);
  }

  private static double calculateRs(double n, double gn, double ra) {
    double rs = (0.25 + 0.5 * (n / gn)) * ra;
    return (rs);
  }

  /*-
  Rs.f <- function(n, N, Ra) {
      (0.25 + 0.5 * (n/N)) * Ra
  }
  
  
  Rs <- Glob
  windgesch2m <- function(v, HW) {
      (v * 4.87)/log(67.8 * HW - 5.42)
  }
  u2 <- windgesch2m(Wind, HW)
  
  L <- function(Temp) {
      2.501 - 0.002361 * Temp
  }
  s <- function(Temp) {
      (4098 * 0.6108 * exp((17.27 * Temp)/(237.3 + Temp)))/((237.3 + 
          Temp)^2)
  }
  dr <- function(J) {
      1 + 0.033 * cos((2 * pi/365) * J)
  }
  theta <- function(J) {
      0.409 * sin((2 * pi/365) * J - 1.39)
  }
  omegas <- function(theta, psi) {
      acos(-tan(psi) * tan(theta))
  }
  N.f <- function(omegas) {
      24/pi * omegas
  }
  Ra <- function(Gsc, dr, omegas, psi, theta) {
      (24 * 60/pi) * Gsc * dr * (omegas * sin(psi) * sin(theta) + 
          cos(psi) * cos(theta) * sin(omegas))
  }
  Rs0 <- function(Ra) {
      0.75 * Ra
  }
  Rns <- function(Rs, alpha) {
      (1 - alpha) * Rs
  }
  Tabs <- function(Temp) {
      273.16 + Temp
  }
  es <- function(Temp,Tmin,Tmax) {
      if(!is.na(Tmin)&&is.na(Tmax)||is.na(Tmin)&&!is.na(Tmax)){stop("Tmax and Tmin must be available both !")}
      e.f <-function(Tinput)0.6108 * exp((17.27 * Tinput)/(237.3 + Tinput))
      if(is.na(Tmin)&&is.na(Tmax)){return(e.f(Temp))}
      else{return(((e.f(Tmin)+e.f(Tmax))/2))}
  }
  ea <- function(Hum, es) {
      es * (Hum/100)
  }
  Rnl <- function(Tabs, ea, Rs, Rs0) {
      4.901e-09 * Tabs^4 * (0.34 - 0.14 * sqrt(ea)) * (1.35 * 
          (Rs/Rs0) - 0.35)
  }
  Rn <- function(Rns, Rnl) {
      Rns - Rnl
  }
  G <- function(Rns) {
      0.2 * Rns
  }
  P <- function(NN) {
      101.3 * ((293 - 0.0065 * NN)/293)^5.26
  }
  rho <- function(Temp, P) {
      P/(1.01 * (Temp + 273) * 0.287)
  }
  gamma <- function(L, P) {
      (0.001013 * P)/(0.622 * L)
  }
  Etfao.f <- function(Rn, G, Temp, u2, es, ea, s, gamma) {
      (0.408 * s * (Rn - G) + gamma * (900/(Temp + 273) * u2 * 
          (es - ea)))/(s + gamma * (1 + 0.34 * u2))
  }
  Latente <- L(Temp)
  Steigung <- s(Temp)
  Relatived <- dr(J)
  DekdS <- theta(J)
  Wbsu <- omegas(theta = DekdS, psi)
  N <- N.f(Wbsu)
  Exts <- Ra(Gsc = Gsc, dr = Relatived, omegas = Wbsu, psi = psi, 
      theta = DekdS)
  Gbu <- Rs0(Ra = Exts)
  Kns <- Rns(Rs = Rs, alpha = alpha)
  AT <- Tabs(Temp = Temp)
  Sd <- es(Temp = Temp,Tmin = Tmin, Tmax= Tmax)
  Ad <- ea(Hum = Hum, es = Sd)
  LNs <- Rnl(Tabs = AT, ea = Ad, Rs = Rs, Rs0 = Gbu)
  Strbil <- Rn(Rns = Kns, Rnl = LNs)
  Bws <- G(Rns = Kns)
  LuftD <- P(NN = NN)
  Ldich <- rho(Temp = Temp, P = LuftD)
  Psy <- gamma(L = Latente, P = LuftD)
  EtfaomG <- Etfao.f(Rn = Strbil, G = 0, Temp = Temp, u2 = u2, 
      es = Sd, ea = Ad, s = Steigung, gamma = Psy)
  FAO56 <- round(digits = 1, EtfaomG)
  */

  /**
   * Method to extract the day of the year 1-366 from a date from a DayData object.
   * 
   * @author MO
   * @param dayData DayData object
   * @return An Integer with the value of day of the year 1-366
   */
  private static Integer tellDoy(DayData dayData) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(dayData.getDate());
    Integer yday = cal.get(Calendar.DAY_OF_YEAR);
    return (yday);
  }
}
