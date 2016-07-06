package com.salesforce.dva.argus.service.broker.HTTP;

/**
 * Runtime Exception for Orchestra.
 *
 * @author  tvaline
 */
public class OrchestraException extends RuntimeException {

    //~ Constructors *********************************************************************************************************************************

    /** @see  java.lang.RuntimeException#RuntimeException(java.lang.String) */
    public OrchestraException(String msg) {
        super(msg);
    }

    /** @see  java.lang.RuntimeException#RuntimeException(java.lang.Throwable) */
    public OrchestraException(Throwable cause) {
        super(cause);
    }

    /** @see  java.lang.RuntimeException#RuntimeException(java.lang.String, java.lang.Throwable) */
    public OrchestraException(String msg, Throwable ex) {
        super(msg, ex);
    }
}
