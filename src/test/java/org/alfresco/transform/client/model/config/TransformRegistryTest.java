/*
 * Copyright 2015-2019 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package org.alfresco.transform.client.model.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test the AbstractTransformRegistry, extended by both T-Engines and ACS repository, which need to read JSON config
 * to understand what is supported.
 *
 * @author adavis
 */
public class TransformRegistryTest
{
    public static final String GIF = "image/gif";
    public static final String JPEG = "image/jpeg";
    public static final String PDF = "application/pdf";
    public static final String DOC = "application/msword";
    public static final String XLS = "application/vnd.ms-excel";
    public static final String PPT = "application/vnd.ms-powerpoint";
    public static final String MSG = "application/vnd.ms-outlook";
    public static final String TXT = "text/plain";

    protected AbstractTransformRegistry registry;

    protected Map<String, Set<TransformOption>> mapOfTransformOptions;
    protected Transformer transformer;
    protected TransformConfig transformConfig;
    protected Map<String, String> actualOptions;

    @Before
    public void setUp() throws Exception
    {
        registry = buildTransformServiceRegistryImpl();
        mapOfTransformOptions = new HashMap<>();
    }

    protected AbstractTransformRegistry buildTransformServiceRegistryImpl() throws Exception
    {
        AbstractTransformRegistry registry = new AbstractTransformRegistry()
        {
            Data data;

            @Override
            protected void logError(String msg)
            {
                // Don't throw an exception. Just log it. Part of testNoTransformOptions
                System.out.println(msg);
            }

            @Override
            protected Data getData()
            {
                if (data == null)
                {
                    data = new Data();
                }
                return data;
            }
        };
        return registry;
    }

    @After
    public void tearDown()
    {
        // shut down
    }

    private void assertAddToPossibleOptions(TransformOptionGroup transformOptionGroup, String actualOptionNames, String expectedNames, String expectedRequired)
    {
        actualOptions = buildActualOptions(actualOptionNames);
        Set<String> expectedNameSet = expectedNames == null || expectedNames.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(Arrays.asList(expectedNames.split(", ")));
        Set<String> expectedRequiredSet = expectedRequired == null || expectedRequired.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(Arrays.asList(expectedRequired.split(", ")));

        Map<String, Boolean> possibleTransformOptions = new HashMap<>();

        registry.addToPossibleTransformOptions(possibleTransformOptions, transformOptionGroup, true, actualOptions);

        assertEquals("The expected options don't match", expectedNameSet, possibleTransformOptions.keySet());
        for (String name: possibleTransformOptions.keySet())
        {
            Boolean required = possibleTransformOptions.get(name);
            if (required)
            {
                assertTrue(name+" should be REQUIRED", expectedRequiredSet.contains(name));
            }
            else
            {
                assertFalse(name+" should be OPTIONAL", expectedRequiredSet.contains(name));
            }
        }
    }

