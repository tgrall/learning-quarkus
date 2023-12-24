# Learning-quarkus : Upload image to a cloud storage

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

The blog post is available here: https://tgrall.github.io/blog/2024/12/21/quarkus-uploading-image-to-the-cloud/


## Prerequisites

As documented in the blog post, this project use 3 different cloud storage services:

- AWS S3
- Azure Blob Storage
- Google Cloud Storage

When you run quarkus in development mode a local data service is used for AWS and Azure, but not for GCP. 
So if you want to test on GCP you need to create a bucket and a service account.

Also this project contains a Terraform script to create the required resources on all providers, 
you can find this in [./terraform](./terraform) folder. 


#### Creating the Cloud resources

```bash
cd ./terraform

terraform init

terraform plan

terraform apply

```

You can get the Azure connection string using the following command:

```bash
terraform output connection_string
```
Copy the string the `application.properties` file, and set the value of the `quarkus.devservices.enabled=` property to `false`.

#### Destroying the Cloud resources

```bash
cd ./terraform

terraform destroy

```


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```


## Testing the application

### Using the Web UI

Once the Quarkus application is started point your browser to the following URL: 

- http://localhost:8080

You can enter some values, select the cloud storage service, and upload a picture.

### Using `curl`

You can call the following endpoint to upload a picture to the cloud storage:

**AWS S3**

```bash
curl -i -X POST http://localhost:8080/api/v1/boards/picture/aws \
  -H 'Content-Type: multipart/form-data' \
  -F 'picture=@./src/test/resources/test-board.png' \
  -F 'board={"id":5 , "year":2024, "brand":"jp-australia", "slug":"jp-australia-2024-ultimate-wave"}'
```

**Azure Blob Storage**

```bash
curl -i -X POST http://localhost:8080/api/v1/boards/picture/azure \
  -H 'Content-Type: multipart/form-data' \
  -F 'picture=@./src/test/resources/test-board.png' \
  -F 'board={"id":5 , "year":2024, "brand":"jp-australia", "slug":"jp-australia-2024-ultimate-wave"}'
```

**Google Cloud Storage**

```bash
curl -i -X POST http://localhost:8080/api/v1/boards/picture/gcp \
  -H 'Content-Type: multipart/form-data' \
  -F 'picture=@./src/test/resources/test-board.png' \
  -F 'board={"id":5 , "year":2024, "brand":"jp-australia", "slug":"jp-australia-2024-ultimate-wave"}'
```

