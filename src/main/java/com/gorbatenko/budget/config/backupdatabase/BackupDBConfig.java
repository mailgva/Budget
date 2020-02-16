package com.gorbatenko.budget.config.backupdatabase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Configuration
@EnableScheduling
public class BackupDBConfig {
    private final String DATABASE_BACKUP_ERROR =
            "Database backuping. Check needed variable ERROR. Not exists (or is empty) section [{}]! Check your application.properties file.";

    private final String BACKUP_TO_ROOT_FOLDER =
            "Database backuping. Not exists (or is empty) section [{}]! File will be place to root folder on Google Drive.";

    protected final String APPLICATION_NAME = "gva-budget";
    protected final String PATH_TO_BACKUP = "src/main/resources/";
    protected final String PATH_TO_BACKUP_FOLDER = PATH_TO_BACKUP + "backup/";
    protected final String PATH_TO_BACKUP_FILE = PATH_TO_BACKUP_FOLDER + "%s.json";
    protected final String PATH_TO_BACKUP_ZIP = PATH_TO_BACKUP + "backup.zip";

    @Value("${spring.data.mongodb.uri}")
    private String connectionURI;

    @Value("${app.backup.enabled:false}")
    private boolean backupEnable;

    @Value("${app.backup.google-drive-client-id:}")
    private String gdriveClientId;

    @Value("${app.backup.google-drive-secret:}")
    private String gdriveSecret;

    @Value("${app.backup.google-drive-refresh-token:}")
    private String gdriveRefreshToken;

    @Value("${app.backup.google-drive-folderid:}")
    private String gdriveFolderId;

    @Scheduled(cron = "${app.backup.cron.expression:-}")
    public void createBackupDataBase() {
        if (!backupEnable) return;

        if (!checkBackupParams()) return;

        if (!doBackupOperations()) {
            log.error("Backup database FAIL at {}!", LocalDateTime.now());
        };
    }

    private boolean doBackupOperations() {
        log.info("Backup database started at {}!", LocalDateTime.now());
        Utils utils = new Utils();
        java.io.File folder = new java.io.File(PATH_TO_BACKUP_FOLDER);
        utils.deleteDir(folder);
        folder.mkdir();

        try {
            utils.createBackUp(connectionURI, PATH_TO_BACKUP_FILE);
        } catch (IOException e) {
            log.error("Backup database. Error during get data from database!");
            e.printStackTrace();
            return false;
        }
        try {
            utils.zipFolder(PATH_TO_BACKUP_FOLDER, PATH_TO_BACKUP_ZIP);
        } catch (IOException e) {
            log.error("Backup database. Error during zip data!");
            e.printStackTrace();
            return false;
        }

        utils.deleteDir(folder);

        try {
            utils.uploadFileToGoogleDrive(PATH_TO_BACKUP_ZIP, APPLICATION_NAME,
                    gdriveFolderId, gdriveClientId, gdriveSecret, gdriveRefreshToken);
        } catch (Exception e) {
            log.error("Backup database. Error during upload file to Google Drive!");
            e.printStackTrace();
            return false;
        }
        java.io.File zipArchive = new java.io.File(PATH_TO_BACKUP_ZIP);
        zipArchive.delete();
        log.info("Backup database successfully completed at {}!", LocalDateTime.now());
        return true;
    }


    private boolean checkBackupParams() {
        boolean result = true;
        if (gdriveClientId.isEmpty()) {
            log.error(DATABASE_BACKUP_ERROR, "app.backup.google-drive-client-id");
            result = false;
        }
        if (gdriveSecret.isEmpty()) {
            log.error(DATABASE_BACKUP_ERROR, "app.backup.google-drive-secret");
            result = false;
        }
        if (gdriveRefreshToken.isEmpty()) {
            log.error(DATABASE_BACKUP_ERROR, "app.backup.google-drive-refresh-token");
            result = false;
        }

        if (gdriveFolderId.isEmpty()) {
            log.warn(BACKUP_TO_ROOT_FOLDER, "app.backup.google-drive-folderid");
        }
        return result;
    }
}
