
#!/bin/sh
awslocal dynamodb create-table   --table-name user   --attribute-definitions AttributeName=username,AttributeType=S   --key-schema AttributeName=username,KeyType=HASH   --billing-mode PAY_PER_REQUEST || true

awslocal dynamodb create-table   --table-name sf_contact   --attribute-definitions AttributeName=id,AttributeType=S   --key-schema AttributeName=id,KeyType=HASH   --billing-mode PAY_PER_REQUEST || true
