package dev.parodos.service;

import dev.parodos.move2kube.ApiException;

import java.io.IOException;
import java.nio.file.Path;

public interface Move2KubeService {
  public void createPlan(String workspaceId, String projectId, Path zipFile) throws ApiException;

  public Path getTransformationOutput(String workspaceId, String projectId, String transformationId) throws IllegalArgumentException, IOException, ApiException;
}
