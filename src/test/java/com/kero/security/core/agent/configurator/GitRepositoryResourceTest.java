package com.kero.security.core.agent.configurator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kero.security.ksdl.resource.additionals.ResourceAddress;
import com.kero.security.lang.provider.resource.GitResource;
import com.kero.security.lang.provider.resource.repository.TextResourceGitRepository;

public class GitRepositoryResourceTest {

	@Test
	public void test() throws URISyntaxException {
		
		Set<String> suffixes = new HashSet<>();
			suffixes.add(".k-s");
			suffixes.add(".ks");	
		
		TextResourceGitRepository repository = new TextResourceGitRepository(null, new URI("https://github.com/Rednoll/Kero-Security-TestGitRep.git"), "master", suffixes);
	
		GitResource<String> res1 = repository.getResource(new ResourceAddress("test scheme"));
		GitResource<String> res2 = repository.getResource(new ResourceAddress("empty.file2"));
		GitResource<String> res3 = repository.getResource(new ResourceAddress("empty.file3"));
		
		assertEquals(res1.readData(), "lol");
		assertEquals(res2.readData(), "kek");
		assertEquals(res3.readData(), "pek");
	}
}
