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
		System.out.println("�Ƿ��б�洢��"+this.isStored+"���洢�ı�����Ϊ��"+this.lenOfTableNum+"������ƫ��λ��Ϊ��"+this.offsetOfBody);
	}
	//չʾ�������Ϣ
	public void showtables(){
		if(lenOfTableNum>0){
			System.out.println("һ����"+tableNames.size()+"�ű�");
			for(int i=0;i<tableNames.size();i++){
				System.out.println(tableNames.get(i));
				System.out.println(tableFields.get(tableNames.get(i)));
			}
		}
	}
}
