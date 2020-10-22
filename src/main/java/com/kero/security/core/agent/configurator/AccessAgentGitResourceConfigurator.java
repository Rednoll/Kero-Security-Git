package com.kero.security.core.agent.configurator;

import java.net.URI;
import java.util.Set;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.kero.security.core.agent.KeroAccessAgent;
import com.kero.security.core.agent.configuration.KeroAccessAgentConfigurator;
import com.kero.security.lang.provider.KsdlProvider;
import com.kero.security.lang.provider.TextualProvider;
import com.kero.security.lang.provider.resource.GitRepositoryResource;
import com.kero.security.lang.provider.resource.KsdlTextResource;

public class AccessAgentGitResourceConfigurator implements KeroAccessAgentConfigurator {

	private CredentialsProvider credentialsProvider;
	private URI remote;
	private String branch;
	private Set<String> suffixes;

	private boolean resourceCacheEnabled;
	private boolean providerCacheEnabled;

	public AccessAgentGitResourceConfigurator(CredentialsProvider credentialsProvider, URI remote, String branch, boolean resourceCacheEnabled, boolean providerCacheEnabled, Set<String> suffixes) {

		this.credentialsProvider = credentialsProvider;
		this.remote = remote;
		this.branch = branch;
		
		this.suffixes = suffixes;
		
		this.resourceCacheEnabled = resourceCacheEnabled;
		this.providerCacheEnabled = providerCacheEnabled;
	}

	@Override
	public void configure(KeroAccessAgent agent) {
		
		KsdlTextResource resource = new GitRepositoryResource(this.credentialsProvider, this.remote, this.branch, this.suffixes);
	
		if(this.resourceCacheEnabled) {
			
			resource = KsdlTextResource.addCacheWrap(resource);
		}
		
		KsdlProvider provider = new TextualProvider(resource);
		
		if(this.providerCacheEnabled) {
			
			provider = KsdlProvider.addCacheWrap(provider);
		}
		
		agent.addKsdlProvider(provider);
	}
}