/*
package com.gorbatenko.budget.config.backupdatabase;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    private static final String MIME_TYPE_FOLDER = "application/vnd.google-apps.folder";

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
            data.append(document.toJson()).append("\n");
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
            for (String child : children) {
                boolean success = deleteDir(new java.io.File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private String getNewToken(String refreshToken, String clientId, String clientSecret) throws IOException {
        ArrayList<String> scopes = new ArrayList<>();
        TokenResponse tokenResponse = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                refreshToken, clientId, clientSecret).setScopes(scopes).setGrantType("refresh_token").execute();

        return tokenResponse.getAccessToken();
    }

    private GoogleCredential getCredentials(
            NetHttpTransport httpTransport,
            JsonFactory jsonFactory,
            String gdriveClientId,
            String gdriveSecret,
            String gdriveRefreshToken) throws IOException {

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
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

        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credentials = getCredentials(httpTransport, jsonFactory, gdriveClientId, gdriveSecret, gdriveRefreshToken);

        Drive driveService = new Drive.Builder(httpTransport, jsonFactory, credentials)
                .setApplicationName(applicationName).build();

        String folderYearName = String.valueOf(LocalDate.now().getYear());
        String folderMonthName = LocalDate.now().getMonth().name();

        File folderYear = getFolderByName(driveService, gdriveFolderId, folderYearName);
        File folderMonth = getFolderByName(driveService, folderYear.getId(), folderMonthName);

        File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setName(createBackupFileName());
        fileMetadata.setParents(Collections.singletonList(folderMonth.getId()));

        java.io.File file = new java.io.File(pathToBackupZip);

        FileContent mediaContent = new FileContent("type:application/zip", file);

        driveService.files()
                .create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
    }

    private File getFolderByName(Drive driveService, String gdriveFolderId, String folderName) throws IOException {
        String query = String.format("mimeType='%s' and '%s' in parents", MIME_TYPE_FOLDER, gdriveFolderId);
        FileList fileList = driveService.files().list()
                .setQ(query)
                .execute();

        List<File> files = fileList.getFiles();
        if (files == null || files.isEmpty()) {
            return createGDriveFolder(driveService, gdriveFolderId, folderName);
        } else {
            for (File file : files) {
                if (folderName.equals(file.getName())) {
                    return file;
                }
            }
        }
        return createGDriveFolder(driveService, gdriveFolderId, folderName);
    }

    private File createGDriveFolder(Drive driveService, String folderIdParent, String folderName) throws IOException {

        File fileMetadata = new File();

        fileMetadata.setName(folderName);
        fileMetadata.setMimeType(MIME_TYPE_FOLDER);

        if (folderIdParent != null) {
            List<String> parents = List.of(folderIdParent);

            fileMetadata.setParents(parents);
        }

        return driveService.files().create(fileMetadata).setFields("id, name").execute();
    }

    private String createBackupFileName() {
        String mask = "backup_%d%02d%02d_%02d%02d.zip";
        LocalDateTime ldt = LocalDateTime.now();
        return String.format(mask, ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute());
    }

}
*/
