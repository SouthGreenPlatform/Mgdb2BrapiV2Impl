package org.brapi.v2.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.OntologyTerm;
import org.brapi.v2.model.Reference;
import org.brapi.v2.model.ReferenceListResponse;
import org.brapi.v2.model.ReferenceListResponseResult;
import org.brapi.v2.model.ReferencesSearchRequest;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.SuccessfulSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.cirad.controller.GigwaMethods;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:12.318Z[GMT]")
@CrossOrigin
@Controller
public class ReferencesApiController implements ReferencesApi {

    private static final Logger log = LoggerFactory.getLogger(ReferencesApiController.class);

    @Autowired AbstractTokenManager tokenManager;
    
	@Override
	public ResponseEntity<ReferenceListResponse> referencesGet(String referenceDbId, String referenceSetDbId, String accession, String md5checksum, Boolean isDerived, Integer minLength, Integer maxLength, Integer page, Integer pageSize, String authorization) {
		ReferencesSearchRequest srr = new ReferencesSearchRequest();
		srr.setPageSize(pageSize);
		srr.setPage(pageSize);
		srr.setMinLength(minLength);
		srr.setMaxLength(maxLength);
		if (referenceSetDbId != null)
			srr.setReferenceSetDbIds(Arrays.asList(referenceSetDbId));
		if (referenceDbId != null)
			srr.setReferenceDbIds(Arrays.asList(referenceDbId));
		if (md5checksum != null)
			srr.setMd5checksums(Arrays.asList(md5checksum));
		if (accession != null)
			srr.setAccessions(Arrays.asList(accession));
		return searchReferencesPost(srr, authorization);
	}

	@Override
	public ResponseEntity<ReferenceListResponse> searchReferencesPost(ReferencesSearchRequest body, String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);

    	try {
	    	ReferenceListResponse rlr = new ReferenceListResponse();
	    	ReferenceListResponseResult result = new ReferenceListResponseResult();
			Metadata metadata = new Metadata();
			rlr.setMetadata(metadata);

	    	String sErrorMsg = "";
        	String programDbId = null;
    		Collection<Integer> referenceSetIds = new HashSet<>();
        	Collection<String> referenceIds = new HashSet<>();
	    	boolean fGotReferenceSetIDs = body != null && body.getReferenceSetDbIds() != null && !body.getReferenceSetDbIds().isEmpty();
	    	
	    	if (body.getReferenceDbIds() != null) {
	        	for (String referenceId : body.getReferenceDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(referenceId, 2);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0]))
						sErrorMsg += "You may only supply IDs of reference records from one program at a time!";

