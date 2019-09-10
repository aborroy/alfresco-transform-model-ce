/*
 *  Copyright 2015-2019 Alfresco Software, Ltd.  All rights reserved.
 *
 *  License rights for this program may be obtained from Alfresco Software, Ltd.
 *  pursuant to a written agreement and any use of this program without such an
 *  agreement is prohibited.
 */
package org.alfresco.transform.client.registry;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Map.Entry;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.model.config.TransformOptionValue;

class TransformRegistryHelper
{
    private static final String TIMEOUT = "timeout";

    static Set<TransformOption> lookupTransformOptions(final Set<String> transformOptionNames,
        final Map<String, Set<TransformOption>> transformOptions, final String readFrom,
        final Consumer<String> logError)
    {
        if (transformOptionNames == null)
        {
            return emptySet();
        }

        final Set<TransformOption> options = new HashSet<>();
        for (String name : transformOptionNames)
        {
            final Set<TransformOption> oneSetOfTransformOptions = transformOptions.get(name);
            if (oneSetOfTransformOptions == null)
            {
                logError.accept("transformOptions in " + readFrom + " with the name " + name +
                                " does not exist. Ignored");
                continue;
            }
            options.add(new TransformOptionGroup(false, oneSetOfTransformOptions));
        }

        return options.size() == 1 ?
               ((TransformOptionGroup) options.iterator().next()).getTransformOptions() :
               options;
    }

    // Returns transformers in increasing supported size order, where lower priority transformers for the same size have
    // been discarded.
    static List<SupportedTransform> retrieveTransformListBySize(final Data data,
        final String sourceMimetype, final String targetMimetype,
        Map<String, String> actualOptions, String transformerName)
    {
        if (actualOptions == null)
        {
            actualOptions = emptyMap();
        }
        if (transformerName != null && transformerName.trim().isEmpty())
        {
            transformerName = null;
        }

        final List<SupportedTransform> cachedTransformList =
            transformerName == null ? null :
            data.retrieveCached(transformerName, sourceMimetype);
        if (cachedTransformList != null)
        {
            return cachedTransformList;
        }

        final List<SupportedTransform> builtTransformList = buildTransformList(data,
            sourceMimetype,
            targetMimetype,
            filterTimeout(actualOptions));

        if (transformerName != null)
        {
            data.cache(transformerName, sourceMimetype, builtTransformList);
        }

        return builtTransformList;
    }

    private static List<SupportedTransform> buildTransformList(
        final Data data, final String sourceMimetype, final String targetMimetype,
        final Map<String, String> actualOptions)
    {
        final Map<String, List<SupportedTransform>> targetMap = data.retrieveTransforms(
            sourceMimetype);
        final List<SupportedTransform> supportedTransformList = targetMap.getOrDefault(
            targetMimetype, emptyList());

        final List<SupportedTransform> transformListBySize = new ArrayList<>();

        for (SupportedTransform supportedTransform : supportedTransformList)
        {
            final Map<String, Boolean> possibleTransformOptions = gatherPossibleTransformOptions(
                supportedTransform.getTransformOptions(), actualOptions);

            if (optionsMatch(possibleTransformOptions, actualOptions))
            {
                addToSupportedTransformList(transformListBySize, supportedTransform);
            }
        }
        return transformListBySize;
    }

    // Add newTransform to the transformListBySize in increasing size order and discards
    // lower priority (numerically higher) transforms with a smaller or equal size.
    private static void addToSupportedTransformList(
        final List<SupportedTransform> transformListBySize,
        final SupportedTransform newTransform)
    {
        for (int i = 0; i < transformListBySize.size(); i++)
        {
            final SupportedTransform existingTransform = transformListBySize.get(i);
            int added = -1;

            final int compare = compare(newTransform.getMaxSourceSizeBytes(),
                existingTransform.getMaxSourceSizeBytes());
            if (compare < 0)
            {
                transformListBySize.add(i, newTransform);
                added = i;
            }
            else if (compare == 0)
            {
                if (newTransform.getPriority() < existingTransform.getPriority())
                {
                    transformListBySize.set(i, newTransform);
                    added = i;
                }
            }

            if (added == i)
            {
                for (i--; i >= 0; i--)
                {
                    if (newTransform.getPriority() <= transformListBySize.get(i).getPriority())
                    {
                        transformListBySize.remove(i);
                    }
                }
                return;
            }
        }
        transformListBySize.add(newTransform);
    }

    private static Map<String, Boolean> gatherPossibleTransformOptions(
        final TransformOptionGroup transformOptionGroup, final Map<String, String> actualOptions)
    {
        final Map<String, Boolean> possibleTransformOptions = new HashMap<>();
        addToPossibleTransformOptions(possibleTransformOptions, transformOptionGroup, true,
            actualOptions);
        return possibleTransformOptions;
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
     * themselves required but not in the actualOptions. In this the optionsMatch method will return false.
     *
     * @return true if any options were added. Used by nested call parents to determine if an option was added from a
     * nested sub group.
     */
    static boolean addToPossibleTransformOptions(
        final Map<String, Boolean> possibleTransformOptions,
        final TransformOptionGroup transformOptionGroup, final Boolean parentGroupRequired,
        final Map<String, String> actualOptions)
    {
        boolean added = false;
        boolean required = false;

        final Set<TransformOption> optionList = transformOptionGroup.getTransformOptions();
        if (optionList != null && !optionList.isEmpty())
        {
            // We need to avoid adding options from a group that is required but its parents are not.
            boolean transformOptionGroupRequired = transformOptionGroup.isRequired() && parentGroupRequired;

            // Check if the group contains options in actualOptions. This will add any options from sub groups.
            for (final TransformOption transformOption : optionList)
            {
                if (transformOption instanceof TransformOptionGroup)
                {
                    added = addToPossibleTransformOptions(possibleTransformOptions,
                        (TransformOptionGroup) transformOption, transformOptionGroupRequired,
                        actualOptions);
                    required |= added;
                }
                else
                {
                    final String name = ((TransformOptionValue) transformOption).getName();
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
                        final TransformOptionValue option = (TransformOptionValue) transformOption;
                        possibleTransformOptions.put(option.getName(), option.isRequired());
                    }
                }
            }
        }

        return added;
    }

    // compare where -1 is unlimited.
    private static int compare(final long a, final long b)
    {
        return a == -1 ? b == -1 ? 0 : 1 : Long.compare(a, b);
    }

    static boolean optionsMatch(final Map<String, Boolean> transformOptions,
        final Map<String, String> actualOptions)
    {
        // Check all required transformOptions are supplied
        final boolean supported = transformOptions
            .entrySet()
            .stream()
            .filter(Entry::getValue)// filter by the required status
            .map(Entry::getKey)// map to the option name
            .allMatch(actualOptions::containsKey);

        if (!supported)
        {
            return false;
        }

        // Check there are no extra unused actualOptions
        return actualOptions
            .keySet()
            .stream()
            .allMatch(transformOptions::containsKey);
    }

    private static Map<String, String> filterTimeout(final Map<String, String> options)
    {
        // Remove the "timeout" property from the actualOptions as it is not used to select a transformer.
        if (!options.containsKey(TIMEOUT))
        {
            return options;
        }
        return options
            .entrySet()
            .stream()
            .filter(e -> TIMEOUT.equals(e.getKey()))
            .collect(toMap(Entry::getKey, Entry::getValue));
    }
}
