/*
 *
 *  Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.hazelcast.samples.amazon.elasticbeanstalk;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.util.EC2MetadataUtils;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.context.SpringManagedContext;
import com.hazelcast.util.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

/**
 * @author László Csontos
 */
public class HazelcastInstanceFactory extends AbstractFactoryBean<HazelcastInstance> {

    public static final String ELASTICBEANSTALK_ENVIRONMENT_NAME = "elasticbeanstalk:environment-name";
    public static final String ELASTICBEANSTALK_EC2_ROLE_NAME = "aws-elasticbeanstalk-ec2-role";
    public static final String HAZELCAST_ENVIRONMENT_NAME = "hazelcast.environment.name";
    public static final String HAZELCAST_ENVIRONMENT_PASSWORD = "hazelcast.environment.password";
    public static final String HAZELCAST_AWS_IAM_ROLE = "hazelcast.aws.iam-role";
    public static final String HAZELCAST_AWS_REGION = "hazelcast.aws.region";
    public static final String HAZELCAST_LOGGING_TYPE = "hazelcast.logging.type";

    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInstanceFactory.class);

    private AmazonEC2 amazonEC2;
    private boolean localOnly;

    @Value("classpath:/hazelcast-aws.xml")
    private Resource hazelcastAWSConfig;

    @Value("classpath:/hazelcast-local.xml")
    private Resource hazelcastLocalConfig;

    @Autowired
    private SpringManagedContext springManagedContext;

    public HazelcastInstanceFactory() {
        try {
            amazonEC2 = new AmazonEC2Client(new InstanceProfileCredentialsProvider());
        } catch (AmazonClientException ace) {
            LOGGER.error("Couldn't authenticate with AWS; local cluster configuration will be used.", ace);
        }
    }

    @Override
    public Class<HazelcastInstance> getObjectType() {
        return HazelcastInstance.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    protected HazelcastInstance createInstance() throws Exception {
        Config config = getConfig();
        config.setProperty(HAZELCAST_LOGGING_TYPE, "slf4j");

        // Enabling @SpringAware by default was causing performance hit so it had been disabled as of 3.5-EA.
        // Although <hz:spring-aware /> makes it possible to enable @SpringAware, it's not an option for programmatic
        // configuration.
        // See https://github.com/hazelcast/hazelcast/issues/5323#issuecomment-103033381
        // See https://github.com/hazelcast/hazelcast/issues/6514#issuecomment-180394893
        config.setManagedContext(springManagedContext);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Override
    protected void destroyInstance(HazelcastInstance hazelcastInstance) throws Exception {
        hazelcastInstance.shutdown();
        shutdownAmazonEC2();
    }

    protected Config getConfig() throws IOException {
        Properties awsProperties = null;
        if (amazonEC2 != null) {
            try {
                awsProperties = getAwsProperties();
            } catch (RuntimeException re) {
                shutdownAmazonEC2();
                LOGGER.error("Auto-detecting cluster membership has failed; falling back to local configuration.", re);
            }
        }

        Resource hazelcastConfig = (amazonEC2 != null) ? hazelcastAWSConfig : hazelcastLocalConfig;
        LOGGER.info("Using {} for cluster configuration.", hazelcastConfig.getFilename());

        XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder(hazelcastConfig.getInputStream());
        if (amazonEC2 != null) {
            xmlConfigBuilder.setProperties(awsProperties);
        }
        return xmlConfigBuilder.build();
    }

    protected Properties getAwsProperties() {
        EC2MetadataUtils.InstanceInfo instanceInfo = EC2MetadataUtils.getInstanceInfo();
        String instanceId = instanceInfo.getInstanceId();

        // EB sets the environment ID and name as the elasticbeanstalk:environment-id and
        // elasticbeanstalk:environment-name EC2 tags on all of the parts of an EB app environment: load balancer,
        // EC2 instances, security groups, etc. Surprisingly, EC2 tags aren't available to instances through the
        // instance metadata interface, but they are available through the normal AWS API’s DescribeTags call.
        Collection<Filter> filters = new ArrayList<Filter>();
        filters.add(new Filter("resource-type").withValues("instance"));
        filters.add(new Filter("resource-id").withValues(instanceId));
        filters.add(new Filter("key").withValues(ELASTICBEANSTALK_ENVIRONMENT_NAME));

        DescribeTagsRequest describeTagsRequest = new DescribeTagsRequest();
        describeTagsRequest.setFilters(filters);
        DescribeTagsResult describeTagsResult = amazonEC2.describeTags(describeTagsRequest);

        if (describeTagsResult == null || describeTagsResult.getTags().isEmpty()) {
            throw new IllegalStateException("No tag " + ELASTICBEANSTALK_ENVIRONMENT_NAME + " found for instance "
                    + instanceId + ".");
        }

        String environmentName = describeTagsResult.getTags().get(0).getValue();
        String environmentPassword = MD5Util.toMD5String(environmentName);

        Properties properties = new Properties();
        properties.setProperty(HAZELCAST_ENVIRONMENT_NAME, environmentName);
        properties.setProperty(HAZELCAST_ENVIRONMENT_PASSWORD, environmentPassword);
        properties.setProperty(HAZELCAST_AWS_IAM_ROLE, ELASTICBEANSTALK_EC2_ROLE_NAME);
        properties.setProperty(HAZELCAST_AWS_REGION, instanceInfo.getRegion());

        return properties;
    }

    protected void shutdownAmazonEC2() {
        if (amazonEC2 == null) {
            return;
        }
        amazonEC2.shutdown();
        amazonEC2 = null;
    }
}
