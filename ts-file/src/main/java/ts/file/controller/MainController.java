package ts.file.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/main")
@Controller
public class MainController {
	
	@Value("${file.out.path}")
	private String fileOutputPath;
	
	private Map<String, Integer> progressMap = new HashMap<>();

	@RequestMapping(method = RequestMethod.GET)
	public String main(){
		return "main";
	}
	
	@RequestMapping(value = "/progress/{id}", method = RequestMethod.GET)
	@ResponseBody
	public int progress(HttpServletRequest request,
			@PathVariable("id") String id){
		
		Integer currentPercent = progressMap.get(id);
		
		if(currentPercent == null){
			return 0;
		}else{
			return currentPercent.intValue();
		}
		
		
	}
	
	@RequestMapping(value = "/id", method = RequestMethod.GET)
	@ResponseBody
	public String getId(HttpServletRequest request, HttpServletResponse response){
		
		String id = request.getSession().getId() + new Date().getTime();
		progressMap.put(id, 0);
		return id;
	}
	
	 @RequestMapping(value = "/fileUpload/{id}", method = RequestMethod.POST)
	 @ResponseBody
	 public String fileUpload(Model model,
	      HttpServletRequest request, HttpServletResponse response,
	      @PathVariable("id") String id) {
		 
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		try{
			if(isMultipart){

				ServletFileUpload upload = new ServletFileUpload();
				FileItemIterator iter = upload.getItemIterator(request);
				
				while (iter.hasNext()) {
				    FileItemStream item = iter.next();
				    //String name = item.getFieldName();
				    InputStream stream = item.openStream();

				    //폼 필드가 파일이 아닌경우
				    if (item.isFormField()) {
				    	 
				    } 
				    //폼 필드가 파일 일 경우
				    else {
				   	 outputFile(stream, fileOutputPath + id, id);
				    }
				}
		}
		 
		}catch(Exception e){
			e.printStackTrace();
		}
		
		progressMap.remove(id);
		
		return request.getContextPath() + "/static/download/" + id;
		 
	 }
	 
	 private void outputFile(InputStream in, String filePath, String id){
		 
		 BufferedReader br = null;
		 BufferedWriter bw = null;
		 
		 try{
			 br = new BufferedReader(new InputStreamReader(in));
			 bw = new BufferedWriter(new FileWriter(filePath));
			 
			 double currentLine = 0;
			 List<String> strList = br.lines().collect(Collectors.toList());
			 double totalLine = strList.size();
			 
			 for(String str : strList){
				 bw.write(str+"\n");
				 bw.flush();
				 currentLine ++;
				 
				 double percent = (currentLine / totalLine) * 100;
				  
				 progressMap.put(id, (int)percent);
			 }
			  
		 }catch(Exception e){
			 e.printStackTrace();
		 }finally{
			 try{
				 if(bw != null) bw.close();
				 if(br != null) br.close();
			 }catch(Exception e){
				 e.printStackTrace();
			 }
		 }
		 
		 
	 }
}
