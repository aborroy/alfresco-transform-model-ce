/*
 * Copyright 2015-2018 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.model;

import java.io.Serializable;
import java.util.Objects;

public class TransformReply implements Serializable
{
    private String requestId;
    private int status;
    private String errorDetails;
    private String sourceReference;
    private String targetReference;
    private String clientData;
    private int schema;
    private InternalContext internalContext;

    //region [Accessors]
    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getErrorDetails()
    {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails)
    {
        this.errorDetails = errorDetails;
    }

    public String getSourceReference()
    {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference)
    {
        this.sourceReference = sourceReference;
    }

    public String getTargetReference()
    {
        return targetReference;
    }

    public void setTargetReference(String targetReference)
    {
        this.targetReference = targetReference;
    }

    public String getClientData()
    {
        return clientData;
    }

    public void setClientData(String clientData)
    {
        this.clientData = clientData;
    }

    public int getSchema()
    {
        return schema;
    }

    public void setSchema(int schema)
    {
        this.schema = schema;
    }

    public InternalContext getInternalContext()
    {
        return internalContext;
    }

    public void setInternalContext(InternalContext internalContext)
    {
        this.internalContext = internalContext;
    }

    //endregion

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TransformReply that = (TransformReply) o;
        return Objects.equals(requestId, that.requestId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(requestId);
    }

    @Override public String toString()
    {
        return "TransformReply{" +
               "requestId='" + requestId + '\'' +
               ", status=" + status +
               ", errorDetails='" + errorDetails + '\'' +
               ", sourceReference='" + sourceReference + '\'' +
               ", targetReference='" + targetReference + '\'' +
               ", clientData='" + clientData + '\'' +
               ", schema=" + schema +
               ", internalContext=" + internalContext +
               '}';
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final TransformReply reply = new TransformReply();

        public Builder withRequestId(final String requestId)
        {
            reply.requestId = requestId;
            return this;
        }

        public Builder withStatus(final int status)
        {
            reply.status = status;
            return this;
        }

        public Builder withErrorDetails(final String errorDetails)
        {
            reply.errorDetails = errorDetails;
            return this;
        }

        public Builder withSourceReference(final String sourceReference)
        {
            reply.sourceReference = sourceReference;
            return this;
        }

        public Builder withTargetReference(final String targetReference)
        {
            reply.targetReference = targetReference;
            return this;
        }

        public Builder withClientData(final String clientData)
        {
            reply.clientData = clientData;
            return this;
        }

        public Builder withSchema(final int schema)
        {
            reply.schema = schema;
            return this;
        }

        public TransformReply build()
        {
            return reply;
        }
    }
}
