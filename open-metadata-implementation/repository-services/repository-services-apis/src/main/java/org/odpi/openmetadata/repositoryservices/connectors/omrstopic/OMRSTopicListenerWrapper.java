/* SPDX-License-Identifier: Apache 2.0 */
/* Copyright Contributors to the ODPi Egeria project. */

package org.odpi.openmetadata.repositoryservices.connectors.omrstopic;

import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditCode;
import org.odpi.openmetadata.repositoryservices.auditlog.OMRSAuditLog;
import org.odpi.openmetadata.repositoryservices.events.OMRSInstanceEvent;
import org.odpi.openmetadata.repositoryservices.events.OMRSRegistryEvent;
import org.odpi.openmetadata.repositoryservices.events.OMRSTypeDefEvent;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * OMRSTopicListenerWrapper is a class that wraps a real OMRSTopicListener when it registers with the
 * OMRSTopicConnector.  Its sole purpose is to catch exceptions from the real OMRSTopicListener and create
 * diagnostics.  The listeners are called in parallel with no mechanism for the connector to properly
 * manage errors from the listener so this wrapper has been installed.  If the real OMRSTopicListener
 * has been implemented properly then no errors should be handled by this wrapper class
 */
public class OMRSTopicListenerWrapper implements OMRSTopicListener
{
    private OMRSTopicListener  realListener;
    private OMRSAuditLog       auditLog;
    private String             serviceName = "<Unknown Service>";


    /**
     * Save the real listener and other error handling information.
     *
     * @param realListener this is the topic listener that was registered.
     * @param serviceName this is the name of the service that owns the topic listener.
     * @param auditLog this is the log destination
     */
    OMRSTopicListenerWrapper(OMRSTopicListener  realListener,
                             String             serviceName,
                             OMRSAuditLog       auditLog)
    {
        this.realListener = realListener;
        this.serviceName = serviceName;
        this.auditLog = auditLog;
    }


    /**
     * Save the real listener and other error handling information.
     *
     * @param realListener this is the topic listener that was registered.
     * @param auditLog this is the log destination
     */
    @Deprecated
    OMRSTopicListenerWrapper(OMRSTopicListener  realListener,
                             OMRSAuditLog       auditLog)
    {
        this.realListener = realListener;
        this.auditLog = auditLog;
    }


    /**
     * Log an audit log message to record an unexpected exception.  We should never see this message.
     * It indicates a logic error in the service that threw the exception.
     *
     * @param event string version of the event
     * @param error exception
     * @param methodName calling activity
     */
    private void logUnhandledException(String     event,
                                       Throwable  error,
                                       String     methodName)
    {
        StringWriter stackTrace = new StringWriter();
        error.printStackTrace(new PrintWriter(stackTrace));

        OMRSAuditCode auditCode = OMRSAuditCode.UNHANDLED_EXCEPTION_FROM_SERVICE_LISTENER;

        auditLog.logException(methodName,
                              auditCode.getLogMessageId(),
                              auditCode.getSeverity(),
                              auditCode.getFormattedLogMessage(serviceName,
                                                               error.getClass().getName(),
                                                               error.getMessage(),
                                                               stackTrace.toString()),
                              event,
                              auditCode.getSystemAction(),
                              auditCode.getUserAction(),
                              error);
    }


    /**
     * Method to pass a Registry event received on topic.
     *
     * @param event inbound event
     */
    public void processRegistryEvent(OMRSRegistryEvent event)
    {
        final String methodName = "processRegistryEvent";

        try
        {
            realListener.processRegistryEvent(event);
        }
        catch (Throwable  error)
        {
            logUnhandledException(event.toString(), error, methodName);
        }
    }


    /**
     * Method to pass a TypeDef event received on topic.
     *
     * @param event inbound event
     */
    public void processTypeDefEvent(OMRSTypeDefEvent event)
    {
        final String methodName = "processTypeDefEvent";

        try
        {
            realListener.processTypeDefEvent(event);
        }
        catch (Throwable  error)
        {
            logUnhandledException(event.toString(), error, methodName);
        }
    }


    /**
     * Method to pass an Instance event received on topic.
     *
     * @param event inbound event
     */
    public void processInstanceEvent(OMRSInstanceEvent event)
    {
        final String methodName = "processInstanceEvent";

        try
        {
            realListener.processInstanceEvent(event);
        }
        catch (Throwable  error)
        {
            logUnhandledException(event.toString(), error, methodName);
        }
    }
}
