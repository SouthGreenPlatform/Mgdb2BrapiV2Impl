package org.brapi.v2.api;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.StringUtils;
import org.brapi.v2.api.cache.MongoBrapiCache;
import org.brapi.v2.model.Call;
import org.brapi.v2.model.CallListResponse;
import org.brapi.v2.model.CallSet;
import org.brapi.v2.model.CallSetsListResponse;
import org.brapi.v2.model.CallSetsListResponseResult;
import org.brapi.v2.model.CallSetsSearchRequest;
import org.brapi.v2.model.CallsListResponseResult;
import org.brapi.v2.model.CallsSearchRequest;
import org.brapi.v2.model.Germplasm;
import org.brapi.v2.model.GermplasmListResponse;
import org.brapi.v2.model.GermplasmListResponseResult;
import org.brapi.v2.model.GermplasmMCPD;
import org.brapi.v2.model.GermplasmNewRequest.BiologicalStatusOfAccessionCodeEnum;
import org.bson.Document;
import org.brapi.v2.model.GermplasmSearchRequest;
import org.brapi.v2.model.IndexPagination;
import org.brapi.v2.model.ListValue;
import org.brapi.v2.model.Metadata;
import org.brapi.v2.model.MetadataTokenPagination;
import org.brapi.v2.model.Sample;
import org.brapi.v2.model.SampleListResponse;
import org.brapi.v2.model.SampleListResponseResult;
import org.brapi.v2.model.SampleSearchRequest;
import org.brapi.v2.model.Status;
import org.brapi.v2.model.Study;
import org.brapi.v2.model.StudyListResponse;
import org.brapi.v2.model.StudyListResponseResult;
import org.brapi.v2.model.StudySearchRequest;
import org.brapi.v2.model.TokenPagination;
import org.brapi.v2.model.Variant;
import org.brapi.v2.model.VariantListResponse;
import org.brapi.v2.model.VariantListResponseResult;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetListResponse;
import org.brapi.v2.model.VariantSetListResponseResult;
import org.brapi.v2.model.VariantSetsSearchRequest;
import org.brapi.v2.model.VariantsSearchRequest;
import org.ga4gh.methods.GAException;
import org.ga4gh.methods.SearchVariantSetsRequest;
import org.ga4gh.methods.SearchVariantsRequest;
import org.ga4gh.models.VariantSetMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeParseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCursor;

import fr.cirad.controller.GigwaMethods;
import fr.cirad.io.brapi.BrapiService;
import fr.cirad.mgdb.model.mongo.maintypes.CustomIndividualMetadata;
import fr.cirad.mgdb.model.mongo.maintypes.CustomIndividualMetadata.CustomIndividualMetadataId;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingProject;
import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.Individual;
import fr.cirad.mgdb.model.mongo.maintypes.VariantData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.model.mongo.subtypes.AbstractVariantData;
import fr.cirad.mgdb.model.mongo.subtypes.ReferencePosition;
import fr.cirad.mgdb.model.mongo.subtypes.SampleGenotype;
import fr.cirad.mgdb.model.mongodao.MgdbDao;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.model.GigwaSearchVariantsRequest;
import fr.cirad.model.GigwaSearchVariantsResponse;
import fr.cirad.tools.mongo.MongoTemplateManager;
import fr.cirad.tools.security.base.AbstractTokenManager;
import fr.cirad.web.controller.rest.BrapiRestController;
import io.swagger.annotations.ApiParam;
import jhi.brapi.api.germplasm.BrapiGermplasm;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-11-19T12:30:08.794Z[GMT]")
@CrossOrigin
@RestController
public class SearchApiController implements SearchApi {