    // transformOptionNames are upper case if required.
    private void assertIsSupported(String actualOptionNames, String transformOptionNames, String unsupportedMsg)
    {
        actualOptions = buildActualOptions(actualOptionNames);

        Map<String, Boolean> transformOptions = new HashMap<>();
        Set<String> transformOptionNameSet = transformOptionNames == null || transformOptionNames.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(Arrays.asList(transformOptionNames.split(", ")));
        for (String name : transformOptionNameSet)
        {
            Boolean required = name.toUpperCase().equals(name);
            transformOptions.put(name, required);
        }

        boolean supported = registry.isSupported(transformOptions, actualOptions);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue("Expected these options to be SUPPORTED", supported);
        }
        else
        {
            assertFalse("Expected these options NOT to be supported, because "+unsupportedMsg, supported);
        }
    }

    private void assertTransformOptions(Set<TransformOption> setOfTransformOptions) throws Exception
    {

        transformer = new Transformer("name", Collections.singleton("testOptions"),
                new HashSet<>(Arrays.asList(
                        new SupportedSourceAndTarget(DOC, TXT, -1),
                        new SupportedSourceAndTarget(XLS, TXT, 1024000))));
        transformConfig = TransformConfig.builder().
                withTransformers(Collections.singletonList(transformer)).
                withTransformOptions(Collections.singletonMap("testOptions", setOfTransformOptions)).
                build();

        registry = buildTransformServiceRegistryImpl();
        registry.register(transformConfig, getBaseUrl(this.transformer), getClass().getName());

        assertTrue(registry.isSupported(XLS, 1024, TXT, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(XLS, 1024000, TXT, null, null));
        assertFalse(registry.isSupported(XLS, 1024001, TXT, Collections.emptyMap(), null));
        assertTrue(registry.isSupported(DOC, 1024001, TXT, null, null));
    }

    protected String getBaseUrl(Transformer transformer)
    {
        return null;
    }

    private void assertTransformerName(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                       Map<String, String> actualOptions, String expectedTransformerName,
                                       Transformer... transformers) throws Exception
    {
        buildAndPopulateRegistry(transformers);
        String transformerName = registry.getTransformerName(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, null);
        assertEquals(sourceMimetype+" to "+targetMimetype+" should have returned "+expectedTransformerName, expectedTransformerName, transformerName);
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                 Map<String, String> actualOptions, String unsupportedMsg) throws Exception
    {
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, unsupportedMsg, transformer);
    }

    private void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                 Map<String, String> actualOptions, String unsupportedMsg,
                                 Transformer... transformers) throws Exception
    {
        buildAndPopulateRegistry(transformers);
        assertSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, null, unsupportedMsg);
    }

    private void buildAndPopulateRegistry(Transformer[] transformers) throws Exception
    {
        registry = buildTransformServiceRegistryImpl();
        for (Transformer transformer : transformers)
        {
            registry.register(transformer, mapOfTransformOptions, getBaseUrl(transformer), getClass().getName());
        }
    }

    protected void assertSupported(String sourceMimetype, long sourceSizeInBytes, String targetMimetype,
                                   Map<String, String> actualOptions, String renditionName,
                                   String unsupportedMsg)
    {
        boolean supported = registry.isSupported(sourceMimetype, sourceSizeInBytes, targetMimetype, actualOptions, renditionName);
        if (unsupportedMsg == null || unsupportedMsg.isEmpty())
        {
            assertTrue(sourceMimetype+" to "+targetMimetype+" should be SUPPORTED", supported);
        }
        else
        {
            assertFalse(sourceMimetype+" to "+targetMimetype+" should NOT be supported", supported);
        }
    }

    private Map<String, String> buildActualOptions(String actualOptionNames)
    {
        Map<String, String> actualOptions = new HashMap<>();
        Set<String> actualOptionNamesSet = actualOptionNames == null || actualOptionNames.isEmpty()
                ? Collections.emptySet()
                : new HashSet<>(Arrays.asList(actualOptionNames.split(", ")));
        for (String name : actualOptionNamesSet)
        {
            actualOptions.put(name, "value for " + name);
        }
        return actualOptions;
    }

    @Test
    public void testOptionalGroups()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(true, new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3")))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList( // OPTIONAL
                                new TransformOptionValue(false, "4.1"),
                                new TransformOptionValue(true, "4.2"),
                                new TransformOptionValue(false, "4.3")))))));

        assertAddToPossibleOptions(transformOptionGroup, "",  "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2", "1, 2", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 3.2", "1, 2, 3.1, 3.2, 3.3", "2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.2", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
    }

    @Test
    public void testRequiredGroup()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(true, new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "1"),
                        new TransformOptionValue(true, "2"),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "3.1"),
                                new TransformOptionValue(false, "3.2"),
                                new TransformOptionValue(false, "3.3")))),
                        new TransformOptionGroup(true, new HashSet<>(Arrays.asList( // REQUIRED
                                new TransformOptionValue(false, "4.1"),
                                new TransformOptionValue(true, "4.2"),
                                new TransformOptionValue(false, "4.3")))))));

        assertAddToPossibleOptions(transformOptionGroup, "",  "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 3.2", "1, 2, 3.1, 3.2, 3.3, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.1", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
        assertAddToPossibleOptions(transformOptionGroup, "2, 4.2", "1, 2, 4.1, 4.2, 4.3", "2, 4.2");
    }

    @Test
    public void testNestedGroups()
    {
        TransformOptionGroup transformOptionGroup =
                new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "1"),
                                new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                        new TransformOptionValue(false, "1.2"),
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionValue(false, "1.2.3")))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "2"),
                                new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                        new TransformOptionValue(false, "2.2"),
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                        new TransformOptionValue(false, "2.2.1.2"))))))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(true, "3"), // REQUIRED
                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                        new TransformOptionValue(false, "3.1.1.2"))))))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "4"),
                                new TransformOptionGroup(true, new HashSet<>(Collections.singletonList( // REQUIRED
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                        new TransformOptionValue(false, "4.1.1.2"))))))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "5"),
                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                        new TransformOptionGroup(true, new HashSet<>(Collections.singletonList( // REQUIRED
                                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                        new TransformOptionValue(false, "5.1.1.2"))))))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "6"),
                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionGroup(true, new HashSet<>(Collections.singletonList( // REQUIRED
                                                        new TransformOptionValue(false, "6.1.1.2"))))))))))))),
                        new TransformOptionGroup(false, new HashSet<>(Arrays.asList(
                                new TransformOptionValue(false, "7"),
                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                        new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                new TransformOptionGroup(false, new HashSet<>(Collections.singletonList(
                                                        new TransformOptionValue(true, "7.1.1.2"))))))))))))) // REQUIRED
                )));

        assertAddToPossibleOptions(transformOptionGroup, "", "", "");
        assertAddToPossibleOptions(transformOptionGroup, "1", "1", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 7", "1, 7", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 7.1.1.2", "1, 7, 7.1.1.2", "7.1.1.2");
        assertAddToPossibleOptions(transformOptionGroup, "1, 6", "1, 6", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 6.1.1.2", "1, 6, 6.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 5", "1, 5", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 5.1.1.2", "1, 5, 5.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 4", "1, 4", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 4.1.1.2", "1, 4, 4.1.1.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "1, 3", "1, 3", "3");
        assertAddToPossibleOptions(transformOptionGroup, "1, 3.1.1.2", "1, 3, 3.1.1.2", "3");

        assertAddToPossibleOptions(transformOptionGroup, "2",       "2", "");
        assertAddToPossibleOptions(transformOptionGroup, "2, 2.2",  "2, 2.2", "");
        assertAddToPossibleOptions(transformOptionGroup, "3",       "3",          "3");
        assertAddToPossibleOptions(transformOptionGroup, "3.1.1.2", "3, 3.1.1.2", "3");
    }

    @Test
    public void testRegistryIsSupportedMethod()
    {
        assertIsSupported("a", "a, B, c", "required option B is missing");
        assertIsSupported("",  "a, B, c", "required option B is missing");
        assertIsSupported("B", "a, B, c", null);
        assertIsSupported("B, c", "a, B, c", null);
        assertIsSupported("B, a, c", "a, B, c", null);

        assertIsSupported("B, d",    "a, B, c", "there is an extra option d");
        assertIsSupported("B, c, d", "a, B, c", "there is an extra option d");
        assertIsSupported("d", "a, B, c", "required option B is missing and there is an extra option d");

        assertIsSupported("a", "a, b, c", null);
        assertIsSupported("", "a, b, c", null);
        assertIsSupported("a, b, c", "a, b, c", null);
    }

    @Test
    public void testNoActualOptions() throws Exception
    {
        assertTransformOptions(new HashSet<>(Arrays.asList(
                new TransformOptionValue(false, "option1"),
                new TransformOptionValue(false, "option2"))));
    }

    @Test
    public void testNoTransformOptions() throws Exception
    {
        assertTransformOptions(Collections.emptySet());
        assertTransformOptions(null);
    }

    @Test
    public void testSupported() throws Exception
    {
        mapOfTransformOptions.put("options1",
                new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height"))));

        transformer = new Transformer("name", Collections.singleton("options1"),
                new HashSet<>(Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1))));

        assertSupported(DOC, 1024, GIF, actualOptions, null);
        assertSupported(DOC, 102400, GIF, actualOptions, null);
        assertSupported(DOC, 102401, GIF, actualOptions, "source is too large");
        assertSupported(DOC, 1024, JPEG, actualOptions, null);
        assertSupported(GIF, 1024, DOC, actualOptions, GIF+" is not a source of this transformer");
        assertSupported(MSG, 1024, GIF, actualOptions, null);
        assertSupported(MSG, 1024, JPEG, actualOptions, MSG+" to "+JPEG+" is not supported by this transformer");

        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width"), null);
        assertSupported(DOC, 1024, GIF, buildActualOptions("page, width, startPage"), "startPage is not an option");
    }

    @Test
    // renditionName used as the cache key, is an alias for a set of actualOptions and the target mimetype.
    // The source mimetype may change.
    public void testCache()
    {
        mapOfTransformOptions.put("options1",
                new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height"))));

        transformer = new Transformer("name", Collections.singleton("options1"),
                new HashSet<>(Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(MSG, GIF, -1))));

        registry.register(transformer, mapOfTransformOptions, getBaseUrl(transformer), getClass().getName());

        assertSupported(DOC, 1024, GIF, actualOptions, "doclib", "");
        assertSupported(MSG, 1024, GIF, actualOptions, "doclib", "");

        assertEquals(102400L, registry.getMaxSize(DOC, GIF, actualOptions, "doclib"));
        assertEquals(-1L, registry.getMaxSize(MSG, GIF, actualOptions, "doclib"));

        // Change the cached value and try and check we are now using the cached value.
        List<AbstractTransformRegistry.SupportedTransform> supportedTransforms = registry.getData().cachedSupportedTransformList.get("doclib").get(DOC);
        supportedTransforms.get(0).maxSourceSizeBytes = 1234L;
        assertEquals(1234L, registry.getMaxSize(DOC, GIF, actualOptions, "doclib"));
    }

    @Test
    public void testGetTransformerName() throws Exception
    {
        Transformer t1 = new Transformer("transformer1", null,
                Collections.singleton(new SupportedSourceAndTarget(MSG, GIF, 100, 50)));
        Transformer t2 = new Transformer("transformer2", null,
                Collections.singleton(new SupportedSourceAndTarget(MSG, GIF, 200, 60)));
        Transformer t3 = new Transformer("transformer3", null,
                Collections.singleton(new SupportedSourceAndTarget(MSG, GIF, 200, 40)));
        Transformer t4 = new Transformer("transformer4", null,
                Collections.singleton(new SupportedSourceAndTarget(MSG, GIF, -1, 100)));
        Transformer t5 = new Transformer("transformer5", null,
                Collections.singleton(new SupportedSourceAndTarget(MSG, GIF, -1, 80)));

        // Select on size - priority is ignored
        assertTransformerName(MSG, 100, GIF, actualOptions, "transformer1", t1, t2);
        assertTransformerName(MSG, 150, GIF, actualOptions, "transformer2", t1, t2);
        assertTransformerName(MSG, 250, GIF, actualOptions, null, t1, t2);
        // Select on priority - t1, t2 and t4 are discarded.
        //                      t3 is a higher priority and has a larger size than t1 and t2.
        //                      Similar story fo t4 with t5.
        assertTransformerName(MSG, 100, GIF, actualOptions, "transformer3", t1, t2, t3, t4, t5);
        assertTransformerName(MSG, 200, GIF, actualOptions, "transformer3", t1, t2, t3, t4, t5);
        // Select on size and priority, t1 and t2 discarded
        assertTransformerName(MSG, 200, GIF, actualOptions, "transformer3", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, actualOptions, "transformer4", t1, t2, t3, t4);
        assertTransformerName(MSG, 300, GIF, actualOptions, "transformer5", t1, t2, t3, t4, t5);
    }

    @Test
    public void testMultipleTransformers() throws Exception
    {
        mapOfTransformOptions.put("options1",
                new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "page"),
                        new TransformOptionValue(false, "width"),
                        new TransformOptionValue(false, "height"))));
        mapOfTransformOptions.put("options2",
                new HashSet<>(Arrays.asList(
                        new TransformOptionValue(false, "opt1"),
                        new TransformOptionValue(false, "opt2"))));
        mapOfTransformOptions.put("options3",
                new HashSet<>(Collections.singletonList(
                        new TransformOptionValue(false, "opt1"))));

        Transformer transformer1 = new Transformer("transformer1", Collections.singleton("options1"),
                new HashSet<>(Arrays.asList(
                        new SupportedSourceAndTarget(DOC, GIF, 102400),
                        new SupportedSourceAndTarget(DOC, JPEG, -1),
                        new SupportedSourceAndTarget(MSG, GIF, -1))));

        Transformer transformer2 = new Transformer("transformer2", Collections.singleton("options2"),
                new HashSet<>(Arrays.asList(
                        new SupportedSourceAndTarget(PDF, GIF, -1),
                        new SupportedSourceAndTarget(PPT, JPEG, -1))));

        Transformer transformer3 = new Transformer("transformer3", Collections.singleton("options3"),
                new HashSet<>(Collections.singletonList(
                        new SupportedSourceAndTarget(DOC, GIF, -1))));

        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(DOC, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        assertSupported(DOC, 102401, GIF, actualOptions, "source is too large", transformer1);
        assertSupported(DOC, 102401, GIF, actualOptions, null, transformer1, transformer3);

        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2 supports these mimetypes", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);

        actualOptions = buildActualOptions("opt1");
        assertSupported(PDF, 1024, GIF, actualOptions, "Only transformer2/4 supports these options", transformer1);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2);
        assertSupported(PDF, 1024, GIF, actualOptions, null, transformer1, transformer2, transformer3);
        assertSupported(PDF, 1024, GIF, actualOptions, "transformer4 supports opt1 but not the source mimetype ", transformer1, transformer3);
    }
}
