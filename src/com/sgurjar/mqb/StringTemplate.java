package com.sgurjar.mqb;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTemplate {
	// ${var}
	private static final Pattern RE_VAR = Pattern.compile("\\$\\{([^\\}]+)\\}");
	
	private final String template;
	
	public StringTemplate(String template) {
		if (template == null)
			throw new IllegalArgumentException();
		this.template = template;
	}
	
	public String getTemplate(){return template;}

	public String inflate(Map<String, ?> vars) {
		StringBuffer sb = new StringBuffer();
		Matcher matcher = RE_VAR.matcher(template);
		while (matcher.find()) {
			matcher.appendReplacement(sb, String.valueOf(vars.get(matcher.group(1))));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
