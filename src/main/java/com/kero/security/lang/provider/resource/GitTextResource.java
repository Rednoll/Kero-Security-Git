package com.kero.security.lang.provider.resource;

import java.io.File;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;

public class GitTextResource extends GitResource<String> {

	public GitTextResource(Repository rep, String path, RevTree commitTree) {
		super(rep, path, commitTree);
	
	}

	@Override
	public String readData() {
		
		return new String(this.readRawData());
	}
}
