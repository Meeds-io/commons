package org.exoplatform.commons.dlp.service;

public interface DlpPermissionsService {

  void addDlpPermissionsPagesAndNavigations(String permissions) throws Exception;

  void removeDlpPermissionsPagesAndNavigations(String oldPermissions) throws Exception;
}
