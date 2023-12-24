
resource "aws_s3_bucket" "aws_quarkus_learning" {
  bucket = var.aws_bucket_name

  force_destroy = true
}

resource "aws_s3_bucket_ownership_controls" "aws_quarkus_learning" {
  bucket = aws_s3_bucket.aws_quarkus_learning.id
  rule {
    object_ownership = "BucketOwnerPreferred"
  }
}


resource "aws_s3_bucket_public_access_block" "aws_quarkus_learning" {
  bucket = aws_s3_bucket.aws_quarkus_learning.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

resource "aws_s3_bucket_acl" "aws_quarkus_learning" {
  depends_on = [
    aws_s3_bucket_ownership_controls.aws_quarkus_learning,
    aws_s3_bucket_public_access_block.aws_quarkus_learning,
  ]

  bucket = aws_s3_bucket.aws_quarkus_learning.id
  acl    = "public-read"
}