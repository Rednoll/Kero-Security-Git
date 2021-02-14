package com.kero.security.core.agent.configurator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kero.security.ksdl.resource.GitResource;

public class GitRepositoryResourceTest {

	@Test
	public void test() throws URISyntaxException {
		
		Set<String> suffixes = new HashSet<>();
			suffixes.add(".k-s");
			suffixes.add(".ks");	
		
		GitResource resource = new GitResource(null, new URI("https://github.com/Rednoll/Kero-Security-TestGitRep.git"), "master", suffixes);
	
		resource.read();
	}
}
