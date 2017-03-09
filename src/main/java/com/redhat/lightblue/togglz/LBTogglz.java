package com.redhat.lightblue.togglz;

import java.util.List;

/**
 * Created by ykoer on 3/6/17.
 */
public class LBTogglz {

    public static final String ENTITY_NAME = "applicationTogglz";

    private String _id;
    private String application;
    private List<Toggle> features;

    public LBTogglz() {
    }

    public LBTogglz(String application) {
        this.application = application;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public List<Toggle> getFeatures() {
        return features;
    }

    public void setFeatures(List<Toggle> features) {
        this.features = features;
    }

    public static class Toggle {
        private String feature;
        private boolean enabled;
        private String strategy;
        private List<Parameter> parameters;

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }

        public List<Parameter> getParameters() {
            return parameters;
        }

        public void setParameters(List<Parameter> parameters) {
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Toggle toggle = (Toggle) o;

            if (enabled != toggle.enabled) return false;
            if (!feature.equals(toggle.feature)) return false;
            if (!strategy.equals(toggle.strategy)) return false;
            return parameters.equals(toggle.parameters);
        }

        @Override
        public int hashCode() {
            int result = feature.hashCode();
            result = 31 * result + (enabled ? 1 : 0);
            result = 31 * result + strategy.hashCode();
            result = 31 * result + parameters.hashCode();
            return result;
        }

        public static class Parameter {
            private String key;
            private String value;

            public Parameter() {
            }

            public Parameter(String key, String value) {
                this.key = key;
                this.value = value;
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Parameter parameter = (Parameter) o;

                if (key != null ? !key.equals(parameter.key) : parameter.key != null) return false;
                return value != null ? value.equals(parameter.value) : parameter.value == null;
            }

            @Override
            public int hashCode() {
                int result = key != null ? key.hashCode() : 0;
                result = 31 * result + (value != null ? value.hashCode() : 0);
                return result;
            }
        }
    }
}
