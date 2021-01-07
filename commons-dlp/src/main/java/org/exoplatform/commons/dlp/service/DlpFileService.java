package org.exoplatform.commons.dlp.service;

import org.exoplatform.commons.dlp.domain.DlpFileEntity;

import java.util.List;

public interface DlpFileService {

    List<DlpFileEntity> getDlpFiles();

    void addDlpFile(DlpFileEntity dlpFile);
    
    void deleteDlpFile(Long fileId);
    
    DlpFileEntity getDlpFileByUUID(String fileId);
}
