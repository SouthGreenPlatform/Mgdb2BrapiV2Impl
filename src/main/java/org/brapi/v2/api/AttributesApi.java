/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.14).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.brapi.v2.api;

import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.brapi.v2.model.GermplasmAttributeListResponse;
import org.brapi.v2.model.GermplasmAttributeNewRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T11:58:54.700Z[GMT]")
@Api(value = "attributes", description = "the attributes API", tags={ "Germplasm Attributes" })
public interface AttributesApi {

	public static final String attributesGet_url = "attributes";
	
//    @ApiOperation(value = "Get the details for a specific Germplasm Attribute", nickname = "attributesAttributeDbIdGet", notes = "Get the details for a specific Germplasm Attribute", response = GermplasmAttributeSingleResponse.class, authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes", })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeSingleResponse.class),
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = "/attributes/{attributeDbId}",
//        produces = { "application/json" }, 
//        method = RequestMethod.GET)
//    ResponseEntity<GermplasmAttributeSingleResponse> attributesAttributeDbIdGet(@ApiParam(value = "The unique id for an attribute",required=true) @PathVariable("attributeDbId") String attributeDbId,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);


//    @ApiOperation(value = "Create new Germplasm Attributes", nickname = "attributesAttributeDbIdPost", notes = "Create new Germplasm Attributes", response = GermplasmAttributeSingleResponse.class, authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes", })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeSingleResponse.class),
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = "/attributes/{attributeDbId}",
//        produces = { "application/json" }, 
//        consumes = { "application/json" },
//        method = RequestMethod.POST)
//    ResponseEntity<GermplasmAttributeSingleResponse> attributesAttributeDbIdPost(@ApiParam(value = "The unique id for an attribute",required=true) @PathVariable("attributeDbId") String attributeDbId,@ApiParam(value = ""  )  @Valid @RequestBody GermplasmAttributeNewRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);


//    @ApiOperation(value = "Get the Categories of Germplasm Attributes", nickname = "attributesCategoriesGet", notes = "List all available attribute categories.", response = GermplasmAttributeCategoryListResponse.class, authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes", })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeCategoryListResponse.class),
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = "/attributes/categories",
//        produces = { "application/json" }, 
//        method = RequestMethod.GET)
//    ResponseEntity<GermplasmAttributeCategoryListResponse> attributesCategoriesGet(@ApiParam(value = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);


    @ApiOperation(value = "Get the Germplasm Attributes", nickname = "attributesGet", notes = "List available attributes.", response = GermplasmAttributeListResponse.class, authorizations = {
        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + attributesGet_url,
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<GermplasmAttributeListResponse> attributesGet(HttpServletResponse response, @ApiParam(value = "The general category for the attribute. very similar to Trait class.") @Valid @RequestParam(value = "attributeCategory", required = false) String attributeCategory,@ApiParam(value = "The unique id for an attribute") @Valid @RequestParam(value = "attributeDbId", required = false) String attributeDbId,@ApiParam(value = "The human readable name for an attribute") @Valid @RequestParam(value = "attributeName", required = false) String attributeName,@ApiParam(value = "Get all attributes associated with this germplasm") @Valid @RequestParam(value = "germplasmDbId", required = false) String germplasmDbId,@ApiParam(value = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);


    @ApiOperation(value = "Create new Germplasm Attributes", nickname = "attributesPost", notes = "Create new Germplasm Attributes", response = GermplasmAttributeListResponse.class, authorizations = {
        @Authorization(value = "AuthorizationToken")    }, tags={ "Germplasm Attributes", })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = GermplasmAttributeListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + attributesGet_url,
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    ResponseEntity<GermplasmAttributeListResponse> attributesPost(@ApiParam(value = ""  )  @Valid @RequestBody List<GermplasmAttributeNewRequest> body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization);

}
