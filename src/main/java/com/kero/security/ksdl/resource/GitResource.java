package com.kero.security.ksdl.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitResource implements KsdlResource {

	private static Logger LOGGER = LoggerFactory.getLogger("Kero-Security-Git");

	private CredentialsProvider credentials;
	private URI remote;
	private String branch;
	
	private Set<String> suffixes;
	
	protected Repository rep;
	protected RevCommit commit;
	
	public GitResource(CredentialsProvider credentials, URI remote, String branch, Set<String> suffixes) {
		
		this.credentials = credentials;
		this.remote = remote;
		this.branch = branch;
		
		this.suffixes = suffixes;
	}
	
	public String read() {
		
		if(rep == null) this.loadRepository();
		
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
			
			StringBuilder builder = new StringBuilder();
			
			for(String path : paths) {
				
				ObjectId id = TreeWalk.forPath(rep, path, this.commit.getTree()).getObjectId(0);
			
				builder.append(new String(rep.open(id).getBytes())+"\n");
			}
			
			walker.close();
			
			return builder.toString();
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
	}
	
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
}