package com.search;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.ScoreOrderFragmentsBuilder;
import org.apache.lucene.search.vectorhighlight.SimpleFragListBuilder;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.model.SearchModel;
import com.util.HelpUtil;

public class SearchHelper{
	
	/**
	 * 创建文档
	 * @param type
	 * @return
	 * @throws IOException
	 */
	private FSDirectory getDirectory(String type) throws IOException{
		
		ResourceBundle resource = ResourceBundle.getBundle("config");
		String indexPath = resource.getString("indexPath");  
		//System.out.println(indexPath);
		
		String pathinfo = indexPath + type;

		File file =new File(pathinfo);
		
		//如果文件夹不存在则创建    
		if(!file.exists() || !file.isDirectory())      
		{       
		    file.mkdir();    
		} 
		return FSDirectory.open(Paths.get(pathinfo)); 
	}
	
	/**
	 * 分词
	 */
	public String Token(String str) {
		
		StringBuffer sb = new StringBuffer();
		try {
			byte[] bt = str.getBytes();// 得到一个操作系统默认的编码格式的字节数组
			InputStream ip = new ByteArrayInputStream(bt);
			Reader read = new InputStreamReader(ip);
			IKSegmenter iks = new IKSegmenter(read, true);
			Lexeme t;
			while ((t = iks.next()) != null) {
				sb.append(t.getLexemeText()+"|");
			}
			read.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return sb.toString();
	}

	/**
	 * 创建索引
	 */
	public boolean createIndex(ArrayList<SearchModel> datalist, String type) {

		Boolean flag= true;
		Analyzer analyzer = new IKAnalyzer(false);//实例化IKAnalyzer分词器，IKAnalyze会对数字和字母分词
		
		try {
			Directory directory = getDirectory(type);//建立内存索引对象 
			
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			
			iwriter.deleteAll();
			for (SearchModel data : datalist) {
				this.createSingleIndex(iwriter,data);
			}
			
			iwriter.commit();
			iwriter.close();
			
		} catch (Exception e) {

			flag= false;
			//e.printStackTrace();
		}
		
		return flag;
	}
	
	/**
	 * 数据单元写入索引
	 * @param iwriter
	 * @param data
	 * @return
	 */
	private boolean createSingleIndex(IndexWriter iwriter, SearchModel data) {
		
		if (data == null) return false;
		
		boolean flag =true;
		
        Document doc = new Document();
        
        FieldType type=new FieldType(TextField.TYPE_STORED); 
        type.setStoreTermVectorOffsets(true);//记录相对增量  
        type.setStoreTermVectorPositions(true);//记录位置信息  
        type.setStoreTermVectors(true);//存储向量信息  
        //type.setTokenized(false);//Set to true to tokenize this field's contents via the configured Analyzer.
        type.freeze();//阻止改动信息  
        
        String textofDetail = HelpUtil.filterHtml(data.getDetail());
//      String filterName = HelpUtil.filterHtml(data.getName());
//      String filterModel = HelpUtil.filterHtml(data.getModel());
//        StoredField sField = new StoredField("id", data.getId());
        Field sField = new Field("id", String.valueOf(data.getId()), type);
        Field tField1 = new Field("name", data.getName(), type);
        Field tField2 = new Field("model", data.getModel(), type);
        Field tField3 = new Field("detail", textofDetail, type);
        Field tField4 = new Field("classid", String.valueOf(data.getClassid()),type);
        Field tField5 = new Field("hits", String.valueOf(data.getHits()), type);
        Field tField6 = new Field("updatetime", HelpUtil.dateToString(data.getUpdatetime()), type);
        Field tField7 = new Field("other", String.valueOf(data.getOther()), type);
        Field tField8 = new Field("visible", String.valueOf(data.isVisible()), type);
        Field tField9 = new Field("classname", data.getClassName(), type);

        tField1.setBoost(10);//设置域的boost值（权重）,默认为1
        tField2.setBoost(10);
        doc.add(sField);
    	doc.add(tField1);
    	doc.add(tField2);
    	doc.add(tField3);
    	doc.add(tField4);
    	doc.add(tField5);
    	doc.add(tField6);
    	doc.add(tField7);
    	doc.add(tField8);
    	doc.add(tField9);
    	
    	//System.out.println("createSingleIndex");
    	//System.out.println(data.isVisible());
    	
    	try {
			iwriter.addDocument(doc);
//			iwriter.commit();
//			iwriter.close();
		} catch (Exception e) {
			flag = false;
			//e.printStackTrace();
		}
        
//    	System.out.println("*********createSingleIndex***********");
//	    System.out.println(doc.get("id"));
//		System.out.println(doc.get("model"));
//		System.out.println(doc.get("name"));
//		System.out.println(doc.get("detail"));
//		System.out.println("********************");
		return flag;
	}
	
	/**
	 * 查询数据
	 */
	public ArrayList<SearchModel> search(String keywords, String type) {

		ArrayList<SearchModel> dataList = new ArrayList<SearchModel>();
		keywords=HelpUtil.filterHtml(keywords);
		if(keywords.length()<=1) return dataList;
		try {
			Directory directory = getDirectory(type);
			DirectoryReader ireader;	
			ireader = DirectoryReader.open(directory);
			
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			BooleanQuery searchQuery = new BooleanQuery();
			BooleanQuery highLightQuery = new BooleanQuery();
				
			String[] fileds = new String[]{ "name", "model", "detail"};//指定搜索域字段;
			BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
						BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD};//在对应文档应该出现，MUST为必须出现，MUST_NOT相反
			Query query = MultiFieldQueryParser.parse(keywords, fileds, flags, analyzer);
			searchQuery.add(query,BooleanClause.Occur.SHOULD);
			highLightQuery.add(query,BooleanClause.Occur.SHOULD);
			
			String IKkeywords = Token(keywords);
			//以三种形式进行分词
			String[] IKkeywordss1 = IKkeywords.split("\\|");//IK分词数组，权重最小
			String IKkeywords2Tem0 = IKkeywords.replaceAll("\\D", "\\|");
			String IKkeywords2Tem1 = IKkeywords.replaceAll("[^(a-zA-Z)]", "\\|");
			String IKkeywords2Tem2 = IKkeywords2Tem0+"|"+IKkeywords2Tem1;
			String[] IKkeywordss2 = (IKkeywords2Tem2.replaceAll("\\|{1,}", "\\|")).split("\\|");//纯数字和字母数组（字母和数字分开）
			String IKkeywords3Tem = IKkeywords.replaceAll("\\W", "\\|");
			String[] IKkeywordss3 = (IKkeywords3Tem.replaceAll("\\|{1,}", "\\|")).split("\\|");//纯数字和字母数组（字母和数字可以连在一起），权重最大
			IKkeywordss1=HelpUtil.mergeIdentical(IKkeywordss1);//合并相同字符串
			IKkeywordss2=HelpUtil.mergeIdentical(IKkeywordss2);
			IKkeywordss3=HelpUtil.mergeIdentical(IKkeywordss3);
			for(int i=0; i<IKkeywordss1.length; i++){
				for(int j=0; j<IKkeywordss2.length; j++){
					if(IKkeywordss2[j].equals(IKkeywordss1[i])){
						IKkeywordss2[j]=" ";
					}
				}
				for(int j=0; j<IKkeywordss3.length; j++){
					if(IKkeywordss3[j].equals(IKkeywordss1[i])){
						IKkeywordss3[j]=" ";
					}
				}
			}
//			System.out.println("IKkeywords: "+IKkeywords);
			
			for(int i=0; i<IKkeywordss1.length; i++){
				if(IKkeywordss1[i].length()>1){
//					System.out.println("IKkeywordss1["+i+"].length():"+ IKkeywordss1[i].length());
					Query qpWM = new WildcardQuery(new Term("model","*"+IKkeywordss1[i]+"*"));//搜索Query
					Query qpWN = new WildcardQuery(new Term("name","*"+IKkeywordss1[i]+"*"));
					Query qpFM = new FuzzyQuery(new Term("model",IKkeywordss1[i]),2);
					Query qpFN = new FuzzyQuery(new Term("name",IKkeywordss1[i]),1);
					
					searchQuery.add(qpWM,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpFM,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpFN,BooleanClause.Occur.SHOULD); 
					if(HelpUtil.filterString_setBootst(IKkeywordss1[i])){
						qpWN.setBoost(100);
					}
//					System.out.println("IKkeywordss["+i+"]:"+IKkeywordss1[i]);
				}
				Query hqpFM = new FuzzyQuery(new Term("model",IKkeywordss1[i]),0);//高亮Query
				Query hqpFN = new FuzzyQuery(new Term("name",IKkeywordss1[i]),0);
				highLightQuery.add(hqpFM,BooleanClause.Occur.SHOULD); 
				highLightQuery.add(hqpFN,BooleanClause.Occur.SHOULD); 
			}
			for(int i=0; i<IKkeywordss2.length; i++){	
				if(IKkeywordss2[i].length()>1){	
					Query qpWM2 = new WildcardQuery(new Term("model","*"+IKkeywordss2[i]+"*"));
					Query qpWN2 = new WildcardQuery(new Term("name","*"+IKkeywordss2[i]+"*"));
					Query qpFM2 = new FuzzyQuery(new Term("model",IKkeywordss2[i]),1);
					Query qpFN2 = new FuzzyQuery(new Term("name",IKkeywordss2[i]),1);
					if(HelpUtil.filterString_setBootst(IKkeywordss2[i])){
						qpWN2.setBoost(100);
					}
					searchQuery.add(qpWM2,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN2,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFM2,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFN2,BooleanClause.Occur.SHOULD);
					
//						System.out.println("IKkeywordss2["+i+"]:"+IKkeywordss2[i]);
				}
					Query hqpFM2 = new FuzzyQuery(new Term("model",IKkeywordss2[i]),0);
					Query hqpFN2 = new FuzzyQuery(new Term("name",IKkeywordss2[i]),0);
					highLightQuery.add(hqpFM2,BooleanClause.Occur.SHOULD); 
					highLightQuery.add(hqpFN2,BooleanClause.Occur.SHOULD); 
			}
			for(int i=0; i<IKkeywordss3.length; i++){
				if(IKkeywordss3[i].length()>1){
//						System.out.println("IKkeywordss3["+i+"].length():"+ IKkeywordss3[i].length());
					Query qpWM3 = new WildcardQuery(new Term("model","*"+IKkeywordss3[i]+"*"));
					Query qpWN3 = new WildcardQuery(new Term("name","*"+IKkeywordss3[i]+"*"));
					Query qpFM3 = new FuzzyQuery(new Term("model",IKkeywordss3[i]),1);
					Query qpFN3 = new FuzzyQuery(new Term("name",IKkeywordss3[i]),1);
					if(HelpUtil.filterString_setBootst(IKkeywordss3[i])){
						qpWN3.setBoost(100000);
					}
					searchQuery.add(qpWM3,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN3,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFM3,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFN3,BooleanClause.Occur.SHOULD);
//						System.out.println("IKkeywordss3["+i+"]:"+IKkeywordss3[i]);
				}
				Query hqpFM3 = new FuzzyQuery(new Term("model",IKkeywordss3[i]),0);
				Query hqpFN3 = new FuzzyQuery(new Term("name",IKkeywordss3[i]),0);
				highLightQuery.add(hqpFM3,BooleanClause.Occur.SHOULD); 
				highLightQuery.add(hqpFN3,BooleanClause.Occur.SHOULD); 
			}
			
			ResourceBundle resource = ResourceBundle.getBundle("config");
			String sdataMax = resource.getString("dataMax"); 
			int dataMax = Integer.parseInt(sdataMax);
			IndexSearcher isearcher = new IndexSearcher(ireader);//实例化搜索器
//			isearcher.setSimilarity(new IKSimilarity());
			TopDocs docs = isearcher.search(searchQuery,dataMax);
			//高亮	
			String[] preTags = new String[] { "<span class='high-light'>"};
			String[] postTags = new String[] { "</span>"};
			FragListBuilder fragListBuilder=new SimpleFragListBuilder();  
			FragmentsBuilder fragmentsBuilder= new ScoreOrderFragmentsBuilder(preTags,postTags);
			FastVectorHighlighter highlighter=new FastVectorHighlighter(true, true,fragListBuilder, fragmentsBuilder);  
			FieldQuery fieldQuery = highlighter.getFieldQuery(highLightQuery,ireader);
		
//			System.out.println("*********查询结果***********");
//			System.out.println("匹配数目："+ docs.scoreDocs.length);
			
			if (docs==null || docs.totalHits==0) {
				return dataList;
			} 
			else {
				//System.out.println("dataMax:"+dataMax);
				//System.out.println("print score:");
				for(ScoreDoc sd : docs.scoreDocs)//遍历搜索到的结果
				{
					//System.out.println(sd.score);
					
					Document doc = isearcher.doc(sd.doc);
					SearchModel data = new SearchModel();
					
					data.setId(Integer.parseInt(doc.get("id")));
					data.setClassid(Integer.parseInt(doc.get("classid")));
					data.setHits(Integer.parseInt(doc.get("hits")));
					data.setOther(doc.get("other"));
					data.setVisible(Boolean.parseBoolean(doc.get("visible")));
					data.setClassName(doc.get("classname"));
					
					//System.out.println("search");
					//System.out.println(data.isVisible());
					try {
						data.setUpdatetime(HelpUtil.stringToDate(doc.get("updatetime")));
					} catch (Exception e) {
						//e.printStackTrace();
					}
//					String model = doc.get("model");
//					String name = doc.get("name");
//					String detail = doc.get("detail");
				        
				    //高亮
					String snipptN=highlighter.getBestFragment(fieldQuery, ireader, sd.doc,"name",100);
					String snipptM=highlighter.getBestFragment(fieldQuery, ireader, sd.doc,"model",100);
					String snipptD=highlighter.getBestFragment(fieldQuery, ireader, sd.doc,"detail",300);
					
					if(snipptM==null){
						data.setModel(doc.get("model"));
			        }else{
			        	data.setModel(snipptM);
			        }
			        if(snipptN==null){
			        	data.setName(doc.get("name"));
			        }else{
			        	data.setName(snipptN);
			        }
			        if(snipptD==null){
						data.setDetail(doc.get("detail"));
			        }else{
			        	data.setDetail(snipptD);
			        }
					dataList.add(data);
//					System.out.println("匹配得分：" + sd.score);
//					System.out.println(data.toString());
				}
			}
			ireader.close();
			directory.close();
			
		} catch (Exception e) {
			//e.printStackTrace();
		}
	    
		return dataList;
	}
	
