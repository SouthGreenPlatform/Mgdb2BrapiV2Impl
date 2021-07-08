/*
 * BrAPI-Core
 * The Breeding API (BrAPI) is a Standardized REST ful Web Service API Specification for communicating Plant Breeding Data. BrAPI allows for easy data sharing between databases and tools involved in plant breeding. <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">General Reference Documentation</h2> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/URL_Structure.md\">URL Structure</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Response_Structure.md\">Response Structure</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Date_Time_Encoding.md\">Date/Time Encoding</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Location_Encoding.md\">Location Encoding</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Error_Handling.md\">Error Handling</a></div> <div class=\"gen-info-link\"><a href=\"https://github.com/plantbreeding/API/blob/master/Specification/GeneralInfo/Search_Services.md\">Search Services</a></div> </div>  <div class=\"current-brapi-section brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Core</h2> <div class=\"brapi-section-description\">The BrAPI Core module contains high level entities used for organization and management. This includes Programs, Trials, Studies, Locations, People, and Lists</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Core\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Core\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapicore.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Phenotyping</h2> <div class=\"brapi-section-description\">The BrAPI Phenotyping module contains entities related to phenotypic observations. This includes Observation Units, Observations, Observation Variables, Traits, Scales, Methods, and Images</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Phenotyping\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Phenotyping\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapiphenotyping.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Genotyping</h2> <div class=\"brapi-section-description\">The BrAPI Genotyping module contains entities related to genotyping analysis. This includes Samples, Markers, Variant Sets, Variants, Call Sets, Calls, References, Reads, and Vendor Orders</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Genotyping\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Genotyping\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapigenotyping.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <div class=\"brapi-section\"> <h2 class=\"brapi-section-title\">BrAPI Germplasm</h2> <div class=\"brapi-section-description\">The BrAPI Germplasm module contains entities related to germplasm management. This includes Germplasm, Germplasm Attributes, Seed Lots, Crosses, Pedigree, and Progeny</div> <div class=\"version-number\">V2.0</div> <div class=\"link-btn\"><a href=\"https://github.com/plantbreeding/API/tree/master/Specification/BrAPI-Germplasm\">GitHub</a></div> <div class=\"link-btn\"><a href=\"https://app.swaggerhub.com/apis/PlantBreedingAPI/BrAPI-Germplasm\">SwaggerHub</a></div> <div class=\"link-btn\"><a href=\"https://brapigermplasm.docs.apiary.io\">Apiary</a></div> <div class=\"stop-float\"></div> </div>  <style> .link-btn{ float: left;  margin: 2px 10px 0 0;  padding: 0 5px;  border-radius: 5px;  background-color: #ddd; } .stop-float{   clear: both; } .version-number{   float: left;    margin: 5px 10px 0 5px; } .brapi-section-title{   margin: 0 10px 0 0;   font-size: 20px; } .current-brapi-section{   font-weight: bolder;   border-radius: 5px;    background-color: #ddd; } .brapi-section{   padding: 5px 5px;  } .brapi-section-description{   margin: 5px 0 0 5px; } </style>
 *
 * OpenAPI spec version: 2.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.brapi.v2.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import io.swagger.annotations.ApiModelProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * ProgramSearchRequest
 */

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-06-24T08:33:25.155Z[GMT]")
public class ProgramSearchRequest extends SearchRequestParametersPaging {
  @SerializedName("commonCropNames")
  private List<String> commonCropNames = null;

  @SerializedName("programDbIds")
  private List<String> programDbIds = null;

  @SerializedName("programNames")
  private List<String> programNames = null;

  @SerializedName("externalReferenceIDs")
  private List<String> externalReferenceIDs = null;

  @SerializedName("externalReferenceSources")
  private List<String> externalReferenceSources = null;

  @SerializedName("abbreviations")
  private List<String> abbreviations = null;

  @SerializedName("leadPersonDbIds")
  private List<String> leadPersonDbIds = null;

  @SerializedName("leadPersonNames")
  private List<String> leadPersonNames = null;

  @SerializedName("objectives")
  private List<String> objectives = null;

