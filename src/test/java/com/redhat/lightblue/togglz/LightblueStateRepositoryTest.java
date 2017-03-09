package com.redhat.lightblue.togglz;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.integration.test.LightblueExternalResource;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import java.util.HashSet;
import java.util.Random;
import java.util.UUID;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by ykoer on 3/6/17.
 */
public class LightblueStateRepositoryTest {

    private final Random random = new Random(System.currentTimeMillis());

    protected LightblueClient lbClient;


    @ClassRule
    public static LightblueExternalResource lightblue = new LightblueExternalResource(new LightblueExternalResource.LightblueTestMethods() {

        public JsonNode[] getMetadataJsonNodes() throws Exception {
            return new JsonNode[] {
                    loadJsonNode("metadata/applicationTogglz.json")
            };
        }
    });

    @Before
    public void before() throws Exception {
        lbClient = lightblue.getLightblueClient();
        lightblue.cleanupMongoCollections(LBTogglz.ENTITY_NAME);
    }

    @Test
    public void testInsertAndUpdate() {

        final LightblueStateRepository lbStateRepository = LightblueStateRepository.newBuilder(lbClient, "unit-test").build();

        final FeatureState expectedNullFeatureState = lbStateRepository.getFeatureState(TestFeature.FEATURE_1);
        assertThat(expectedNullFeatureState).isNull();

        final boolean expectedEnabled = random.nextBoolean();
        final String expectedStrategyId = UUID.randomUUID().toString();
        final String expectedKey1 = "expectedKey1_" + UUID.randomUUID().toString();
        final String expectedValue1 = "expectedValue1_" + UUID.randomUUID().toString();
        final FeatureState initialFeatureState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(expectedEnabled)
                .setStrategyId(expectedStrategyId)
                .setParameter(expectedKey1, expectedValue1);

        lbStateRepository.setFeatureState(initialFeatureState);

        final FeatureState actualFeatureState = lbStateRepository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualFeatureState.getFeature()).isEqualTo(initialFeatureState.getFeature());
        assertThat(actualFeatureState.getStrategyId()).isEqualTo(expectedStrategyId);
        assertThat(actualFeatureState.isEnabled()).isEqualTo(expectedEnabled);
        assertThat(actualFeatureState.getParameter(expectedKey1)).isEqualTo(expectedValue1);
        assertThat(actualFeatureState.getParameterNames()).isEqualTo(new HashSet<String>() {{add(expectedKey1);}});

        final boolean updatedEnabled = !expectedEnabled;
        final String updatedStrategyId = UUID.randomUUID().toString();
        final String expectedKey2 = "expectedKey2_" + UUID.randomUUID().toString();
        final String expectedValue2 = "expectedValue2_" + UUID.randomUUID().toString();
        final FeatureState updatedFeatureState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(updatedEnabled)
                .setStrategyId(updatedStrategyId)
                .setParameter(expectedKey2, expectedValue2);

        lbStateRepository.setFeatureState(updatedFeatureState);

        final FeatureState actualUpdatedFeatureState = lbStateRepository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualUpdatedFeatureState.getFeature()).isEqualTo(updatedFeatureState.getFeature());
        assertThat(actualUpdatedFeatureState.getStrategyId()).isEqualTo(updatedStrategyId);
        assertThat(actualUpdatedFeatureState.isEnabled()).isEqualTo(updatedEnabled);
        assertThat(actualUpdatedFeatureState.getParameter(expectedKey2)).isEqualTo(expectedValue2);
        assertThat(actualUpdatedFeatureState.getParameterNames()).isEqualTo(new HashSet<String>() {{add(expectedKey2);}});

    }

    private enum TestFeature implements Feature {
        FEATURE_1
    }
}
