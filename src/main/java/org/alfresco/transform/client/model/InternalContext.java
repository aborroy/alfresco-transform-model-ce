/*
 * Copyright 2015-2018 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds required contextual information.
 *
 * @author Denis Ungureanu
 * created on 10/01/2019
 */
public class InternalContext implements Serializable
{
    private MultiStep multiStep;
    private int attemptedRetries;
    private String currentSourceMediaType;
    private String currentTargetMediaType;
    private String replyToDestination;
    private Long currentSourceSize;
    private Map<String, String> transformRequestOptions = new HashMap<>();

    public MultiStep getMultiStep()
    {
        return multiStep;
    }

    public void setMultiStep(MultiStep multiStep)
    {
        this.multiStep = multiStep;
    }

    public int getAttemptedRetries()
    {
        return attemptedRetries;
    }

    public void setAttemptedRetries(int attemptedRetries)
    {
        this.attemptedRetries = attemptedRetries;
    }

    public String getCurrentSourceMediaType()
    {
        return currentSourceMediaType;
    }

    public void setCurrentSourceMediaType(String currentSourceMediaType)
    {
        this.currentSourceMediaType = currentSourceMediaType;
    }

    public String getCurrentTargetMediaType()
    {
        return currentTargetMediaType;
    }

    public void setCurrentTargetMediaType(String currentTargetMediaType)
    {
        this.currentTargetMediaType = currentTargetMediaType;
    }

    /**
     * Gets the reply to destination name.
     *
     * @return replyToDestination
     */
    public String getReplyToDestination()
    {
        return replyToDestination;
    }

    /**
     * Sets the reply to destination name.
     * Note: replyToDestination is populated from jmsMessage replyTo field sent by T-Client
     *
     * @param replyToDestination reply to destination name
     */
    public void setReplyToDestination(String replyToDestination)
    {
        this.replyToDestination = replyToDestination;
    }

    public Long getCurrentSourceSize()
    {
        return currentSourceSize;
    }

    public void setCurrentSourceSize(Long currentSourceSize)
    {
        this.currentSourceSize = currentSourceSize;
    }

    public Map<String, String> getTransformRequestOptions()
    {
        return transformRequestOptions;
    }

    public void setTransformRequestOptions(
        Map<String, String> transformRequestOptions)
    {
        this.transformRequestOptions = transformRequestOptions;
    }

    @Override public String toString()
    {
        return "InternalContext{" +
               "multiStep=" + multiStep +
               ", attemptedRetries=" + attemptedRetries +
               ", currentSourceMediaType='" + currentSourceMediaType + '\'' +
               ", currentTargetMediaType='" + currentTargetMediaType + '\'' +
               ", replyToDestination='" + replyToDestination + '\'' +
               ", currentSourceSize=" + currentSourceSize +
               ", transformRequestOptions=" + transformRequestOptions +
               '}';
    }
}
