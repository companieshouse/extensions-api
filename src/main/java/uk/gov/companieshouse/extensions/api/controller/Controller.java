package uk.gov.companieshouse.extensions.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.dto.Form;

@RestController
@RequestMapping("/api/extensions")
public class Controller {

	@GetMapping("/test")
	public String test(@RequestParam("test") String test) {
		StringBuilder sb = new  StringBuilder(test);
		return sb.reverse().toString();
	}
	
	@PostMapping("/input")
	public String get(@RequestBody Form form) {
		return "Form received - Name: " + form.getCompanyName() + " Reason: " + form.getReason();
	}
	
	@PostMapping(value= "/uploadFile")
	public String uploadFile(@RequestParam("file") MultipartFile file) throws Exception {
		return null;		 
	}
	
}
