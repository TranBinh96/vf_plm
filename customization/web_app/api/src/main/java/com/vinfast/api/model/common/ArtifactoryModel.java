package com.vinfast.api.model.common;

import com.vinfast.api.common.extensions.ArtifactoryExtension;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.model.File;
import org.jfrog.artifactory.client.model.RepoPath;

import java.io.InputStream;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArtifactoryModel {
    private String username;
    private String password;
    private String artifactoryUrl;
    private String repoName;
    private Artifactory artifactory = null;

    public void createArtifactory() throws Exception {
        artifactory = ArtifactoryClientBuilder.create().setUrl(artifactoryUrl).setUsername(username).setPassword(password).build();
        System.out.println(artifactory.toString());
    }

    public String getRawFilePath(String ecuId, String partType, String fileName) {
        return String.format("%s/%s/%s", ecuId, partType, fileName);
    }

    public List<RepoPath> searchFile(String fileToSearchPath) {
        return artifactory.searches().repositories(repoName).artifactsByName(fileToSearchPath).doSearch();
    }

    public void uploadFile(String fileToUploadPath, InputStream fileToUpload){
        artifactory.repository(repoName).upload(fileToUploadPath, fileToUpload).doUpload();
    }

    public boolean checkExistInRepo(String ecuPart, String partType, String fileToSearch) {
        List<RepoPath> fileResult = searchFile(fileToSearch);
        if (fileResult.size() > 0) {
            String filePath = String.format("%s/%s/%s", ecuPart, partType, fileToSearch);
            for (RepoPath repoPath : fileResult) {
                if (repoPath.getItemPath().compareToIgnoreCase(filePath) == 0)
                    return true;
            }
        }
        return false;
    }

    public File getFile(String fileToSearch) {
        return artifactory.repository(repoName).file(fileToSearch).info();
    }
}