    private static final Logger log = LoggerFactory.getLogger(SearchApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;
    
    @Autowired private GigwaGa4ghServiceImpl ga4ghService;
    
    @Autowired private BrapiRestController brapiV1Service;
    
    @Autowired AbstractTokenManager tokenManager;
    
    @Autowired MongoBrapiCache cache;

    @org.springframework.beans.factory.annotation.Autowired
    public SearchApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

	@Override
	public ResponseEntity<CallListResponse> searchCallsPost(CallsSearchRequest body, String authorization) throws SocketException, UnknownHostException, UnsupportedEncodingException {
		String token = ServerinfoApiController.readToken(authorization);
			
    	CallListResponse clr = new CallListResponse();
    	CallsListResponseResult result = new CallsListResponseResult();
    	MetadataTokenPagination metadata = new MetadataTokenPagination();
		clr.setMetadata(metadata);

		boolean fGotVariantSetList = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
		boolean fGotVariantList = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
		boolean fGotCallSetList = body.getCallSetDbIds() != null && !body.getCallSetDbIds().isEmpty();
		if (!fGotVariantSetList && !fGotVariantList && !fGotCallSetList) {
			Status status = new Status();
			status.setMessage("You must specify at least one of callSetDbId, variantDbId, or variantSetDbId!");
			metadata.addStatusItem(status);
			return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);
		}

		String module = null;
		
		if (fGotVariantSetList) {
			for (String variantDbId : body.getVariantSetDbIds()) {
				if (module == null)
					module = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 3)[0];
				else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 3)[0])) {
					Status status = new Status();
					status.setMessage("You must specify VariantSets belonging to the same referenceSet!");
					metadata.addStatusItem(status);
					return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);

				}
			}
		}
		
		if (fGotVariantList) {
			for (String variantDbId : body.getVariantDbIds()) {
				if (module == null)
					module = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2)[0];
				else if (!module.equals(GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2)[0])) {
					Status status = new Status();
					status.setMessage("You may specify VariantSets / Variants only belonging to the same referenceSet!");
					metadata.addStatusItem(status);
					return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);

				}
			}
		}

    	HashMap<Integer, String> sampleIndividuals = new HashMap<>();	// we are going to need the individual each sample is related to, in order to build callSetDbIds
		if (fGotCallSetList) {
			for (String callSetDbId : body.getCallSetDbIds()) {
				String[] info = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
				if (module == null)
					module = info[0];
				else if (!module.equals(info[0])) {
					Status status = new Status();
					status.setMessage("You may specify VariantSets / Variants / CallSets only belonging to the same referenceSet!");
					metadata.addStatusItem(status);
					return new ResponseEntity<>(clr, HttpStatus.BAD_REQUEST);
				}
				sampleIndividuals.put(Integer.parseInt(info[2]), info[1]);
			}
			
			// identify the runs those samples are involved in
			body.setVariantSetDbIds(new ArrayList<>());
			for (GenotypingSample sp : MongoTemplateManager.get(module).find(new Query(Criteria.where("_id").in(sampleIndividuals.keySet())), GenotypingSample.class)) {
				String variantSetDbId = module + GigwaMethods.ID_SEPARATOR + sp.getProjectId() + GigwaMethods.ID_SEPARATOR + sp.getRun();
				if (!body.getVariantSetDbIds().contains(variantSetDbId)) {
					body.getVariantSetDbIds().add(variantSetDbId);
					fGotVariantSetList = true;
				}
			}
		}

		MongoTemplate mongoTemplate = MongoTemplateManager.get(module);

    	// check permissions
    	Collection<Integer> projectIDs = fGotVariantSetList ? body.getVariantSetDbIds().stream().map(vsId -> Integer.parseInt(GigwaSearchVariantsRequest.getInfoFromId(vsId, 3)[1])).collect(Collectors.toSet()) :
    		mongoTemplate.findDistinct(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(GigwaMethods.ID_SEPARATOR))).collect(Collectors.toList()))), "_id." + VariantRunDataId.FIELDNAME_PROJECT_ID, VariantRunData.class, Integer.class);
    	List<Integer> forbiddenProjectIDs = new ArrayList<>();
		for (int pj : projectIDs)
			if (!tokenManager.canUserReadProject(token, module, pj))
				forbiddenProjectIDs.add(pj);
		projectIDs.removeAll(forbiddenProjectIDs);
		if (projectIDs.isEmpty() && !forbiddenProjectIDs.isEmpty())
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);

		List<Criteria> crits = new ArrayList<>();
		if (fGotVariantSetList) {
			List<Criteria> vsCrits = new ArrayList<>();
			for (String vsId : body.getVariantSetDbIds()) {
				String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
				vsCrits.add(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(info[2])));
			}
			crits.add(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
		}
		
		if (fGotVariantList) {
			List<String> varIDs = body.getVariantDbIds().stream().map(varDbId -> varDbId.substring(1 + varDbId.indexOf(GigwaMethods.ID_SEPARATOR))).collect(Collectors.toList());
			crits.add(Criteria.where("_id." + VariantRunDataId.FIELDNAME_VARIANT_ID).in(varIDs));
		}

		Query runQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));

    	// now deal with samples
		if (fGotCallSetList) {	// project necessary fields to get only the required genotypes
			runQuery.fields().include(VariantRunData.FIELDNAME_KNOWN_ALLELE_LIST);
			for (String callSetDbId : body.getCallSetDbIds()) {
				String[] splitCallSetDbId = GigwaSearchVariantsRequest.getInfoFromId(callSetDbId, 3);
				runQuery.fields().include(VariantRunData.FIELDNAME_SAMPLEGENOTYPES + "." + splitCallSetDbId[2]);
			}
		}
		else {	// find out which samples are involved and keep track of corresponding individuals
	    	Query sampleQuery;
			if (fGotVariantSetList) {
				List<Criteria> vsCrits = new ArrayList<>();
				for (String vsId : body.getVariantSetDbIds()) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(vsId, 3);
					vsCrits.add(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(Integer.parseInt(info[1])), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(info[2])));
				}
				sampleQuery = new Query(new Criteria().orOperator(vsCrits.toArray(new Criteria[vsCrits.size()])));
			}
			else
				sampleQuery = new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).in(projectIDs));	// we only had a list of variants as input so all we can filter on is the list of projects thery are involved in
	
        	for (GenotypingSample gs : mongoTemplate.find(sampleQuery, GenotypingSample.class))
        		sampleIndividuals.put(gs.getId(), gs.getIndividual());
		}

        int page = body.getPageToken() == null ? 0 : Integer.parseInt(body.getPageToken());
		int theoriticalPageSize = body.getPageSize() == null || body.getPageSize() > VariantsApi.MAX_CALL_MATRIX_SIZE ? VariantsApi.MAX_CALL_MATRIX_SIZE : body.getPageSize();
        int numberOfMarkersPerPage = (int) Math.ceil(1f * theoriticalPageSize / sampleIndividuals.size());
        Integer nTotalMarkerCount = fGotVariantList ? body.getVariantDbIds().size() : null;
        if (nTotalMarkerCount == null) {	// we don't have a definite variant list: see if we can guess it (only possible for single-run projects since there is no run index on VariantRunData)
        	if (mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where("_id").in(projectIDs), Criteria.where(GenotypingProject.FIELDNAME_RUNS + ".1").exists(false))), GenotypingProject.class) == projectIDs.size())
        		nTotalMarkerCount = (int) mongoTemplate.count(new Query(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).in(projectIDs)), VariantRunData.class);
        }
        
    	String unknownGtCode = body.getUnknownString() == null ? "-" : body.getUnknownString();
    	String phasedSeparator = body.getSepPhased() == null ? "|" : URLDecoder.decode(body.getSepPhased(), "UTF-8");
    	String unPhasedSeparator = body.getSepUnphased() == null ? "/" : body.getSepUnphased();
    	result.setSepUnphased(unPhasedSeparator);

        try {
        	List<AbstractVariantData> varList = VariantsApiController.getSortedVariantListChunk(mongoTemplate, VariantRunData.class, runQuery, page * numberOfMarkersPerPage, numberOfMarkersPerPage);
        	HashMap<Integer, String> previousPhasingIds = new HashMap<>();

        	HashSet<String> distinctVariantIDs = new HashSet<>();
        	for (AbstractVariantData v : varList) {
        		VariantRunData vrd = (VariantRunData) v;
        		distinctVariantIDs.add(v.getVariantId());
        		for (Integer spId : vrd.getSampleGenotypes().keySet()) {
        			SampleGenotype sg = vrd.getSampleGenotypes().get(spId);
					String currentPhId = (String) sg.getAdditionalInfo().get(VariantData.GT_FIELD_PHASED_ID);
					boolean fPhased = currentPhId != null && currentPhId.equals(previousPhasingIds.get(spId));
					previousPhasingIds.put(spId, currentPhId == null ? vrd.getId().getVariantId() : currentPhId);	/*FIXME: check that phasing data is correctly exported*/

					String gtCode = sg.getCode(), genotype;
					if (gtCode == null || gtCode.length() == 0)
						genotype = unknownGtCode;
					else
					{
						List<String> alleles = vrd.getAllelesFromGenotypeCode(gtCode);
						if (!Boolean.TRUE.equals(body.isExpandHomozygotes()) && new HashSet<String>(alleles).size() == 1)
							genotype = alleles.get(0);
						else
							genotype = StringUtils.join(alleles, fPhased ? phasedSeparator : unPhasedSeparator);
					}
        			Call call = new Call();
        			ListValue lv = new ListValue();
        			lv.addValuesItem(genotype);
        			call.setGenotype(lv);
        			call.setVariantDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + vrd.getId().getVariantId());
        			call.setVariantName(call.getVariantDbId());
        			call.setCallSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sampleIndividuals.get(spId) + GigwaGa4ghServiceImpl.ID_SEPARATOR + spId);
        			call.setCallSetName(call.getCallSetDbId());
        			for (String key : sg.getAdditionalInfo().keySet())
        				call.putAdditionalInfoItem(key, sg.getAdditionalInfo().get(key).toString());
                	result.addDataItem(call);
        		}
        	}

        	int nNextPage = page + 1;
        	TokenPagination pagination = new TokenPagination();
			pagination.setPageSize(numberOfMarkersPerPage * sampleIndividuals.size());
			if (nTotalMarkerCount != null) {
				pagination.setTotalCount(nTotalMarkerCount);
				pagination.setTotalPages(varList.isEmpty() ? 0 : (int) Math.ceil((float) pagination.getTotalCount() / pagination.getPageSize()));
			}
			
			pagination.setCurrentPageToken("" + page);
			if ((pagination.getTotalPages() != null && nNextPage < pagination.getTotalPages()) || varList.size() > 0)
				pagination.setNextPageToken("" + nNextPage);
			if (page > 0)
				pagination.setPrevPageToken("" + (page - 1));
			metadata.setPagination(pagination);
		
			clr.setResult(result);
			return new ResponseEntity<CallListResponse>(clr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

//    public ResponseEntity<CallListResponse> searchCallsSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    }, {\n      \"genotype_likelihood\" : [ 0.8008281904610115, 0.8008281904610115 ],\n      \"phaseset\" : \"phaseset\",\n      \"callSetName\" : \"callSetName\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"variantDbId\" : \"variantDbId\",\n      \"variantName\" : \"variantName\",\n      \"genotype\" : {\n        \"values\" : [ \"\", \"\" ]\n      }\n    } ],\n    \"unknownString\" : \"unknownString\",\n    \"expandHomozygotes\" : true,\n    \"sepPhased\" : \"sepPhased\",\n    \"sepUnphased\" : \"sepUnphased\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    public ResponseEntity<CallSetsListResponse> searchCallsetsPost(	@ApiParam(value = "CallSet Search request")  @Valid @RequestBody CallSetsSearchRequest body,
    																@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
    	Authentication auth = tokenManager.getAuthenticationFromToken(token);
    	String sCurrentUser = auth == null || "anonymousUser".equals(auth.getName()) ? "anonymousUser" : auth.getName();
    	
        try {
    		Status status = new Status();
    		HttpStatus httpCode = null;
    		
        	CallSetsListResponse cslr = new CallSetsListResponse();
        	CallSetsListResponseResult result = new CallSetsListResponseResult();
			Metadata metadata = new Metadata();
			cslr.setMetadata(metadata);
			
        	boolean fTriedToAccessForbiddenData = false;
        	HashMap<String /*module*/, Query> sampleQueryByModule = new HashMap<>();
        	
			if ((body.getCallSetDbIds() == null || body.getCallSetDbIds().isEmpty()) && (body.getVariantSetDbIds() == null || body.getVariantSetDbIds().isEmpty())) {
				status.setMessage("Some callSetDbIds or variantSetDbIds must be specified as parameter!");
				metadata.addStatusItem(status);
				httpCode = HttpStatus.BAD_REQUEST;
			}
			else {
				if (body.getVariantSetDbIds() == null || body.getVariantSetDbIds().isEmpty()) {	// no variantSets specified, but we have a list of callSets
		        	HashMap<String /*module*/, HashSet<Integer> /*samples, null means all*/> samplesByModule = new HashMap<>();
					for (String csId : body.getCallSetDbIds()) {
						String[] info = GigwaSearchVariantsRequest.getInfoFromId(csId, 3);
						HashSet<Integer> moduleSamples = samplesByModule.get(info[0]);
						if (moduleSamples == null) {
							moduleSamples = new HashSet<>();
							samplesByModule.put(info[0], moduleSamples);
						}
						moduleSamples.add(Integer.parseInt(info[2]));
					}
		        	for (String module : samplesByModule.keySet()) { // make sure we filter out any samples that are from projects the user is not allowed to see
			        	MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
			        	HashSet<Integer> moduleSamples = samplesByModule.get(module);
			        	Query query = new Query(Criteria.where("_id").in(moduleSamples));
			        	HashMap<Integer, Boolean> projectAccessPermissions = new HashMap<>();
			        	for (GenotypingSample sample : mongoTemplate.find(query, GenotypingSample.class)) {
			        		Boolean fPjAllowed = projectAccessPermissions.get(sample.getProjectId());
			        		if (fPjAllowed == null) {
			        			fPjAllowed = tokenManager.canUserReadProject(token, module, sample.getProjectId());
			        			projectAccessPermissions.put(sample.getProjectId(), fPjAllowed);
			        		}
		            		if (!fPjAllowed) {
		            			fTriedToAccessForbiddenData = true;
		            			moduleSamples.remove(sample.getId());
		            		}
			        	}

			        	if (moduleSamples.size() > 0)
			        		sampleQueryByModule.put(module, query);
		        	}
				}
				else
					for (String variantSetDbId : body.getVariantSetDbIds()) {
						String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 3);
			        	int projId = Integer.parseInt(info[1]);
		    			if (tokenManager.canUserReadProject(token, info[0], projId))
			    			sampleQueryByModule.put(info[0], new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(info[2]))));
		    			else
		    				fTriedToAccessForbiddenData = true;
					}

				int nTotalCallSetsEncountered = 0;
				for (String module : sampleQueryByModule.keySet()) {
		        	MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
    				Map<String, Integer> indIdToSampleIdMap = new HashMap<>();
    				List<GenotypingSample> samples = mongoTemplate.find(sampleQueryByModule.get(module), GenotypingSample.class);
    				for (GenotypingSample sample : samples)
    					indIdToSampleIdMap.put(sample.getIndividual(), sample.getId());

    				// attach individual metadata to samples
    				Query q = new Query(Criteria.where("_id").in(indIdToSampleIdMap.keySet()));
    				q.with(Sort.by(Sort.Direction.ASC, "_id"));
    				List<Individual> listInd = mongoTemplate.find(q, Individual.class);
    				q = new Query(Criteria.where("_id." + CustomIndividualMetadataId.FIELDNAME_USER).is(sCurrentUser));
    				List<CustomIndividualMetadata> cimdList = mongoTemplate.find(q, CustomIndividualMetadata.class);
    				if(!cimdList.isEmpty()) {
    					HashMap<String /* indivID */, HashMap<String, Comparable> /* additional info */> indMetadataByIdMap = new HashMap<>();
    					for (CustomIndividualMetadata cimd : cimdList)
    						indMetadataByIdMap.put(cimd.getId().getIndividualId(), cimd.getAdditionalInfo());
    					
    					for( int i=0 ; i<listInd.size(); i++) {
    						String indId = listInd.get(i).getId();
    						HashMap<String, Comparable>  ai = indMetadataByIdMap.get(indId);
    		                if(ai != null && !ai.isEmpty())
    		                	listInd.get(i).getAdditionalInfo().putAll(ai);
    					}
    				}
    				
					for (int i=0; i<samples.size(); i++) {
						GenotypingSample sample = samples.get(i);
						nTotalCallSetsEncountered++;
		            	CallSet callset = new CallSet();
		            	callset.setCallSetDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getIndividual() + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getId());
		            	callset.setCallSetName(callset.getCallSetDbId());
		            	callset.setSampleDbId(callset.getCallSetDbId());
			            callset.setVariantSetIds(Arrays.asList(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getProjectId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getRun()));
            			final Individual ind = listInd.get(i);
            			if (!ind.getAdditionalInfo().isEmpty())
            				callset.setAdditionalInfo(ind.getAdditionalInfo().keySet().stream().collect(Collectors.toMap(k -> k, k -> (List<String>) Arrays.asList(ind.getAdditionalInfo().get(k).toString()))));
	            		result.addDataItem(callset);
					}

				}

	        	if (nTotalCallSetsEncountered == 0 && fTriedToAccessForbiddenData)
	        		httpCode = HttpStatus.FORBIDDEN;
	        	else {
	    			IndexPagination pagination = new IndexPagination();
	    			pagination.setPageSize(result.getData().size());
	    			pagination.setCurrentPage(body.getPage());
	    			pagination.setTotalPages(1);
	    			pagination.setTotalCount(result.getData().size());
	    			metadata.setPagination(pagination);
	        	}
			}

			cslr.setResult(result);
            return new ResponseEntity<>(cslr, httpCode == null ? HttpStatus.OK : httpCode);

        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    public ResponseEntity<CallSetsListResponse> searchCallsetsSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<CallSetsListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    }, {\n      \"sampleDbId\" : \"sampleDbId\",\n      \"callSetName\" : \"callSetName\",\n      \"created\" : \"created\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"callSetDbId\" : \"callSetDbId\",\n      \"updated\" : \"updated\",\n      \"variantSetIds\" : [ \"variantSetIds\", \"variantSetIds\" ],\n      \"studyDbId\" : \"studyDbId\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", CallSetsListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<CallSetsListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<SuccessfulSearchResponse> searchMarkerpositionsPost(@ApiParam(value = ""  )  @Valid @RequestBody MarkerPositionSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<SuccessfulSearchResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"searchResultsDbId\" : \"551ae08c\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SuccessfulSearchResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<SuccessfulSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<MarkerPositionListResponse> searchMarkerpositionsSearchResultsDbIdPost(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<MarkerPositionListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"mapDbId\" : \"3d52bdf3\",\n      \"linkageGroupName\" : \"Chromosome 3\",\n      \"markerDbId\" : \"a1eb250a\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"mapName\" : \"Genome Map 1\",\n      \"position\" : 2390,\n      \"markerName\" : \"Marker_2390\"\n    }, {\n      \"mapDbId\" : \"3d52bdf3\",\n      \"linkageGroupName\" : \"Chromosome 3\",\n      \"markerDbId\" : \"a1eb250a\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"mapName\" : \"Genome Map 1\",\n      \"position\" : 2390,\n      \"markerName\" : \"Marker_2390\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", MarkerPositionListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<MarkerPositionListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//public ResponseEntity<SuccessfulSearchResponse> searchReferencesPost(@ApiParam(value = "References Search request"  )  @Valid @RequestBody SearchReferencesRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<SuccessfulSearchResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"searchResultsDbId\" : \"551ae08c\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SuccessfulSearchResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<SuccessfulSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<ReferenceListResponse> searchReferencesSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<ReferenceListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"referenceDbId\" : \"referenceDbId\",\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"length\" : \"length\",\n      \"sourceDivergence\" : 0.8008282,\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ],\n      \"referenceName\" : \"referenceName\"\n    }, {\n      \"referenceDbId\" : \"referenceDbId\",\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"length\" : \"length\",\n      \"sourceDivergence\" : 0.8008282,\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ],\n      \"referenceName\" : \"referenceName\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", ReferenceListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<SuccessfulSearchResponse> searchReferencesetsPost(@ApiParam(value = "" ,required=true )  @Valid @RequestBody SearchReferenceSetsRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//    	try {
//            return new ResponseEntity<SuccessfulSearchResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"searchResultsDbId\" : \"551ae08c\"\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SuccessfulSearchResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<SuccessfulSearchResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    public ResponseEntity<ReferenceListResponse1> searchReferencesetsSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<ReferenceListResponse1>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"assemblyPUI\" : \"assemblyPUI\",\n      \"description\" : \"description\",\n      \"referenceSetDbId\" : \"referenceSetDbId\",\n      \"referenceSetName\" : \"referenceSetName\",\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ]\n    }, {\n      \"isDerived\" : true,\n      \"sourceURI\" : \"sourceURI\",\n      \"species\" : {\n        \"termURI\" : \"termURI\",\n        \"term\" : \"term\"\n      },\n      \"md5checksum\" : \"md5checksum\",\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"assemblyPUI\" : \"assemblyPUI\",\n      \"description\" : \"description\",\n      \"referenceSetDbId\" : \"referenceSetDbId\",\n      \"referenceSetName\" : \"referenceSetName\",\n      \"sourceAccessions\" : [ \"sourceAccessions\", \"sourceAccessions\" ]\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", ReferenceListResponse1.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<ReferenceListResponse1>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    private String integerToString(Integer n) {
		return n == null ? null : String.valueOf(n);
	}

	public ResponseEntity<SampleListResponse> searchSamplesPost(@ApiParam(value = "")  @Valid @RequestBody SampleSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);

        try {
        	SampleListResponse slr = new SampleListResponse();
        	SampleListResponseResult result = new SampleListResponseResult();
        	String refSetDbId = null;
        	Integer projId = null;
        	Collection<Integer> sampleIds = new HashSet<>();
			Metadata metadata = new Metadata();
			slr.setMetadata(metadata);
			String sErrorMsg = "";

        	if ((body.getObservationUnitDbIds() != null && body.getObservationUnitDbIds().size() > 0) || (body.getPlateDbIds() != null && body.getPlateDbIds().size() > 0))
        		sErrorMsg += "Searching by Plate or ObservationUnit is not supported! ";
        	else if (body.getStudyDbIds() != null) {
        		Collection<String> studyIds = new HashSet<>();
	        	for (String studyId : body.getStudyDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(studyId, 2);
					if (refSetDbId == null)
						refSetDbId = info[0];
					else if (!refSetDbId.equals(info[0]))
						sErrorMsg += "You may only ask for sample records from one referenceSet at a time!";
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1])))
						sErrorMsg += "You may only ask for germplasm records from one study at a time!";
					studyIds.add(info[1]);
	        	}
	        	sampleIds = MgdbDao.getSamplesForProject(refSetDbId, projId, null).stream().map(sp -> sp.getId()).collect(Collectors.toList());
        	}
        	else if (body.getGermplasmDbIds() != null) {
        		Collection<String> germplasmIds = new HashSet<>();
	        	for (String gpId : body.getGermplasmDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 3);
					if (refSetDbId == null)
						refSetDbId = info[0];
					else if (!refSetDbId.equals(info[0]))
						sErrorMsg += "You may only ask for sample records from one referenceSet at a time!";
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1])))
						sErrorMsg += "You may only ask for germplasm records from one study at a time!";
					germplasmIds.add(info[2]);
	        	}
	        	sampleIds = MgdbDao.getSamplesForProject(refSetDbId, projId, germplasmIds).stream().map(sp -> sp.getId()).collect(Collectors.toList());
        	}
        	else if (body.getSampleDbIds() != null)
	        	for (String spId : body.getSampleDbIds()) {
	        		String[] info = GigwaSearchVariantsRequest.getInfoFromId(spId, 4);
					if (refSetDbId == null)
						refSetDbId = info[0];
					else if (!refSetDbId.equals(info[0]))
						sErrorMsg += "You may only ask for sample records from one referenceSet at a time!";
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1])))
						sErrorMsg += "You may only ask for sample records from one study at a time!";
					sampleIds.add(Integer.parseInt(info[3]));
	        	}
        	else
        		sErrorMsg += "You must provide either a list of germplasmDbIds or a list of sampleDbIds!";

   			if (!sErrorMsg.isEmpty()) {
				Status status = new Status();
				status.setMessage(sErrorMsg);
				metadata.addStatusItem(status);
				return new ResponseEntity<>(slr, HttpStatus.BAD_REQUEST);
			}

   			if (!tokenManager.canUserReadProject(token, refSetDbId, projId))
   				return new ResponseEntity<SampleListResponse>(HttpStatus.FORBIDDEN);

        	MongoTemplate mongoTemplate = MongoTemplateManager.get(refSetDbId);
        	Query q = new Query(Criteria.where("_id").in(sampleIds));
        	long count = mongoTemplate.count(q, GenotypingSample.class);
            if (body.getPageSize() != null) {
            	q.limit(body.getPageSize());
                if (body.getPage() != null)
                	q.skip(body.getPage() * body.getPageSize());
            }
            
            List<GenotypingSample> genotypingSamples = mongoTemplate.find(q, GenotypingSample.class);
        	for (GenotypingSample mgdbSample : genotypingSamples) {
        		Sample sample = new Sample();
        		sample.sampleDbId(ga4ghService.createId(refSetDbId, projId, mgdbSample.getIndividual(), mgdbSample.getId()));
        		sample.germplasmDbId(ga4ghService.createId(refSetDbId, projId, mgdbSample.getIndividual()));
        		sample.setSampleName(mgdbSample.getSampleName());
        		sample.studyDbId(refSetDbId + GigwaMethods.ID_SEPARATOR + projId);
        		result.addDataItem(sample);
        	}

			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(body.getPageSize());
			pagination.setCurrentPage(body.getPage());
			pagination.setTotalPages(body.getPageSize() == null ? 1 : (int) Math.ceil((float) count / body.getPageSize()));
			pagination.setTotalCount((int) count);
			metadata.setPagination(pagination);
        	
			slr.setResult(result);
            return new ResponseEntity<SampleListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<SampleListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    public ResponseEntity<SampleListResponse> searchSamplesSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {		
//            return new ResponseEntity<SampleListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", SampleListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<SampleListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
    public ResponseEntity<VariantListResponse> searchVariantsPost(@ApiParam(value = "Variant Search request") @Valid @RequestBody VariantsSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	boolean fGotVariants = body.getVariantDbIds() != null && !body.getVariantDbIds().isEmpty();
    	boolean fGotVariantSets = body.getVariantSetDbIds() != null && !body.getVariantSetDbIds().isEmpty();
    	boolean fGotRefDbId = body.getReferenceDbId() != null && !body.getReferenceDbId().isEmpty();

		String token = ServerinfoApiController.readToken(authorization);

		String module = null;
		int projId;
		Query varQuery = null, runQuery = null;

		VariantListResponseResult result = new VariantListResponseResult();
		VariantListResponse vlr = new VariantListResponse();
    	MetadataTokenPagination metadata = new MetadataTokenPagination();
		vlr.setMetadata(metadata);
		vlr.setResult(result);
		Status status = new Status();

        if (body.getPageSize() == null || body.getPageSize() > VariantsApi.MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE)
        	body.setPageSize(VariantsApi.MAX_SUPPORTED_VARIANT_COUNT_PER_PAGE);
        int page = body.getPageToken() == null ? 0 : Integer.parseInt(body.getPageToken());

		try {
			if (fGotVariants) {
				HashSet<String> variantIDs = new HashSet<>();
				for (String variantDbId : body.getVariantDbIds()) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantDbId, 2);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only ask for variant records from one referenceSet at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
					}
					module = info[0];
					variantIDs.add(info[1]);
				}
				varQuery = new Query(Criteria.where("_id").in(variantIDs));
			}
	    	else if (fGotRefDbId) {
	        	String[] info = GigwaSearchVariantsRequest.getInfoFromId(body.getReferenceDbId(), 2);
	        	module = info[0];
	    		List<Criteria> crits = new ArrayList<>();	    		
	        	crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_SEQUENCE).is(info[1]));
	    		if (body.getStart() != null)
	        		crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE).gte(body.getStart()));
	    		if (body.getEnd() != null)
	        		crits.add(Criteria.where(VariantData.FIELDNAME_REFERENCE_POSITION + "." + ReferencePosition.FIELDNAME_START_SITE).lte(body.getEnd()));
	    		varQuery = new Query(new Criteria().andOperator(crits.toArray(new Criteria[crits.size()])));
	    	}
			else if (fGotVariantSets) {
				HashMap<Integer, Set<String>> runsByProject = new HashMap<>();
		    	for (String variantSetDbId : body.getVariantSetDbIds()) {
		    		String[] info = GigwaSearchVariantsRequest.getInfoFromId(variantSetDbId, 3);
					if (module != null && !module.equals(info[0])) {
						status.setMessage("You may only ask for variantSet records from one referenceSet at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
					}
					module = info[0];

					projId = Integer.parseInt(info[1]);
					if (!tokenManager.canUserReadProject(token, info[0], info[1])) {
						status.setMessage("You are not allowed to access this content");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(vlr, HttpStatus.FORBIDDEN);
					}
					
					Set<String> projectRuns = runsByProject.get(projId);
					if (projectRuns == null) {
						projectRuns = new HashSet<>();
						runsByProject.put(projId, projectRuns);
					}
					projectRuns.add(info[2]);
		    	}
		    	
		    	List<Criteria> orCrits = new ArrayList<>();
		    	for (Integer proj : runsByProject.keySet())
		    		orCrits.add(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(proj), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).in(runsByProject.get(proj))));
		    	runQuery = new Query(new Criteria().orOperator(orCrits.toArray(new Criteria[orCrits.size()])));
				runQuery.fields().exclude(VariantRunData.FIELDNAME_SAMPLEGENOTYPES);
			}
			
			if (!tokenManager.canUserReadDB(token, module)) {
				status.setMessage("You are not allowed to access this content");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.FORBIDDEN);
			}
			
			List<AbstractVariantData> varList;
			if (varQuery != null) {
				varQuery.limit(body.getPageSize());
				if (page > 0)
					varQuery.skip(page * body.getPageSize());
				varList = IteratorUtils.toList(MongoTemplateManager.get(module).find(varQuery, VariantData.class).iterator());
			}
			else if (runQuery != null)
	        	varList = VariantsApiController.getSortedVariantListChunk(MongoTemplateManager.get(module), fGotVariants ? VariantData.class : VariantRunData.class, runQuery, page * body.getPageSize(), body.getPageSize());
			else {
				status.setMessage("At least a variantDbId, a variantSetDbId, or a referenceDbId must be specified as parameter!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(vlr, HttpStatus.BAD_REQUEST);
			}
			
        	for (AbstractVariantData dbVariant : varList) {
        		Variant variant = new Variant();
        		variant.setVariantDbId(module + GigwaGa4ghServiceImpl.ID_SEPARATOR + (dbVariant instanceof VariantRunData ? ((VariantRunData) dbVariant).getId().getVariantId() : ((VariantData) dbVariant).getId()));
        		List<String> alleles = dbVariant.getKnownAlleleList();
        		if (alleles.size() > 0)
        			variant.setReferenceBases(alleles.get(0));
        		if (alleles.size() > 1)
        			variant.setAlternateBases(alleles.subList(1, alleles.size()));
        		variant.setVariantType(dbVariant.getType());
        		if (dbVariant.getReferencePosition() != null) {
	        		variant.setReferenceName(dbVariant.getReferencePosition().getSequence());
	        		variant.setStart((int) dbVariant.getReferencePosition().getStartSite());
	        		variant.setEnd((int) (dbVariant.getReferencePosition().getEndSite() != null ? dbVariant.getReferencePosition().getEndSite() : (variant.getReferenceBases() != null ? (variant.getStart() + variant.getReferenceBases().length() - 1) : null)));
        		}
        		if (dbVariant.getSynonyms() != null && !dbVariant.getSynonyms().isEmpty()) {
        			List<String> synonyms = new ArrayList<>();
        			for (TreeSet<String> synsForAType : dbVariant.getSynonyms().values())
        				synonyms.addAll(synsForAType);
        			variant.setVariantNames(synonyms);
        		}
        		result.addDataItem(variant);
        	}

        	int nNextPage = page + 1;
        	TokenPagination pagination = new TokenPagination();
    		pagination.setPageSize(body.getPageSize());
    		pagination.setCurrentPageToken("" + page);
    		if (!varList.isEmpty())
    			pagination.setNextPageToken("" + nNextPage);
    		if (page > 0)
    			pagination.setPrevPageToken("" + (page - 1));
			metadata.setPagination(pagination);

        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<>(vlr, HttpStatus.INTERNAL_SERVER_ERROR);
        }

		return new ResponseEntity<>(vlr, HttpStatus.OK);
    }
