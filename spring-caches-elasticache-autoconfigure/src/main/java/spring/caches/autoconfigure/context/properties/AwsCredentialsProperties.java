/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spring.caches.autoconfigure.context.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import spring.caches.autoconfigure.context.ContextCredentialsAutoConfiguration;

/**
 * Properties related to AWS credentials.
 *
 * @author Tom Gianos
 * @see ContextCredentialsAutoConfiguration
 * @since 2.0.2
 */
@ConfigurationProperties(prefix = AwsCredentialsProperties.PREFIX)
public class AwsCredentialsProperties {

    /**
     * TODO Evaluate the possibility to change this prefix.
     * <p>
     * The prefix used for AWS credentials related properties.
     */
    public static final String PREFIX = "spring.caches.elasticache.aws.credentials";

    /**
     * The access key to be used with a static provider.
     */
    private String accessKey;

    /**
     * The secret key to be used with a static provider.
     */
    private String secretKey;

    /**
     * Configures an instance profile credentials provider with no further configuration.
     */
    private boolean instanceProfile;

    /**
     * The AWS profile name.
     */
    private String profileName;

    /**
     * The AWS profile path.
     */
    private String profilePath;

    public String getAccessKey() {
        return this.accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isInstanceProfile() {
        return this.instanceProfile;
    }

    public void setInstanceProfile(boolean instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfilePath() {
        return this.profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

}
