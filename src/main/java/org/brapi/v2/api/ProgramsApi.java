/**
 * NOTE: This class is auto generated by the swagger code generator program (3.0.25).
 * https://github.com/swagger-api/swagger-codegen
 * Do not edit the class manually.
 */
package org.brapi.v2.api;

import javax.validation.Valid;

import org.brapi.v2.model.ProgramListResponse;
import org.brapi.v2.model.ProgramSearchRequest;
import org.brapi.v2.model.ProgramSingleResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-06-24T08:33:51.198Z[GMT]")
@Api(value = "programs", description = "the programs API", tags={ "Programs" })
public interface ProgramsApi {

	public static final String programsGet_url = "programs";
    public static final String searchProgramsPost_url = "search/programs";
	public static final String programsProgramDbIdGet_url = "programs/{programDbId}";

    @ApiOperation(value = "Returns a filtered list of `Program` objects", notes = "Returns a filtered list of `Program` objects. Empty body accepted", authorizations = { @Authorization(value = "AuthorizationToken")    }, tags={ "Programs" })
        @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "OK", response = ProgramListResponse.class),
//            @ApiResponse(code = 202, message = "Accepted", response = Model202AcceptedSearchResponse.class),
            @ApiResponse(code = 400, message = "Bad Request", response = String.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
            @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
        @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + searchProgramsPost_url,
            produces = { "application/json" }, 
            consumes = { "application/json" }, 
            method = RequestMethod.POST)
        ResponseEntity<ProgramListResponse> searchProgramsPost(@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value="Authorization", required=false) String authorization, @ApiParam @Valid @RequestBody ProgramSearchRequest body);

    @ApiOperation(value = "Get a filtered list of breeding Programs", notes = "Get a filtered list of breeding Programs. This list can be filtered by common crop name to narrow results to a specific crop.", authorizations = {
        @Authorization(value = "AuthorizationToken")    }, tags={ "Programs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ProgramListResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + programsGet_url,
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<ProgramListResponse> programsGet(@ApiParam(value = "Filter by the common crop name. Exact match.") @Valid @RequestParam(value = "commonCropName", required = false) String commonCropName, @ApiParam(value = "Program filter to only return trials associated with given program id.") @Valid @RequestParam(value = "programDbId", required = false) String programDbId, @ApiParam(value = "Filter by program name. Exact match.") @Valid @RequestParam(value = "programName", required = false) String programName, @ApiParam(value = "Filter by program abbreviation. Exact match.") @Valid @RequestParam(value = "abbreviation", required = false) String abbreviation, @ApiParam(value = "An external reference ID. Could be a simple string or a URI. (use with `externalReferenceSource` parameter)") @Valid @RequestParam(value = "externalReferenceID", required = false) String externalReferenceID, @ApiParam(value = "An identifier for the source system or database of an external reference (use with `externalReferenceID` parameter)") @Valid @RequestParam(value = "externalReferenceSource", required = false) String externalReferenceSource, @ApiParam(value = "Used to request a specific page of data to be returned.  The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page, @ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value="Authorization", required=false) String authorization);


//    @ApiOperation(value = "Add new breeding Programs to the database", notes = "Add new breeding Programs to the database. The `programDbId` is set by the server, all other fields are take from the request body, or a default value is used.", authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Programs" })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = ProgramListResponse.class),
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = "/programs",
//        produces = { "application/json" }, 
//        consumes = { "application/json" }, 
//        method = RequestMethod.POST)
//    ResponseEntity<ProgramListResponse> programsPost(@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value="Authorization", required=false) String authorization, @ApiParam(value = "") @Valid @RequestBody List<ProgramNewRequest> body);


    @ApiOperation(value = "Get a breeding Program by Id", notes = "Get a single breeding Program by Id. This can be used to quickly get the details of a Program when you have the Id from another entity.", authorizations = {
        @Authorization(value = "AuthorizationToken")    }, tags={ "Programs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = ProgramSingleResponse.class),
        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
    @RequestMapping(value = ServerinfoApi.URL_BASE_PREFIX + "/" + programsProgramDbIdGet_url,
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    ResponseEntity<ProgramSingleResponse> programsProgramDbIdGet(@ApiParam(value = "Filter by the common crop name. Exact match.", required=true) @PathVariable("programDbId") String programDbId, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value="Authorization", required=false) String authorization);


//    @ApiOperation(value = "Update an existing Program", notes = "Update the details of an existing breeding Program.", authorizations = {
//        @Authorization(value = "AuthorizationToken")    }, tags={ "Programs" })
//    @ApiResponses(value = { 
//        @ApiResponse(code = 200, message = "OK", response = ProgramSingleResponse.class),
//        @ApiResponse(code = 400, message = "Bad Request", response = String.class),
//        @ApiResponse(code = 401, message = "Unauthorized", response = String.class),
//        @ApiResponse(code = 403, message = "Forbidden", response = String.class) })
//    @RequestMapping(value = "/programs/{programDbId}",
//        produces = { "application/json" }, 
//        consumes = { "application/json" }, 
//        method = RequestMethod.PUT)
//    ResponseEntity<ProgramSingleResponse> programsProgramDbIdPut(@ApiParam(value = "Filter by the common crop name. Exact match.", required=true) @PathVariable("programDbId") String programDbId, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>") @RequestHeader(value="Authorization", required=false) String authorization, @ApiParam(value = "") @Valid @RequestBody ProgramNewRequest body);

}