//
//    public ResponseEntity<VariantListResponse> searchVariantsSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<VariantListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"created\" : \"1573671122\",\n      \"referenceBases\" : \"ATCGATTGAGCTCTAGCG\",\n      \"start\" : \"500\",\n      \"cipos\" : [ -12000, 1000 ],\n      \"variantType\" : \"DUP\",\n      \"ciend\" : [ -1000, 0 ],\n      \"alternate_bases\" : [ \"TAGGATTGAGCTCTATAT\" ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : [ \"c8ae400b\", \"ef2c204b\" ],\n      \"filtersFailed\" : [ \"d629a669\", \"3f14f578\" ],\n      \"svlen\" : \"1500\",\n      \"variantDbId\" : \"628e89c5\",\n      \"variantNames\" : [ \"RefSNP_ID_1\", \"06ea312e\" ],\n      \"end\" : \"518\",\n      \"filtersApplied\" : true,\n      \"filtersPassed\" : true,\n      \"updated\" : \"1573672019\",\n      \"referenceName\" : \"chr20\"\n    }, {\n      \"created\" : \"1573671122\",\n      \"referenceBases\" : \"ATCGATTGAGCTCTAGCG\",\n      \"start\" : \"500\",\n      \"cipos\" : [ -12000, 1000 ],\n      \"variantType\" : \"DUP\",\n      \"ciend\" : [ -1000, 0 ],\n      \"alternate_bases\" : [ \"TAGGATTGAGCTCTATAT\" ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : [ \"c8ae400b\", \"ef2c204b\" ],\n      \"filtersFailed\" : [ \"d629a669\", \"3f14f578\" ],\n      \"svlen\" : \"1500\",\n      \"variantDbId\" : \"628e89c5\",\n      \"variantNames\" : [ \"RefSNP_ID_1\", \"06ea312e\" ],\n      \"end\" : \"518\",\n      \"filtersApplied\" : true,\n      \"filtersPassed\" : true,\n      \"updated\" : \"1573672019\",\n      \"referenceName\" : \"chr20\"\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<VariantListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    public ResponseEntity<VariantSetListResponse> searchVariantsetsPost(@ApiParam(value = "Variantset Search request") @Valid @RequestBody VariantSetsSearchRequest body, @ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);
		HttpStatus httpCode = null;

        try {
        	VariantSetListResponse vslr = new VariantSetListResponse();
			Metadata metadata = new Metadata();
			vslr.setMetadata(metadata);
        	VariantSetListResponseResult result = new VariantSetListResponseResult();
	        int pageToken = 0;

	        if ((body.getVariantSetDbIds() != null || body.getStudyDbIds() != null) || body.getCallSetDbIds() == null) {
	    		List<String> relevantIDs = body.getVariantSetDbIds() != null ? body.getVariantSetDbIds() : body.getStudyDbIds();
	    		Map<String /*module*/, Map<Integer /*project*/, List<String> /*runs (all if null)*/>> variantSetDbIDsByStudyAndRefSet = parseVariantSetOrStudyDbIDs(relevantIDs);
	        
		        for (String refSetDbId : variantSetDbIDsByStudyAndRefSet.isEmpty() ? MongoTemplateManager.getAvailableModules() : variantSetDbIDsByStudyAndRefSet.keySet()) {
	    			MongoTemplate mongoTemplate = MongoTemplateManager.get(refSetDbId);
	    			Collection<Integer> allowedPjIDs = new HashSet<>();
		        	Map<Integer, List<String>> variantSetDbIDsByStudy = variantSetDbIDsByStudyAndRefSet.get(refSetDbId);
		        	for (int pjId : variantSetDbIDsByStudy != null ? variantSetDbIDsByStudy.keySet() : mongoTemplate.findDistinct("_id", GenotypingProject.class, Integer.class))
	            		if (tokenManager.canUserReadProject(token, refSetDbId, pjId))
	            			allowedPjIDs.add(pjId);
	
	    	        Query q = new Query(Criteria.where("_id").in(allowedPjIDs));
	    	        q.fields().include(GenotypingProject.FIELDNAME_RUNS);
	    	        for (GenotypingProject proj : mongoTemplate.find(q, GenotypingProject.class)) {
	    	        	List<String> wantedProjectRuns = variantSetDbIDsByStudy == null ? new ArrayList<>() : variantSetDbIDsByStudy.get(proj.getId());
	    	        	for (String run : proj.getRuns())
		    	        	if (wantedProjectRuns.isEmpty() || wantedProjectRuns.contains(run)) {
		    	        		VariantSet variantSet = cache.getVariantSet(mongoTemplate, refSetDbId + GigwaGa4ghServiceImpl.ID_SEPARATOR + proj.getId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + run);
			    	            result.addDataItem(variantSet);
		    	        	}
	    	        }
		        }
		    }
	        else {	// no study or variantSet specified, but we have a list of callSets
	        	HashMap<String /*module*/, HashSet<Integer> /*samples*/> samplesByModule = new HashMap<>();
				for (String csId : body.getCallSetDbIds()) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(csId, 3);
					HashSet<Integer> moduleSamples = samplesByModule.get(info[0]);
					if (moduleSamples == null) {
						moduleSamples = new HashSet<>();
						samplesByModule.put(info[0], moduleSamples);
					}
					moduleSamples.add(Integer.parseInt(info[2]));
				}
				HashSet<String> addedVariantSets = new HashSet<>();	// will be used to avoid adding the same variantSet several times
	        	for (String module : samplesByModule.keySet()) {
	        		MongoTemplate mongoTemplate = MongoTemplateManager.get(module);
	    	        for (GenotypingSample sample : mongoTemplate.find(new Query(Criteria.where("_id").in(samplesByModule.get(module))), GenotypingSample.class)) {
	    	        	String variantSetDbId = module + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getProjectId() + GigwaGa4ghServiceImpl.ID_SEPARATOR + sample.getRun();
	    	        	if (!addedVariantSets.contains(variantSetDbId)) {
	    	        		VariantSet variantSet = cache.getVariantSet(mongoTemplate, variantSetDbId);
		    	            result.addDataItem(variantSet);
		    	            addedVariantSets.add(variantSetDbId);
	    	        	}
	    	        }
	        	}
        	}
        	
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(pageToken);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);

			vslr.setResult(result);
            return new ResponseEntity<VariantSetListResponse>(vslr, httpCode == null ? HttpStatus.OK : httpCode);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<VariantSetListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
