/*
 * Copyright 2015-2019 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.registry;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.alfresco.transform.client.registry.TransformServiceRegistry.optionsMatch;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.transform.client.model.config.SupportedSourceAndTarget;
import org.alfresco.transform.client.model.config.TransformConfig;
import org.alfresco.transform.client.model.config.TransformOption;
import org.alfresco.transform.client.model.config.TransformOptionGroup;
import org.alfresco.transform.client.model.config.TransformOptionValue;
import org.alfresco.transform.client.model.config.Transformer;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Test the AbstractTransformRegistry, extended by both T-Engines and ACS repository, which need to read JSON config
 * to understand what is supported.
 *
 * @author adavis
 */
public class TransformRegistryTest
{
    private static final String GIF = "image/gif";
    private static final String JPEG = "image/jpeg";
    private static final String PDF = "application/pdf";
    private static final String DOC = "application/msword";
    private static final String XLS = "application/vnd.ms-excel";
    private static final String PPT = "application/vnd.ms-powerpoint";
    private static final String MSG = "application/vnd.ms-outlook";
    private static final String TXT = "text/plain";

    private AbstractTransformRegistry registry;
    private Map<String, Set<TransformOption>> mapOfTransformOptions;

    @Before
    public void setUp()
    {
        registry = buildTransformServiceRegistryImpl();
        mapOfTransformOptions = new HashMap<>();
    }

    private static AbstractTransformRegistry buildTransformServiceRegistryImpl()
    {
        return new AbstractTransformRegistry()
        {
            private Data data = new Data();

            @Override
            protected void logError(String msg)
            {
                // Don't throw an exception. Just log it. Part of testNoTransformOptions
                System.out.println(msg);
            }

            @Override
            protected Data getData()
            {
                return data;
            }
        };
    }

    private void assertAddToPossibleOptions(final TransformOptionGroup transformOptionGroup,
        final Set<String> actualOptionNames, final Set<String> expectedNameSet,
        final Set<String> expectedRequiredSet)
    {
        final Map<String, Boolean> possibleTransformOptions = new HashMap<>();

        registry.addToPossibleTransformOptions(possibleTransformOptions, transformOptionGroup, true,
            buildActualOptions(actualOptionNames));

        assertEquals("The expected options don't match", expectedNameSet,
            possibleTransformOptions.keySet());

        possibleTransformOptions.forEach((name, required) -> {
            if (required)
            {
                assertTrue(name + " should be REQUIRED", expectedRequiredSet.contains(name));
            }
            else
            {
                assertFalse(name + " should be OPTIONAL", expectedRequiredSet.contains(name));
            }
        });
    }

    // transformOptionNames are upper case if required.
    private void assertIsSupported(final Set<String> actualOptionNames,
        final Set<String> transformOptionNames, final String unsupportedMsg)
    {
        final Map<String, Boolean> transformOptions = transformOptionNames
            .stream()
            .collect(toMap(identity(), name -> name.toUpperCase().equals(name)));

        boolean supported = optionsMatch(transformOptions, buildActualOptions(actualOptionNames));
        if (isBlank(unsupportedMsg))
        {
            assertTrue("Expected these options to be SUPPORTED", supported);
        }
        else
        {
            assertFalse("Expected these options NOT to be supported, because " + unsupportedMsg,
                supported);
        }
    }

    private void assertTransformOptions(Set<TransformOption> setOfTransformOptions)
    {
        final Transformer transformer = new Transformer("name", singleton("testOptions"), set(
            new SupportedSourceAndTarget(DOC, TXT, -1),
            new SupportedSourceAndTarget(XLS, TXT, 1024000)));
        final TransformConfig transformConfig = TransformConfig
            .builder()
            .withTransformers(singletonList(transformer))
            .withTransformOptions(singletonMap("testOptions", setOfTransformOptions))
            .build();

        registry = buildTransformServiceRegistryImpl();
        registry.register(transformConfig, null, getClass().getName());

        assertTrue(registry.isSupported(XLS, 1024, TXT, emptyMap(), null));
        assertTrue(registry.isSupported(XLS, 1024000, TXT, null, null));
        assertFalse(registry.isSupported(XLS, 1024001, TXT, emptyMap(), null));
        assertTrue(registry.isSupported(DOC, 1024001, TXT, null, null));
    }

