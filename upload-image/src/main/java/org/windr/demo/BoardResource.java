package org.windr.demo;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.CREATED;

@Path(value = "/api/v1/boards")
public class BoardResource {

    @ConfigProperty(name = "bucket.name")
    private String BUCKET_NAME;

    @Inject
    BlobServiceClient azureBlobServiceClient; // Azure Blob Storage

    @Inject
    Storage googleStorage; // Google Cloud Storage

    @Inject
    S3Client s3Client;

    public static class BoardMetadata {
        public long id;
        public String brand;
        public int year;
        public String slug;

        public BoardMetadata() {
        }
    }

    @POST
    @Path("/picture/{cloud : (azure|aws|gcp)?}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@RestForm @PartType(MediaType.APPLICATION_JSON) BoardMetadata board,
            @RestForm("picture") FileUpload picture, @PathParam("cloud") String cloud) {
        // if cloud is empty, default to azure
        if (cloud == null || cloud.isEmpty()) {
            cloud = "azure";
        }

        if (picture == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A picture is required").build();
        }

        String url = null;
        Map<String, Object> json = new HashMap<>();
        json.put("board_id", board.id);


        try {
            System.out.println("Uploading to " + cloud);
            switch (cloud) {
                case "azure" -> url = uploadToAzure(picture, board);
                case "aws" -> url = uploadToAWS(picture, board);
                case "gcp" -> url = uploadToGCP(picture, board);
            }
            if (url != null) {
                json.put("url", url);
            }
        } catch (Exception e) {
            Log.error("An error occurred while processing the request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the request: " + e.getMessage()).build();
        }

        return Response.ok(json).status(CREATED).build();
    }

    private String uploadToAzure(FileUpload picture, BoardMetadata board) {
        String container = "catalog";
        String blobName = getBlobName(picture.fileName(), board);
        try {
            Map<String, String> metadata = Collections.singletonMap("metadata", "value");
            BlobContainerCreateOptions options = new BlobContainerCreateOptions().setMetadata(metadata)
                    .setPublicAccessType(PublicAccessType.BLOB);
            BlobContainerClient blobContainerClient = azureBlobServiceClient
                    .createBlobContainerIfNotExistsWithResponse(container, options, Context.NONE).getValue();
            BlobHttpHeaders headers = new BlobHttpHeaders();
            headers.setContentType(picture.contentType());
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.uploadFromFile(picture.uploadedFile().toAbsolutePath().toString(), true);
            blobClient.setHttpHeaders(headers);
            return blobClient.getBlobUrl();
        } catch (BlobStorageException e) {
            Log.error("An error occurred while uploading the file to Google Blob Storage: " + e.getMessage());
            throw new RuntimeException("An error occurred while uploading the file to Azure Blob Storage: "
                    + e.getErrorCode() + " - " + e.getStatusCode());
        }
    }

    private String uploadToAWS(FileUpload picture, BoardMetadata board) {
        Log.info("Uploading to AWS: " + board.slug +" - in bucket " + BUCKET_NAME);
        String blobName = "catalog/"+ getBlobName(picture.fileName(), board);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(blobName)
                    .contentType(picture.contentType())
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            PutObjectResponse putResponse =  s3Client.putObject(
                    putRequest,
                    picture.uploadedFile().toAbsolutePath());
            return s3Client.utilities().getUrl(builder -> builder.bucket(BUCKET_NAME).key(blobName)).toString();
        } catch (S3Exception e) {

            e.printStackTrace();

            Log.error("An error occurred while uploading the file to AWS Blob Storage: " + e.getMessage());
            throw new RuntimeException("An error occurred while uploading the file to AWS Blob Storage: "
                    + e.getMessage());
        }
    }

    private String uploadToGCP(FileUpload picture, BoardMetadata board) {
        String blobName = "catalog/" + getBlobName(picture.fileName(), board);
        try {
            BlobInfo blobInfo = BlobInfo.newBuilder(BUCKET_NAME, blobName).setContentType(picture.contentType())
                    .setAcl(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))).build();
            Blob blob = googleStorage.createFrom(blobInfo, picture.uploadedFile().toAbsolutePath());
            return "https://storage.googleapis.com/" + BUCKET_NAME + "/" + blobInfo.getBlobId().getName();
        } catch (IOException e) {
            Log.error("An error occurred while uploading the file to Google Blob Storage: " + e.getMessage());
            throw new RuntimeException("An error occurred while uploading the file to Google Blob Storage: " + e.getMessage());
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public BoardMetadata get() {
        BoardMetadata board = new BoardMetadata();
        board.id = 1;
        board.brand = "Firewire";
        board.year = 2019;
        board.slug = "firewire-2019";
        return board;
    }

    private String getBlobName(String fileName, BoardMetadata board) {
        String extension = Optional.ofNullable(fileName).filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1)).orElse("");
        return board.brand + "/" + board.year + "/" + board.slug + "." + extension;
    }

}
