package com.vinfast.api.model.common;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.vinfast.api.common.config.AfterSaleConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AzureStorageModel {
    private CloudBlobContainer blobContainer;

    public void createConnection(String connection) throws Exception {
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(connection);
        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        // Retrieve reference to a previously created container.
        blobContainer = blobClient.getContainerReference("ecn");
    }

    public void uploadFile(File file, String _FileName) throws Exception {
        String fileName = _FileName.isEmpty() ? file.getName() : _FileName;
        CloudBlockBlob blob = blobContainer.getBlockBlobReference(fileName);
        blob.upload(Files.newInputStream(file.toPath()), file.length());
    }
}
