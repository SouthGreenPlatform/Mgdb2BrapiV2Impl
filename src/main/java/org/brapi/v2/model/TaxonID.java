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
 * TaxonID
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-20T14:32:35.470Z[GMT]")
public class TaxonID   {
  @JsonProperty("sourceName")
  private String sourceName = null;

  @JsonProperty("taxonId")
  private String taxonId = null;

  public TaxonID sourceName(String sourceName) {
    this.sourceName = sourceName;
    return this;
  }

  /**
   * The human readable name of the taxonomy provider
   * @return sourceName
  **/
  @ApiModelProperty(example = "NCBI", required = true, value = "The human readable name of the taxonomy provider")
      @NotNull

    public String getSourceName() {
    return sourceName;
  }

  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  public TaxonID taxonId(String taxonId) {
    this.taxonId = taxonId;
    return this;
  }

  /**
   * The identifier (name, ID, URI) of a particular taxonomy within the source provider
   * @return taxonId
  **/
  @ApiModelProperty(example = "2026747", required = true, value = "The identifier (name, ID, URI) of a particular taxonomy within the source provider")
      @NotNull

    public String getTaxonId() {
    return taxonId;
  }

  public void setTaxonId(String taxonId) {
    this.taxonId = taxonId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TaxonID taxonID = (TaxonID) o;
    return Objects.equals(this.sourceName, taxonID.sourceName) &&
        Objects.equals(this.taxonId, taxonID.taxonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sourceName, taxonId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TaxonID {\n");
    
    sb.append("    sourceName: ").append(toIndentedString(sourceName)).append("\n");
    sb.append("    taxonId: ").append(toIndentedString(taxonId)).append("\n");
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
