/*
 * Copyright 2015-2018 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.model;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * TransformRequestValidator
 * <p/>
 * Transform request validator
 */
public class TransformRequestValidator implements Validator
{
    @Override
    public boolean supports(Class<?> aClass)
    {
        return aClass.isAssignableFrom(TransformRequest.class);
    }

    @Override
    public void validate(Object o, Errors errors)
    {
        final TransformRequest request = (TransformRequest) o;

        if (request == null)
        {
            errors.reject(null, "request cannot be null");
        }
        else
        {
            String requestId = request.getRequestId();
            if (requestId == null || requestId.isEmpty())
            {
                errors.rejectValue("requestId", null, "requestId cannot be null or empty");
            }
            String sourceReference = request.getSourceReference();
            if (sourceReference == null || sourceReference.isEmpty())
            {
                errors.rejectValue("sourceReference", null,
                    "sourceReference cannot be null or empty");
            }
            Long sourceSize = request.getSourceSize();
            if (sourceSize == null || sourceSize.longValue() <= 0)
            {
                errors.rejectValue("sourceSize", null,
                    "sourceSize cannot be null or have its value smaller than 0");
            }
            String sourceMediaType = request.getSourceMediaType();
            if (sourceMediaType == null || sourceMediaType.isEmpty())
            {
                errors.rejectValue("sourceMediaType", null,
                    "sourceMediaType cannot be null or empty");
            }
            String targetMediaType = request.getTargetMediaType();
            if (targetMediaType == null || targetMediaType.isEmpty())
            {
                errors.rejectValue("targetMediaType", null,
                    "targetMediaType cannot be null or empty");
            }
            String targetExtension = request.getTargetExtension();
            if (targetExtension == null || targetExtension.isEmpty())
            {
                errors.rejectValue("targetExtension", null,
                    "targetExtension cannot be null or empty");
            }
            String clientData = request.getClientData();
            if (clientData == null || clientData.isEmpty())
            {
                errors.rejectValue("clientData", String.valueOf(request.getSchema()),
                    "clientData cannot be null or empty");
            }
            if (request.getSchema() < 0)
            {
                errors.rejectValue("schema", String.valueOf(request.getSchema()),
                    "schema cannot be less than 0");
            }
        }
    }
}
