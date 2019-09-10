/*
 * #%L
 * Alfresco Transform Model
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
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
