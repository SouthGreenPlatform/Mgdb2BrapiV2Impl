package org.brapi.v2.api.cache;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.brapi.v2.api.ServerinfoApi;
import org.brapi.v2.api.VariantsetsApi;
import org.brapi.v2.model.VariantSet;
import org.brapi.v2.model.VariantSetAvailableFormats;
import org.brapi.v2.model.VariantSetAvailableFormats.DataFormatEnum;
import org.brapi.v2.model.VariantSetAvailableFormats.FileFormatEnum;
import org.mortbay.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import fr.cirad.mgdb.model.mongo.maintypes.GenotypingSample;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData;
import fr.cirad.mgdb.model.mongo.maintypes.VariantRunData.VariantRunDataId;
import fr.cirad.mgdb.service.GigwaGa4ghServiceImpl;
import fr.cirad.tools.AppConfig;
import fr.cirad.web.controller.BackOfficeController;

@Component
public class MongoBrapiCache {

	private static final String BRAPI_CACHE_COLL_VARIANTSET = "brapiCache_VariantSet";

	@Autowired private AppConfig appConfig;
	
	private HttpServletRequest request;

	private String publicHostName;
	
    @Autowired
    public MongoBrapiCache(HttpServletRequest request) {
        this.request = request;
    }
	
	public VariantSet getVariantSet(MongoTemplate mongoTemplate, String variantSetDbId) throws SocketException, UnknownHostException {
		VariantSet variantSet = mongoTemplate.findById(variantSetDbId, VariantSet.class, BRAPI_CACHE_COLL_VARIANTSET);
    	if (variantSet == null) {
    		long before = System.currentTimeMillis();
    		variantSet = new VariantSet();
   			List<VariantSetAvailableFormats> formatList = new ArrayList<VariantSetAvailableFormats>();
    		VariantSetAvailableFormats format = new VariantSetAvailableFormats();
        	format.setDataFormat(DataFormatEnum.FLAPJACK);
        	format.setFileFormat(FileFormatEnum.TEXT_TSV);
        	formatList.add(format);
			variantSet.setAvailableFormats(formatList);
//          Analysis analysisItem = new Analysis();
//	        analysisItem.setAnalysisDbId(ga4ghVariantSet.getId());
//	        analysisItem.setType("TODO: check how to deal with this field");
//          variantSet.addAnalysisItem(analysisItem);
			
			String[] splitId = variantSetDbId.split(GigwaGa4ghServiceImpl.ID_SEPARATOR);
			int projId = Integer.parseInt(splitId[1]);
			variantSet.setReferenceSetDbId(splitId[0]);
			variantSet.setStudyDbId(splitId[0] + GigwaGa4ghServiceImpl.ID_SEPARATOR + projId);
			variantSet.setVariantSetDbId(variantSetDbId);
			variantSet.setVariantSetName(splitId[2]);
//    	    variantSet.setCallSetCount(mongoTemplate.findDistinct(new Query(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(proj.getId())), GenotypingSample.FIELDNAME_INDIVIDUAL, GenotypingSample.class, String.class).size());
			variantSet.setCallSetCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where(GenotypingSample.FIELDNAME_PROJECT_ID).is(projId), Criteria.where(GenotypingSample.FIELDNAME_RUN).is(splitId[2]))), GenotypingSample.class));
//    	    variantSet.setVariantCount((int) mongoTemplate.getCollection(mongoTemplate.getCollectionName(VariantData.class)).estimatedDocumentCount() /* this counts all variants in the database */);
			variantSet.setVariantCount((int) mongoTemplate.count(new Query(new Criteria().andOperator(Criteria.where("_id." + VariantRunDataId.FIELDNAME_PROJECT_ID).is(projId), Criteria.where("_id." + VariantRunDataId.FIELDNAME_RUNNAME).is(splitId[2]))), VariantRunData.class));

			if (variantSet.getCallSetCount() == 0 && variantSet.getVariantCount() == 0)
				return null;	// this run probably doesn't exist
			mongoTemplate.save(variantSet, BRAPI_CACHE_COLL_VARIANTSET);
			Log.debug("VariantSet cache generated for " + variantSetDbId + " in " + (System.currentTimeMillis() - before) / 1000 + "s");
    	}
    	
    	// construct export URLs dynamically because we might get wrong URLs in cases where multiple instances are connected to a same DB
    	String sWebAppRoot = appConfig.get("enforcedWebapRootUrl");
    	for (VariantSetAvailableFormats format : variantSet.getAvailableFormats())
        	format.setFileURL((sWebAppRoot == null ? getPublicHostName(request) + request.getContextPath() : sWebAppRoot) + request.getServletPath() + ServerinfoApi.URL_BASE_PREFIX + "/" + VariantsetsApi.variantsetsExportIntoFormat_url.replace("{variantSetDbId}", variantSetDbId).replace("{dataFormat}", format.getDataFormat().toString()));

		return variantSet;
	}

	private String getPublicHostName(HttpServletRequest request2) throws SocketException, UnknownHostException {
		if (publicHostName == null)
			publicHostName = BackOfficeController.determinePublicHostName(request);
		return publicHostName;
	}
}
