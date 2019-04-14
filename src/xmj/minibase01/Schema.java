package xmj.minibase01;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Schema {
	public static final int META_HEAD_SIZE=12; //Ԫ��Ϣͷ������Ϊ12
	public static final int MAX_TABLE_NAME_LEN=10; //��������󳤶�Ϊ10
	public static final int MAX_TABLE_NUM=100; //���ı����
	public static final int TABLE_NAME_ENTRY_LEN=MAX_TABLE_NAME_LEN+4+4; //һ��������ڵĳ���
	public static final int TABLE_NAME_HEAD_SIZE=MAX_TABLE_NUM*TABLE_NAME_ENTRY_LEN; //��ͷ������
	public static final int MAX_FIELD_NAME_LEN=10; //�����������
	public static final int MAX_FIELD_LEN=10+4+4; //�򳤶�
	public static final int MAX_NUM_OF_FIELD_PER_TABLE=5; //ÿ�ű����������
	public static final int FIELD_ENTRY_SIZE_PER_TABLE=MAX_FIELD_LEN * MAX_NUM_OF_FIELD_PER_TABLE;  //ÿ�ű��ܵ���ĳ���
	public static final int MAX_FIELD_SECTION_SIZE = FIELD_ENTRY_SIZE_PER_TABLE * MAX_TABLE_NUM; //�ܵ��򲿷ֵĴ�С�����еı���򳤶�֮�ͣ�
	public static final int BODY_BEGIN_INDEX = META_HEAD_SIZE + TABLE_NAME_HEAD_SIZE;
	public static final int count=0; //��¼ʵ������
	public static Head headObj=null;
	public static final String filename="D:\\minibase_data\\all.sch";
	private int body_begin_index = BODY_BEGIN_INDEX;
	
	//��������ʽ��
	public  String fillTablename(String tableName){
		if(tableName.trim().length()<MAX_TABLE_NAME_LEN){
			tableName = tableName.format("%10s", tableName);// ������ʽ��
		}
		return tableName;
	}
	//�����ж���ʵ��
	public int howmany(){
		return count;
	}
	//�г����б�ı���
	public void viewTableNames(){
		System.out.println("��ʼ�鿴���б���");
		for (int i=0;i<headObj.tableNames.size();i++){
			System.out.println("�����ǣ�"+headObj.tableNames.get(i).get(0));
		
		}
		System.out.println("ִ����ϣ�");
	}
	//�鿴��ṹ
	public void viewTableStructure(String tablename){
		tablename=tablename.trim();
		System.out.println(tablename+"��Ľṹ���£�");
		List<List<String>> fieldlist=new ArrayList<>();
		for(int i=0;i<headObj.tableNames.size();i++){
			if(headObj.tableNames.get(i).get(0).trim().equals(tablename)){
				fieldlist=headObj.tableFields.get(tablename);
				System.out.println(fieldlist);
			}
		}
	}
	public Schema() throws IOException{
		//System.out.println("��ʼ��schema����");
		File fi=new File(filename);
		if(!fi.exists()) {
			fi.createNewFile(); //ģʽ�ļ������ڣ��򴴽�һ��ģʽ�ļ�
		}
		
		System.out.println("ģʽ�ļ��ǣ�"+filename);
		RandomAccessFile file=new RandomAccessFile(fi,"rw");
		int buflen=META_HEAD_SIZE+TABLE_NAME_HEAD_SIZE+MAX_FIELD_SECTION_SIZE; //������ģʽ�ļ��Ĵ�С(���Ĵ�С)
		byte[] buf=new byte[buflen];
		int len=file.read(buf);
		file.close();
		if(len==-1){ //ģʽ�ļ���û������
			this.body_begin_index=BODY_BEGIN_INDEX; //�ļ������ʼλ��
			file=new RandomAccessFile(filename,"rw");
			file.seek(0); 
			//����д��metahead��Ϣ
			file.writeBoolean(false);
			file.writeInt(0);
			file.writeInt(this.body_begin_index);
			file.close(); 
			
			List<List<String>> tablenamelist=new ArrayList<>();
			Map<String, List<List<String>>> fieldnamelist=new HashMap();
			headObj=new Head(false,0,this.body_begin_index,tablenamelist,fieldnamelist); //����ͷ������
			System.out.println("all.sch���Ԫ��Ϣ�Ѿ��޸��Լ�ͷ�������Ѿ�����");
			
		}
		else{ //ԭ��ģʽ�ļ���������
			System.out.println("ģʽ�ļ���������");
			file=new RandomAccessFile(filename,"rw");
			file.seek(0);
			Boolean isStored=file.readBoolean();
			int tmptablenum=file.readInt();
			int tmpoffset=file.readInt();
			file.close();
			System.out.println("�Ƿ��б�洢��"+isStored);
			System.out.println("�м��ű�洢��"+tmptablenum);
			System.out.println("�ļ����ƫ�ƣ�"+tmpoffset);
			
			this.body_begin_index=tmpoffset;
			List<List<String>> namelist=new ArrayList<>();
			Map<String, List<List<String>>> fieldlist=new HashMap();
			
			if(isStored==false){ //ģʽ�ļ��ﻹû�д洢��
				headObj=new Head(false,0,this.body_begin_index,namelist,fieldlist);
				System.out.println("ģʽ�ļ���û�б�");
			}
			else{ //ģʽ�ļ����б�
				System.out.println("ģʽ�ļ�����"+tmptablenum+"�ű�");
				for(int i=0;i<tmptablenum;i++){
					file=new RandomAccessFile(filename,"rw");
					file.seek(META_HEAD_SIZE+i*TABLE_NAME_ENTRY_LEN); //ָ��ģʽ�ļ�ͷ��ñ��λ��
					byte[] tmpname=new byte[10];
					file.read(tmpname); //��ȡ����
					int tmpnum=file.readInt(); //��ȡ�����
					int temppos=file.readInt();// ��ȡƫ��
					System.out.println("������"+new String(tmpname).trim());
					System.out.println("����������"+tmpnum);
					System.out.println("����ģʽ�ļ������λ�ã�"+temppos);
					List<String> tempnamemix=new ArrayList<>();
					tempnamemix.add(new String(tmpname));
					tempnamemix.add(String.valueOf(tmpnum));
					tempnamemix.add(String.valueOf(temppos));  //��Щֵ��������namelist
					namelist.add(tempnamemix);
					
					
					if(tmpnum>0)//���������0�����ж����������fieldlist
					{
						List<List<String>> fields=new ArrayList<>();
						for(int j=0;j<tmpnum;j++){
							//file=new RandomAccessFile(filename,"rw");
							file.seek(META_HEAD_SIZE+TABLE_NAME_HEAD_SIZE+j*MAX_FIELD_LEN);
							byte[] fieldname=new byte[10];
							file.read(fieldname); //��ȡ����
							int tmptype=file.readInt(); //��ȡ������
							int tempoff=file.readInt();// ��ȡ�򳤶�
							
							System.out.println("����"+new String(fieldname).trim()+"\t������"+tmptype+"\t�򳤶�"+tempoff);
							List<String> fieldimf=new ArrayList<>();
							fieldimf.add(new String(fieldname));
							fieldimf.add(String.valueOf(tmptype));
							fieldimf.add(String.valueOf(tempoff));  //��Щֵ��������������Ӧ������Ϣ�б�
							fields.add(fieldimf);
						}
						fieldlist.put(new String(tmpname).trim(), fields); //��fieldslist���������
						file.close();
					}
					
				}
				headObj=new Head(true,tmptablenum,tmpoffset,namelist,fieldlist);
				
			}
		}
		
	}
	 //ɾ�����е�ģʽ�ļ�����
	public void deleteAll() throws IOException{
		System.out.println("��ʼִ��ɾ��ģʽ�ļ�");
		headObj.tableNames.clear();
		headObj.tableFields.clear();
		headObj.isStored=false;
		headObj.lenOfTableNum=0;
		headObj.offsetOfBody=this.body_begin_index;
		
		File f=new File(filename);
		FileWriter fw=new FileWriter(f);
		fw.write("");
		fw.flush();
		fw.close();
		
	}
	//��ģʽ�ļ����һ�ű�
	public void appendtable(String tablename,List<List<String>> fieldlist) throws IOException{
		System.out.println("��ʼִ�в���һ�ű����");
		tablename=tablename.trim();
		RandomAccessFile  file=null;
		if(tablename.length()==0||tablename.length()>10||fieldlist.size()==0){
			System.out.println("��������Ч�Ļ������б�����Ч��");
		}
		else{
			int fieldnum=fieldlist.size();  //��ȡ�����
			System.out.println("��������ģʽ�ļ�����д������Ϣ");
			/*byte[] fieldbuf=new byte[MAX_FIELD_LEN*fieldnum];
			int beginindex=0;*/
			//����ÿ����
			
			//����ӵı������Ϣд���ļ���
			file=new RandomAccessFile(filename,"rw");
			System.out.println("��ģʽ�ļ�����ӱ������Ϣ");
			for(int i=0;i<fieldnum;i++){
				
				String fieldname=fieldlist.get(i).get(0); //��ȡ���б�ĵ�i���������
				int fieldtype=Integer.parseInt(fieldlist.get(i).get(1)); //��ȡ���б�ĵ�i������Ϣ�ĵ�1���������ͣ�
				int fieldlen=Integer.parseInt(fieldlist.get(i).get(2)); //��ȡ���б�ĵ�i������Ϣ��2�����򳤶ȣ�
				fieldname=fillTablename(fieldname);
				file.seek(headObj.offsetOfBody+i*MAX_FIELD_LEN);
				file.write(fieldname.getBytes()); //����������������͡��򳤶�д���ļ�
				file.writeInt(fieldtype);
				file.writeInt(fieldlen);
				
			}
			file.close();
			
			System.out.println("�޸��������ͷ���ṹ");
			file=new RandomAccessFile(filename,"rw");
			tablename=fillTablename(tablename);
			List<String> namecontent=new ArrayList<>();
			namecontent.add(tablename);
			namecontent.add(String.valueOf(fieldnum));
			namecontent.add(String.valueOf(headObj.offsetOfBody));
			file.seek(META_HEAD_SIZE+headObj.lenOfTableNum*TABLE_NAME_ENTRY_LEN);
			file.write(tablename.getBytes());
			file.writeInt(fieldnum);
			file.writeInt(headObj.offsetOfBody); //�ʱ��ƫ�Ƶ�ַ��ԭ����ƫ�Ƶ�ַ�������޸ĺ��body_begin_index
			headObj.isStored=true;
			headObj.lenOfTableNum+=1;
			headObj.offsetOfBody+=fieldnum*MAX_FIELD_LEN;
			headObj.tableNames.add(namecontent);
			headObj.tableFields.put(tablename.trim(),fieldlist);
			file.seek(0);
			file.writeBoolean(headObj.isStored); //д��true
			file.writeInt(headObj.lenOfTableNum);  //д�����
			file.writeInt(headObj.offsetOfBody); //д���޸ĺ��ƫ�ƣ���һ���±�ò����λ�ã�
			file.close();
			
			
			
			
		}
	}
	//�Ƿ��ҵ�ָ�����Ƶı�
	public boolean find_table(String tablename){
		for(int i =0;i<headObj.tableNames.size();i++){
			if(tablename.equals(headObj.tableNames.get(i).get(0).trim())){
				return true;
			}
			
		}
		return false;
	}
	//��headObj�����������д��all.sch
	public void writetofile() throws IOException{
		//��all.sch�ļ����
		File f=new File(filename);
		FileWriter fw=new FileWriter(f);
		fw.write("");
		fw.flush();
		fw.close();
		
		RandomAccessFile  file=new RandomAccessFile(filename,"rw");
		file.seek(0);
		file.writeBoolean(headObj.isStored);
		file.writeInt(headObj.lenOfTableNum);
		file.writeInt(headObj.offsetOfBody);
		file.close();
		
		
		for(int i=0;i<headObj.lenOfTableNum;i++){
			file=new RandomAccessFile(filename,"rw");
			file.seek(META_HEAD_SIZE+i*TABLE_NAME_ENTRY_LEN);
			file.write(headObj.tableNames.get(i).get(0).getBytes()); //д�����
			file.writeInt(Integer.parseInt(headObj.tableNames.get(i).get(1))); //д���������
			file.writeInt(Integer.parseInt(headObj.tableNames.get(i).get(2))); //д����ƫ��
			file.seek(Integer.parseInt(headObj.tableNames.get(i).get(2)));
			List<List<String>> fields=new ArrayList<>();
			fields=headObj.tableFields.get(headObj.tableNames.get(i).get(0).trim()); //Ϊ��ǰ����������Ϣ����
			for(int j=0;j<Integer.parseInt(headObj.tableNames.get(i).get(1));j++){
				
				//д��������Ϣ
				file.write(fields.get(j).get(0).getBytes());
				//д����������Ϣ
				file.writeInt(Integer.parseInt(fields.get(j).get(1)));
				//д���򳤶���Ϣ
				file.writeInt(Integer.parseInt(fields.get(j).get(2)));
				
			}
			file.close();
		}	
	}
	//ɾ��һ�ű�ģʽ�ļ�ҲҪ�޸������Ϣ�������headObjҲҪ�����޸�,�ٶ�ģʽ�ļ��޸ģ�ֱ�ӵ��������writetofile������
	public boolean delete_table_schema(String tablename) throws IOException{
		tablename=tablename.trim();
		int tmpindex=-1;
		for(int i=0;i<headObj.lenOfTableNum;i++){
			if(tablename.equals(headObj.tableNames.get(i).get(0).trim())){
				tmpindex=i; //��ȡ���ű���tableNames�������
			}
		}
		//����������ű�
		if(tmpindex>=0){
			headObj.tableNames.remove(tmpindex);
			headObj.tableFields.remove(tablename);
			headObj.lenOfTableNum-=1;
			//ɾ�����ű�󣬻��������ı���ڣ���Ҫ�޸��������ƫ��
			if (headObj.lenOfTableNum>0){
				Stream<List<String>> st=headObj.tableNames.stream();
				//���������������б��ֱ��������б�������б���ƫ���б�
				//map�Ժ�ֻ����ָ��ת������ת�����Ԫ��
				List<String> tablenames=st.map((x)->x.get(0)).collect(Collectors.toList());
				st=headObj.tableNames.stream();
				List<String> fieldsnum=st.map((x)->x.get(1)).collect(Collectors.toList());
				st = headObj.tableNames.stream();
				List<String> offsets = st.map((x) -> x.get(2)).collect(Collectors.toList());
				//��������ƫ���б�ĵ�һ��Ԫ��ֵ������ɾ�����ǵ�һ�ű�
				offsets.set(0, Integer.toString(BODY_BEGIN_INDEX));
				//��1�±꿪ʼ�����ɾ�����ǵ�һ�ű��������Ѿ��޸��˵�һ�ű��ƫ�ƣ����ɾ���Ĳ��ǵ�һ�ű����һ�ű�Ҳ����Ҫ����
				for(int i=1;i<offsets.size();i++){
					//����i���ƫ��=ǰһ�����ƫ��+ǰһ����������*����򳤶�
					offsets.set(i, String.valueOf(Integer.parseInt(offsets.get(i-1))+Integer.parseInt(fieldsnum.get(i-1))*MAX_FIELD_LEN));
				}
				//���µı����б��������б��򳤶��б���������tableNames
				//headObj.tableNames.clear(); //�Ƚ�ԭ����tableNames���
				for(int i=0;i<headObj.lenOfTableNum;i++){
					//�ϲ������б�ĵ�i��Ԫ�أ������i�ű���Ϊ�µ��б�
					List<String> name=new ArrayList<>();
					name.add(tablenames.get(i));
					name.add(fieldsnum.get(i));
					name.add(offsets.get(i));
					headObj.tableNames.set(i,name);
				}
				//�����µ�����ƫ�ƣ������һ�ű��offset�������һ���������������򳤶�
				headObj.offsetOfBody=Integer.parseInt(headObj.tableNames.get(headObj.lenOfTableNum-1).get(2))+Integer.parseInt(headObj.tableNames.get(headObj.lenOfTableNum-1).get(1))*MAX_FIELD_LEN;
				writetofile();  //��������д���ļ�����
			}
			else{
				System.out.println("ɾ���ñ��û�б����");
				headObj.offsetOfBody= BODY_BEGIN_INDEX;
				headObj.isStored=false;
				headObj.lenOfTableNum=0;
				writetofile();
			}
			return true;
		}
		else{
			System.out.println("�޷��ҵ��ñ�");
			return false;
		}
	}
	//��ȡ�����б�
	public List<String> get_tablenamelist(){
		Stream<List<String>> st=headObj.tableNames.stream();
		List<String> tablenames=st.map((x)->x.get(0).trim()).collect(Collectors.toList());
		return tablenames;
	}
	

}
	
	
	