	/**
	 * 查找域目标数量
	 * @param field
	 * @param name
	 * @param type
	 */
    public int searchByTerm(String field,String name,String type)  
    {  
    	int searchNum = 0;
        try {  
        	Directory directory = getDirectory(type);
	        IndexReader ireader = DirectoryReader.open(directory);
	        IndexSearcher searcher = new IndexSearcher(ireader);  
	        
	        Query query =new TermQuery(new Term(field,name));  
	        
            TopDocs tds=searcher.search(query, 1000);  
            searchNum = tds.totalHits;
            //System.out.println("一共查询了："+tds.totalHits);  
            /*
            for(ScoreDoc sd:tds.scoreDocs)  
            {  
                Document doc=searcher.doc(sd.doc);  
                System.out.println(sd.doc);    
                System.out.println("id:"+doc.get("id"));    
                System.out.println("classid:"+doc.get("classid"));
                System.out.println("detail:"+doc.get("detail"));
                System.out.println("hits:"+doc.get("hits"));
                System.out.println("model:"+doc.get("model"));
                System.out.println("name:"+doc.get("name"));
                System.out.println("other:"+doc.get("other"));
                System.out.println("updatetime:"+doc.get("updatetime"));
                System.out.println("fieldid:"+doc.getField("id"));
                System.out.println("fieldsid:"+doc.getFields("id"));
                System.out.println("valueid:"+doc.getValues("id"));
            }  
               */          
            ireader.close();  
            
            
        } catch (Exception e) {  
        	//e.printStackTrace();
        }  
        return searchNum;
    }  
     
