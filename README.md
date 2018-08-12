
# S3FXViewer
S3FXViewer is GUI based on Java FX for AWS S3. The project is forked from umbertop/s3zilla.
It is a simple S3 client with the following feature:
- configure multiple accounts (saved in text file without encryption)
- configure http proxy
- upload/download/delete files/objects

# Build the project
The project is a simple Java maven project.
To compile it:

    mvn clean install

To run it:
extract the target/s3fxviewer-\<version\>-dist.zip and execute
    
    java -jar s3fxviewer-<version>.jar

# Use the client
## Add a new client
Just click `Account` -> `Add` to add an S3 account. Enter at least a `Display name`and a `region` to create and account.
## Activate a account
To activate (use) a the created account just click on the menu item (`Account` -> `<Display name of the S3 account>`.
## Select a bucket
After you selected an S3 account all available buckets will shown in the `Bucket`menu. To select a bucket just click on it.
## Managing objects/files
After you selected a bucket use the context menu in the main area for all actions.
