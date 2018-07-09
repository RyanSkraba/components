// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.components.pubsub.runtime;

import static org.talend.components.pubsub.runtime.PubSubConnection.createCredentials;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.beam.sdk.util.RetryHttpRequestInitializer;
import org.apache.beam.sdk.util.Transport;
import org.talend.components.pubsub.PubSubDatastoreProperties;
import org.talend.daikon.exception.TalendRuntimeException;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.pubsub.Pubsub;
import com.google.api.services.pubsub.PubsubScopes;
import com.google.api.services.pubsub.model.AcknowledgeRequest;
import com.google.api.services.pubsub.model.ListTopicSubscriptionsResponse;
import com.google.api.services.pubsub.model.ListTopicsResponse;
import com.google.api.services.pubsub.model.PublishRequest;
import com.google.api.services.pubsub.model.PublishResponse;
import com.google.api.services.pubsub.model.PubsubMessage;
import com.google.api.services.pubsub.model.PullRequest;
import com.google.api.services.pubsub.model.PullResponse;
import com.google.api.services.pubsub.model.ReceivedMessage;
import com.google.api.services.pubsub.model.Subscription;
import com.google.api.services.pubsub.model.Topic;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.hadoop.util.ChainingHttpRequestInitializer;
import com.google.common.collect.ImmutableList;

public class PubSubClient {

    private static final String PROJECT_FORMAT = "projects/%s";

    private static final String TOPIC_FORMAT = PROJECT_FORMAT + "/topics/%s";

    private static final String SUBSCRIPTION_FORMAT = PROJECT_FORMAT + "/subscriptions/%s";

    private final String PROJECT_NAME;

    private Pubsub client;

    public PubSubClient(PubSubDatastoreProperties datastore, boolean runOnDataflow) {

        Credentials credentials = null;
        if (runOnDataflow || datastore.serviceAccountFile.getValue() == null) {
            try {
                credentials = GoogleCredentials.getApplicationDefault().createScoped(PubsubScopes.all());
            } catch (IOException e) {
                throw TalendRuntimeException.createUnexpectedException(e);
            }
        } else {
            credentials = createCredentials(datastore);
        }
        this.PROJECT_NAME = datastore.projectName.getValue();
        this.client = new Pubsub.Builder(Transport.getTransport(), Transport.getJsonFactory(),
                chainHttpRequestInitializer(credentials,
                        // Do not log 404. It clutters the output and is possibly even required by the caller.
                        new RetryHttpRequestInitializer(ImmutableList.of(404)))).build();
    }

    public static String getProjectPath(String project) {
        return String.format(PROJECT_FORMAT, project);
    }

    public static String getTopicPath(String project, String topic) {
        return String.format(TOPIC_FORMAT, project, topic);
    }

    public static String getSubscriptionPath(String project, String subscription) {
        return String.format(SUBSCRIPTION_FORMAT, project, subscription);
    }

    public String getProjectPath() {
        return getProjectPath(PROJECT_NAME);
    }

    public String getTopicPath(String topic) {
        return getTopicPath(PROJECT_NAME, topic);
    }

    public String getSubscriptionPath(String subscription) {
        return getSubscriptionPath(PROJECT_NAME, subscription);
    }

    private HttpRequestInitializer chainHttpRequestInitializer(Credentials credential,
            HttpRequestInitializer httpRequestInitializer) {
        if (credential == null) {
            return httpRequestInitializer;
        } else {
            return new ChainingHttpRequestInitializer(new HttpCredentialsAdapter(credential), httpRequestInitializer);
        }
    }

    public Set<String> listTopics() throws IOException {
        ListTopicsResponse listTopicsResponse = client.projects().topics().list(getProjectPath()).execute();
        List<Topic> topics = listTopicsResponse.getTopics();
        Set<String> topicsName = new HashSet<>();
        for (Topic topic : topics) {
            String topicName = topic.getName();
            String[] split = topicName.split("/");
            topicsName.add(split[3]);
        }
        return topicsName;
    }

    public Set<String> listSubscriptions(String topic) throws IOException {
        ListTopicSubscriptionsResponse listTopicSubscriptionsResponse = client.projects().topics().subscriptions()
                .list(getTopicPath(topic)).execute();
        List<String> subscriptions = listTopicSubscriptionsResponse.getSubscriptions();
        Set<String> subscriptionsName = new HashSet<>();
        for (String subscription : subscriptions) {
            String[] split = subscription.split("/");
            subscriptionsName.add(split[3]);
        }
        return subscriptionsName;
    }

    public List<ReceivedMessage> pull(String subscription, int maxNum) throws IOException {
        PullResponse response = client.projects().subscriptions()
                .pull(getSubscriptionPath(subscription), new PullRequest().setReturnImmediately(false).setMaxMessages(maxNum))
                .execute();
        return response.getReceivedMessages();
    }

    public void ack(String subscription, List<String> ackIds) throws IOException {
        client.projects().subscriptions()
                .acknowledge(getSubscriptionPath(subscription), new AcknowledgeRequest().setAckIds(ackIds)).execute();
    }

    public void createTopic(String topic) throws IOException {
        client.projects().topics().create(getTopicPath(topic), new Topic()).execute();
    }

    public void createSubscription(String topic, String subscription) throws IOException {
        client.projects().subscriptions()
                .create(getSubscriptionPath(subscription), new Subscription().setTopic(getTopicPath(topic))).execute();
    }

    public void deleteSubscription(String subscription) throws IOException {
        client.projects().subscriptions().delete(getSubscriptionPath(subscription)).execute();
    }

    public void deleteTopic(String topic) throws IOException {
        client.projects().topics().delete(getTopicPath(topic)).execute();
    }

    public void publish(String topic, List<PubsubMessage> messages) throws IOException {
        PublishRequest request = new PublishRequest().setMessages(messages);
        PublishResponse response = client.projects().topics().publish(getTopicPath(topic), request).execute();
    }

}