    /**
	 * 获取搜索结果条数
	 * @param keywords
	 * @param type
	 * @return
	 */
	public int getSearchCount(String keywords, String type) {
		int cnt = 0;
		keywords=HelpUtil.filterHtml(keywords);
		if(keywords.length()<=1) return cnt;
		try {
			Directory directory = getDirectory(type);
			DirectoryReader ireader = DirectoryReader.open(directory);
				
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			BooleanQuery searchQuery = new BooleanQuery();
			BooleanQuery highLightQuery = new BooleanQuery();
				
			String[] fileds = new String[]{ "name", "model", "detail"};//指定搜索域字段;
			BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD,
						BooleanClause.Occur.SHOULD,BooleanClause.Occur.SHOULD};//在对应文档应该出现，MUST为必须出现，MUST_NOT相反
			Query query = MultiFieldQueryParser.parse(keywords, fileds, flags, analyzer);
			searchQuery.add(query,BooleanClause.Occur.SHOULD);
			highLightQuery.add(query,BooleanClause.Occur.SHOULD);
			
			String IKkeywords = Token(keywords);
			//以三种形式进行分词
			String[] IKkeywordss1 = IKkeywords.split("\\|");//IK分词数组，权重最小
			String IKkeywords2Tem0 = IKkeywords.replaceAll("\\D", "\\|");
			String IKkeywords2Tem1 = IKkeywords.replaceAll("[^(a-zA-Z)]", "\\|");
			String IKkeywords2Tem2 = IKkeywords2Tem0+"|"+IKkeywords2Tem1;
			String[] IKkeywordss2 = (IKkeywords2Tem2.replaceAll("\\|{1,}", "\\|")).split("\\|");//纯数字和字母数组（字母和数字分开）
			String IKkeywords3Tem = IKkeywords.replaceAll("\\W", "\\|");
			String[] IKkeywordss3 = (IKkeywords3Tem.replaceAll("\\|{1,}", "\\|")).split("\\|");//纯数字和字母数组（字母和数字可以连在一起），权重最大
			IKkeywordss1=HelpUtil.mergeIdentical(IKkeywordss1);//合并相同字符串
			IKkeywordss2=HelpUtil.mergeIdentical(IKkeywordss2);
			IKkeywordss3=HelpUtil.mergeIdentical(IKkeywordss3);
			for(int i=0; i<IKkeywordss1.length; i++){
				for(int j=0; j<IKkeywordss2.length; j++){
					if(IKkeywordss2[j].equals(IKkeywordss1[i])){
						IKkeywordss2[j]=" ";
					}
				}
				for(int j=0; j<IKkeywordss3.length; j++){
					if(IKkeywordss3[j].equals(IKkeywordss1[i])){
						IKkeywordss3[j]=" ";
					}
				}
			}
//			System.out.println("IKkeywords: "+IKkeywords);
			
			for(int i=0; i<IKkeywordss1.length; i++){
				if(IKkeywordss1[i].length()>1){
//					System.out.println("IKkeywordss1["+i+"].length():"+ IKkeywordss1[i].length());
					Query qpWM = new WildcardQuery(new Term("model","*"+IKkeywordss1[i]+"*"));//搜索Query
					Query qpWN = new WildcardQuery(new Term("name","*"+IKkeywordss1[i]+"*"));
					Query qpFM = new FuzzyQuery(new Term("model",IKkeywordss1[i]),2);
					Query qpFN = new FuzzyQuery(new Term("name",IKkeywordss1[i]),1);
					
					searchQuery.add(qpWM,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpFM,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpFN,BooleanClause.Occur.SHOULD); 
					if(HelpUtil.filterString_setBootst(IKkeywordss1[i])){
						qpWN.setBoost(100);
					}
//					System.out.println("IKkeywordss["+i+"]:"+IKkeywordss1[i]);
				}
				Query hqpFM = new FuzzyQuery(new Term("model",IKkeywordss1[i]),0);//高亮Query
				Query hqpFN = new FuzzyQuery(new Term("name",IKkeywordss1[i]),0);
				highLightQuery.add(hqpFM,BooleanClause.Occur.SHOULD); 
				highLightQuery.add(hqpFN,BooleanClause.Occur.SHOULD); 
			}
			for(int i=0; i<IKkeywordss2.length; i++){	
				if(IKkeywordss2[i].length()>1){	
					Query qpWM2 = new WildcardQuery(new Term("model","*"+IKkeywordss2[i]+"*"));
					Query qpWN2 = new WildcardQuery(new Term("name","*"+IKkeywordss2[i]+"*"));
					Query qpFM2 = new FuzzyQuery(new Term("model",IKkeywordss2[i]),1);
					Query qpFN2 = new FuzzyQuery(new Term("name",IKkeywordss2[i]),1);
					if(HelpUtil.filterString_setBootst(IKkeywordss2[i])){
						qpWN2.setBoost(100);
					}
					searchQuery.add(qpWM2,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN2,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFM2,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFN2,BooleanClause.Occur.SHOULD);
					
//						System.out.println("IKkeywordss2["+i+"]:"+IKkeywordss2[i]);
				}
					Query hqpFM2 = new FuzzyQuery(new Term("model",IKkeywordss2[i]),0);
					Query hqpFN2 = new FuzzyQuery(new Term("name",IKkeywordss2[i]),0);
					highLightQuery.add(hqpFM2,BooleanClause.Occur.SHOULD); 
					highLightQuery.add(hqpFN2,BooleanClause.Occur.SHOULD); 
			}
			for(int i=0; i<IKkeywordss3.length; i++){
				if(IKkeywordss3[i].length()>1){
//						System.out.println("IKkeywordss3["+i+"].length():"+ IKkeywordss3[i].length());
					Query qpWM3 = new WildcardQuery(new Term("model","*"+IKkeywordss3[i]+"*"));
					Query qpWN3 = new WildcardQuery(new Term("name","*"+IKkeywordss3[i]+"*"));
					Query qpFM3 = new FuzzyQuery(new Term("model",IKkeywordss3[i]),1);
					Query qpFN3 = new FuzzyQuery(new Term("name",IKkeywordss3[i]),1);
					if(HelpUtil.filterString_setBootst(IKkeywordss3[i])){
						qpWN3.setBoost(100000);
					}
					searchQuery.add(qpWM3,BooleanClause.Occur.SHOULD); 
					searchQuery.add(qpWN3,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFM3,BooleanClause.Occur.SHOULD);
					searchQuery.add(qpFN3,BooleanClause.Occur.SHOULD);
//						System.out.println("IKkeywordss3["+i+"]:"+IKkeywordss3[i]);
				}
				Query hqpFM3 = new FuzzyQuery(new Term("model",IKkeywordss3[i]),0);
				Query hqpFN3 = new FuzzyQuery(new Term("name",IKkeywordss3[i]),0);
				highLightQuery.add(hqpFM3,BooleanClause.Occur.SHOULD); 
				highLightQuery.add(hqpFN3,BooleanClause.Occur.SHOULD); 
			}
			
			ResourceBundle resource = ResourceBundle.getBundle("config");
			String sdataMax = resource.getString("dataMax"); 
			int dataMax = Integer.parseInt(sdataMax);
			IndexSearcher isearcher = new IndexSearcher(ireader);//实例化搜索器
			TopDocs docs = isearcher.search(searchQuery,dataMax);
			//isearcher.count(searchQuery);
			cnt = docs.scoreDocs.length;
			
			//System.out.println("匹配数目："+ cnt);
		}catch (Exception e) {
			//e.printStackTrace();
		}
	   
