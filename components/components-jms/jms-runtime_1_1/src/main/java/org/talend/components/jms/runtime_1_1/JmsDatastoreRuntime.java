package org.talend.components.jms.runtime_1_1;

import net.bytebuddy.implementation.bytecode.Throw;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.talend.components.api.component.runtime.RuntimableRuntime;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.api.exception.ComponentException;
import org.talend.components.api.properties.ComponentProperties;
import org.talend.components.common.datastore.DatastoreProperties;
import org.talend.components.common.datastore.runtime.DatastoreRuntime;
import org.talend.components.jms.JmsDatastoreProperties;
import org.talend.components.jms.JmsMessageType;
import org.talend.daikon.NamedThing;
import org.talend.daikon.SimpleNamedThing;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.properties.Properties;
import org.talend.daikon.properties.ValidationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import static java.lang.Thread.sleep;

public class JmsDatastoreRuntime implements DatastoreRuntime {

    protected transient JmsDatastoreProperties properties;

    private JmsDatastoreProperties.JmsVersion version;

    private String contextProvider;

    private String serverUrl;

    private String connectionFactoryName;

    private String userName;

    private String userPassword;

    private JmsMessageType msgType;

    @Override public Iterable<ValidationResult> doHealthChecks(RuntimeContainer container) {
        try {
        // create connection factory
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(properties.serverUrl.getValue());
        // Create a Connection
        Connection connection = connectionFactory.createConnection();
        connection.start();
        connection.close();

        if (connection != null) {
            return Arrays.asList(ValidationResult.OK);
        }

        } catch (JMSException e) {
            throw new ComponentException(e);
        } return null;
    }

    @Override
    public ValidationResult initialize(RuntimeContainer container, Properties properties) {
        this.properties = (JmsDatastoreProperties) properties;
        return ValidationResult.OK;
    }
/*
    public ConnectionFactory getConnectionFactory() {
        Context context = null;
        Hashtable<String, String> env  = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY,"org.exolab.jms.jndi.InitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:3035");
        env.put(Context.SECURITY_PRINCIPAL, "admin");
        env.put(Context.SECURITY_CREDENTIALS, "openjms");

        ConnectionFactory connection = null;
        System.out.println("test : " + connectionFactoryName.getValue());
        try {
            context = new InitialContext(env);
            System.out.println("context");
            connection = (ConnectionFactory)context.lookup("ConnectionFactory");
            //TODO check if username required how it works
            /*
            if (datastore.needUserIdentity.getValue()) {
                connection = tcf.createConnection(datastore.userName.getValue(),datastore.userPassword.getValue());
            } else {
                connection = tcf.createTopicConnection();
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }

        return null;
    }
        */
}
