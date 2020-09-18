package com.kero.security.core.agent.configurator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.kero.security.core.agent.KeroAccessAgent;
import com.kero.security.core.agent.KeroAccessAgentFactory;
import com.kero.security.core.agent.KeroAccessAgentFactoryImpl;
import com.kero.security.core.exception.AccessException;

public class AccessAgentGitResourceConfiguratorTest {

	@Test
	public void test() throws URISyntaxException {
		
		Set<String> suffixes = new HashSet<>();
			suffixes.add(".k-s");
			suffixes.add(".ks");	
		
		AccessAgentGitResourceConfigurator conf = new AccessAgentGitResourceConfigurator(null, new URI("https://github.com/Rednoll/Kero-Security-TestGitRep.git"), "master", true, true, suffixes);
		
		KeroAccessAgentFactory factory = new KeroAccessAgentFactoryImpl();
			factory.addConfigurator(conf);
			
		KeroAccessAgent agent = factory.create();
		
		TestObject ownerProtected = agent.protect(new TestObject(), "OWNER");
	
		assertDoesNotThrow(ownerProtected::getText);
		assertDoesNotThrow(ownerProtected::getObj2);
	
		TestObject msProtected = agent.protect(new TestObject(), "MS");
		
		assertThrows(AccessException.class, msProtected::getObj2);
		
		TestObject2 obj2 = agent.protect(new TestObject2(), "OWNER");
		
		assertThrows(AccessException.class, obj2::getKek);
	}
}