		return cnt;
	}

	/**
	 * 增加一条索引
	 * @param data
	 * @param type
	 * @return
	 */
	public boolean addIndex(SearchModel data, String type) {
		try {  
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			Directory directory = getDirectory(type);//建立内存索引对象 
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory,config);
			
	    	this.createSingleIndex(iwriter, data);
	    	//System.out.println("add " + type + " data.id " + data.getId());
			
	    	iwriter.commit();
            iwriter.close();//关闭  
			
            directory.close();
            //System.out.println("add complete");
            //  
            //writer.deleteDocuments(new Term(field, ""));  
        }catch (Exception e) {  
        	//e.printStackTrace();
        }
		return false;
	}
	
	/**
	 * 更新一条索引
	 * @param id
	 * @param data
	 * @param type
	 * @return
	 */
	public boolean updateIndex(String id, SearchModel data, String type) {
		try {  
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			Directory directory = getDirectory(type);//建立内存索引对象 
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory,config);
			
			//System.out.println("update " + type + " data.name " + data.getName());
			
			Document doc = new Document();
			FieldType ftype=new FieldType(TextField.TYPE_STORED); 
	        ftype.setStoreTermVectorOffsets(true);//记录相对增量  
	        ftype.setStoreTermVectorPositions(true);//记录位置信息  
	        ftype.setStoreTermVectors(true);//存储向量信息  
	        //此处删除了“阻止改动信息”
//	        StoredField sField = new StoredField("id", data.getId());
	        Field sField = new Field("id", String.valueOf(data.getId()), ftype);
	        Field tField1 = new Field("name", data.getName(), ftype);
	        Field tField2 = new Field("model", data.getModel(), ftype);
	        Field tField3 = new Field("detail", data.getDetail(), ftype);
	        Field tField4 = new Field("classid", String.valueOf(data.getClassid()),ftype);
	        Field tField5 = new Field("hits", String.valueOf(data.getHits()), ftype);
	        Field tField6 = new Field("updatetime", HelpUtil.dateToString(data.getUpdatetime()), ftype);
	        Field tField7 = new Field("other", data.getOther(), ftype);
	        Field tField8 = new Field("visible", String.valueOf(data.isVisible()), ftype);
	        Field tField9 = new Field("classname", data.getClassName(), ftype);
	        tField1.setBoost(10);//设置域的boost值（权重）,默认为1
	        tField2.setBoost(10);
	        doc.add(sField);
	    	doc.add(tField1);
	    	doc.add(tField2);
	    	doc.add(tField3);
	    	doc.add(tField4);
	    	doc.add(tField5);
	    	doc.add(tField6);
	    	doc.add(tField7);
	    	doc.add(tField8);
	    	doc.add(tField9);
	    	//更新文档对应id
	    	iwriter.updateDocument(new Term("id", id), doc);
	    	
	    	iwriter.commit();
            iwriter.close();//关闭  
            
            directory.close();
            //System.out.println("update complete");
            //  
            //writer.deleteDocuments(new Term(field, ""));  
        }catch (Exception e) { 
        	e.printStackTrace();
        }
		return false;
	}

	/**
	 * 删除所有索引
	 * @param type
	 * @return
	 */
	public boolean deleteAllIndex(String type) {
		try{
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			Directory directory = getDirectory(type);//建立内存索引对象 
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);
			
			iwriter.deleteAll();
			iwriter.commit();
			iwriter.close();
			directory.close();
		}catch (Exception e) {
			//e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * 删除一条索引
	 * @param id
	 * @param type
	 * @return
	 */
	public boolean deleteIndex(String id, String type) {
		try {  
			//当为 true时，分词器进行最大词长切分；当为false 时，分词器进行最细粒度切分。
			//要与createindex传参一致才能实现全词匹配
			Analyzer analyzer = new IKAnalyzer(false);
			Directory directory = getDirectory(type);//建立内存索引对象 
			IndexWriterConfig config = new IndexWriterConfig(analyzer);
			IndexWriter iwriter = new IndexWriter(directory,config);
			
			//DirectoryReader rd = DirectoryReader.open(iwriter,true);
			//rd.document(273);
			//rd.document(0);
			
			//term删除法
            iwriter.deleteDocuments(new Term("id",id));
            
            //id没分词的删除法
            //boolean b = iwriter.tryDeleteDocument(rd, 0);
            
            //System.out.println("delete " + type + " id " + id);
            
            iwriter.commit();//使用IndexWriter进行Document删除操作时，文档并不会立即被删除，而是把这个删除动作缓存起来，当IndexWriter.Commit()或IndexWriter.Close()时，删除操作才会被真正执行。 
            iwriter.close();//关闭  
            directory.close();
            //System.out.println("delete complete");
        }catch (Exception e) {  
        	//e.printStackTrace();
        }
		return false;
	}

}
