package org.brapi.v2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Contact
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T14:22:13.640Z[GMT]")
public class Contact   {
  @JsonProperty("contactDbId")
  private String contactDbId = null;

  @JsonProperty("email")
  private String email = null;

  @JsonProperty("instituteName")
  private String instituteName = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("orcid")
  private String orcid = null;

  @JsonProperty("type")
  private String type = null;

  public Contact contactDbId(String contactDbId) {
    this.contactDbId = contactDbId;
    return this;
  }

  /**
   * The ID which uniquely identifies this contact
   * @return contactDbId
  **/
  @ApiModelProperty(example = "5f4e5509", required = true, value = "The ID which uniquely identifies this contact")
      @NotNull

    public String getContactDbId() {
    return contactDbId;
  }

  public void setContactDbId(String contactDbId) {
    this.contactDbId = contactDbId;
  }

  public Contact email(String email) {
    this.email = email;
    return this;
  }

  /**
   * The contacts email address 
   * @return email
  **/
  @ApiModelProperty(example = "bob@bob.com", value = "The contacts email address ")
  
    public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Contact instituteName(String instituteName) {
    this.instituteName = instituteName;
    return this;
  }

  /**
   * The name of the institution which this contact is part of
   * @return instituteName
  **/
  @ApiModelProperty(example = "The BrAPI Institute", value = "The name of the institution which this contact is part of")
  
    public String getInstituteName() {
    return instituteName;
  }

  public void setInstituteName(String instituteName) {
    this.instituteName = instituteName;
  }

  public Contact name(String name) {
    this.name = name;
    return this;
  }

  /**
   * The full name of this contact person
   * @return name
  **/
  @ApiModelProperty(example = "Bob Robertson", value = "The full name of this contact person")
  
    public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Contact orcid(String orcid) {
    this.orcid = orcid;
    return this;
  }

  /**
   * The Open Researcher and Contributor ID for this contact person (orcid.org)
   * @return orcid
  **/
  @ApiModelProperty(example = "http://orcid.org/0000-0001-8640-1750", value = "The Open Researcher and Contributor ID for this contact person (orcid.org)")
  
    public String getOrcid() {
    return orcid;
  }

  public void setOrcid(String orcid) {
    this.orcid = orcid;
  }

  public Contact type(String type) {
    this.type = type;
    return this;
  }

  /**
   * The type of person this contact represents (ex: Coordinator, Scientist, PI, etc.)
   * @return type
  **/
  @ApiModelProperty(example = "PI", value = "The type of person this contact represents (ex: Coordinator, Scientist, PI, etc.)")
  
    public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Contact contact = (Contact) o;
    return Objects.equals(this.contactDbId, contact.contactDbId) &&
        Objects.equals(this.email, contact.email) &&
        Objects.equals(this.instituteName, contact.instituteName) &&
        Objects.equals(this.name, contact.name) &&
        Objects.equals(this.orcid, contact.orcid) &&
        Objects.equals(this.type, contact.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contactDbId, email, instituteName, name, orcid, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Contact {\n");
    
    sb.append("    contactDbId: ").append(toIndentedString(contactDbId)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    instituteName: ").append(toIndentedString(instituteName)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    orcid: ").append(toIndentedString(orcid)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
