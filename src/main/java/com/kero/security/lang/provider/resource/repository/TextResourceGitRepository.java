package com.kero.security.lang.provider.resource.repository;

import java.net.URI;
import java.util.Set;

import org.eclipse.jgit.transport.CredentialsProvider;

import com.kero.security.lang.provider.resource.GitResource;
import com.kero.security.lang.provider.resource.GitTextResource;

public class TextResourceGitRepository extends ResourceGitRepository<String> {

	public TextResourceGitRepository(CredentialsProvider credentials, URI remote, String branch, Set<String> suffixes) {
		super(credentials, remote, branch, suffixes);
	
	}
	
	@Override
	protected GitTextResource getResource(String path) {
		
		return new GitTextResource(this.rep, path, this.commit.getTree());
	}
}
