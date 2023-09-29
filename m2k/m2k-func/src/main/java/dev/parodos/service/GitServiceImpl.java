package dev.parodos.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.jgit.api.ArchiveCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.archive.ZipFormat;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

@ApplicationScoped
public class GitServiceImpl implements GitService {
  private static final Logger log = LoggerFactory.getLogger(GitServiceImpl.class);

  @Override
  public Git cloneRepo(String repo, String branch, String token, Path targetDirectory) throws GitAPIException {
    try {
      CloneCommand cloneCommand = Git.cloneRepository().setURI(repo).setDirectory(targetDirectory.toFile());
      if (token != null && !token.isBlank()) {
        log.info("Using token credentials to clone");
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("x-token-auth", token);
        cloneCommand.setCredentialsProvider(credentialsProvider);
      }

      log.info("Cloning repo {} in {}", repo, targetDirectory);
      return cloneCommand.call();
    } catch (InvalidRemoteException e) {
      log.error("remote repository server '{}' is not available", repo, e);
      throw e;
    } catch (GitAPIException e) {
      log.error("Cannot clone repository: {}", repo, e);
      throw e;
    }
  }

  @Override
  public Git generateRepositoryArchive(String repo, String branch, String token, Path archiveOutputPath) throws GitAPIException, IOException {
    ArchiveCommand.registerFormat("zip", new ZipFormat());
    Git clonedRepo = cloneRepo(repo, branch, token, archiveOutputPath.getParent());
    log.info("Creating zip {} of branch {} of repo {}", archiveOutputPath, branch, repo);
    try (OutputStream out = new FileOutputStream(archiveOutputPath.toFile())) {
      clonedRepo.archive()
          .setFormat("zip")
          .setTree(clonedRepo.getRepository().resolve(branch))
          .setOutputStream(out)
          .call().close();
    } catch (TransportException e) {
      log.error("Cannot connect to repository server '{}'", repo, e);
      throw e;
    } catch (GitAPIException e) {
      log.error("Cannot archive repository: {}", repo, e);
      throw e;
    } catch (FileNotFoundException e) {
      log.error("File: {} not found", archiveOutputPath, e);
      throw e;
    } catch (IOException e) {
      log.error("Error while writing to file: {}", archiveOutputPath, e);
      throw e;
    }

    return clonedRepo;
  }

  @Override
  public void createBranch(Git repo, String branch) throws GitAPIException {
    log.info("Creating branch {} in repo {}", branch, repo.toString());
    repo.branchCreate().setName(branch).call();
    repo.checkout().setName(branch).call();
  }

  @Override
  public boolean branchExists(Git repo, String branch) throws GitAPIException {
    return repo.branchList()
        .setListMode(ListBranchCommand.ListMode.ALL)
        .call()
        .stream()
        .map(ref -> ref.getName())
        .anyMatch(branchName -> branchName.contains(branch));
  }

  @Override
  public void commit(Git repo, String commitMessage, String filePattern) throws GitAPIException {
    log.info("Committing files matching the pattern '{}' with message '{}' to repo {}", filePattern, commitMessage, repo);
    repo.add().setUpdate(true).addFilepattern(filePattern).call();
    repo.add().addFilepattern(filePattern).call();
    CommitCommand commit = repo.commit().setMessage(commitMessage);
    commit.setSign(Boolean.FALSE);
    commit.call();
  }

  @Override
  public void push(Git repo, String token) throws GitAPIException {
    log.info("Pushing to repo {}", repo);
    PushCommand pushCommand = repo.push().setForce(false);
    if (token != null && !token.isBlank()) {
      log.info("Using token credentials to push");
      CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider("x-token-auth", token);
      pushCommand.setCredentialsProvider(credentialsProvider);
    }
    pushCommand.call();
  }
}
