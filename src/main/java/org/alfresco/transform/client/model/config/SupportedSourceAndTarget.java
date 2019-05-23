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
package org.alfresco.transform.client.model.config;

import java.util.Objects;

/**
 * Represents a single source and target combination supported by a transformer. File extensions are used to keep the
 * json human readable. Each par also has an optional maximum size for the source content.
 */
public class SupportedSourceAndTarget
{
    private String sourceMediaType;
    private String targetMediaType;
    private long maxSourceSizeBytes = -1;
    private int priority = 50;

    public SupportedSourceAndTarget()
    {
    }

    public SupportedSourceAndTarget(String sourceMediaType, String targetMediaType,
        long maxSourceSizeBytes)
    {
        this.sourceMediaType = sourceMediaType;
        this.targetMediaType = targetMediaType;
        this.maxSourceSizeBytes = maxSourceSizeBytes;
    }

    public SupportedSourceAndTarget(String sourceMediaType, String targetMediaType,
        long maxSourceSizeBytes, int priority)
    {
        this(sourceMediaType, targetMediaType, maxSourceSizeBytes);
        this.priority = priority;
    }

    public String getSourceMediaType()
    {
        return sourceMediaType;
    }

    public void setSourceMediaType(String sourceMediaType)
    {
        this.sourceMediaType = sourceMediaType;
    }

    public String getTargetMediaType()
    {
        return targetMediaType;
    }

    public void setTargetMediaType(String targetMediaType)
    {
        this.targetMediaType = targetMediaType;
    }

    public long getMaxSourceSizeBytes()
    {
        return maxSourceSizeBytes;
    }

    public void setMaxSourceSizeBytes(long maxSourceSizeBytes)
    {
        this.maxSourceSizeBytes = maxSourceSizeBytes;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SupportedSourceAndTarget that = (SupportedSourceAndTarget) o;
        return maxSourceSizeBytes == that.maxSourceSizeBytes &&
               priority == that.priority &&
               Objects.equals(sourceMediaType, that.sourceMediaType) &&
               Objects.equals(targetMediaType, that.targetMediaType);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sourceMediaType, targetMediaType, maxSourceSizeBytes, priority);
    }

    @Override
    public String toString()
    {
        return "SupportedSourceAndTarget{" +
               "sourceMediaType='" + sourceMediaType + '\'' +
               ", targetMediaType='" + targetMediaType + '\'' +
               ", maxSourceSizeBytes=" + maxSourceSizeBytes +
               ", priority=" + priority +
               '}';
    }
}