  public ProgramSearchRequest commonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
    return this;
  }

  public ProgramSearchRequest addCommonCropNamesItem(String commonCropNamesItem) {
    if (this.commonCropNames == null) {
      this.commonCropNames = new ArrayList<String>();
    }
    this.commonCropNames.add(commonCropNamesItem);
    return this;
  }

   /**
   * Common name for the crop which this program is for
   * @return commonCropNames
  **/
  @ApiModelProperty(example = "[\"Tomatillo\",\"Paw Paw\"]", value = "Common name for the crop which this program is for")
  public List<String> getCommonCropNames() {
    return commonCropNames;
  }

  public void setCommonCropNames(List<String> commonCropNames) {
    this.commonCropNames = commonCropNames;
  }

  public ProgramSearchRequest programDbIds(List<String> programDbIds) {
    this.programDbIds = programDbIds;
    return this;
  }

  public ProgramSearchRequest addProgramDbIdsItem(String programDbIdsItem) {
    if (this.programDbIds == null) {
      this.programDbIds = new ArrayList<String>();
    }
    this.programDbIds.add(programDbIdsItem);
    return this;
  }

   /**
   * A program identifier to search for
   * @return programDbIds
  **/
  @ApiModelProperty(example = "[\"8f5de35b\",\"0e2d4a13\"]", value = "A program identifier to search for")
  public List<String> getProgramDbIds() {
    return programDbIds;
  }

  public void setProgramDbIds(List<String> programDbIds) {
    this.programDbIds = programDbIds;
  }

  public ProgramSearchRequest programNames(List<String> programNames) {
    this.programNames = programNames;
    return this;
  }

  public ProgramSearchRequest addProgramNamesItem(String programNamesItem) {
    if (this.programNames == null) {
      this.programNames = new ArrayList<String>();
    }
    this.programNames.add(programNamesItem);
    return this;
  }

   /**
   * A name of a program to search for
   * @return programNames
  **/
  @ApiModelProperty(example = "[\"Better Breeding Program\",\"Best Breeding Program\"]", value = "A name of a program to search for")
  public List<String> getProgramNames() {
    return programNames;
  }

  public void setProgramNames(List<String> programNames) {
    this.programNames = programNames;
  }

  public ProgramSearchRequest externalReferenceIDs(List<String> externalReferenceIDs) {
    this.externalReferenceIDs = externalReferenceIDs;
    return this;
  }

  public ProgramSearchRequest addExternalReferenceIDsItem(String externalReferenceIDsItem) {
    if (this.externalReferenceIDs == null) {
      this.externalReferenceIDs = new ArrayList<String>();
    }
    this.externalReferenceIDs.add(externalReferenceIDsItem);
    return this;
  }

   /**
   * List of external reference IDs. Could be a simple strings or a URIs. (use with &#x60;externalReferenceSources&#x60; parameter)
   * @return externalReferenceIDs
  **/
  @ApiModelProperty(example = "[\"http://purl.obolibrary.org/obo/ro.owl\",\"14a19841\"]", value = "List of external reference IDs. Could be a simple strings or a URIs. (use with `externalReferenceSources` parameter)")
  public List<String> getExternalReferenceIDs() {
    return externalReferenceIDs;
  }

  public void setExternalReferenceIDs(List<String> externalReferenceIDs) {
    this.externalReferenceIDs = externalReferenceIDs;
  }

  public ProgramSearchRequest externalReferenceSources(List<String> externalReferenceSources) {
    this.externalReferenceSources = externalReferenceSources;
    return this;
  }

  public ProgramSearchRequest addExternalReferenceSourcesItem(String externalReferenceSourcesItem) {
    if (this.externalReferenceSources == null) {
      this.externalReferenceSources = new ArrayList<String>();
    }
    this.externalReferenceSources.add(externalReferenceSourcesItem);
    return this;
  }

   /**
   * List of identifiers for the source system or database of an external reference (use with &#x60;externalReferenceIDs&#x60; parameter)
   * @return externalReferenceSources
  **/
  @ApiModelProperty(example = "[\"OBO Library\",\"Field App Name\"]", value = "List of identifiers for the source system or database of an external reference (use with `externalReferenceIDs` parameter)")
  public List<String> getExternalReferenceSources() {
    return externalReferenceSources;
  }

  public void setExternalReferenceSources(List<String> externalReferenceSources) {
    this.externalReferenceSources = externalReferenceSources;
  }

  public ProgramSearchRequest abbreviations(List<String> abbreviations) {
    this.abbreviations = abbreviations;
    return this;
  }

  public ProgramSearchRequest addAbbreviationsItem(String abbreviationsItem) {
    if (this.abbreviations == null) {
      this.abbreviations = new ArrayList<String>();
    }
    this.abbreviations.add(abbreviationsItem);
    return this;
  }

   /**
   * An abbreviation of a program to search for
   * @return abbreviations
  **/
  @ApiModelProperty(example = "[\"P1\",\"P2\"]", value = "An abbreviation of a program to search for")
  public List<String> getAbbreviations() {
    return abbreviations;
  }

  public void setAbbreviations(List<String> abbreviations) {
    this.abbreviations = abbreviations;
  }

  public ProgramSearchRequest leadPersonDbIds(List<String> leadPersonDbIds) {
    this.leadPersonDbIds = leadPersonDbIds;
    return this;
  }

  public ProgramSearchRequest addLeadPersonDbIdsItem(String leadPersonDbIdsItem) {
    if (this.leadPersonDbIds == null) {
      this.leadPersonDbIds = new ArrayList<String>();
    }
    this.leadPersonDbIds.add(leadPersonDbIdsItem);
    return this;
  }

   /**
   * The person DbIds of the program leader to search for
   * @return leadPersonDbIds
  **/
  @ApiModelProperty(example = "[\"d8bd96c7\",\"a2b9c8e7\"]", value = "The person DbIds of the program leader to search for")
  public List<String> getLeadPersonDbIds() {
    return leadPersonDbIds;
  }

  public void setLeadPersonDbIds(List<String> leadPersonDbIds) {
    this.leadPersonDbIds = leadPersonDbIds;
  }

  public ProgramSearchRequest leadPersonNames(List<String> leadPersonNames) {
    this.leadPersonNames = leadPersonNames;
    return this;
  }

  public ProgramSearchRequest addLeadPersonNamesItem(String leadPersonNamesItem) {
    if (this.leadPersonNames == null) {
      this.leadPersonNames = new ArrayList<String>();
    }
    this.leadPersonNames.add(leadPersonNamesItem);
    return this;
  }

   /**
   * The names of the program leader to search for
   * @return leadPersonNames
  **/
  @ApiModelProperty(example = "[\"Bob Robertson\",\"Rob Robertson\"]", value = "The names of the program leader to search for")
  public List<String> getLeadPersonNames() {
    return leadPersonNames;
  }

  public void setLeadPersonNames(List<String> leadPersonNames) {
    this.leadPersonNames = leadPersonNames;
  }

  public ProgramSearchRequest objectives(List<String> objectives) {
    this.objectives = objectives;
    return this;
  }

  public ProgramSearchRequest addObjectivesItem(String objectivesItem) {
    if (this.objectives == null) {
      this.objectives = new ArrayList<String>();
    }
    this.objectives.add(objectivesItem);
    return this;
  }

   /**
   * A program objective to search for
   * @return objectives
  **/
  @ApiModelProperty(example = "[\"Objective Code One\",\"This is a longer objective search query\"]", value = "A program objective to search for")
  public List<String> getObjectives() {
    return objectives;
  }

  public void setObjectives(List<String> objectives) {
    this.objectives = objectives;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProgramSearchRequest programSearchRequest = (ProgramSearchRequest) o;
    return Objects.equals(this.commonCropNames, programSearchRequest.commonCropNames) &&
        Objects.equals(this.programDbIds, programSearchRequest.programDbIds) &&
        Objects.equals(this.programNames, programSearchRequest.programNames) &&
        Objects.equals(this.externalReferenceIDs, programSearchRequest.externalReferenceIDs) &&
        Objects.equals(this.externalReferenceSources, programSearchRequest.externalReferenceSources) &&
        Objects.equals(this.abbreviations, programSearchRequest.abbreviations) &&
        Objects.equals(this.leadPersonDbIds, programSearchRequest.leadPersonDbIds) &&
        Objects.equals(this.leadPersonNames, programSearchRequest.leadPersonNames) &&
        Objects.equals(this.objectives, programSearchRequest.objectives) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commonCropNames, programDbIds, programNames, externalReferenceIDs, externalReferenceSources, abbreviations, leadPersonDbIds, leadPersonNames, objectives, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProgramSearchRequest {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    commonCropNames: ").append(toIndentedString(commonCropNames)).append("\n");
    sb.append("    programDbIds: ").append(toIndentedString(programDbIds)).append("\n");
    sb.append("    programNames: ").append(toIndentedString(programNames)).append("\n");
    sb.append("    externalReferenceIDs: ").append(toIndentedString(externalReferenceIDs)).append("\n");
    sb.append("    externalReferenceSources: ").append(toIndentedString(externalReferenceSources)).append("\n");
    sb.append("    abbreviations: ").append(toIndentedString(abbreviations)).append("\n");
    sb.append("    leadPersonDbIds: ").append(toIndentedString(leadPersonDbIds)).append("\n");
    sb.append("    leadPersonNames: ").append(toIndentedString(leadPersonNames)).append("\n");
    sb.append("    objectives: ").append(toIndentedString(objectives)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}