//    private Map<String /* module */, List<Integer /* project */>> parseStudyDbIDs(List<String> studySetDbIds) {
//    	Map<String, List<Integer>> result = new HashMap<>();
//    	for (String variantSetId : studySetDbIds) {
//    		String[] splitId = variantSetId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
//    		List<Integer> moduleProjects = result.get(splitId[0]);
//    		if (moduleProjects == null) {
//    			moduleProjects = new ArrayList<>();
//    			result.put(splitId[0], moduleProjects);
//    		}
//    		moduleProjects.add(Integer.parseInt(splitId[1]));
//    	}
//    	return result;
//    }
//    
//    private Map<String /* module */, Map<Integer /* project */, List<String> /* runs */>> parseVariantSetDbIDs(List<String> variantSetDbIds) {
//    	Map<String, Map<Integer, List<String>>> result = new HashMap<>();
//    	for (String variantSetId : variantSetDbIds) {
//    		String[] splitId = variantSetId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
//    		Map<Integer, List<String>> moduleProjectsAndRuns = result.get(splitId[0]);
//    		if (moduleProjectsAndRuns == null) {
//    			moduleProjectsAndRuns = new HashMap<>();
//    			result.put(splitId[0], moduleProjectsAndRuns);
//    		}
//    		int pjId = Integer.parseInt(splitId[1]);
//    		List<String> projectRuns = moduleProjectsAndRuns.get(pjId);
//    		if (projectRuns == null) {
//    			projectRuns = new ArrayList<>();
//    			moduleProjectsAndRuns.put(pjId, projectRuns);
//    		}
//    		projectRuns.add(splitId[2]);
//    	}
//    	return result;
//    }

    /* study IDs have 2 levels: module+project, variantSet IDs have 3 levels: module+project+run */
    private Map<String /* module */, Map<Integer /* project */, List<String> /* runs */>> parseVariantSetOrStudyDbIDs(List<String> variantSetOrStudyDbIds) {
    	Map<String, Map<Integer, List<String>>> result = new HashMap<>();
    	if (variantSetOrStudyDbIds != null)
	    	for (String variantSetOrStudyId : variantSetOrStudyDbIds) {
	    		String[] splitId = variantSetOrStudyId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
	    		Map<Integer, List<String>> moduleProjectsAndRuns = result.get(splitId[0]);
	    		if (moduleProjectsAndRuns == null) {
	    			moduleProjectsAndRuns = new HashMap<>();
	    			result.put(splitId[0], moduleProjectsAndRuns);
	    		}
	    		int pjId = Integer.parseInt(splitId[1]);
	    		List<String> projectRuns = moduleProjectsAndRuns.get(pjId);
	    		if (projectRuns == null) {
	    			projectRuns = new ArrayList<>();
	    			moduleProjectsAndRuns.put(pjId, projectRuns);
	    		}
	    		if (splitId.length == 3)	// otherwise it was a study ID: the run list will remain empty, meaning all of them are requested 
	    			projectRuns.add(splitId[2]);
	    	}
    	return result;
    }

