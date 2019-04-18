/*
 * Copyright 2015-2018 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds required contextual information for a multi-step transform.
 *
 * @author Lucian Tuca
 * created on 19/12/2018
 */
public class MultiStep implements Serializable
{
    private String initialRequestId;
    private String initialSourceMediaType;
    private List<String> transformsToBeDone = new ArrayList<>();

    // regions [Accessors]

    public String getInitialSourceMediaType()
    {
        return initialSourceMediaType;
    }

    public void setInitialSourceMediaType(String initialSourceMediaType)
    {
        this.initialSourceMediaType = initialSourceMediaType;
    }

    public String getInitialRequestId()
    {
        return initialRequestId;
    }

    public void setInitialRequestId(String initialRequestId)
    {
        this.initialRequestId = initialRequestId;
    }

    public List<String> getTransformsToBeDone()
    {
        return transformsToBeDone;
    }

    public void setTransformsToBeDone(List<String> transformsToBeDone)
    {
        this.transformsToBeDone = transformsToBeDone;
    }

    //endregion

    @Override public String toString()
    {
        return "MultiStep{" +
               "initialRequestId='" + initialRequestId + '\'' +
               ", initialSourceMediaType='" + initialSourceMediaType + '\'' +
               ", transformsToBeDone=" + transformsToBeDone +
               '}';
    }
}
