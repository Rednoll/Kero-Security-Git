package com.kero.security.lang.provider.resource.repository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kero.security.ksdl.resource.additionals.ResourceAddress;
import com.kero.security.ksdl.resource.repository.KsdlResourceRepository;
import com.kero.security.lang.provider.resource.GitResource;

public abstract class ResourceGitRepository<T> implements KsdlResourceRepository<GitResource<T>> {

	private static Logger LOGGER = LoggerFactory.getLogger("Kero-Security-Git");

	private CredentialsProvider credentials;
	private URI remote;
	private String branch;
	
	private Set<String> suffixes;
	
	protected Repository rep;
	protected RevCommit commit;
	
	public ResourceGitRepository(CredentialsProvider credentials, URI remote, String branch, Set<String> suffixes) {
		
		this.credentials = credentials;
		this.remote = remote;
		this.branch = branch;
		
		this.suffixes = suffixes;
	}
	
	public String adaptAddress(ResourceAddress address) {
		
		return address.getRaw().replaceAll("\\"+address.SEPARATOR, "/");
	}
	
	@Override
	public Collection<GitResource<T>> getAll() {

		if(rep == null) loadRepository();
		
		try {

			TreeFilter walkerFilter = this.assemblyFilter();
			
			TreeWalk walker = new TreeWalk(rep);
				walker.addTree(this.commit.getTree());
				walker.setRecursive(true);
				walker.setFilter(walkerFilter);

			List<String> paths = new ArrayList<>();
				
			LOGGER.debug("Begin scan repository.");
			while(walker.next()) {
				
				String path = walker.getPathString();
				
				LOGGER.debug("Found scheme file: "+path);
				paths.add(path);
			}
			
			Collection<GitResource<T>> result = new HashSet<>();
						
				for(String path : paths) {
					
					result.add(getResource(path));
				}
			
			walker.close();
			
			return result;
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public GitResource<T> getResource(ResourceAddress address) {
	
		if(rep == null) loadRepository();
		
		try {
			
			String fullPath = adaptAddress(address);
			
			String pathParent = fullPath.replaceAll("[^\\/]+$", "");
			String name = fullPath.replaceAll(".+\\/", "");
			
			TreeFilter walkerFilter = this.assemblyFilter();
			
				if(!pathParent.isEmpty()) {
					
					walkerFilter = AndTreeFilter.create(walkerFilter, PathFilter.create(pathParent));
				}
			
			TreeWalk walker = new TreeWalk(rep);
				walker.addTree(this.commit.getTree());
				walker.setRecursive(true);
				walker.setFilter(walkerFilter);
			
			try {
				
				while(walker.next()) {
					
					String suspectPath = walker.getPathString();
					String suspectName = suspectPath.replaceAll(".+\\/|\\..*$", "");
					String suspectPathParent = suspectPath.replaceAll("[^\\/]+$", "");
					
					if(suspectPathParent.equals(pathParent) && suspectName.equals(name)) {
					
						walker.close();
						
						return getResource(suspectPath);
					}
				}
				
				throw new RuntimeException("Can't found file at: "+fullPath);
			}
			catch(MissingObjectException e) {
				
				throw new RuntimeException("Can't found file at: "+fullPath);
			}
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
	protected abstract GitResource<T> getResource(String path);
	
	private void loadRepository() {
		
		LOGGER.debug("Creating in memory Git repository.");
	
		this.rep = new InMemoryRepository(new DfsRepositoryDescription());
	
		try {

			Git git = new Git(rep);
			FetchCommand command = git.fetch();
			
			command
				.setRemote(remote.toString())
				.setRefSpecs("+refs/heads/*:refs/heads/*");
				
				if(this.credentials != null) {
					
					command.setCredentialsProvider(this.credentials);
				}

			LOGGER.info("Begin fetch "+remote.toString()+" branch: "+this.branch);
			command.call();
			LOGGER.info("Fetch complete.");
		
			ObjectId lastCommitId = rep.resolve("refs/heads/"+branch);
			RevWalk revWalk = new RevWalk(rep);
			
			this.commit = revWalk.parseCommit(lastCommitId);
			
			revWalk.close();
			git.close();
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
	private TreeFilter assemblyFilter() {
		
		List<TreeFilter> filters = new ArrayList<>();
		
		for(String suffix : this.suffixes) {
			
			filters.add(PathSuffixFilter.create(suffix));
		}
		
		return OrTreeFilter.create(filters);
	}

	@Override
	public String getName() {
		
		return remote.getPath()+"#"+branch;
	}
}
