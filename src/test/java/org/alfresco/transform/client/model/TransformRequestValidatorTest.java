/*
 * Copyright 2015-2018 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */

package org.alfresco.transform.client.model;

import static org.alfresco.transform.client.model.Mimetype.MIMETYPE_IMAGE_PNG;
import static org.alfresco.transform.client.model.Mimetype.MIMETYPE_PDF;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;

/**
 * TransformRequestValidatorTest
 * <p/>
 * Unit test that checks the Transform request validation.
 */
public class TransformRequestValidatorTest
{
    private TransformRequestValidator validator = new TransformRequestValidator();

    @Test
    public void testSupports()
    {
        Assert.assertTrue(validator.supports(TransformRequest.class));
    }

    @Test
    public void testNullRequest()
    {
        Errors errors = new DirectFieldBindingResult(null, "request");
        validator.validate(null, errors);

        Assert.assertEquals(1, errors.getAllErrors().size());
        Assert.assertEquals("request cannot be null",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingId()
    {
        TransformRequest request = new TransformRequest();
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("requestId cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingSourceReference()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("sourceReference cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingSourceSize()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("sourceSize cannot be null or have its value smaller than 0",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingSourceMediaType()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("sourceMediaType cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingTargetMediaType()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        request.setSourceMediaType(MIMETYPE_PDF);
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("targetMediaType cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingTargetExtension()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        request.setSourceMediaType(MIMETYPE_PDF);
        request.setTargetMediaType(MIMETYPE_IMAGE_PNG);
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("targetExtension cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingClientData()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        request.setSourceMediaType(MIMETYPE_PDF);
        request.setTargetMediaType(MIMETYPE_IMAGE_PNG);
        request.setTargetExtension("png");
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("clientData cannot be null or empty",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testMissingSchema()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        request.setSourceMediaType(MIMETYPE_PDF);
        request.setTargetMediaType(MIMETYPE_IMAGE_PNG);
        request.setTargetExtension("png");
        request.setClientData("ACS");
        request.setSchema(-1);
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertFalse(errors.getAllErrors().isEmpty());
        Assert.assertEquals("schema cannot be less than 0",
            errors.getAllErrors().iterator().next().getDefaultMessage());
    }

    @Test
    public void testCompleteTransformRequest()
    {
        TransformRequest request = new TransformRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setSourceReference(UUID.randomUUID().toString());
        request.setSourceSize(32L);
        request.setSourceMediaType(MIMETYPE_PDF);
        request.setTargetMediaType(MIMETYPE_IMAGE_PNG);
        request.setTargetExtension("png");
        request.setClientData("ACS");
        Errors errors = new DirectFieldBindingResult(request, "request");

        validator.validate(request, errors);

        Assert.assertTrue(errors.getAllErrors().isEmpty());
    }
}
