package com.gorbatenko.budget.config.backupdatabase;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
//import com.mongodb.MongoClient;
//import com.mongodb.MongoClientURI;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    private MongoClient createMongoClient(ConnectionString uri) {
        MongoClientSettings.Builder settings = MongoClientSettings.builder();
        settings.applyConnectionString(uri);
        settings.readPreference(ReadPreference.primaryPreferred());

        return MongoClients.create(settings.build());
    }

    protected void createBackUp(String connectionURI, String pathToBackupFile) throws IOException {
        ConnectionString uri = new ConnectionString(connectionURI);

        MongoClient mongoClient = createMongoClient(uri);

        String database = uri.getDatabase();
        MongoDatabase mongoDatabase = mongoClient.getDatabase(database);

        for(String collectionName : mongoDatabase.listCollectionNames()) {
            saveCollectionToFile(mongoClient, database, collectionName, pathToBackupFile);
        }
        mongoClient.close();
    }

    private void saveCollectionToFile(MongoClient mongoClient, String database, String name, String pathToBackupFile) throws IOException {
        StringBuilder data = new StringBuilder();

        MongoCollection<Document> collection = mongoClient.getDatabase(database).getCollection(name);
        for(Document document : collection.find()) {
            data.append(document.toJson() + "\n");
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(String.format(pathToBackupFile, name)));
        writer.write(data.toString());
        writer.close();
    }

    protected void zipFolder(String pathToBackupFolder, String pathToBackupZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(pathToBackupZipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        java.io.File fileToZip = new java.io.File(pathToBackupFolder);

        zipFile(fileToZip, fileToZip.getName(), zipOut);
        zipOut.close();
        fos.close();
    }

    private void zipFile(java.io.File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            java.io.File[] children = fileToZip.listFiles();
            for (java.io.File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        fis.close();
    }

    public boolean deleteDir(java.io.File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new java.io.File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete(); // The directory is empty now and can be deleted.
    }

    private String getNewToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        ArrayList<String> scopes = new ArrayList<>();
        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                refreshToken, clientId, clientSecret).setScopes(scopes).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }

    private Credential getCredentials(String gdriveClientId,
                                     String gdriveSecret,
                                     String gdriveRefreshToken) throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(gdriveClientId, gdriveSecret)
                .build();

        credential.setAccessToken(getNewToken(gdriveRefreshToken, gdriveClientId, gdriveSecret));

        credential.setRefreshToken(gdriveRefreshToken);

        return credential;
    }


    public void uploadFileToGoogleDrive(String pathToBackupZip,
                                        String applicationName,
                                        String gdriveFolderId,
                                        String gdriveClientId,
                                        String gdriveSecret,
                                        String gdriveRefreshToken
                                        ) throws GeneralSecurityException, IOException {

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

        Drive driveService = new Drive.Builder(
                HTTP_TRANSPORT,
                JSON_FACTORY,
                getCredentials(gdriveClientId, gdriveSecret, gdriveRefreshToken))
                .setApplicationName(applicationName).build();
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(createBackupFileName());
        fileMetadata.setParents(Collections.singletonList(gdriveFolderId));
        java.io.File file = new java.io.File(pathToBackupZip);
        FileContent mediaContent = new FileContent("type:application/zip", file);
        driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
    }

    private String createBackupFileName() {
        String mask = "backup_%d%02d%02d_%02d%02d.zip";
        LocalDateTime ldt = LocalDateTime.now();
        return String.format(mask, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute());
    }

}