					referenceIds.add(info[1]);
	        	}
        	}
	    	else if (fGotReferenceSetIDs) {
	        	for (String referenceSetId : body.getReferenceSetDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(referenceSetId, 2);
					if (programDbId == null)
						programDbId = info[0];
					else if (!programDbId.equals(info[0]))
						sErrorMsg += "You may only supply IDs of referenceSet records from one program at a time!";

		        	int projId = Integer.parseInt(info[1]);
		    		if (tokenManager.canUserReadProject(token, programDbId, projId))
		    			referenceSetIds.add(projId);
		    	}
        	}
        	else
        		sErrorMsg += "You must provide either a list of referenceSetDbIds or a list of referenceDbIds!";
	    	
   			if (!sErrorMsg.isEmpty()) {
				Status status = new Status();
				status.setMessage(sErrorMsg);
				metadata.addStatusItem(status);
				return new ResponseEntity<>(rlr, HttpStatus.BAD_REQUEST);
			}

   			if (referenceIds.isEmpty() && fGotReferenceSetIDs && referenceSetIds.isEmpty()) 
   				return new ResponseEntity<>(rlr, HttpStatus.FORBIDDEN);	// none of them was allowed for this user to see

   			MongoTemplate mongoTemplate = MongoTemplateManager.get(programDbId);
   			Query q = !referenceSetIds.isEmpty() ? new Query(Criteria.where("_id").in(referenceSetIds)) : new Query();
   			for (String seq : mongoTemplate.findDistinct(q, GenotypingProject.FIELDNAME_SEQUENCES, GenotypingProject.class, String.class))
   				if (referenceIds.isEmpty() || referenceIds.contains(seq)) {
   					Reference ref = new Reference();
   					String speciesName = MongoTemplateManager.getSpecies(programDbId);
   					if (speciesName != null) {
	   					OntologyTerm speciesOT = new OntologyTerm();
	   					speciesOT.setTermURI("TAXRANK:0000006");
	   					speciesOT.setTerm(speciesName);
			        	ref.setSpecies(speciesOT);
   					}
			    	ref.setReferenceDbId(programDbId + GigwaMethods.ID_SEPARATOR + seq);
			    	ref.setReferenceName(seq);
			    	result.addDataItem(ref);
   			}

			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
	
			rlr.setResult(result);		
            return new ResponseEntity<ReferenceListResponse>(rlr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<ReferenceListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

//    public ResponseEntity<ReferenceListResponse> referencesGet(@ApiParam(value = "The ID of the `Reference` to be retrieved.") @Valid @RequestParam(value = "referenceDbId", required = false) String referenceDbId,@ApiParam(value = "The ID of the `ReferenceSet` to be retrieved.") @Valid @RequestParam(value = "referenceSetDbId", required = false) String referenceSetDbId,@ApiParam(value = "If unset, return the reference sets for which the `accession` matches this string (case-sensitive, exact match).") @Valid @RequestParam(value = "accession", required = false) String accession,@ApiParam(value = "If specified, return the references for which the `md5checksum` matches this string (case-sensitive, exact match). See `Reference::md5checksum` for details.") @Valid @RequestParam(value = "md5checksum", required = false) String md5checksum,@ApiParam(value = "If the reference is derived from a source sequence") @Valid @RequestParam(value = "isDerived", required = false) Boolean isDerived,@ApiParam(value = "The minimum length of the reference sequences to be retrieved.") @Valid @RequestParam(value = "minLength", required = false) Integer minLength,@ApiParam(value = "The maximum length of the reference sequences to be retrieved.") @Valid @RequestParam(value = "maxLength", required = false) Integer maxLength,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<ReferenceListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"referenceDbId\" : \"referenceDbId\",\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"length\" : \"length\",\n      \"sourceDivergence\" : 0.8008282,\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ],\n      \"referenceName\" : \"referenceName\"\n    }, {\n      \"referenceDbId\" : \"referenceDbId\",\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"length\" : \"length\",\n      \"sourceDivergence\" : 0.8008282,\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ],\n      \"referenceName\" : \"referenceName\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", ReferenceListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<ReferenceBasesResponse> referencesReferenceDbIdBasesGet(@ApiParam(value = "The ID of the `Reference` to be retrieved.",required=true) @PathVariable("referenceDbId") String referenceDbId,@ApiParam(value = "The start position (0-based) of this query. Defaults to 0. Genomic positions are non-negative integers less than reference length. Requests spanning the join of circular genomes are represented as two requests one on each side of the join (position 0).") @Valid @RequestParam(value = "start", required = false) Long start,@ApiParam(value = "The end position (0-based, exclusive) of this query. Defaults to the length of this `Reference`.") @Valid @RequestParam(value = "end", required = false) Long end,@ApiParam(value = "The continuation token, which is used to page through large result sets. To get the next page of results, set this parameter to the value of `next_page_token` from the previous response.") @Valid @RequestParam(value = "pageToken", required = false) String pageToken,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<ReferenceBasesResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"sequence\" : \"sequence\",\n    \"offset\" : \"offset\",\n    \"nextPageToken\" : \"nextPageToken\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", ReferenceBasesResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceBasesResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<ReferenceResponse> referencesReferenceDbIdGet(@ApiParam(value = "The ID of the `Reference` to be retrieved.",required=true) @PathVariable("referenceDbId") String referenceDbId) {
//        try {
//            return new ResponseEntity<ReferenceResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"referenceDbId\" : \"referenceDbId\",\n    \"isDerived\" : true,\n    \"sourceURI\" : \"sourceURI\",\n    \"species\" : {\n      \"termURI\" : \"termURI\",\n      \"term\" : \"term\"\n    },\n    \"md5checksum\" : \"md5checksum\",\n    \"additionalInfo\" : {\n      \"key\" : \"additionalInfo\"\n    },\n    \"length\" : \"length\",\n    \"sourceDivergence\" : 0.8008282,\n    \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ],\n    \"referenceName\" : \"referenceName\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", ReferenceResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

}
