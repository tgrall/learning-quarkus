package org.windr.demo;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.options.BlobContainerCreateOptions;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static jakarta.ws.rs.core.Response.Status.CREATED;


@Path(
value = "/api/v1/boards"
)
public class BoardResource {

    @Inject
    BlobServiceClient azureBlobServiceClient;

    public static class BoardMetadata {
        public long id;
        public String brand;
        public int year;
        public String slug;
        public BoardMetadata() {
        }
    }

    @POST
    @Path("/picture{/cloud?}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(
            @RestForm @PartType(MediaType.APPLICATION_JSON) BoardMetadata board,
            @RestForm("picture") FileUpload picture,
            @DefaultValue("azure") @PathParam("cloud") String cloud
    ) {
        Map<String, Object> json = new HashMap<>();
        json.put("board_id", board.id);

        try {
            switch (cloud) {
                case "azure" -> {
                    String url = uploadToAzure(picture, board);
                    json.put("url", url);
                }
                case "aws" -> uploadToAWS(picture, board);
                case "gcp" -> uploadToGCP(picture, board);
                default -> {
                }
            }
        } catch (Exception e) {
            Log.error("An error occurred while processing the request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while processing the request: " + e.getMessage())
                    .build();
        }

        return Response.ok(json).status(CREATED).build();
    }


    private String uploadToAzure(FileUpload picture, BoardMetadata board) {
        String container = "catalog";
        String extension = getExtensionByStringHandling(picture.fileName()).orElse("jpg");
        String blobName = board.brand + "/" + board.year + "/" + board.slug +"." + extension;
        try {
            Map<String, String> metadata = Collections.singletonMap("metadata", "value");
            BlobContainerCreateOptions options =
                    new BlobContainerCreateOptions().setMetadata(metadata).setPublicAccessType(PublicAccessType.BLOB);
            BlobContainerClient blobContainerClient =
                    azureBlobServiceClient.createBlobContainerIfNotExistsWithResponse(container, options, Context.NONE).getValue();
            BlobHttpHeaders headers = new BlobHttpHeaders();
            headers.setContentType(picture.contentType());
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            blobClient.uploadFromFile(picture.uploadedFile().toAbsolutePath().toString(), true);
            blobClient.setHttpHeaders(headers);
            return blobClient.getBlobUrl();
        } catch (BlobStorageException e) {
            throw new RuntimeException("An error occurred while uploading the file to Azure Blob Storage: " + e.getErrorCode() + " - " + e.getStatusCode() );
        }
    }

    private String uploadToAWS(FileUpload picture, BoardMetadata board) {

        Log.info("Uploading to AWS: " + board.slug);

        return board.slug;
    }

    private String uploadToGCP(FileUpload picture, BoardMetadata board) {

        Log.info("Uploading to GCP: " + board.slug);

        return board.slug;
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


    public Optional<String> getExtensionByStringHandling(String filename) {
        return Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1));
    }


}
