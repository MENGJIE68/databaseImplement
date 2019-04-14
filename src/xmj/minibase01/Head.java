package xmj.minibase01;

import java.util.List;
import java.util.Map;

public class Head {
	public boolean isStored;
	public int lenOfTableNum;
	public int offsetOfBody;
	public List<List<String>> tableNames;
	public Map<String,List<List<String>>> tableFields;
	
	public Head(boolean is,int tablenum,int bodyoff,List<List<String>> tablenames,Map<String,List<List<String>>> tablefields){
		this.isStored=is;
		this.lenOfTableNum=tablenum;
		this.offsetOfBody=bodyoff;
		this.tableNames=tablenames;
		this.tableFields=tablefields;
		System.out.println("是否有表存储："+this.isStored+"，存储的表数量为："+this.lenOfTableNum+"，空闲偏移位置为："+this.offsetOfBody);
	}
	//展示输出表信息
	public void showtables(){
		if(lenOfTableNum>0){
			System.out.println("一共有"+tableNames.size()+"张表");
			for(int i=0;i<tableNames.size();i++){
				System.out.println(tableNames.get(i));
				System.out.println(tableFields.get(tableNames.get(i)));
			}
		}
	}
}
