package xmj.minibase01;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
	public static final String choiceOption ="���������ѡ��: \n1:���һ���µı�ṹ������ \n2:ɾ��һ����ṹ������" + 
			"\n3:�鿴��ṹ������ \n4:ɾ�����б������ \n5:����sql�����в�ѯ" + 
			"\n6:�����ֶιؼ���ɾ��һ�� \n7:�����ֶιؼ��ָ���һ�� \n. to quit):\n";
	public static String tableName = null;
	
	public static void main(String[] args) throws IOException {
		Schema schemaObj=new Schema();
		Storage dataObj=null;
		System.out.println(choiceOption);
		Scanner sc=new Scanner(System.in);
		int choice=sc.nextInt();
		while(true) {
			if (choice==1) //���һ���µı�ṹ������
			{
				System.out.println("����ִ�����һ�ű�Ͳ����¼�Ĳ���");
				System.out.println("����������ӵı�����");
				String tableName=sc.next().trim();
				List<List<String>> insertFieldList=new ArrayList<>();
				List<String> tablenamelists=schemaObj.get_tablenamelist();
				System.out.println(tablenamelists);
				if(!tablenamelists.contains(tableName)){ //�ñ�����
					System.out.println("�ñ����ڣ������һ����");
					dataObj=new Storage(tableName); //�½�һ���ñ�����Ӧ�����ݶ���
					insertFieldList=dataObj.getFieldlist();  //��ȡ����Ϣ�б�
					schemaObj.appendtable(tableName,insertFieldList); //��ģʽ�ļ���Ӹñ����Ϣ
					
				}
				else{ //�ñ��Ѿ�����
					dataObj=new Storage(tableName);
					
					insertFieldList=dataObj.getFieldlist();
					while(true){
						List<String> record=new ArrayList<>(); //record��һ����¼�������иñ�����ݣ�
						for (int i =0;i<insertFieldList.size();i++){
							System.out.println("��"+(i+1)+"�����������ƣ�"+insertFieldList.get(i).get(0)+",��������Ϊ��"+insertFieldList.get(i).get(1)
									+",���Գ���Ϊ��"+insertFieldList.get(i).get(2));
							String a=sc.next();
							record.add(a);
						}
						if(dataObj.insert_record(record)){
							System.out.println("�����¼�ɹ���");
						}
						else{
							System.out.println("�������");
						}
						System.out.println("����������¼������1��������������¼������2");
						int ch=sc.nextInt();
						if(ch==2){
							break;
						}
					}
					dataObj=null;	
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==2){ //��ģʽ�ļ���ɾ��һ�ű�ṹ����������
				System.out.println("����ִ��ɾ��һ�ű�������ݵĲ���");
				System.out.println("������Ҫɾ���ı���");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){ //�ҵ����ű�
					if(schemaObj.delete_table_schema(tablename)){ //��ģʽ�ļ���ɾ���ɹ�
						dataObj=new Storage(tablename);
						dataObj.deletetabledata(tablename); //ɾ�����ű������
						dataObj=null;
					}
					else{
						System.out.println("��ģʽ�ļ���ɾ�����ű�ʧ�ܣ�");
					}
				}
				else{
					System.out.println("������ ��"+tablename);
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
				
			}
			else if(choice==3){ //�鿴��ṹ������
				System.out.println("����ִ�в鿴��ṹ�����ݵĲ���");
				List<String> tablenamelist=schemaObj.get_tablenamelist();
				System.out.println("���еı������£�");
				System.out.println(tablenamelist);
				System.out.println("������Ҫ�鿴�ı���");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					schemaObj.viewTableStructure(tablename);
					dataObj=new Storage(tablename);
					dataObj.show_tabledata();
					dataObj=null;
				}
				else{
					System.out.println("�����ڱ�"+tablename);
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==4){ //ɾ�����б������
				System.out.println("����ִ��ɾ�����б�����ݵĲ���");
				List<String> tablenamelist=schemaObj.get_tablenamelist();
				for(int i=0;i<tablenamelist.size();i++){
					String tablename=tablenamelist.get(i).trim();
					dataObj=new Storage(tablename);
					dataObj.deletetabledata(tablename);
					dataObj=null;
				}
				schemaObj.deleteAll(); //ɾ��ģʽ�ļ�
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==5){
				System.out.println("��������һ��SQL��ѯ��䣺");
				String str1=sc.nextLine();
				String sqlstr=sc.nextLine(); //��ȡ���ո���ַ���
				Judge ju=new Judge();
				Node nodeobj=ju.judge(sqlstr); //����judge�������õ��﷨��
				nodeobj.show(nodeobj);
				ParseNode2 p2=new ParseNode2();
				p2.construct_logical_tree();
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if(choice==6){//�����ֶιؼ���ɾ��һ��
				System.out.println("����ִ�и��ݹؼ���ɾ�����һ�м�¼");
				System.out.println("������Ҫɾ���ı�����");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					//����ҵ��ñ�
					dataObj=new Storage(tablename);
					System.out.println("Ҫɾ���ļ�¼�Ĺؼ���ȡֵΪ��");
					String field_value=sc.next();
					if(dataObj.delete_record(field_value))
					{
						System.out.println("ɾ��������¼�ɹ���");
					}
					else{
						System.out.println("�����ڸùؼ��ֶ�Ӧ�ļ�¼��ɾ��ʧ�ܣ�");
					}
				}
				else{
					System.out.println("�����ڸñ�");
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else if (choice==7){
				//�����ֶιؼ��ָ���һ��
				System.out.println("����ִ�и�������Ĺؼ��ָ��±��һ����¼");
				System.out.println("������Ҫ���µļ�¼�ı�����");
				String tablename=sc.next();
				tablename=tablename.trim();
				if(schemaObj.find_table(tablename)){
					dataObj=new Storage(tablename);
					System.out.println("������Ҫ���µļ�¼�Ĺؼ���ȡֵ��");
					String field_value=sc.next();
					if(dataObj.update_record(field_value)){
						System.out.println("�ɹ����¼�¼��");
					}
					else{
						System.out.println("�����ҵ�����Ĺؼ��ֶ�Ӧ�ļ�¼������ʧ�ܣ�");
					}
					
				}
				else{
					System.out.println("�����ҵ��ñ�");
				}
				System.out.println(choiceOption);
				choice=sc.nextInt();
			}
			else{
				
			}
		}
	}
	
}