    protected String getBaseUrl(Transformer transformer)
    {
        return null;
    }

    private void assertTransformerName(String sourceMimetype, long sourceSizeInBytes,
        String targetMimetype, Map<String, String> actualOptions, String expectedTransformerName,
        Transformer... transformers)
    {
        buildAndPopulateRegistry(transformers);
        String transformerName = registry.getTransformerName(sourceMimetype, sourceSizeInBytes,
            targetMimetype, actualOptions, null);
        assertEquals(
            sourceMimetype + " to " + targetMimetype + " should have returned " + expectedTransformerName,
            expectedTransformerName, transformerName);
    }

    private void assertSupported(final Transformer transformer, final String sourceMimetype,
        final long sourceSizeInBytes, final String targetMimetype,
        final Map<String, String> actualOptions, final String unsupportedMsg)
    {
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions,
            unsupportedMsg, transformer);
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes,
        String targetMimetype, Map<String, String> actualOptions, String unsupportedMsg,
        Transformer... transformers)
    {
        buildAndPopulateRegistry(transformers);
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, null,
            unsupportedMsg);
    }

    private void buildAndPopulateRegistry(Transformer[] transformers)
    {
        registry = buildTransformServiceRegistryImpl();
        stream(transformers)
            .forEach(t -> registry.register(t, mapOfTransformOptions, null, getClass().getName()));
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes,
        String targetMimetype, Map<String, String> actualOptions, String renditionName,
        String unsupportedMsg)
    {
        boolean supported = registry.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype,
            actualOptions, renditionName);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue(sourceMimetype + " to " + targetMimetype + " should be SUPPORTED",
                supported);
        }
        else
        {
            assertFalse(sourceMimetype + " to " + targetMimetype + " should NOT be supported",
                supported);
        }
    }

    private static Map<String, String> buildActualOptions(final Set<String> optionNames)
    {
        return optionNames
            .stream()
            .collect(toMap(identity(), name -> "value for " + name));
    }

    @Test
    public void testOptionalGroups()
    {
        final TransformOptionGroup transformOptionGroup =
            new TransformOptionGroup(true, set(
                new TransformOptionValue(false, "1"),
                new TransformOptionValue(true, "2"),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "3.1"),
                    new TransformOptionValue(false, "3.2"),
                    new TransformOptionValue(false, "3.3"))),
                new TransformOptionGroup(false, set( // OPTIONAL
                    new TransformOptionValue(false, "4.1"),
                    new TransformOptionValue(true, "4.2"),
                    new TransformOptionValue(false, "4.3")))));

        assertAddToPossibleOptions(transformOptionGroup, emptySet(),
            set("1", "2"), set("2"));
        assertAddToPossibleOptions(transformOptionGroup, set("1"),
            set("1", "2"), set("2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2"),
            set("1", "2"), set("2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "3.2"),
            set("1", "2", "3.1", "3.2", "3.3"), set("2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "4.1"),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "4.2"),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
    }

    @Test
    public void testRequiredGroup()
    {
        TransformOptionGroup transformOptionGroup =
            new TransformOptionGroup(true, set(
                new TransformOptionValue(false, "1"),
                new TransformOptionValue(true, "2"),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "3.1"),
                    new TransformOptionValue(false, "3.2"),
                    new TransformOptionValue(false, "3.3"))),
                new TransformOptionGroup(true, set(
                    new TransformOptionValue(false, "4.1"),
                    new TransformOptionValue(true, "4.2"),
                    new TransformOptionValue(false, "4.3")))));

        assertAddToPossibleOptions(transformOptionGroup, emptySet(),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("1"),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "3.2"),
            set("1", "2", "3.1", "3.2", "3.3", "4.1", "4.2", "4.3"), set("2", "4.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "4.1"),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("2", "4.2"),
            set("1", "2", "4.1", "4.2", "4.3"), set("2", "4.2"));
    }

    @Test
    public void testNestedGroups()
    {
        TransformOptionGroup transformOptionGroup =
            new TransformOptionGroup(false, set(
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "1"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionValue(false, "1.2"),
                        new TransformOptionGroup(false, set(
                            new TransformOptionValue(false, "1.2.3"))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "2"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionValue(false, "2.2"),
                        new TransformOptionGroup(false, set(
                            new TransformOptionGroup(false, set(
                                new TransformOptionValue(false, "2.2.1.2"))))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(true, "3"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionGroup(false, set(
                            new TransformOptionGroup(false, set(
                                new TransformOptionValue(false, "3.1.1.2"))))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "4"),
                    new TransformOptionGroup(true, set(
                        new TransformOptionGroup(false, set(
                            new TransformOptionGroup(false, set(
                                new TransformOptionValue(false, "4.1.1.2"))))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "5"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionGroup(true, set(
                            new TransformOptionGroup(false, set(
                                new TransformOptionValue(false, "5.1.1.2"))))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "6"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionGroup(false, set(
                            new TransformOptionGroup(true, set(
                                new TransformOptionValue(false, "6.1.1.2"))))))))),
                new TransformOptionGroup(false, set(
                    new TransformOptionValue(false, "7"),
                    new TransformOptionGroup(false, set(
                        new TransformOptionGroup(false, set(
                            new TransformOptionGroup(false, set(
                                new TransformOptionValue(true, "7.1.1.2")))))))))
            ));

        assertAddToPossibleOptions(transformOptionGroup, emptySet(),
            emptySet(), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1"),
            set("1"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "7"),
            set("1", "7"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "7.1.1.2"),
            set("1", "7", "7.1.1.2"), set("7.1.1.2"));
        assertAddToPossibleOptions(transformOptionGroup, set("1", "6"),
            set("1", "6"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "6.1.1.2"),
            set("1", "6", "6.1.1.2"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "5"),
            set("1", "5"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "5.1.1.2"),
            set("1", "5", "5.1.1.2"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "4"),
            set("1", "4"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "4.1.1.2"),
            set("1", "4", "4.1.1.2"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("1", "3"),
            set("1", "3"), set("3"));
        assertAddToPossibleOptions(transformOptionGroup, set("1", "3.1.1.2"),
            set("1", "3", "3.1.1.2"), set("3"));

        assertAddToPossibleOptions(transformOptionGroup, set("2"),
            set("2"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("2", "2.2"),
            set("2", "2.2"), emptySet());
        assertAddToPossibleOptions(transformOptionGroup, set("3"),
            set("3"), set("3"));
        assertAddToPossibleOptions(transformOptionGroup, set("3.1.1.2"),
            set("3", "3.1.1.2"), set("3"));
    }

    @Test
    public void testRegistryIsSupportedMethod()
    {
        assertIsSupported(set("a"), set("a", "B", "c"), "required option B is missing");
        assertIsSupported(emptySet(), set("a", "B", "c"), "required option B is missing");
        assertIsSupported(set("B"), set("a", "B", "c"), null);
        assertIsSupported(set("B", "c"), set("a", "B", "c"), null);
        assertIsSupported(set("B", "a", "c"), set("a", "B", "c"), null);

        assertIsSupported(set("B", "d"), set("a", "B", "c"), "there is an extra option d");
        assertIsSupported(set("B", "c", "d"), set("a", "B", "c"), "there is an extra option d");
        assertIsSupported(set("d"), set("a", "B", "c"),
            "required option B is missing and there is an extra option d");

        assertIsSupported(set("a"), set("a", "b", "c"), null);
        assertIsSupported(emptySet(), set("a", "b", "c"), null);
        assertIsSupported(set("a", "b", "c"), set("a", "b", "c"), null);
    }

    @Test
    public void testNoActualOptions()
    {
        assertTransformOptions(set(
            new TransformOptionValue(false, "option1"),
            new TransformOptionValue(false, "option2")));
    }

    @Test
    public void testNoTransformOptions()
    {
        assertTransformOptions(emptySet());
        assertTransformOptions(null);
    }

    @Test
    public void testSupported()
    {
        mapOfTransformOptions.put("options1", set(
            new TransformOptionValue(false, "page"),
            new TransformOptionValue(false, "width"),
            new TransformOptionValue(false, "height")));
        final Transformer transformer = new Transformer("name", singleton("options1"), set(
            new SupportedSourceAndTarget(DOC, GIF, 102400),
            new SupportedSourceAndTarget(DOC, JPEG, -1),
            new SupportedSourceAndTarget(MSG, GIF, -1)));

        assertSupported(transformer, DOC, 1024, GIF, emptyMap(), null);
        assertSupported(transformer, DOC, 102400, GIF, emptyMap(), null);
        assertSupported(transformer, DOC, 102401, GIF, emptyMap(), "source is too large");
        assertSupported(transformer, DOC, 1024, JPEG, emptyMap(), null);
        assertSupported(transformer, GIF, 1024, DOC, emptyMap(),
            GIF + " is not a source of this transformer");
        assertSupported(transformer, MSG, 1024, GIF, emptyMap(), null);
        assertSupported(transformer, MSG, 1024, JPEG, emptyMap(),
            MSG + " to " + JPEG + " is not supported by this transformer");

        assertSupported(transformer, DOC, 1024, GIF, buildActualOptions(set("page", "width")),
            null);
        assertSupported(transformer, DOC, 1024, GIF,
            buildActualOptions(set("page", "width", "startPage")), "startPage is not an option");
    }

    @Test
    // renditionName used as the cache key, is an alias for a set of actualOptions and the target mimetype.
    // The source mimetype may change.
    public void testCache()
    {
        mapOfTransformOptions.put("options1", set(
            new TransformOptionValue(false, "page"),
            new TransformOptionValue(false, "width"),
            new TransformOptionValue(false, "height")));

        final Transformer transformer = new Transformer("name", singleton("options1"), set(
            new SupportedSourceAndTarget(DOC, GIF, 102400),
            new SupportedSourceAndTarget(MSG, GIF, -1)));

        registry.register(transformer, mapOfTransformOptions, null, getClass().getName());

        assertSupported(DOC, 1024, GIF, emptyMap(), "doclib", "");
        assertSupported(MSG, 1024, GIF, emptyMap(), "doclib", "");

        assertEquals(102400L, registry.getMaxSize(DOC, GIF, emptyMap(), "doclib"));
        assertEquals(-1L, registry.getMaxSize(MSG, GIF, emptyMap(), "doclib"));

        // check we are now using the cached value.
        final SupportedTransform cachedSupportedTransform = new SupportedTransform(
            new Data(),
            "name1",
            emptySet(),
            Long.MAX_VALUE,
            0);

        registry.getData()
                .getCachedSupportedTransformList()
                .get("doclib")
                .get(DOC)
                .add(cachedSupportedTransform);
        assertEquals(Long.MAX_VALUE, registry.getMaxSize(DOC, GIF, emptyMap(), "doclib"));
    }

    @Test
    public void testGetTransformerName()
    {
        Transformer t1 = new Transformer("transformer1", null,
            singleton(new SupportedSourceAndTarget(MSG, GIF, 100, 50)));
        Transformer t2 = new Transformer("transformer2", null,
            singleton(new SupportedSourceAndTarget(MSG, GIF, 200, 60)));
        Transformer t3 = new Transformer("transformer3", null,
            singleton(new SupportedSourceAndTarget(MSG, GIF, 200, 40)));
        Transformer t4 = new Transformer("transformer4", null,
            singleton(new SupportedSourceAndTarget(MSG, GIF, -1, 100)));
        Transformer t5 = new Transformer("transformer5", null,
            singleton(new SupportedSourceAndTarget(MSG, GIF, -1, 80)));

        // Select on size - priority is ignored
        assertTransformerName(MSG, 100, GIF, emptyMap(), "transformer1", t1, t2);
        assertTransformerName(MSG, 150, GIF, emptyMap(), "transformer2", t1, t2);
        assertTransformerName(MSG, 250, GIF, emptyMap(), null, t1, t2);
        // Select on priority - t1, t2 and t4 are discarded.
        //                      t3 is a higher priority and has a larger size than t1 and t2.
        //                      Similar story fo t4 with t5.
        assertTransformerName(MSG, 100, GIF, emptyMap(), "transformer3", t1, t2, t3, t4, t5);
        assertTransformerName(MSG, 200, GIF, emptyMap(), "transformer3", t1, t2, t3, t4, t5);
        // Select on size and priority, t1 and t2 discarded
        assertTransformerName(MSG, 200, GIF, emptyMap(), "transformer3", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, emptyMap(), "transformer4", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, emptyMap(), "transformer5", t1, t2, t3, t4, t5);
    }

    @Test
    public void testMultipleTransformers()
    {
        mapOfTransformOptions.put("options1", set(
            new TransformOptionValue(false, "page"),
            new TransformOptionValue(false, "width"),
            new TransformOptionValue(false, "height")));
        mapOfTransformOptions.put("options2", set(
            new TransformOptionValue(false, "opt1"),
            new TransformOptionValue(false, "opt2")));
        mapOfTransformOptions.put("options3", new HashSet<>(singletonList(
            new TransformOptionValue(false, "opt1"))));

        Transformer transformer1 = new Transformer("transformer1", singleton("options1"), set(
            new SupportedSourceAndTarget(DOC, GIF, 102400),
            new SupportedSourceAndTarget(DOC, JPEG, -1),
            new SupportedSourceAndTarget(MSG, GIF, -1)));

        Transformer transformer2 = new Transformer("transformer2", singleton("options2"), set(
            new SupportedSourceAndTarget(PDF, GIF, -1),
            new SupportedSourceAndTarget(PPT, JPEG, -1)));

        Transformer transformer3 = new Transformer("transformer3", singleton("options3"),
            new HashSet<>(singletonList(
                new SupportedSourceAndTarget(DOC, GIF, -1))));

        assertSupported(DOC, 1024, GIF, emptyMap(), null, transformer1);
        assertSupported(DOC, 1024, GIF, emptyMap(), null, transformer1, transformer2);
        assertSupported(DOC, 1024, GIF, emptyMap(), null, transformer1, transformer2,
            transformer3);

        assertSupported(DOC, 102401, GIF, emptyMap(), "source is too large", transformer1);
        assertSupported(DOC, 102401, GIF, emptyMap(), null, transformer1, transformer3);

        assertSupported(PDF, 1024, GIF, emptyMap(), "Only transformer2 supports these mimetypes",
            transformer1);
        assertSupported(PDF, 1024, GIF, emptyMap(), null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, emptyMap(), null, transformer1, transformer2,
            transformer3);

        final Map<String, String> actualOptions = buildActualOptions(set("opt1"));
        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2/4 supports these options",
            transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2,
            transformer3);
        assertSupported(PDF, 1024, GIF, actualOptions,
            "transformer4 supports opt1 but not the source mimetype ", transformer1, transformer3);
    }

    @SafeVarargs
    private static <T> Set<T> set(T... elements)
    {
        if (elements == null || elements.length == 0)
        {
            return emptySet();
        }
        return ImmutableSet.copyOf(elements);
    }
}