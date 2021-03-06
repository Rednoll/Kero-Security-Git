package com.kero.security.core.agent.configurator;

import java.net.URI;
import java.util.Set;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.kero.security.ksdl.agent.KsdlAgent;
import com.kero.security.ksdl.agent.configurator.KsdlAgentConfigurator;
import com.kero.security.ksdl.resource.GitResource;

public class KsdlAgentGitResourceConfigurator implements KsdlAgentConfigurator {
	
	private CredentialsProvider credentialsProvider;
	private URI remote;
	private String branch;
	private Set<String> suffixes;

	public KsdlAgentGitResourceConfigurator(CredentialsProvider credentialsProvider, URI remote, String branch, Set<String> suffixes) {

		this.credentialsProvider = credentialsProvider;
		this.remote = remote;
		this.branch = branch;
		
		this.suffixes = suffixes;
	}

	@Override
	public void configure(KsdlAgent agent) {
		
		GitResource resource = new GitResource(credentialsProvider, remote, branch, suffixes);
		
		agent.addResource(resource);
	}
}