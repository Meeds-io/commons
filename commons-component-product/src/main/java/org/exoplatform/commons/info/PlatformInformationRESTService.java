/**
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.commons.info;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * @author <a href="mailto:anouar.chattouna@exoplatform.com">Anouar
 *         Chattouna</a>
 * @version $Revision$
 */
@Path("/platform")
public class PlatformInformationRESTService implements ResourceContainer {

  private static final Log             LOG               = ExoLogger.getLogger(PlatformInformationRESTService.class);

  public static final java.lang.String COMMUNITY_EDITION  = "community";

  public static final java.lang.String ENTERPRISE_EDITION = "enterprise";

  public static final java.lang.String MIN_MOBILE_SUPPORTED_VERSION = "4.3.0+";

  private ProductInformations          platformInformations;

  private UserACL                      userACL;

  public PlatformInformationRESTService(ProductInformations productInformations, UserACL userACL) {
    this.platformInformations = productInformations;
    this.userACL = userACL;
  }

  /**
   * This method return a JSON Object with the platform required informations.
   * 
   * @return REST response
   */
  @GET
  @Path("/info")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPlatformInformation() {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      String plfProfile = ExoContainer.getProfiles().toString().trim();
      String runningProfile = plfProfile.substring(1, plfProfile.length() - 1);
      JsonPlatformInfo jsonPlatformInfo = new JsonPlatformInfo();
      if (userACL.isUserInGroup(userACL.getAdminGroups())) {
        jsonPlatformInfo.setPlatformVersion(platformInformations.getVersion());
        jsonPlatformInfo.setPlatformBuildNumber(platformInformations.getBuildNumber());
        jsonPlatformInfo.setPlatformRevision(platformInformations.getRevision());
        jsonPlatformInfo.setIsMobileCompliant(isMobileCompliant().toString());
        jsonPlatformInfo.setRunningProfile(runningProfile);
        jsonPlatformInfo.setPlatformEdition(getPlatformEdition());
        if ((platformInformations.getEdition() != null) && (!platformInformations.getEdition().equals(""))) {
          jsonPlatformInfo.setDuration(platformInformations.getDuration());
          jsonPlatformInfo.setDateOfKeyGeneration(platformInformations.getDateOfLicence());
          jsonPlatformInfo.setNbUsers(platformInformations.getNumberOfUsers());
          jsonPlatformInfo.setProductCode(platformInformations.getProductCode());
          jsonPlatformInfo.setUnlockKey(platformInformations.getProductKey());
        }
      } else {
        // Add a compliant version to mobile application
        // without exposing real version of platform
        // to anonymous or connected users
        jsonPlatformInfo.setPlatformVersion(MIN_MOBILE_SUPPORTED_VERSION);
      }
      if (LOG.isDebugEnabled()) {
        LOG.debug("Getting Platform Informations: eXo Platform (v" + platformInformations.getVersion() + " - build "
            + platformInformations.getBuildNumber() + " - rev. " + platformInformations.getRevision());
      }
      return Response.ok(jsonPlatformInfo, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      LOG.error("An error occurred while getting platform version information.", e);
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
  }

  public JsonPlatformInfo getJsonPlatformInfo() {
    try {
      String plfProfile = ExoContainer.getProfiles().toString().trim();
      String runningProfile = plfProfile.substring(1, plfProfile.length() - 1);
      JsonPlatformInfo jsonPlatformInfo = new JsonPlatformInfo();
      jsonPlatformInfo.setPlatformVersion(platformInformations.getVersion());
      jsonPlatformInfo.setPlatformBuildNumber(platformInformations.getBuildNumber());
      jsonPlatformInfo.setPlatformRevision(platformInformations.getRevision());
      jsonPlatformInfo.setIsMobileCompliant(isMobileCompliant().toString());
      jsonPlatformInfo.setRunningProfile(runningProfile);
      jsonPlatformInfo.setPlatformEdition(getPlatformEdition());
      jsonPlatformInfo.setDuration(platformInformations.getDuration());
      jsonPlatformInfo.setDateOfKeyGeneration(platformInformations.getDateOfLicence());
      jsonPlatformInfo.setNbUsers(platformInformations.getNumberOfUsers());
      jsonPlatformInfo.setProductCode(platformInformations.getProductCode());
      jsonPlatformInfo.setUnlockKey(platformInformations.getProductKey());
      if (LOG.isDebugEnabled()) {
        LOG.debug("Getting Platform Informations: eXo Platform (v" + platformInformations.getVersion() + " - build "
            + platformInformations.getBuildNumber() + " - rev. " + platformInformations.getRevision());
      }
      return jsonPlatformInfo;
    } catch (Exception e) {
      LOG.error("An error occured while getting platform version information.", e);
      return null;
    }
  }

  private Boolean isMobileCompliant() {
    String platformEdition = getPlatformEdition();
    return (platformEdition != null && ((platformEdition.equals(COMMUNITY_EDITION)) ||
        (platformEdition.equalsIgnoreCase(ProductInformations.ENTERPRISE_EDITION)) ||
        (platformEdition.equals(ProductInformations.EXPRESS_EDITION))));
  }

  public String getPlatformEdition() {
    try {
      String platformEdition = ExoContainer.hasProfile(COMMUNITY_EDITION) ? COMMUNITY_EDITION : ENTERPRISE_EDITION;
      if (StringUtils.isBlank(platformEdition) && platformInformations != null) {
        platformEdition = platformInformations.getEdition();
      }
      return platformEdition;
    } catch (Exception e) {
      LOG.error("An error occurred while getting the platform edition information.", e);
    }
    return null;
  }

  public static class JsonPlatformInfo {

    private String platformVersion;

    private String platformBuildNumber;

    private String platformRevision;

    private String platformEdition;

    private String isMobileCompliant;

    private String runningProfile;

    private String nbUsers;

    private String duration;

    private String buildNumber;

    private String productCode;

    private String dateOfKeyGeneration;

    private String unlockKey;

    public void setNbUsers(String nbUsers) {
      this.nbUsers = nbUsers;
    }

    public void setDuration(String duration) {
      this.duration = duration;
    }

    public void setProductCode(String productCode) {
      this.productCode = productCode;
    }

    public String getUnlockKey() {
      return unlockKey;
    }

    public void setUnlockKey(String unlockKey) {
      this.unlockKey = unlockKey;
    }

    public void setDateOfKeyGeneration(String dateOfKeyGeneration) {
      this.dateOfKeyGeneration = dateOfKeyGeneration;
    }

    public String getNbUsers() {
      return nbUsers;
    }

    public String getProductCode() {
      return productCode;
    }

    public String getDateOfKeyGeneration() {
      return dateOfKeyGeneration;
    }

    public String getDuration() {
      return duration;
    }

    public String getBuildNumber() {
      return buildNumber;
    }

    public String getPlatformVersion() {
      return platformVersion;
    }

    public void setPlatformVersion(String platformVersion) {
      this.platformVersion = platformVersion;
    }

    public String getIsMobileCompliant() {
      return this.isMobileCompliant;
    }

    public void setIsMobileCompliant(String isMobileCompliant) {
      this.isMobileCompliant = isMobileCompliant;
    }

    public String getPlatformBuildNumber() {
      return platformBuildNumber;
    }

    public void setPlatformBuildNumber(String platformBuildNumber) {
      this.platformBuildNumber = platformBuildNumber;
    }

    public String getPlatformRevision() {
      return platformRevision;
    }

    public void setPlatformRevision(String platformRevision) {
      this.platformRevision = platformRevision;
    }

    public String getPlatformEdition() {
      return this.platformEdition;
    }

    public void setPlatformEdition(String platformEdition) {
      this.platformEdition = platformEdition;
    }

    public String getRunningProfile() {
      return this.runningProfile;
    }

    public void setRunningProfile(String runningProfile) {
      this.runningProfile = runningProfile;
    }

  }
}
