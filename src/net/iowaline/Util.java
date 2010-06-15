package net.iowaline;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author aaron
 */
public class Util {

    public static String formatMilliSeconds( long numMilliseconds ){
        return Util.formatSecondFractions(numMilliseconds, 1000);
    }

    /**
     *
     * @param numUnits
     * @return
     */
    public static String formatSecondFractions( long numUnits, int denominator ){
        if ( numUnits == 0 ){
            return "0:00:00";
        }

        StringBuffer timeStr = new StringBuffer(10);
//        long deciSeconds = (numMicroseconds / denominator ) % 10;
        long seconds = (numUnits / denominator ) % 60;
        long minutes = (numUnits / denominator / 60 ) % 60;
        long hours = (numUnits / denominator / 60 / 60 );
        timeStr.append( hours );
        timeStr.append( ":" );
        if ( minutes < 10 ){
           timeStr.append( "0" ).append(minutes);
        } else {
            timeStr.append(minutes);
        }
        timeStr.append(":");
        if ( seconds < 10 ){
            timeStr.append( "0" ).append(seconds);
        } else {
            timeStr.append(seconds);
        }
//        timeStr.append(".");
//        timeStr.append(deciSeconds);
        return timeStr.toString();
    }

}
