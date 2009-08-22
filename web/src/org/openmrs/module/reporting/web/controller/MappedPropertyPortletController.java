package org.openmrs.module.reporting.web.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.evaluation.EvaluationUtil;
import org.openmrs.module.evaluation.parameter.Mapped;
import org.openmrs.module.evaluation.parameter.Parameter;
import org.openmrs.module.evaluation.parameter.Parameterizable;
import org.openmrs.module.util.ParameterizableUtil;

/**
 * This Controller loads a Mapped property given the passed parameters
 */
public class MappedPropertyPortletController extends ParameterizablePortletController {
	
	protected final Log log = LogFactory.getLog(getClass());

	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map model) {
		
		super.populateModel(request, model);

		String uuid = (String)model.get("uuid");
		String property = (String)model.get("property");
		String collectionKey = (String)model.get("collectionKey");
		String mappedUuid = (String) model.get("mappedUuid");
		
		Class<?> clazz = (Class<?>)model.get("clazz");
		Parameterizable obj = (Parameterizable)model.get("obj");
		
		// Get generic type of the Mapped property, if specified
		Class<? extends Parameterizable> mappedType = null;
		if (StringUtils.isNotEmpty(property)) {
			mappedType = ParameterizableUtil.getMappedType(clazz, property);
		}
		model.put("mappedType", mappedType);

		if (StringUtils.isNotEmpty(uuid)) {

	    	// Retrieve the child property, or null
	       	Parameterizable mappedObj = null;
	       	Map<String, String> mappings = new HashMap<String, String>();
	       	
	       	if (StringUtils.isEmpty(mappedUuid)) {
	       		Mapped<Parameterizable> mapped = ParameterizableUtil.getMappedProperty(obj, property, collectionKey);
	       		if (mapped != null) {
	       			mappedObj = mapped.getParameterizable();
	       			mappings = mapped.getParameterMappings();
	       		}
	       	}
	       	else if (mappedUuid != null) {
	       		mappedObj = ParameterizableUtil.getParameterizable(mappedUuid, mappedType);
	       	}
	       	model.put("mappedObj", mappedObj);
	       	model.put("mappings", mappings);
	       	
	       	Map<String, String> mappedParams = new HashMap<String, String>();
	       	Map<String, String> complexParams = new HashMap<String, String>();
	       	Map<String, String> fixedParams = new HashMap<String, String>();
	       	Map<String, Set<String>> allowedParams = new HashMap<String, Set<String>>();
       	
	       	if (mappedObj != null) {
				for (Parameter p : mappedObj.getParameters()) {
					String mappedVal = mappings.get(p.getName());
					
					Set<String> allowed  = new HashSet<String>();
					for (Parameter parentParam : obj.getParameters()) {
						if (p.getClazz() == parentParam.getClazz()) {
							allowed.add(parentParam.getName());
						}
					}
					allowedParams.put(p.getName(), allowed);
					
					if (mappedVal != null) {
						if (EvaluationUtil.isExpression(mappedVal)) {
							mappedVal = EvaluationUtil.stripExpression(mappedVal);
							if (obj.getParameter(mappedVal) != null) {
								mappedParams.put(p.getName(), mappedVal);
							}
							else {
								complexParams.put(p.getName(), mappedVal);
							}
						}
						else {
							fixedParams.put(p.getName(), mappedVal);
						}
					}
				}
	       	}
			model.put("allowedParams", allowedParams);
			model.put("mappedParams", mappedParams);
			model.put("complexParams", complexParams);
			model.put("fixedParams", fixedParams);
		}
	}
}