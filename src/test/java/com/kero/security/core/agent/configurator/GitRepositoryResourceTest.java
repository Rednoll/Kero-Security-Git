package com.kero.security.core.agent.configurator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kero.security.lang.provider.resource.GitRepositoryResource;

public class GitRepositoryResourceTest {

	@Test
	public void test() throws URISyntaxException {
		
		Set<String> suffixes = new HashSet<>();
			suffixes.add(".k-s");
			suffixes.add(".ks");	
		
		GitRepositoryResource resource = new GitRepositoryResource(null, new URI("https://github.com/Rednoll/Kero-Security-TestGitRep.git"), "master", suffixes);
	
		String rawText = resource.getRawText();
	
		assertEquals("kek\nlol", rawText);
	}
}
