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
package org.alfresco.transform.client.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Data
{
    private Map<String, Map<String, List<SupportedTransform>>> transformers =
        new ConcurrentHashMap<>();
    private Map<String, Map<String, List<SupportedTransform>>> cachedSupportedTransformList =
        new ConcurrentHashMap<>();
    protected int transformerCount = 0;
    protected int transformCount = 0;

    public Map<String, Map<String, List<SupportedTransform>>> getTransformers()
    {
        return transformers;
    }

    public Map<String, Map<String, List<SupportedTransform>>> getCachedSupportedTransformList()
    {
        return cachedSupportedTransformList;
    }

    public void cache(final String transformerName, final String sourceMimetype,
        final List<SupportedTransform> transformListBySize)
    {
        cachedSupportedTransformList
            .get(transformerName)
            .put(sourceMimetype, transformListBySize);
    }

    public List<SupportedTransform> retrieve(final String transformerName,
        final String sourceMimetype)
    {
        return cachedSupportedTransformList
            .computeIfAbsent(transformerName, k -> new ConcurrentHashMap<>())
            .get(sourceMimetype);
    }

    @Override
    public String toString()
    {
        return transformerCount == 0 && transformCount == 0
               ? ""
               : "(transformers: " + transformerCount + " transforms: " + transformCount + ")";
    }
}

