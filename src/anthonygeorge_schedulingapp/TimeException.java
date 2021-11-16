/*
Gives an error message that is set when the exception is thrown. Used for giving an error when
business hours are invalid
 */
package anthonygeorge_schedulingapp;

/**
 *
 * @author Anthony
 */
public class TimeException extends Exception{
    public  TimeException(String s){
        super(s);
    }
}