//    public ResponseEntity<VariantSetListResponse> searchVariantsetsSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<VariantSetListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ {\n      \"availableFormats\" : [ {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      }, {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      } ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : \"variantSetDbId\",\n      \"callSetCount\" : 0,\n      \"referenceSetDbId\" : \"referenceSetDbId\",\n      \"variantSetName\" : \"variantSetName\",\n      \"analysis\" : [ {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      }, {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      } ],\n      \"studyDbId\" : \"studyDbId\",\n      \"variantCount\" : 6\n    }, {\n      \"availableFormats\" : [ {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      }, {\n        \"dataFormat\" : \"DartSeq\",\n        \"fileURL\" : \"http://example.com/aeiou\",\n        \"fileFormat\" : \"text/csv\"\n      } ],\n      \"additionalInfo\" : {\n        \"key\" : \"additionalInfo\"\n      },\n      \"variantSetDbId\" : \"variantSetDbId\",\n      \"callSetCount\" : 0,\n      \"referenceSetDbId\" : \"referenceSetDbId\",\n      \"variantSetName\" : \"variantSetName\",\n      \"analysis\" : [ {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      }, {\n        \"software\" : [ \"software\", \"software\" ],\n        \"analysisDbId\" : \"analysisDbId\",\n        \"created\" : \"created\",\n        \"description\" : \"description\",\n        \"type\" : \"type\",\n        \"updated\" : \"updated\",\n        \"analysisName\" : \"analysisName\"\n      } ],\n      \"studyDbId\" : \"studyDbId\",\n      \"variantCount\" : 6\n    } ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", VariantSetListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<VariantSetListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

	public ResponseEntity<StudyListResponse> searchStudiesPost(@ApiParam(value = "Study Search request")  @Valid @RequestBody StudySearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
    	String token = ServerinfoApiController.readToken(authorization);

    	try {
	    	StudyListResponse slr = new StudyListResponse();
	    	StudyListResponseResult result = new StudyListResponseResult();
	    	for (String refSetDbId : MongoTemplateManager.getAvailableModules()) {
	        	List<org.ga4gh.models.VariantSet> ga4ghVariantSets = ga4ghService.searchVariantSets(new SearchVariantSetsRequest(refSetDbId, null, null)).getVariantSets();
	        	List<org.ga4gh.models.VariantSet> forbiddenVariantSets = new ArrayList<>();
	        	for (org.ga4gh.models.VariantSet ga4ghVariantSet : ga4ghVariantSets)
	        	{
	        		int variantSetId = Integer.parseInt(ga4ghVariantSet.getId().split(GigwaGa4ghServiceImpl.ID_SEPARATOR)[1]);
	        		if (!tokenManager.canUserReadProject(token, refSetDbId, variantSetId))
	        			forbiddenVariantSets.add(ga4ghVariantSet);
	        	}
	        	ga4ghVariantSets.removeAll(forbiddenVariantSets);
	
	    		for (final org.ga4gh.models.VariantSet ga4ghVariantSet : ga4ghVariantSets) {
	            	result.addDataItem(new Study() {{
		            		setStudyDbId(ga4ghVariantSet.getId());
		            		setStudyType("genotype");
		            		setStudyName(ga4ghVariantSet.getName());	/* variantSets in GA4GH correspond to projects, i.e. studies in BrAPI v2 */

		            		for (VariantSetMetadata metadata : ga4ghVariantSet.getMetadata()) {
		            			if ("description".equals(metadata.getKey()))
		            				setStudyDescription(metadata.getValue());
		            			else
		            				putAdditionalInfoItem(metadata.getKey(), metadata.getValue());
		            		}
	            		}} );
	    		}
	    	}
	    	
			Metadata metadata = new Metadata();
			IndexPagination pagination = new IndexPagination();
			pagination.setPageSize(result.getData().size());
			pagination.setCurrentPage(0);
			pagination.setTotalPages(1);
			pagination.setTotalCount(result.getData().size());
			metadata.setPagination(pagination);
			slr.setMetadata(metadata);
	
			slr.setResult(result);		
            return new ResponseEntity<StudyListResponse>(slr, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Couldn't serialize response for content type application/json", e);
            return new ResponseEntity<StudyListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<GermplasmListResponse> searchGermplasmPost(HttpServletResponse response, @ApiParam(value = "Germplasm Search request") @Valid @RequestBody GermplasmSearchRequest body,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization)  throws Exception {
    	String token = ServerinfoApiController.readToken(authorization);

		if (body.getCommonCropNames() != null && body.getCommonCropNames().size() > 0)
			return new ResponseEntity<>(HttpStatus.OK);	// not supported

    	try {
			GermplasmListResponse glr = new GermplasmListResponse();
			GermplasmListResponseResult result = new GermplasmListResponseResult();
			Metadata metadata = new Metadata();
			glr.setMetadata(metadata);

			String refSetDbId = null;
			Integer projId = null;
			Collection<String> germplasmIdsToReturn = new HashSet<>(), requestedGermplasmIDs;
			if (body.getStudyDbIds() != null && !body.getStudyDbIds().isEmpty()) {
				if (body.getStudyDbIds().size() > 1) {
					Status status = new Status();
					status.setMessage("You may only ask for germplasm records from one study at a time!");
					metadata.addStatusItem(status);
					return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
				}
				String[] info = GigwaSearchVariantsRequest.getInfoFromId(body.getStudyDbIds().get(0), 2);
				refSetDbId = info[0];
				projId = Integer.parseInt(info[1]);
				germplasmIdsToReturn = MgdbDao.getProjectIndividuals(refSetDbId, projId);
			}
			else if (body.getGermplasmDbIds() != null && !body.getGermplasmDbIds().isEmpty()) {
				requestedGermplasmIDs = body.getGermplasmDbIds();
				for (String gpId : requestedGermplasmIDs) {
					String[] info = GigwaSearchVariantsRequest.getInfoFromId(gpId, 3);
					if (refSetDbId == null)
						refSetDbId = info[0];
					else if (!refSetDbId.equals(info[0])) {
						Status status = new Status();
						status.setMessage("You may only ask for germplasm records from one referenceSet at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
					}
					if (projId == null)
						projId = Integer.parseInt(info[1]);
					else if (!projId.equals(Integer.parseInt(info[1]))) {
						Status status = new Status();
						status.setMessage("You may only ask for germplasm records from one study at a time!");
						metadata.addStatusItem(status);
						return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
					}
					germplasmIdsToReturn.add(info[2]);
				}
			}
			else {
				Status status = new Status();
				status.setMessage("Either a studyDbId or a list of germplasmDbIds must be specified as parameter!");
				metadata.addStatusItem(status);
				return new ResponseEntity<>(glr, HttpStatus.BAD_REQUEST);
			}

   			if (!tokenManager.canUserReadProject(token, refSetDbId, projId))
   				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
 			
   			fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest gsr = new fr.cirad.web.controller.rest.BrapiRestController.GermplasmSearchRequest();
   	    	gsr.accessionNumbers = body.getAccessionNumbers();
   	    	gsr.germplasmPUIs = body.getGermplasmPUIs();
   	    	gsr.germplasmGenus = body.getGermplasmGenus();
   	    	gsr.germplasmSpecies = body.getGermplasmSpecies();
   	    	gsr.germplasmNames = body.getGermplasmNames() == null ? null : body.getGermplasmNames().stream().map(nm -> nm.substring(1 + nm.lastIndexOf(GigwaMethods.ID_SEPARATOR))).collect(Collectors.toList());
   	    	gsr.germplasmDbIds = germplasmIdsToReturn;
   	    	gsr.page = body.getPage();
   	    	gsr.pageSize = body.getPageSize();
   	    	
   			Map<String, Object> v1response = (Map<String, Object>) brapiV1Service.executeGermplasmSearch(request, response, refSetDbId, gsr);
   			Map<String, Object> v1Result = (Map<String, Object>) v1response.get("result");
   	    	ArrayList<Map<String, Object>> v1data = (ArrayList<Map<String, Object>>) v1Result.get("data");
   	    	String lowerCaseIdFieldName = BrapiService.BRAPI_FIELD_germplasmDbId.toLowerCase();
   	    	for (Map<String, Object> v1germplasmRecord : v1data) {
    			Germplasm germplasm = new Germplasm();
    			
   	    		for (String key : v1germplasmRecord.keySet()) {
   	    			String sLCkey = key.toLowerCase();
   	    			Object val = v1germplasmRecord.get(key);
					if (!BrapiGermplasm.germplasmFields.containsKey(sLCkey) && !lowerCaseIdFieldName.equals(sLCkey)) {
						if ("additionalinfo".equals(sLCkey)) {
							for (String aiKey : ((HashMap<String, String>) val).keySet())
								germplasm.putAdditionalInfoItem(aiKey, ((HashMap<String, String>) val).get(aiKey));
						}
						else	
							germplasm.putAdditionalInfoItem(key, val.toString());
					}
					else {
						switch (sLCkey) {
							case "germplasmdbid":
								germplasm.germplasmDbId(ga4ghService.createId(refSetDbId, projId, val.toString()));
								break;
							case "germplasmname":
								germplasm.setGermplasmName(val.toString());
								break;
							case "defaultdisplayname":
								germplasm.setDefaultDisplayName(val.toString());
								break;
							case "accessionnumber":
								germplasm.setAccessionNumber(val.toString());
								break;
							case "germplasmpui":
								germplasm.setGermplasmPUI(val.toString());
								break;
							case "pedigree":
								germplasm.setPedigree(val.toString());
								break;
							case "seedsource":
								germplasm.setSeedSource(val.toString());
								break;
							case "commoncropname":
								germplasm.setCommonCropName(val.toString());
								break;
							case "institutecode":
								germplasm.setInstituteCode(val.toString());
								break;
							case "institutename":
								germplasm.setInstituteName(val.toString());
								break;
							case "biologicalstatusofaccessioncode":
								germplasm.setBiologicalStatusOfAccessionCode(BiologicalStatusOfAccessionCodeEnum.fromValue(val.toString()));
								break;
							case "countryoforigincode":
								germplasm.setCountryOfOriginCode(val.toString());
								break;
							case "typeofgermplasmstoragecode":
								germplasm.setTypeOfGermplasmStorageCode(Arrays.asList(GermplasmMCPD.StorageTypeCodesEnum.fromValue(val.toString()).toString()));
								break;
							case "genus":
								germplasm.setGermplasmGenus(val.toString());
								break;
							case "species":
								germplasm.setGermplasmSpecies(val.toString());
								break;
							case "speciesauthority":
								germplasm.setSpeciesAuthority(val.toString());
								break;
							case "subtaxa":
								germplasm.setSubtaxa(val.toString());
								break;
							case "subtaxaauthority":
								germplasm.setSubtaxaAuthority(val.toString());
								break;
							case "acquisitiondate":
								try {
									germplasm.setAcquisitionDate(LocalDate.parse(val.toString()));
								}
								catch (DateTimeParseException dtpe){
									log.error("Unable to parse germplasm acquisition date: " + val);
								}
								break;
						}
					}
   	    		}
				result.addDataItem(germplasm);
   	    	}
			glr.setResult(result);
			IndexPagination pagination = new IndexPagination();
			jhi.brapi.api.Metadata v1Metadata = (jhi.brapi.api.Metadata) v1response.get("metadata");
			pagination.setPageSize(v1Metadata.getPagination().getPageSize());
			pagination.setCurrentPage(v1Metadata.getPagination().getCurrentPage());
			pagination.setTotalPages(v1Metadata.getPagination().getTotalPages());
			pagination.setTotalCount((int) v1Metadata.getPagination().getTotalCount());
			metadata.setPagination(pagination);			
			
			return new ResponseEntity<>(glr, HttpStatus.OK);
		} catch (Exception e) {
			log.error("Couldn't serialize response for content type application/json", e);
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

//    public ResponseEntity<GermplasmListResponse> searchGermplasmSearchResultsDbIdGet(@ApiParam(value = "Permanent unique identifier which references the search results",required=true) @PathVariable("searchResultsDbId") String searchResultsDbId,@ApiParam(value = "Which result page is requested. The page indexing starts at 0 (the first page is 'page'= 0). Default is `0`.") @Valid @RequestParam(value = "page", required = false) Integer page,@ApiParam(value = "The size of the pages to be returned. Default is `1000`.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize,@ApiParam(value = "HTTP HEADER - Token used for Authorization   <strong> Bearer {token_string} </strong>" ) @RequestHeader(value="Authorization", required=false) String authorization) {
//        try {
//            return new ResponseEntity<GermplasmListResponse>(objectMapper.readValue("{\n  \"result\" : {\n    \"data\" : [ \"\", \"\" ]\n  },\n  \"metadata\" : {\n    \"pagination\" : {\n      \"totalPages\" : 1,\n      \"pageSize\" : \"1000\",\n      \"currentPage\" : 0,\n      \"totalCount\" : 1\n    },\n    \"datafiles\" : [ {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    }, {\n      \"fileDescription\" : \"This is an Excel data file\",\n      \"fileName\" : \"datafile.xslx\",\n      \"fileSize\" : 4398,\n      \"fileMD5Hash\" : \"c2365e900c81a89cf74d83dab60df146\",\n      \"fileURL\" : \"https://wiki.brapi.org/examples/datafile.xslx\",\n      \"fileType\" : \"application/vnd.ms-excel\"\n    } ],\n    \"status\" : [ {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    }, {\n      \"messageType\" : \"INFO\",\n      \"message\" : \"Request accepted, response successful\"\n    } ]\n  },\n  \"@context\" : [ \"https://brapi.org/jsonld/context/metadata.jsonld\" ]\n}", GermplasmListResponse.class), HttpStatus.NOT_IMPLEMENTED);
//        } catch (IOException e) {
//            log.error("Couldn't serialize response for content type application/json", e);
//            return new ResponseEntity<GermplasmListResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
}
