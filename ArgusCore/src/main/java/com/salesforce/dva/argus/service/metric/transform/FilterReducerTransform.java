package com.salesforce.dva.argus.service.metric.transform;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.salesforce.dva.argus.entity.Metric;
import com.salesforce.dva.argus.system.SystemAssert;

public class FilterReducerTransform implements Transform{
	@Inject
	Provider<TransformFactory> _transformFactory;
	
	@Override
	public List<Metric> transform(List<Metric> metrics) {
		throw new UnsupportedOperationException("At least need one constant. format podId cs.*");
	}

	@Override
	public List<Metric> transform(List<Metric> metrics, List<String> constants) {
		SystemAssert.requireArgument(constants!=null&&constants.size()==2, "Need two constants. format podId cs.*");
		String tagName=constants.get(0);
		final String tagPattern=constants.get(1);
		
		List<Map<String,String>> tags=metrics.stream().map(m -> m.getTags()).collect(Collectors.toList());
		SystemAssert.requireArgument(tags.stream().filter(m -> m.get(tagName)!=null).collect(Collectors.toList()).size()!=0,"invalid tag");
		
		if (tagPattern.length()>=4&&tagPattern.substring(0, 3).equals("NOT")){
			String antiTagPattern=tagPattern.substring(3);
			return metrics.stream().filter(m -> !Pattern.matches(antiTagPattern, m.getTag(tagName))).collect(Collectors.toList());
			
		}
		
		return metrics.stream().filter(m -> Pattern.matches(tagPattern, m.getTag(tagName))).collect(Collectors.toList());
	}

	@Override
	public List<Metric> transform(List<Metric>... metrics) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultScopeName() {
		return TransformFactory.Function.FILTER.name();
	}

}
