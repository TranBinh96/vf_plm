package com.vinfast.api.common.extensions;

import java.util.List;

import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.model.File;
import org.jfrog.artifactory.client.model.RepoPath;

public class ArtifactoryExtension {
    public static Artifactory createArtifactory(String username, String password, String artifactoryUrl) {
        if (username.isEmpty() || password.isEmpty() || artifactoryUrl.isEmpty()) {
            throw new IllegalArgumentException("Arguments passed to createArtifactory are not valid");
        }

        return ArtifactoryClientBuilder.create().setUrl(artifactoryUrl).setUsername(username).setPassword(password).build();
    }

    public static String getRawFilePath(String ecuId, String partType, String fileName) {
        return String.format("%s/%s/%s", ecuId, partType, fileName);
    }

    public static File getFileInfo(Artifactory artifactory, String repoName, String fileToSearch) {
        if (artifactory == null || repoName.isEmpty() || fileToSearch.isEmpty()) {
            throw new IllegalArgumentException("Arguments passed to serachFile are not valid");
        }

        return artifactory.repository(repoName).file(fileToSearch).info();
    }

    public static List<RepoPath> searchFile(Artifactory artifactory, String repoName, String fileToSearch) {
        if (artifactory == null || repoName.isEmpty() || fileToSearch.isEmpty()) {
            throw new IllegalArgumentException("Arguments passed to serachFile are not valid");
        }

        return artifactory.searches().repositories(repoName).artifactsByName(fileToSearch).doSearch();
    }
}
