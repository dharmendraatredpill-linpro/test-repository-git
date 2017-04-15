if (typeof dr == "undefined" || !dr) {
	var dr = {};
}

Alfresco.forms.validation.poNumber = function poNumber(field, args, event,
		form, silent, test) {
	return YAHOO.lang.trim(field.value).length !== 0;
}

Alfresco.forms.validation.projectNumber = function projectNumber(field, args,
		event, form, silent, message) {
	return YAHOO.lang.trim(field.value).length !== 0;
}

Alfresco.forms.validation.projectName = function projectName(field, args,
		event, form, silent, message) {
	return YAHOO.lang.trim(field.value).length !== 0;
}