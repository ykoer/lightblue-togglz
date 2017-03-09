package com.redhat.lightblue.togglz;

import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.LightblueException;
import com.redhat.lightblue.client.Projection;
import com.redhat.lightblue.client.request.data.DataFindRequest;
import com.redhat.lightblue.client.request.data.DataSaveRequest;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.ArrayList;
import java.util.List;

import static com.redhat.lightblue.client.Query.BinOp.eq;
import static com.redhat.lightblue.client.Query.withValue;

/**
 * Created by ykoer on 3/6/17.
 */
public class LightblueStateRepository implements StateRepository {

    private final LightblueClient lbClient;
    private final String application;
    private final String entityName;
    private final String version;


    public LightblueStateRepository(Builder builder) {
        this.lbClient = builder.lbClient;
        this.application= builder.application;
        this.entityName = builder.entityName;
        this.version = builder.version;
    }

    public FeatureState getFeatureState(Feature feature) {

        DataFindRequest request = new DataFindRequest(entityName, version);
        request.where(withValue("application", eq, application));
        request.select(
                Projection.array("features",
                        withValue("feature", eq, feature.name()),
                        true,
                        Projection.includeFieldRecursively("*"),
                        null
                )
        );

        try {
            LBTogglz[] result = lbClient.data(request, LBTogglz[].class);

            if (result != null && result.length==1&&result[0].getFeatures()!=null && result[0].getFeatures().size()==1) {
                LBTogglz.Toggle toggle = result[0].getFeatures().iterator().next();
                FeatureState state = new FeatureState(feature);

                state.setEnabled(toggle.isEnabled());

                String strategyValue = toggle.getStrategy();
                if (strategyValue!=null) {
                    state.setStrategyId(strategyValue.trim());
                }

                List<LBTogglz.Toggle.Parameter> parameters = toggle.getParameters();
                if (parameters!=null) {
                    for (LBTogglz.Toggle.Parameter parameter : parameters) {
                        state.setParameter(parameter.getKey(), parameter.getValue().trim());
                    }
                }

                return state;
            }
        } catch (LightblueException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void setFeatureState(FeatureState featureState) {

        try {
            LBTogglz togglz  = getTogglz(application, featureState.getFeature().name());

            if (togglz==null) {
                togglz = new LBTogglz(application);
            }

            List<LBTogglz.Toggle> togglezList = togglz.getFeatures();
            if (togglz.getFeatures()==null) {
                togglezList = new ArrayList<LBTogglz.Toggle>();
                togglezList.add(new LBTogglz.Toggle());
                togglz.setFeatures(togglezList);
            }

            LBTogglz.Toggle toggle = togglezList.get(0);
            toggle.setFeature(featureState.getFeature().name());
            toggle.setEnabled(featureState.isEnabled());

            if (featureState.getStrategyId() != null) {
                toggle.setStrategy(featureState.getStrategyId());
            }

            if (featureState.getParameterNames().size() > 0) {
                List<LBTogglz.Toggle.Parameter> parameters = new ArrayList<LBTogglz.Toggle.Parameter>();

                for (String key : featureState.getParameterNames()) {
                    parameters.add(new LBTogglz.Toggle.Parameter(key, featureState.getParameter(key)));
                }
                toggle.setParameters(parameters);
            }

            DataSaveRequest save = new DataSaveRequest(entityName, version);
            save.create(togglz);
            save.setUpsert(true);
            save.returns(Projection.excludeFieldRecursively("*"));

            lbClient.data(save);

        } catch (LightblueException e) {
            e.printStackTrace();
        }
    }

    private LBTogglz getTogglz(String application, String feature) throws LightblueException {
        DataFindRequest request = new DataFindRequest(entityName, version);
        request.where(withValue("application", eq, application));
        request.select(
                Projection.includeField("*"),
                Projection.array("features",
                        withValue("feature", eq, feature),
                        true,
                        Projection.includeFieldRecursively("*"),
                        null
                )
        );
        return lbClient.data(request, LBTogglz.class);
    }

    /**
     * Creates a new builder for creating a {@link LightblueStateRepository}.
     *
     * @param lbClient the client instance to use for connecting to Lightblue
     * @param application the application used for storing features states.
     */
    public static Builder newBuilder(LightblueClient lbClient, String application) {
        return new Builder(lbClient, application);
    }


    public static class Builder {

        private final LightblueClient lbClient;

        private final String application;
        private String entityName = "applicationTogglz";
        private String version = null; // use default version

        /**
         * Creates a new builder for a {@link LightblueStateRepository}.
         *
         * @param lbClient the client instance to use for connecting to MongoDB
         * @param application the application used for storing features states.
         */
        public Builder(LightblueClient lbClient, String application) {
            this.lbClient = lbClient;
            this.application = application;
        }

        /**
         * Creates a new builder for a {@link LightblueStateRepository}.
         *  @param lbClient the client instance to use for connecting to MongoDB
         * @param application the application used for storing features states.
         * @param application
         * @param entityName
         * @param version
         */
        public Builder(LightblueClient lbClient, String application, String entityName, String version) {
            this.lbClient = lbClient;
            this.application = application;
            this.entityName = entityName;
            this.version = version;
        }

        /**
         * Creates a new {@link LightblueStateRepository} using the current settings.
         */
        public LightblueStateRepository build() {
            return new LightblueStateRepository(this);
        }
    }

}