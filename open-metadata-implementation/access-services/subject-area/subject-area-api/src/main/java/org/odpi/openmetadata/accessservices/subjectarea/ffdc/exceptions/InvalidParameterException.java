/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.accessservices.subjectarea.ffdc.exceptions;


import java.util.Map;

/**
 * The InvalidParameterException is thrown by the Subject Area OMAS when a parameter is null or an invalid
 * value.
 */
public class InvalidParameterException extends SubjectAreaCheckedExceptionBase
{
    /**
     * invalid property name
     * @return invalid property name
     */
    public String getInvalidPropertyName() {
        return invalidPropertyName;
    }

    /**
     * invalid property value
     * @return in valid property value
     */
    public String getInvalidPropertyValue() {
        return invalidPropertyValue;
    }

    private String invalidPropertyName;
    private String invalidPropertyValue;
    private Map<String,Object> relatedProperties;
    /**
     * This is the typical constructor used for creating a InvalidParameterException.
     *
     * @param httpCode http response code to use if this exception flows over a rest call
     * @param className name of class reporting error
     * @param actionDescription description of function it was performing when error detected
     * @param errorMessage description of error
     * @param systemAction actions of the system as a result of the error
     * @param userAction instructions for correcting the error
     */
    public InvalidParameterException(int  httpCode, String className, String  actionDescription, String errorMessage, String systemAction, String userAction)
    {
        super(httpCode, className, actionDescription, errorMessage, systemAction, userAction);
    }


    /**
     * This is the constructor used for creating a InvalidParameterException that resulted from a previous error.
     *
     * @param httpCode http response code to use if this exception flows over a rest call
     * @param className name of class reporting error
     * @param actionDescription description of function it was performing when error detected
     * @param errorMessage description of error
     * @param systemAction actions of the system as a result of the error
     * @param userAction instructions for correcting the error
     * @param caughtError the error that resulted in this exception.
     * */
    public InvalidParameterException(int  httpCode, String className, String  actionDescription, String errorMessage, String systemAction, String userAction, Throwable caughtError)
    {
        super(httpCode, className, actionDescription, errorMessage, systemAction, userAction, caughtError);
    }

    public InvalidParameterException(int  httpCode, String className, String  actionDescription, String errorMessage, String systemAction, String userAction, String invalidPropertyName, String invalidPropertyValue) {
        super(httpCode, className, actionDescription, errorMessage, systemAction, userAction);
        this.invalidPropertyName = invalidPropertyName;
        this.invalidPropertyValue = invalidPropertyValue;
    }

    public Map<String, Object> getRelatedProperties() {
        return relatedProperties;
    }

    public void setRelatedProperties(Map<String, Object> relatedProperties) {
        this.relatedProperties = relatedProperties;
    }
}
