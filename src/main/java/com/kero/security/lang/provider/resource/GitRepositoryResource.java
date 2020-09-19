package com.kero.security.lang.provider.resource;

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
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitRepositoryResource implements KsdlTextResource {

	private static Logger LOGGER = LoggerFactory.getLogger("Kero-Security-Git");

	private CredentialsProvider credentials;
	private URI remote;
	private String branch;
	
	private Set<String> suffixes;
	
	public GitRepositoryResource(CredentialsProvider credentials, URI remote, String branch, Set<String> suffixes) {
		
		this.credentials = credentials;
		this.remote = remote;
		this.branch = branch;
		
		this.suffixes = suffixes;
	}
	
	@Override
	public String getRawText() {
		
		StringBuilder builder = new StringBuilder();
	
		LOGGER.debug("Creating in memory Git repository.");
		
		DfsRepositoryDescription repDesc = new DfsRepositoryDescription();
		Repository rep = new InMemoryRepository(repDesc);
	
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
			RevCommit commit = revWalk.parseCommit(lastCommitId);
			RevTree commitTree = commit.getTree();
			
			TreeFilter walkerFilter = this.assemblyFilter();
			
			TreeWalk walker = new TreeWalk(rep);
				walker.addTree(commitTree);
				walker.setRecursive(true);
				walker.setFilter(walkerFilter);

			List<String> paths = new ArrayList<>();
				
			LOGGER.debug("Begin scan repository.");
			while(walker.next()) {
				
				String path = walker.getPathString();
				
				LOGGER.debug("Found scheme file: "+path);
				paths.add(path);
			}
			
			for(String path : paths) {
				
				ObjectId id = TreeWalk.forPath(rep, path, commitTree).getObjectId(0);
				
				LOGGER.debug("Load scheme file: "+path);
				builder.append(new String(rep.open(id).getBytes())+"\n");
			}
			
			revWalk.close();
			walker.close();
			git.close();
		}
		catch(Exception e) {
			
			throw new RuntimeException(e);
		}
		
		return builder.toString();
	}
	
	private TreeFilter assemblyFilter() {
		
		List<TreeFilter> filters = new ArrayList<>();
		
		for(String suffix : this.suffixes) {
			
			filters.add(PathSuffixFilter.create(suffix));
		}
		
		return OrTreeFilter.create(filters);
	}
}
