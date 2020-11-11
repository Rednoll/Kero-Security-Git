package com.kero.security.lang.provider.resource;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kero.security.ksdl.resource.KsdlResource;
import com.kero.security.ksdl.resource.additionals.ResourceAddress;

public abstract class GitResource<T> implements KsdlResource<T> {

	private static Logger LOGGER = LoggerFactory.getLogger("Kero-Security-Git");

	protected Repository rep;
	protected String path;
	protected RevTree commitTree;
	
	public GitResource(Repository rep, String path, RevTree commitTree) {
	
		this.rep = rep;
		this.path = path;
		this.commitTree = commitTree;
	}
	
	protected byte[] readRawData() {
		
		try {
			
			LOGGER.debug("Load resource: "+path);
			
			ObjectId id = TreeWalk.forPath(rep, path, commitTree).getObjectId(0);
		
			return rep.open(id).getBytes();
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}

	@Override
	public ResourceAddress getAddress() {
	
		String address = this.path;
		
		address = address.replaceFirst("\\..+$", "");
		address = address.replaceAll("\\/", ResourceAddress.SEPARATOR);
		
		if(address.startsWith(ResourceAddress.SEPARATOR)) {
			
			address = address.substring(1);
		}
		
		return new ResourceAddress(address);
	}
}
