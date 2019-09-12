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

import static org.alfresco.transform.client.registry.TransformRegistryHelper.retrieveTransformListBySize;
import static org.alfresco.transform.client.registry.TransformRegistryHelper.lookupTransformOptions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.transform.client.model.config.TransformConfig;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.Transformer;

/**
 * Used to work out if a transformation is supported. Sub classes should implement {@link #getData()} to return an
 * instance of the {@link TransformCache} class. This allows sub classes to periodically replace the registry's data with newer
 * values. They may also extend the Data class to include extra fields and methods.
 */
public abstract class AbstractTransformRegistry implements TransformServiceRegistry
{
    /**
     * Logs an error message if there is an error in the configuration supplied to the
     * {@link #register(org.alfresco.transform.client.model.config.Transformer, Map, String, String)}.
     *
     * @param msg to be logged.
     */
    protected abstract void logError(String msg);

    /**
     * Returns the data held by the registry. Sub classes may extend the base Data and replace it at run time.
     *
     * @return the Data object that contains the registry's data.
     */
    public abstract TransformCache getData();

    /**
     * Registers all the transformer in the transformConfig.
     *
     * @param transformConfig which contains the transformers and their options
     * @param baseUrl         where the config can be read from. Only needed when it is remote. Is null when local.
     * @param readFrom        debug message for log messages, indicating what type of config was read.
     */
    protected void registerAll(final TransformConfig transformConfig, final String baseUrl,
        final String readFrom)
    {
        transformConfig
            .getTransformers()
            .forEach(t -> register(t, transformConfig.getTransformOptions(), baseUrl, readFrom));
    }

    /**
     * Registers a single transformer.
     *
     * @param transformer      to be registered
     * @param transformOptions all the transform options
     * @param baseUrl          where the transformer was read from when remote.
     * @param readFrom         debug message for log messages, indicating what type of config was read.
     */
    public void register(final Transformer transformer,
        final Map<String, Set<TransformOption>> transformOptions, final String baseUrl,
        final String readFrom)
    {
        getData().incrementTransformerCount();
        transformer
            .getSupportedSourceAndTargetList()
            .forEach(e -> getData().appendTransform(e.getSourceMediaType(), e.getTargetMediaType(),
                new SupportedTransform(
                    transformer.getTransformerName(),
                    lookupTransformOptions(transformer.getTransformOptions(), transformOptions,
                        readFrom, this::logError),
                    e.getMaxSourceSizeBytes(),
                    e.getPriority())));
    }

    /**
     * Works out the name of the transformer (might not map to an actual transformer) that will be used to transform
     * content of a given source mimetype and size into a target mimetype given a list of actual transform option names
     * and values (Strings) plus the data contained in the Transform objects registered with this class.
     *
     * @param sourceMimetype    the mimetype of the source content
     * @param sourceSizeInBytes the size in bytes of the source content. Ignored if negative.
     * @param targetMimetype    the mimetype of the target
     * @param actualOptions     the actual name value pairs available that could be passed to the Transform Service.
     * @param renditionName     (optional) name for the set of options and target mimetype. If supplied is used to cache
     *                          results to avoid having to work out if a given transformation is supported a second time.
     *                          The sourceMimetype and sourceSizeInBytes may still change. In the case of ACS this is the
     *                          rendition name.
     */
    @Override
    public String findTransformerName(final String sourceMimetype, final long sourceSizeInBytes,
        final String targetMimetype, final Map<String, String> actualOptions,
        final String renditionName)
    {
        return retrieveTransformListBySize(getData(), sourceMimetype, targetMimetype, actualOptions,
            renditionName)
            .stream()
            .filter(t -> t.getMaxSourceSizeBytes() == -1 ||
                         t.getMaxSourceSizeBytes() >= sourceSizeInBytes)
            .findFirst()
            .map(SupportedTransform::getName)
            .orElse(null);
    }

    @Override
    public long findMaxSize(final String sourceMimetype, final String targetMimetype,
        final Map<String, String> actualOptions, final String renditionName)
    {
        final List<SupportedTransform> supportedTransforms = retrieveTransformListBySize(getData(),
            sourceMimetype, targetMimetype, actualOptions, renditionName);
        return supportedTransforms.isEmpty() ? 0 :
               supportedTransforms.get(supportedTransforms.size() - 1).getMaxSourceSizeBytes();
    }
}
