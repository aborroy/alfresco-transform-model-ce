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

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to work out if a transformation is supported. Sub classes should implement {@link #getData()} to return an
 * instance of the {@link Data} class. This allows sub classes to periodically replace the registry's data with newer
 * values. They may also extend the Data class to include extra fields and methods.
 */
public abstract class AbstractTransformRegistry implements TransformServiceRegistry
{
    private static final String TIMEOUT = "timeout";

    public static class Data
    {
        Map<String, Map<String, List<SupportedTransform>>> transformers = new ConcurrentHashMap<>();
        Map<String, Map<String, List<SupportedTransform>>> cachedSupportedTransformList = new ConcurrentHashMap<>();
        protected int transformerCount = 0;
        protected int transformCount = 0;

        @Override
        public String toString()
        {
            return transformerCount == 0 && transformCount == 0
                   ? ""
                   : "(transformers: " + transformerCount + " transforms: " + transformCount + ")";
        }
    }

    static class SupportedTransform
    {
        TransformOptionGroup transformOptions;
        long maxSourceSizeBytes;
        private String name;
        private int priority;

        SupportedTransform(Data data, String name, Set<TransformOption> transformOptions,
            long maxSourceSizeBytes, int priority)
        {
            // Logically the top level TransformOptionGroup is required, so that child options are optional or required
            // based on their own setting.
            this.transformOptions = new TransformOptionGroup(true, transformOptions);
            this.maxSourceSizeBytes = maxSourceSizeBytes;
            this.name = name;
            this.priority = priority;
            data.transformCount++;
        }

        @Override public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SupportedTransform that = (SupportedTransform) o;
            return maxSourceSizeBytes == that.maxSourceSizeBytes &&
                   priority == that.priority &&
                   Objects.equals(transformOptions, that.transformOptions) &&
                   Objects.equals(name, that.name);
        }

        @Override public int hashCode()
        {
            return Objects.hash(transformOptions, maxSourceSizeBytes, name, priority);
        }
    }

    /**
     * Logs an error message if there is an error in the configuration supplied to the
     * {@link #register(Transformer, Map, String, String)}.
     *
     * @param msg to be logged.
     */
    protected abstract void logError(String msg);

    /**
     * Returns the data held by the registry. Sub classes may extend the base Data and replace it at run time.
     *
     * @return the Data object that contains the registry's data.
     */
    protected abstract Data getData();

    /**
     * Registers all the transformer in the transformConfig.
     *
     * @param transformConfig which contains the transformers and their options
     * @param baseUrl         where the config can be read from. Only needed when it is remote. Is null when local.
     * @param readFrom        debug message for log messages, indicating what type of config was read.
     */
    public void register(final TransformConfig transformConfig, final String baseUrl,
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
    protected void register(final Transformer transformer,
        final Map<String, Set<TransformOption>> transformOptions, final String baseUrl,
        final String readFrom)
    {
        getData().transformerCount++;
        transformer
            .getSupportedSourceAndTargetList()
            .forEach(e -> getData().transformers
                .computeIfAbsent(e.getSourceMediaType(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(e.getTargetMediaType(), k -> new ArrayList<>())
                .add(new SupportedTransform(getData(), transformer.getTransformerName(),
                    lookupTransformOptions(transformer.getTransformOptions(), transformOptions,
                        readFrom), e.getMaxSourceSizeBytes(), e.getPriority())));
    }

    private Set<TransformOption> lookupTransformOptions(final Set<String> transformOptionNames,
        final Map<String, Set<TransformOption>> transformOptions, final String readFrom)
    {
        if (transformOptionNames == null)
        {
            return emptySet();
        }

        final Set<TransformOption> optionGroups = new HashSet<>();
        for (String name : transformOptionNames)
        {
            final Set<TransformOption> oneSetOfTransformOptions = transformOptions.get(name);
            if (oneSetOfTransformOptions == null)
            {
                logError("transformOptions in " + readFrom + " with the name " + name +
                         " does not exist. Ignored");
                continue;
            }
            optionGroups.add(new TransformOptionGroup(false, oneSetOfTransformOptions));
        }

        return optionGroups.size() == 1 ?
               ((TransformOptionGroup) optionGroups.iterator().next()).getTransformOptions() :
               optionGroups;
    }

    @Override
    public boolean isSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
        Map<String, String> actualOptions, String renditionName)
    {
        long maxSize = getMaxSize(sourceMimetype, targetMimetype, actualOptions, renditionName);
        return maxSize != 0 && (maxSize == -1L || maxSize >= sourceSizeInBytes);
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
    public String getTransformerName(final String sourceMimetype, final long sourceSizeInBytes,
        final String targetMimetype, final Map<String, String> actualOptions,
        final String renditionName)
    {
        return getTransformListBySize(sourceMimetype, targetMimetype, actualOptions, renditionName)
            .stream()
            .filter(t -> t.maxSourceSizeBytes == -1 || t.maxSourceSizeBytes >= sourceSizeInBytes)
            .findFirst()
            .map(t -> t.name)
            .orElse(null);
    }

    @Override
    public long getMaxSize(final String sourceMimetype, final String targetMimetype,
        final Map<String, String> actualOptions, final String renditionName)
    {
        final List<SupportedTransform> supportedTransforms = getTransformListBySize(sourceMimetype,
            targetMimetype, actualOptions, renditionName);
        return supportedTransforms.isEmpty() ? 0 : supportedTransforms.get(
            supportedTransforms.size() - 1).maxSourceSizeBytes;
    }

    // Returns transformers in increasing supported size order, where lower priority transformers for the same size have
    // been discarded.
    private List<SupportedTransform> getTransformListBySize(String sourceMimetype,
        String targetMimetype, Map<String, String> actualOptions, String renditionName)
    {
        if (actualOptions == null)
        {
            actualOptions = emptyMap();
        }
        if (renditionName != null && renditionName.trim().isEmpty())
        {
            renditionName = null;
        }

        final Data data = getData();
        List<SupportedTransform> transformListBySize =
            renditionName == null ? null :
            data.cachedSupportedTransformList
                .computeIfAbsent(renditionName, k -> new ConcurrentHashMap<>())
                .get(sourceMimetype);
        if (transformListBySize != null)
        {
            return transformListBySize;
        }

        // Remove the "timeout" property from the actualOptions as it is not used to select a transformer.
        if (actualOptions.containsKey(TIMEOUT))
        {
            actualOptions = new HashMap<>(actualOptions);
            actualOptions.remove(TIMEOUT);
        }

        transformListBySize = new ArrayList<>();
        Map<String, List<SupportedTransform>> targetMap = data.transformers.get(sourceMimetype);
        if (targetMap != null)
        {
            List<SupportedTransform> supportedTransformList = targetMap.get(targetMimetype);
            if (supportedTransformList != null)
            {
                for (SupportedTransform supportedTransform : supportedTransformList)
                {
                    TransformOptionGroup transformOptions = supportedTransform.transformOptions;
                    Map<String, Boolean> possibleTransformOptions = new HashMap<>();
                    addToPossibleTransformOptions(possibleTransformOptions, transformOptions, true,
                        actualOptions);
                    if (isSupported(possibleTransformOptions, actualOptions))
                    {
                        addToSupportedTransformList(transformListBySize, supportedTransform);
                    }
                }
            }
        }

        if (renditionName != null)
        {
            data.cachedSupportedTransformList
                .get(renditionName)
                .put(sourceMimetype, transformListBySize);
        }

        return transformListBySize;
    }

    // Add newTransform to the transformListBySize in increasing size order and discards
    // lower priority (numerically higher) transforms with a smaller or equal size.
    private void addToSupportedTransformList(final List<SupportedTransform> transformListBySize,
        final SupportedTransform newTransform)
    {
        for (int i = 0; i < transformListBySize.size(); i++)
        {
            SupportedTransform existingTransform = transformListBySize.get(i);
            int added = -1;

            int compare = compare(newTransform.maxSourceSizeBytes,
                existingTransform.maxSourceSizeBytes);
            if (compare < 0)
            {
                transformListBySize.add(i, newTransform);
                added = i;
            }
            else if (compare == 0)
            {
                if (newTransform.priority < existingTransform.priority)
                {
                    transformListBySize.set(i, newTransform);
                    added = i;
                }
            }

            if (added == i)
            {
                for (i--; i >= 0; i--)
                {
                    existingTransform = transformListBySize.get(i);
                    if (newTransform.priority <= existingTransform.priority)
                    {
                        transformListBySize.remove(i);
                    }
                }
                return;
            }
        }
        transformListBySize.add(newTransform);
    }

    // compare where -1 is unlimited.
    private static int compare(final long a, final long b)
    {
        return a == -1 ? b == -1 ? 0 : 1 : Long.compare(a, b);
    }

    /**
     * Flatten out the transform options by adding them to the supplied possibleTransformOptions.</p>
     *
     * If possible discards options in the supplied transformOptionGroup if the group is optional and the actualOptions
     * don't provide any of the options in the group. Or to put it another way:<p/>
     *
     * It adds individual transform options from the transformOptionGroup to possibleTransformOptions if the group is
     * required or if the actualOptions include individual options from the group. As a result it is possible that none
     * of the group are added if it is optional. It is also possible to add individual transform options that are
     * themselves required but not in the actualOptions. In this the isSupported method will return false.
     *
     * @return true if any options were added. Used by nested call parents to determine if an option was added from a
     * nested sub group.
     */
    boolean addToPossibleTransformOptions(Map<String, Boolean> possibleTransformOptions,
        TransformOptionGroup transformOptionGroup,
        Boolean parentGroupRequired, Map<String, String> actualOptions)
    {
        boolean added = false;
        boolean required = false;

        Set<TransformOption> optionList = transformOptionGroup.getTransformOptions();
        if (optionList != null && !optionList.isEmpty())
        {
            // We need to avoid adding options from a group that is required but its parents are not.
            boolean transformOptionGroupRequired = transformOptionGroup.isRequired() && parentGroupRequired;

            // Check if the group contains options in actualOptions. This will add any options from sub groups.
            for (TransformOption transformOption : optionList)
            {
                if (transformOption instanceof TransformOptionGroup)
                {
                    added = addToPossibleTransformOptions(possibleTransformOptions,
                        (TransformOptionGroup) transformOption,
                        transformOptionGroupRequired, actualOptions);
                    required |= added;
                }
                else
                {
                    String name = ((TransformOptionValue) transformOption).getName();
                    if (actualOptions.containsKey(name))
                    {
                        required = true;
                    }
                }
            }

            if (required || transformOptionGroupRequired)
            {
                for (TransformOption transformOption : optionList)
                {
                    if (transformOption instanceof TransformOptionValue)
                    {
                        added = true;
                        TransformOptionValue transformOptionValue = (TransformOptionValue) transformOption;
                        String name = transformOptionValue.getName();
                        boolean optionValueRequired = transformOptionValue.isRequired();
                        possibleTransformOptions.put(name, optionValueRequired);
                    }
                }
            }
        }

        return added;
    }

    boolean isSupported(Map<String, Boolean> transformOptions, Map<String, String> actualOptions)
    {
        boolean supported = true;

        // Check all required transformOptions are supplied
        for (Map.Entry<String, Boolean> transformOption : transformOptions.entrySet())
        {
            Boolean required = transformOption.getValue();
            if (required)
            {
                String name = transformOption.getKey();
                if (!actualOptions.containsKey(name))
                {
                    supported = false;
                    break;
                }
            }
        }

        if (supported)
        {
            // Check there are no extra unused actualOptions
            for (String actualOption : actualOptions.keySet())
            {
                if (!transformOptions.containsKey(actualOption))
                {
                    supported = false;
                    break;
                }
            }
        }
        return supported;
    }
}
