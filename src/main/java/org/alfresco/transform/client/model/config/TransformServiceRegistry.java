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

import java.util.Map;

/**
 * Used by clients work out if a transformation is supported by a Transform Service.
 */
public interface TransformServiceRegistry
{
    /**
     * Works out if the Transform Server should be able to transform content of a given source mimetype and size into a
     * target mimetype given a list of actual transform option names and values (Strings) plus the data contained in the
     * Transformer objects registered with this class.
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if {@code -1}.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     * @return {@code}true{@code} if it is supported.
     */
    boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                        Map<String, String> actualOptions, String transformName);

    /**
     * Returns the maximun size (in bytes) of the source content that can be transformed.
     * @param sourceMimetype the mimetype of the source content
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param transformName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     * @return the maximum size (in bytes) of the source content that can be transformed. If {@code -1} there is no
     * limit, but if {@code 0} the transform is not supported.
     */
    long getMaxSize(String sourceMimetype, String targetMimetype,
                    Map<String, String> actualOptions, String transformName);

    /**
     * Works out the name of the transformer (might not map to an actual transformer) that will be used to transform
     * content of a given source mimetype and size into a target mimetype given a list of actual transform option names
     * and values (Strings) plus the data contained in the Transformer objects registered with this class.
     * @param sourceMimetype the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param targetMimetype the mimetype of the target
     * @param actualOptions the actual name value pairs available that could be passed to the Transform Service.
     * @param renditionName (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                      results to avoid having to work out if a given transformation is supported a second time.
     *                      The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                      rendition name.
     * @return the name of the transformer or {@code}null{@code} if not set or there is no supported transformer.
     */
    String getTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                     Map<String, String> actualOptions, String renditionName);
}
