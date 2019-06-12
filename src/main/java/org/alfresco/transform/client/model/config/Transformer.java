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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a set of transformations supported by the Transform Service that share the same transform options. Each
 * may be an actual transformer or the amalgamation of multiple transformers. It is possible that more than one
 * transformer may able to perform a transformation from one mimetype to another. The actual selection of transformer
 * is up to the Transform Service to decide. Clients may use TransformServiceRegistry#isSupported to decide
 * if they should send a request to the Transform Service. As a result clients have a simple generic view of
 * transformations which allows new transformations to be added without the need change client data structures other
 * than to define new name value pairs. For this to work the Transform Service defines unique names for each option.
 * <ul>
 * <li>transformerName - is unique. The client should infer nothing from the name as it is simply a label.</lI>
 * <li>transformOptions - a grouping of individual transformer transformOptions. The group may be optional and may
 * contain nested transformOptions.</li>
 * </ul>
 * For local transforms, this structure is extended when defining a pipeline transform.
 * <ul>
 * <li>transformerPipeline - an array of pairs of transformer name and target extension for each transformer in the
 * pipeline. The last one should not have an extension as that is defined by the request and should be in the
 * supported list.</li>
 * </ul>
 */
public class Transformer
{
    private String transformerName;
    private Set<String> transformOptions = new HashSet<>();
    private Set<SupportedSourceAndTarget> supportedSourceAndTargetList = new HashSet<>();
    private List<TransformStep> transformerPipeline = new ArrayList<>();

    public Transformer()
    {
    }

    public Transformer(String transformerName, Set<String> transformOptions,
        Set<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        this.transformerName = transformerName;
        this.transformOptions = transformOptions;
        this.supportedSourceAndTargetList = supportedSourceAndTargetList;
    }

    public Transformer(String transformerName, Set<String> transformOptions,
        Set<SupportedSourceAndTarget> supportedSourceAndTargetList,
        List<TransformStep> transformerPipeline)
    {
        this(transformerName, transformOptions, supportedSourceAndTargetList);
        this.transformerPipeline = transformerPipeline;
    }

    public String getTransformerName()
    {
        return transformerName;
    }

    public void setTransformerName(String transformerName)
    {
        this.transformerName = transformerName;
    }

    public List<TransformStep> getTransformerPipeline()
    {
        return transformerPipeline;
    }

    public void setTransformerPipeline(List<TransformStep> transformerPipeline)
    {
        this.transformerPipeline = transformerPipeline;
    }

    public Set<String> getTransformOptions()
    {
        return transformOptions;
    }

    public void setTransformOptions(Set<String> transformOptions)
    {
        this.transformOptions = transformOptions;
    }

    public Set<SupportedSourceAndTarget> getSupportedSourceAndTargetList()
    {
        return supportedSourceAndTargetList;
    }

    public void setSupportedSourceAndTargetList(
        Set<SupportedSourceAndTarget> supportedSourceAndTargetList)
    {
        this.supportedSourceAndTargetList = supportedSourceAndTargetList;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transformer that = (Transformer) o;
        return Objects.equals(transformerName, that.transformerName) &&
               Objects.equals(transformerPipeline, that.transformerPipeline) &&
               Objects.equals(transformOptions, that.transformOptions) &&
               Objects.equals(supportedSourceAndTargetList,
                   that.supportedSourceAndTargetList);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(transformerName, transformerPipeline, transformOptions,
            supportedSourceAndTargetList);
    }

    @Override
    public String toString()
    {
        return "Transformer{" +
               "transformerName='" + transformerName + '\'' +
               ", transformerPipeline=" + transformerPipeline +
               ", transformOptions=" + transformOptions +
               ", supportedSourceAndTargetList=" + supportedSourceAndTargetList +
               '}';
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private Transformer transformer = new Transformer();

        private Builder()
        {
        }

        public Transformer build()
        {
            return transformer;
        }

        public Builder withTransformerName(final String transformerName)
        {
            transformer.transformerName = transformerName;
            return this;
        }

        public Builder withTransformerPipeline(final List<TransformStep> transformerPipeline)
        {
            transformer.transformerPipeline = transformerPipeline;
            return this;
        }

        public Builder withTransformOptions(final Set<String> transformOptions)
        {
            transformer.transformOptions = transformOptions;
            return this;
        }

        public Builder withSupportedSourceAndTargetList(
            final Set<SupportedSourceAndTarget> supportedSourceAndTargetList)
        {
            transformer.supportedSourceAndTargetList = supportedSourceAndTargetList;
            return this;
        }
    }
}