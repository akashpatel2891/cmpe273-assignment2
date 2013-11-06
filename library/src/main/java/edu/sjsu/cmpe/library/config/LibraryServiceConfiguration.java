package edu.sjsu.cmpe.library.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class LibraryServiceConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String stompQueueName;

    @NotEmpty
    @JsonProperty
    private String stompTopicName;
    
    private String apolloUser;
    private String apolloPassword;
    private int apolloPort;
    private String apolloHost;


    /**
     * @return the stompQueueName
     */
    public String getStompQueueName() {
	return stompQueueName;
    }

    /**
     * @param stompQueueName
     *            the stompQueueName to set
     */
    public void setStompQueueName(String stompQueueName) {
	this.stompQueueName = stompQueueName;
    }

    /**
     * @return the stompTopicName
     */
    public String getStompTopicName() {
	return stompTopicName;
    }

    /**
     * @param stompTopicName
     *            the stompTopicName to set
     */
    public void setStompTopicName(String stompTopicName) {
	this.stompTopicName = stompTopicName;
    }
    
    /**
     * 
     * @return apolloUser
     */
    public String getApolloUser() {
		return apolloUser;
	}

    /**
     * 
     * @param apolloUser
     * 			apolloUser to set
     */
	public void setApolloUser(String apolloUser) {
		this.apolloUser = apolloUser;
	}

	/**
	 * 
	 * @return apolloPassword
	 */
	public String getApolloPassword() {
		return apolloPassword;
	}

	/**
	 * 
	 * @param apolloPassword
	 * 			apolloPassword to set
	 */
	public void setApolloPassword(String apolloPassword) {
		this.apolloPassword = apolloPassword;
	}

	/**
	 * 
	 * @return apolloPort
	 */
	public int getApolloPort() {
		return apolloPort;
	}

	/**
	 * 
	 * @param apolloPort
	 * 			apolloPort to set
	 */
	public void setApolloPort(int apolloPort) {
		this.apolloPort = apolloPort;
	}


	/**
	 * 
	 * @return apolloHost
	 */
	public String getApolloHost() {
		return apolloHost;
	}

	/**
	 * 
	 * @param apolloHost
	 * 			apolloHost to set
	 */
	public void setApolloHost(String apolloHost) {
		this.apolloHost = apolloHost;
	}

	
}
