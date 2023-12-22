package org.windr.demo;

import com.azure.core.util.Context;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.PublicAccessType;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.text.MessageFormat;
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
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

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
    public Response upload(
            @RestForm @PartType(MediaType.APPLICATION_JSON) BoardMetadata board,
            @RestForm("picture") FileUpload picture,
            @DefaultValue("azure") @PathParam("cloud") String cloud
    ) {
        Map<String, Object> json = new HashMap<>();
        json.put("board_id", board.id);


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

        return Response.ok(json).status(CREATED).build();
    }


    private String uploadToAzure(FileUpload picture, BoardMetadata board) {
        String container = "catalog";
        String extension = getExtensionByStringHandling(picture.fileName()).orElse("jpg");
        String blobName = board.brand + "/" + board.year + "/" + board.slug +"." + extension;
        BlobContainerClient blobContainerClient = azureBlobServiceClient.createBlobContainerWithResponse(container, Collections.emptyMap(), PublicAccessType.BLOB, Context.NONE).getValue();
        BlobHttpHeaders headers = new BlobHttpHeaders();
        headers.setContentType(picture.contentType());
        BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(picture.uploadedFile().toAbsolutePath().toString(), true);
        blobClient.setHttpHeaders(headers);
        return blobClient.getBlobUrl();
    }

    /**
     * Upload to GCP
     * @param picture
     * @param board
     * @return
     */
    private String uploadToAWS(FileUpload picture, BoardMetadata board) {

        Log.info("Uploading to AWS: " + board.slug);

        return board.slug;
    }

    /**
     * Upload to GCP
     * @param picture
     * @param board
     * @return
     */
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
